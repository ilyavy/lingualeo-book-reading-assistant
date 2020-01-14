package jila.reader;

import java.io.File;
import java.io.IOException;

/**
 * Abstract file reader with static factory to create a concrete instance.
 */
public abstract class BookFileReader {

    String filePath;

    public BookFileReader(String filePath) {
        this.filePath = filePath;
    }

    public abstract String readIntoString() throws IOException;

    /**
     * Creates a concrete book file reader by the specified file path. A concrete implementation is chosen based
     * on the file's extension.
     *
     * @param filePath path to the file to read
     * @return
     */
    public static BookFileReader createInstance(final String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            throw new IllegalArgumentException("The book's file is not found");
        }

        if (file.getName().endsWith(".txt")) {
            return new TxtFileReader(filePath);
        } else {
            throw new UnsupportedOperationException("This file extension is not supported yet");
        }
    }
}
