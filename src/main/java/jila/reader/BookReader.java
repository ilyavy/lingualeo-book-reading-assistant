package jila.reader;

import java.io.File;
import java.io.IOException;

public abstract class BookReader {

    String filePath;

    public BookReader(String filePath) {
        this.filePath = filePath;
    }

    public abstract String readIntoString() throws IOException;

    public static BookReader createInstance(final String filePath) {
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
