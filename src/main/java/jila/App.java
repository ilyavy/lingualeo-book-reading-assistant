package jila;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonReader;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import jila.parser.BookTextParser;
import jila.parser.Word;
import jila.reader.BookReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;


public class App extends Application {
    private WebView browser;
    private WebEngine webEngine;
    private LingualeoApi leo;

    private final int ITEMS_ON_PAGE = 6;
    private List<? extends Word> words;

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
            if (selectedFile == null)
                return;

            BookTextParser bp = new BookTextParser();
            BookReader reader = BookReader.createInstance(selectedFile.getAbsolutePath());
            try {
                words = bp.parse(reader.readIntoString());
            } catch (IOException e) {
                e.printStackTrace();
                // TODO: log and show error window
            }

            // Show results
            showWords(1);
        }
    }

    /**
     * Shows the list of words at the specified page
     * in the UI. The specified page should fit the range
     * of available pages.
     *
     * @param page
     */
    private void showWords(int page) {
        if (page < 1 || page > (int) Math.ceil((double) words.size() / ITEMS_ON_PAGE)) {
            return;
        }

        Document doc = webEngine.getDocument();

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
            String email = (String) webEngine.executeScript(
                    "document.getElementById('a_email').value");
            String password = (String) webEngine.executeScript(
                    "document.getElementById('a_password').value");

            Task<JsonObject> task = new Task<>() {
                @Override
                protected JsonObject call() throws Exception {
                    JsonObject profileInfo = leo.login(email, password);
                    return profileInfo;
                }
            };
            task.setOnSucceeded(stateEvent -> {
                JsonObject profileInfo = task.getValue();
                webEngine.executeScript("showProfileInfo(" + profileInfo.toString() + ")");
            });

            new Thread(task).start();
        }
    }

    protected class ButtonAddWordsListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            Document doc = webEngine.getDocument();
            String res = (String) webEngine.executeScript("selectedWords();");

            JsonReader jreader = Json.createReader(
                    new StringReader(res));
            JsonArray jarr = jreader.readArray();
            System.out.println(jarr);

            for (int i = 0; i < jarr.size(); i++) {
                JsonObject jobj = jarr.getJsonObject(i);
                String word = jobj.getString("word");
                String translate = leo.getTranslate(word);
                System.out.println("word: " + word + "; translate: " + translate);
                leo.addWord(word, translate, jobj.getString("context"));

                int wordId = Integer.valueOf(jobj.getString("id"));
                System.out.println("Deleting from the list: " + wordId);
                words.remove(wordId - i);
            }

            // Refresh the list of words
            String page = (String) webEngine.executeScript(
                    "document.getElementById('words_paginator_pages_page').value");
            showWords(Integer.valueOf(page));
        }

    }


    protected class ButtonPaginatorGoListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            String page = (String) webEngine.executeScript(
                    "document.getElementById('words_paginator_pages_page').value");
            showWords(Integer.valueOf(page));
        }
    }


    protected class ButtonPaginatorPreviousListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            String page = (String) webEngine.executeScript(
                    "document.getElementById('words_paginator_pages_page').value");
            showWords(Integer.valueOf(page) - 1);
        }
    }

    protected class ButtonPaginatorNextListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            String page = (String) webEngine.executeScript(
                    "document.getElementById('words_paginator_pages_page').value");
            showWords(Integer.valueOf(page) + 1);
        }
    }


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
