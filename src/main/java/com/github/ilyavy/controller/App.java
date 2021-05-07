package com.github.ilyavy.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import com.github.ilyavy.model.Word;
import com.github.ilyavy.parser.BookTextParser;
import com.github.ilyavy.parser.Lemmatizer;
import com.github.ilyavy.parser.SimpleSequentialBookTextParser;
import com.github.ilyavy.reader.BookFileReader;
import com.github.ilyavy.view.View;
import com.github.ilyavy.view.ViewEvent;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Entry point for a GUI-rich application.
 * CONTROLLER
 */
public class App extends Application {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private LingualeoApi leoApi;

    private View view;

    private WebView browser;

    // Volatile is set here to insure that when the reference is updated with a new list,
    // it becomes visible for all the threads, the list is supposed to be changed only in 'batch' mode, as a whole.
    private volatile List<? extends Word> words;

    WebView getBrowser() {
        return browser;
    }

    View getView() {
        return view;
    }

    @Override
    public void start(Stage stage) {
        leoApi = new LingualeoApi();
        browser = new WebView();

        view = View.from(browser)
                .setEventHandler(ViewEvent.LOGIN, new ButtonLoginHandler())
                .setEventHandler(ViewEvent.ANALYZE_BOOK, new ButtonAnalyzeHandler(stage))
                .setEventHandler(ViewEvent.ADD_WORDS_TO_DICTIONARY, new ButtonAddWordsHandler())
                .setEventHandler(ViewEvent.RESULTS_GOTO_PAGE, () -> view.showWords(words, view.getSpecifiedPageToGo()))
                .setEventHandler(ViewEvent.RESULTS_NEXT_PAGE,
                        () -> view.showWords(words, view.getSpecifiedPageToGo() + 1))
                .setEventHandler(ViewEvent.RESULTS_PREVIOUS_PAGE,
                        () -> view.showWords(words, view.getSpecifiedPageToGo() - 1));

        VBox frame = new VBox(1);
        frame.getChildren().add(browser);

        Scene scene = new Scene(frame);
        stage.setScene(scene);
        stage.show();

        browser.setPrefSize(3000, 3000);
        browser.requestFocus();

        logger.info("Application has started");

        if (leoApi.isUserAuthenticated()) {
            view.doOnReady(() -> view.showUserProfile(leoApi.getLingualeoProfile()));
        }
    }

    public static void main(String[] args) {
        launch();
    }

    /**
     * Login user into Lingualeo.
     */
    protected class ButtonLoginHandler implements Runnable {
        @Override
        public void run() {
            leoApi.login(view.getLogin(), view.getPassword())
                    .subscribe(profile -> {
                        try {
                            view.showUserProfile(profile);
                        } catch (View.ViewInteractionException e) {
                            logger.error("Showing user profile error", e);
                        }
                    }, e -> logger.error("Login error", e));

            view.showLoading();
        }
    }

    /**
     * Listener for analyze button.
     */
    protected class ButtonAnalyzeHandler implements Runnable {
        Stage stage;

        public ButtonAnalyzeHandler(Stage stage) {
            this.stage = stage;
        }

        @Override
        public void run() {
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
                Lemmatizer lemmatizer = new Lemmatizer(); // todo: lazy creation? creation at startup?
                BookTextParser parser = new SimpleSequentialBookTextParser(lemmatizer);

                List<String> sentences = parser.parseTextIntoSentences(reader.readIntoString());
                var result = new ArrayList<>(parser.countWords(sentences).values());
                result.sort(Comparator.comparingLong(Word::getCount).reversed());
                words = result;

                return words;
            };

            Mono.fromCallable(parseBook)
                    .subscribeOn(Schedulers.single())
                    .subscribe(
                            wordsList -> view.showWords(wordsList, 1),
                            e -> logger.error("Book parsing error", e)); // TODO: add error window

            view.showLoading();
        }
    }

    /**
     * Adds words to the user's dictionary.
     */
    protected class ButtonAddWordsHandler implements Runnable {
        @Override
        public void run() {
            Mono.fromCallable(() -> view.getSelectedWords())
                    .flatMapMany(Flux::fromIterable)
                    .concatMap(leoApi::requestAndSetTranslation)
                    .concatMap(leoApi::addWordToDictionary)
                    .index()
                    .subscribeOn(Schedulers.single())
                    .subscribe(tuple -> {
                                int indexToRemove = (int) (tuple.getT2().getId() - tuple.getT1());
                                words.remove(indexToRemove);
                                logger.debug("{} - word is added", tuple.getT2().getWord());
                            },
                            e -> logger.error("Adding words to the dictionary error", e),
                            () -> view.showWords(words, view.getCurrentPage()));

            view.showLoading();
        }
    }
}
