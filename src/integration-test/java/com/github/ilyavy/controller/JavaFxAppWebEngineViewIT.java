package com.github.ilyavy.controller;

import java.util.List;

import com.github.ilyavy.model.LingualeoProfile;
import com.github.ilyavy.model.Word;
import com.github.ilyavy.dao.UserDataDao;
import com.github.ilyavy.service.LingualeoService;
import com.github.ilyavy.service.UserService;
import com.github.ilyavy.service.parser.word.SimpleWord;
import com.github.ilyavy.view.View;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ApplicationExtension.class)
class JavaFxAppWebEngineViewIT {

    private JavaFxApp javaFxApp;

    private final Object javaFxSyncLock = new Object();

    void checkElementDisplayProperty(String elementId, String visibility) throws InterruptedException {
        synchronized (javaFxSyncLock) {
            Runnable assertion = () -> {
                synchronized (javaFxSyncLock) {
                    try {
                        var display = (String) javaFxApp.getBrowser().getEngine()
                                .executeScript("document.getElementById('" + elementId + "').style.display");
                        assertEquals(visibility, display, "'" + elementId + "' display property's value");
                    } finally {
                        javaFxSyncLock.notifyAll();
                    }
                }
            };
            Platform.runLater(assertion);
            javaFxSyncLock.wait();
        }
    }

    void checkElementValue(String elementId, String expectedValue) throws InterruptedException {
        synchronized (javaFxSyncLock) {
            Runnable assertion = () -> {
                synchronized (javaFxSyncLock) {
                    try {
                        var actualValue = (String) javaFxApp.getBrowser().getEngine()
                                .executeScript("document.getElementById('" + elementId + "').innerHTML");
                        assertEquals(expectedValue, actualValue, "'" + elementId + "' value");
                    } finally {
                        javaFxSyncLock.notifyAll();
                    }
                }
            };
            Platform.runLater(assertion);
            javaFxSyncLock.wait();
        }
    }

    @BeforeAll
    public static void setupSpec() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "true");
    }

    @Start
    private void start(Stage stage) throws Exception {
        javaFxApp = new JavaFxApp();
        javaFxApp.userService = new UserService(null, null);
        javaFxApp.lingualeoService = new LingualeoService();
        javaFxApp.start(stage);
    }

    @Test
    void viewShowLoading() throws InterruptedException {
        javaFxApp.getView().showLoading();

        checkElementDisplayProperty("words", "none");
        checkElementDisplayProperty("auth_loading", "block");
    }

    @Test
    void viewShowUserProfile() throws View.ViewInteractionException, InterruptedException {
        LingualeoProfile profile = new LingualeoProfile()
                .setExpLevel(1)
                .setHungryPct(1)
                .setNickname("nickname")
                .setId(1000)
                .setWordsCount(10)
                .setWordsKnown(50);

        javaFxApp.getView().showUserProfile(profile);

        checkElementDisplayProperty("auth", "none");
        checkElementDisplayProperty("profile_info", "table");

        checkElementValue("profile_words_known", String.valueOf(profile.getWordsKnown()));
        checkElementValue("profile_words_cnt", String.valueOf(profile.getWordsCount()));
        checkElementValue("profile_satiety_percent", String.valueOf(profile.getHungryPct()));
    }

    @Test
    void showWords() throws InterruptedException {
        Word word = new SimpleWord("testword")
                .setCount(1)
                .setTranslate("тестовое слово")
                .setContext("This is a testword");

        javaFxApp.getView().showWords(List.of(word), 1);

        checkElementDisplayProperty("words", "block");
        checkElementDisplayProperty("welcome_screen", "none");
    }
}
