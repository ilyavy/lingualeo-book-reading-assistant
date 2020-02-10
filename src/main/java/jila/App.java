package jila;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker.State;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import jila.model.SimpleWord;
import jila.model.Word;
import jila.parser.BookTextParser;
import jila.parser.SimpleSequentialBookTextParser;
import jila.reader.BookFileReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Entry point for a GUI-rich application.
 */
public class App extends Application {
    private WebView browser;
    private WebEngine webEngine;
    private LingualeoApi leo;

    private static final int ITEMS_ON_PAGE = 6;
    private volatile List<? extends Word> words;

    @Override
    public void start(Stage stage) throws Exception {
        leo = new LingualeoApi();
        browser = new WebView();
        webEngine = browser.getEngine();
        webEngine.setJavaScriptEnabled(true);

        webEngine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
            if (newState == State.SUCCEEDED) {
                setClickEvent("button_analyze", new ButtonAnalyzeListener(stage));
                setClickEvent("a_button_login", new ButtonLoginListener());
                setClickEvent("words_button_add", new ButtonAddWordsListener());
                setClickEvent("goto_lingualeo", new LinkExternalListener());
                setClickEvent("words_paginator_go", new ButtonPaginatorGoListener());
                setClickEvent("words_paginator_previous", new ButtonPaginatorPreviousListener());
                setClickEvent("words_paginator_next", new ButtonPaginatorNextListener());
            }
        });

        webEngine.load(getClass().getResource("/view/html/index.html").toExternalForm());

        webEngine.setOnAlert(e -> System.out.println("alert: " + e.toString()));

        VBox frame = new VBox(1);
        frame.getChildren().add(browser);

        Scene scene = new Scene(frame);
        stage.setScene(scene);
        stage.show();

        browser.setPrefSize(3000, 3000);
        browser.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * @param resPath
     * @return
     */
    protected String readTemplate(String resPath) {
        StringBuilder result = new StringBuilder();

        if (resPath.isEmpty() || resPath == null) {
            return null;
        }

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    getClass().getClassLoader().getResourceAsStream(resPath)));
            String line;
            while ((line = br.readLine()) != null) {
                result.append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    /**
     * Set EventListener on click on specified element.
     *
     * @param elementId
     * @param listener
     */
    protected void setClickEvent(String elementId, EventListener listener) {
        Document doc = webEngine.getDocument();
        Element el = doc.getElementById(elementId);
        EventTarget et = (EventTarget) el;
        et.addEventListener("click", listener, false);
    }


    // BUTTONS AND LINKS LISTENERS

    /**
     * Listener for analyze button.
     */
    protected class ButtonAnalyzeListener implements EventListener {
        Stage stage;

        public ButtonAnalyzeListener(Stage stage) {
            this.stage = stage;
        }

        @Override
        public void handleEvent(Event evt) {
            evt.preventDefault();

            // Choose file dialog
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose a book");
            fileChooser.getExtensionFilters()
                    .addAll(new ExtensionFilter("Text Files", "*.txt"),
                            new ExtensionFilter("All Files", "*.*"));
            File selectedFile = fileChooser.showOpenDialog(stage);

            // Parse book's file, if the file was chosen
            if (selectedFile == null) {
                return;
            }

            Callable<List<? extends Word>> parseBook = () -> {
                BookFileReader reader = BookFileReader.createInstance(new File(selectedFile.getAbsolutePath()));
                BookTextParser parser = new SimpleSequentialBookTextParser();

                List<String> sentences = parser.parseTextIntoSentences(reader.readIntoString());
                var result = new ArrayList<>(parser.countWords(sentences).values());
                result.sort(Comparator.comparingLong(Word::getCount).reversed());
                words = result;

                return words;
            };

            Mono.fromCallable(parseBook)
                    .subscribeOn(Schedulers.single())
                    .subscribe(
                            words -> Platform.runLater(() -> showWords(1)),
                            Throwable::printStackTrace); // TODO: substitute with logging, add error window

            webEngine.executeScript("loginLoading();");
        }
    }

    /**
     * Shows the list of words at the specified page
     * in the UI. The specified page should fit the range
     * of available pages.
     *
     * @param page
     */
    public void showWords(int page) {
        if (page < 1 || page > (int) Math.ceil((double) words.size() / ITEMS_ON_PAGE)) {
            return;
        }

        List<Word> wordsToShow = new ArrayList<>();
        for (int i = (page - 1) * ITEMS_ON_PAGE;
             i < Math.min(page * ITEMS_ON_PAGE, words.size()); i++) {
            Word word = words.get(i);
            word.setId(i);
            wordsToShow.add(word);
        }

        try {
            String wordsToShowStr = new ObjectMapper().writeValueAsString(wordsToShow);
            String template = readTemplate("view/html/word.html");
            webEngine.executeScript("printEntitiesList('" + template + "', " + wordsToShowStr + ", " + page + ", "
                    + (words.size() / ITEMS_ON_PAGE) + ")");

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


    /**
     * Login user into LinguaLeo.
     */
    protected class ButtonLoginListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            final var email = (String) webEngine.executeScript("document.getElementById('a_email').value");
            final var password = (String) webEngine.executeScript("document.getElementById('a_password').value");

            leo.login(email, password)
                    .subscribeOn(Schedulers.single())
                    .subscribe(this::showProfileInfo, Throwable::printStackTrace);

            webEngine.executeScript("loginLoading();");
        }

        private void showProfileInfo(String profileInfo) {
            Platform.runLater(() -> webEngine.executeScript("showProfileInfo(" + profileInfo + ")"));
        }
    }

    /**
     * Adds words to the user's dictionary.
     */
    protected class ButtonAddWordsListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            var wordsToAddStr = (String) webEngine.executeScript("selectedWords();");
            int page = Integer.parseInt((String) webEngine.executeScript(
                    "document.getElementById('words_paginator_pages_page').value"));

            Mono.fromCallable(() -> new ObjectMapper().readValue(wordsToAddStr, SimpleWord[].class))
                    .flatMapMany(Flux::fromArray)
                    .concatMap(leo::requestAndSetTranslation)
                    .concatMap(leo::addWordToDictionary)
                    .index()
                    .subscribeOn(Schedulers.single())
                    .subscribe(tuple -> {
                                int indexToRemove = (int) (tuple.getT2().getId() - tuple.getT1());
                                words.remove(indexToRemove);
                                System.out.println(tuple.getT2().getWord() + " - word is added");
                            },
                            Throwable::printStackTrace,
                            () -> Platform.runLater(() -> showWords(page)));

            webEngine.executeScript("showWelcomeScreen();" +
                    "document.getElementById('profile_info').style.display = 'none'; " +
                    "document.getElementById('auth').style.display = 'block'; " +
                    "loginLoading();");
        }
    }

    /**
     * Displays the specified page of results for a user.
     */
    protected class ButtonPaginatorGoListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            String page = (String) webEngine.executeScript(
                    "document.getElementById('words_paginator_pages_page').value");
            showWords(Integer.parseInt(page));
        }
    }

    /**
     * Displays the previous page of results for a user.
     */
    protected class ButtonPaginatorPreviousListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            String page = (String) webEngine.executeScript(
                    "document.getElementById('words_paginator_pages_page').value");
            showWords(Integer.parseInt(page) - 1);
        }
    }

    /**
     * Displays the next page of results for a user.
     */
    protected class ButtonPaginatorNextListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            String page = (String) webEngine.executeScript(
                    "document.getElementById('words_paginator_pages_page').value");
            showWords(Integer.parseInt(page) + 1);
        }
    }

    /**
     * Opens links to external resources in a browser set by default in a system.
     */
    protected class LinkExternalListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            evt.preventDefault();
            try {
                URL url = new URL(evt.getCurrentTarget().toString());
                Desktop.getDesktop().browse(url.toURI());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
