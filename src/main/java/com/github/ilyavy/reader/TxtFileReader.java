package com.github.ilyavy.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Reads files in plain text format with the extension ".txt".
 */
class TxtFileReader extends BookFileReader {

    TxtFileReader(final File bookFile) {
        super(bookFile);
    }

    @Override
    public String readIntoString() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(bookFile))) {
            return br.lines().collect(Collectors.joining(" "));
        }
    }
}
