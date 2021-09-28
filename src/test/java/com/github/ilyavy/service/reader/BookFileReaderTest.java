package com.github.ilyavy.service.reader;

import java.io.File;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookFileReaderTest {

    @Test
    void createInstanceFileDoesNotExistThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                BookFileReader.createInstance(new File("")));
    }

    @Test
    void createInstancePlainTextIsSupported() {
        var fileInPlainTextFormat = mock(File.class);
        when(fileInPlainTextFormat.exists()).thenReturn(true);
        when(fileInPlainTextFormat.getName()).thenReturn("book.txt");

        var txtFileReader = BookFileReader.createInstance(fileInPlainTextFormat);
        assertTrue(txtFileReader instanceof TxtFileReader);
    }

    @Test
    void createInstanceUnsupportedFormatThrowsException() {
        var fileWithUnsupportedFormat = mock(File.class);
        when(fileWithUnsupportedFormat.exists()).thenReturn(true);
        when(fileWithUnsupportedFormat.getName()).thenReturn("book.blabla");

        assertThrows(UnsupportedOperationException.class, () ->
                BookFileReader.createInstance(fileWithUnsupportedFormat));
    }
}