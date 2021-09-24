package com.github.ilyavy.reader;

import java.io.File;
import java.io.IOException;

/**
 * Abstract file reader with static factory to create a concrete instance.
 * Supported formats: plain text (.txt).
 */
public abstract sealed class BookFileReader permits TxtFileReader {

    File bookFile;

    BookFileReader(File bookFile) {
        this.bookFile = bookFile;
    }

    /**
     * Reads the book's file into string.
     *
     * @return text of the book in string
     * @throws IOException in case of problems with reading the file
     */
    public abstract String readIntoString() throws IOException;

    /**
     * Creates a concrete book file reader by the specified file path. A concrete implementation is chosen based
     * on the file's extension.
     *
     * @param bookFile file referencing a book
     * @return BookFileReader instance
     * @throws IllegalArgumentException      if the file is specified does not exist
     * @throws UnsupportedOperationException if an extension of the file is not supported
     */
    public static BookFileReader createInstance(final File bookFile) {
        if (!bookFile.exists()) {
            throw new IllegalArgumentException("The book's file is not found");
        }

        if (bookFile.getName().endsWith(".txt")) {
            return new TxtFileReader(bookFile);
        } else {
            throw new UnsupportedOperationException("This file extension is not supported yet");
        }
    }
}
