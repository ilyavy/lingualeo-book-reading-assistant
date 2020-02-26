package com.github.ilyavy.view;

import java.util.List;

import javax.annotation.Nonnull;

import com.github.ilyavy.model.LingualeoProfile;
import com.github.ilyavy.model.Word;
import javafx.scene.web.WebView;

/**
 * View interface by MVC paradigm, has methods to control the state of GUI.
 */
public interface View {

    /**
     * Provides callback on the change of the view's state to 'ready', when it is built and shown to a user.
     * @param runnable runnable to call at the change to 'ready' state
     */
    void doOnReady(Runnable runnable);

    /**
     * Shows loading screen.
     */
    void showLoading();

    /**
     * Shows user profile.
     *
     * @param profile user's profile
     */
    void showUserProfile(LingualeoProfile profile);

    /**
     * Shows the list of words at the specified page. The specified page should fit the range of available pages.
     * There is no way to control the number of words shown at each page, it is controlled by view itself.
     *
     * @param words list of words to show
     * @param page  page number
     */
    void showWords(List<? extends Word> words, int page);

    /**
     * Returns the list of selected by a user words from the list of shown words.
     *
     * @return list of selected words
     */
    List<Word> getSelectedWords();

    /**
     * Returns the current page number of shown to a user words.
     *
     * @return current page number
     */
    int getCurrentPage();

    /**
     * Returns the page number, which is chosen by a user to go to directly.
     *
     * @return page number, to which a user wants to go to directly
     */
    int getSpecifiedPageToGo();

    /**
     * Returns entered by a user login.
     *
     * @return login
     */
    String getLogin();

    /**
     * Returns entered by a user password.
     *
     * @return password
     */
    String getPassword();

    /**
     * Throws an exception, this method is invoked for all unsupported root element types.
     *
     * @param rootElement javaFx element
     * @return it always throws IllegalArgumentException
     */
    static View from(Object rootElement) {
        throw new IllegalArgumentException(
                "Cannot create an instance of view with the specified root element." +
                        "Allowed object types are: WebView");
    }

    /**
     * Create a view from the specified WebView.
     *
     * @param rootElement WebView, which will be a root element for a view
     * @return view of the corresponding type
     */
    static View from(@Nonnull WebView rootElement) {
        return new WebEngineView(rootElement.getEngine());
    }

    View setEventHandler(ViewEvent event, Runnable runnable);

    /**
     * A generic runtime exception, thrown for any problems occurred while interacting with a view.
     */
    class ViewInteractionException extends RuntimeException {
        public ViewInteractionException(Throwable cause) {
            super(cause);
        }
    }
}
