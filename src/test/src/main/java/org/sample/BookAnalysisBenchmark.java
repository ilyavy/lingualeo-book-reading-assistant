package org.sample;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jila.parser.BookTextParser;
import jila.reader.BookFileReader;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
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

    public void futures() {

    }

    public void concurrentMapWithAtomics() {

    }
}
