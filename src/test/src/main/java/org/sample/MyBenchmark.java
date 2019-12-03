package org.sample;

import java.io.IOException;
import java.util.List;

import jila.parser.BookTextParser;
import jila.reader.BookFileReader;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;

/*@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})*/
public class BookAnalysisBenchmark {

    private BookTextParser bookParser;

    private List<String> sentences;

    @Setup
    public void setup() throws IOException {
        bookParser = new BookTextParser();

        // String text = BookFileReader.createInstance("little_red_riding_hood.txt").readIntoString();
        String text = BookFileReader.createInstance("war-peace.txt").readIntoString();

        sentences = bookParser.getSentences(text);
        System.out.println("The number of sentences: " + sentences.size());
        text = null;
    }

    @Benchmark
    public void sequential() throws IOException {
        bookParser.sequential(sentences);
    }

    @Benchmark
    public void forkJoinWithThreshold() throws IOException {
        bookParser.forkJoinWithThreshold(sentences);
    }


}
