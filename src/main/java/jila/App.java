package jila;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;

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
import jila.parser.BookTextParser;
import jila.parser.SimpleSequentialBookTextParser;
import jila.parser.Word;
import jila.reader.BookFileReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
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
        leo = new LingualeoApi(LingualeoApi.createConnector());
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
                BookFileReader reader = BookFileReader.createInstance(selectedFile.getAbsolutePath());
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

        JsonBuilderFactory arrayFactory = Json.createBuilderFactory(null);
        JsonArrayBuilder jsonArrayBuilder = arrayFactory.createArrayBuilder();

        Word word = null;
        for (int i = (page - 1) * ITEMS_ON_PAGE;
             i < Math.min(page * ITEMS_ON_PAGE, words.size()); i++) {
            word = words.get(i);
            Map<String, String> attr = new HashMap<>(2);
            attr.put("id", String.valueOf(i));
            jsonArrayBuilder.add(word.toJsonObject(attr));
        }
        JsonArray jsonArray = jsonArrayBuilder.build();

        String template = readTemplate("view/html/word.html");
        webEngine.executeScript("printEntitiesList('" + template + "', " +
                jsonArray.toString() + ", " + page + ", "
                + (words.size() / ITEMS_ON_PAGE) + ")");
    }


    /**
     * Login user into LinguaLeo.
     */
    protected class ButtonLoginListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            final var email = (String) webEngine.executeScript("document.getElementById('a_email').value");
            final var password = (String) webEngine.executeScript("document.getElementById('a_password').value");

            Mono.fromCallable(() -> leo.login(email, password))
                    .subscribeOn(Schedulers.single())
                    .subscribe(profileInfo -> Platform.runLater(
                            () -> webEngine.executeScript("showProfileInfo(" + profileInfo.toString() + ")")));

            webEngine.executeScript("loginLoading();");
        }
    }

    /**
     * Adds words to the user's dictionary.
     */
    protected class ButtonAddWordsListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            final var res = (String) webEngine.executeScript("selectedWords();");
            JsonArray wordsToAdd = Json.createReader(new StringReader(res)).readArray();

            Callable<Void> addWordsToDictionary = () -> {
                for (int i = 0; i < wordsToAdd.size(); i++) {
                    JsonObject jobj = wordsToAdd.getJsonObject(i);
                    String word = jobj.getString("word");
                    String translate = leo.getTranslate(word);
                    leo.addWord(word, translate, jobj.getString("context"));

                    int wordId = Integer.parseInt(jobj.getString("id"));
                    words.remove(wordId - i);
                }
                return null;
            };

            Mono.fromCallable(addWordsToDictionary)
                    .subscribeOn(Schedulers.single())
                    .subscribe(
                            r -> {
                                // Refresh the list of words
                                Platform.runLater(() -> {
                                    var page = (String) webEngine.executeScript(
                                            "document.getElementById('words_paginator_pages_page').value");
                                    showWords(Integer.parseInt(page));
                                });
                            },
                            Throwable::printStackTrace);
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
