package org.sample;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
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

    private List<String> sentences;

    @Setup
    public void setup() throws IOException {
        String text = BookFileReader.createInstance("war-peace.txt").readIntoString();

        BookTextParser bp = new BookTextParser();
        sentences = bp.getSentences(text);
    }

    @Benchmark
    public void sequential() throws IOException {
        BookTextParser bookParser = new BookTextParser();
        bookParser.sequential(sentences);
    }

    @Benchmark
    public void forkJoinWithThreshold() throws IOException {
        BookTextParser bookParser = new BookTextParser();
        bookParser.forkJoinWithThreshold(sentences);
    }

    @Benchmark
    public void futures() throws ExecutionException, InterruptedException, IOException {
        BookTextParser bookParser = new BookTextParser();
        bookParser.futures(sentences);
    }

    @Benchmark
    public void concurrentMapWithAtomicsUsingForkJoin() {
        BookTextParser bookParser = new BookTextParser();
        bookParser.concurrentMapWithAtomicsUsingForkJoin(sentences);
    }
}
