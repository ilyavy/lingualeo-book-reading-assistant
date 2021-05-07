package com.github.ilyavy.reader;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TxtFileReaderTest {

    @Test
    void readIntoStringSimpleBookWithTwoSentences() throws IOException {
        var txtFileReader = BookFileReader
                .createInstance(new File("./src/test/resources/SimplePlainTextBook.txt"));
        var text = txtFileReader.readIntoString();

        assertEquals("This is simple plain text book. It is in .txt format.", text);
    }

    @Test
    void readIntoStringIOExceptionIsRethrown() {
        var txtFileReader = new TxtFileReader(new File("non-existing-file.txt"));
        assertThrows(IOException.class, txtFileReader::readIntoString);
    }
}
