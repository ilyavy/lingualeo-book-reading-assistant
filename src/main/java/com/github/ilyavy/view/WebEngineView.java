package com.github.ilyavy.view;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ilyavy.model.LingualeoProfile;
import com.github.ilyavy.model.Word;
import com.github.ilyavy.parser.word.SimpleWord;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 * A {@link View} based on {@link WebView} JavaFx element. All the content is shown using JavaFx {@link WebEngine}.
 */
class WebEngineView implements View {

    private static final Logger logger = LoggerFactory.getLogger(WebEngineView.class);

    private WebEngine webEngine;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final int ITEMS_ON_PAGE = 6;

    private int currentPage;

    WebEngineView(@Nonnull WebEngine webEngine) {
        this.webEngine = webEngine;
        this.webEngine.setJavaScriptEnabled(true);
        this.webEngine.load(getClass().getResource("/view/html/index.html").toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                setClickEvent("goto_lingualeo", new LinkExternalListener());
            }
        });
    }

    @Override
    public void doOnReady(Runnable runnable) {
        webEngine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                new Thread(runnable).start();
            }
        });
    }

    @Override
    public void showLoading() {
        Platform.runLater(() -> webEngine.executeScript("showWelcomeScreen();" +
                "document.getElementById('profile_info').style.display = 'none'; " +
                "document.getElementById('auth').style.display = 'block'; " +
                "loginLoading();"));
    }

    @Override
    public void showUserProfile(@Nonnull LingualeoProfile profile) {
        try {
            var profileStr = objectMapper.writeValueAsString(profile);
            Platform.runLater(() -> webEngine.executeScript("showProfileInfo(" + profileStr + ")"));
        } catch (JsonProcessingException e) {
            throw new ViewInteractionException(e);
        }
    }

    @Override
    public void showWords(@Nonnull List<? extends Word> words, int page) {
        Platform.runLater(() -> {
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
                String wordsToShowStr = objectMapper.writeValueAsString(wordsToShow);
                String template = readTemplate("view/html/word.html");
                webEngine.executeScript("printEntitiesList('" + template + "', " + wordsToShowStr + ", " + page + ", "
                        + (words.size() / ITEMS_ON_PAGE) + ")");

            } catch (JsonProcessingException e) {
                logger.error("Showing words error", e);
            }
        });

        currentPage = page;
    }

    private String readTemplate(String resPath) {
        StringBuilder result = new StringBuilder();

        if (resPath == null || resPath.isEmpty()) {
            return null;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(resPath))))) {

            String line;
            while ((line = br.readLine()) != null) {
                result.append(line);
            }

        } catch (Exception e) {
            logger.error("Reading template error", e);
        }

        return result.toString();
    }

    @Override
    public List<Word> getSelectedWords() {
        try {
            var wordsToAddStr = (String) webEngine.executeScript("selectedWords();");
            return Arrays.asList(objectMapper.readValue(wordsToAddStr, SimpleWord[].class));

        } catch (JsonProcessingException e) {
            throw new ViewInteractionException(e);
        }
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public int getSpecifiedPageToGo() {
        return Integer.parseInt((String) webEngine.executeScript(
                "document.getElementById('words_paginator_pages_page').value"));
    }

    @Override
    public String getLogin() {
        return (String) webEngine.executeScript("document.getElementById('a_email').value");
    }

    @Override
    public String getPassword() {
        return (String) webEngine.executeScript("document.getElementById('a_password').value");
    }

    @Override
    public View setEventHandler(ViewEvent event, Runnable action) {
        webEngine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                var elementId = switch (event) {
                    case LOGIN -> "a_button_login";
                    case ANALYZE_BOOK -> "button_analyze";
                    case ADD_WORDS_TO_DICTIONARY -> "words_button_add";
                    case RESULTS_GOTO_PAGE -> "words_paginator_go";
                    case RESULTS_NEXT_PAGE -> "words_paginator_next";
                    case RESULTS_PREVIOUS_PAGE -> "words_paginator_previous";
                };
                setClickEvent(elementId, evt -> action.run());
            }
        });
        return this;
    }

    private void setClickEvent(@Nonnull String elementId, @Nonnull EventListener listener) {
        var doc = webEngine.getDocument();
        var element = doc.getElementById(elementId);
        var eventTarget = (EventTarget) element;
        eventTarget.addEventListener("click", listener, false);
    }

    /**
     * Opens links to external resources in a browser set by default in a system.
     */
    private static class LinkExternalListener implements EventListener {
        @Override
        public void handleEvent(Event evt) {
            evt.preventDefault();
            try {
                URL url = new URL(evt.getCurrentTarget().toString());
                Desktop.getDesktop().browse(url.toURI());
            } catch (Exception e) {
                logger.error("Going to external resources error", e);
            }
        }
    }
}
