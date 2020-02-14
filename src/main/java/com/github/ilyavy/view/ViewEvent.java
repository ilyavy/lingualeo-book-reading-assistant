package com.github.ilyavy.view;

/**
 * Events, which can occur on the {@link View} side, and to which handlers can be attached.
 */
public enum ViewEvent {

    /** A request to analyze a book. */
    ANALYZE_BOOK,

    /** A request to login into Lingualeo with the specified at View side login and password. */
    LOGIN,

    /** A request to add selected at View side words to dictionary. */
    ADD_WORDS_TO_DICTIONARY,

    /** A request to go to the specified page in the results of book's analysis list. */
    RESULTS_GOTO_PAGE,

    /** A request to go to the next page in the results of book's analysis list. */
    RESULTS_NEXT_PAGE,

    /** A request to go to the previous page in the results of book's analysis list. */
    RESULTS_PREVIOUS_PAGE
}
