package jila.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

class TxtFileReader extends BookReader {

    TxtFileReader(final String filePath) {
        super(filePath);
    }

    @Override
    public String readIntoString() throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("The book's file is not found");
        }

        StringBuilder sb = new StringBuilder();

        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append(" ");
        }

        return sb.toString();
    }
}
