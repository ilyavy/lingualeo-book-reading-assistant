package jila.perfomance;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jila.parser.*;
import jila.reader.BookFileReader;
import org.openjdk.jmh.annotations.*;

@BenchmarkMode({Mode.SampleTime, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, warmups = 5, jvmArgs = {"-Xms2G", "-Xmx2G"})
public class BookAnalysisBenchmark {

    private List<String> sentences;

    @Setup
    public void setup() throws IOException {
        String text = BookFileReader.createInstance("../book-samples/war-peace.txt").readIntoString();

        BookTextParser bp = new SimpleSequentialBookTextParser();
        sentences = bp.parseTextIntoSentences(text);
    }

    @Benchmark
    public void sequential() {
        BookTextParser bookParser = new SimpleSequentialBookTextParser();
        bookParser.countWords(sentences);
    }

    @Benchmark
    public void forkJoinWithThreshold() {
        BookTextParser bookParser = new ForkJoinBookTextParser();
        bookParser.countWords(sentences);
    }

    @Benchmark
    public void futures() {
        BookTextParser bookParser = new FuturesBookTextParser();
        bookParser.countWords(sentences);
    }

    @Benchmark
    public void concurrentMapWithAtomicsUsingForkJoin() {
        BookTextParser bookParser = new ConcurrentMapWithAtomicWordCountersUsingForkJoinBookTextParser();
        bookParser.countWords(sentences);
    }

    @Benchmark
    public void concurrentMapWithAtomicsUsingThreadsAndPhaser() {
        BookTextParser bookParser = new ConcurrentMapWithAtomicWordCountersUsingThreadsAndPhaser();
        bookParser.countWords(sentences);
    }

    @Benchmark
    public void singleStreamNaive() {
        BookTextParser bookParser = new SingleStreamNaiveBookTextParser();
        bookParser.countWords(sentences);
    }

    @Benchmark
    public void parallelStreamsNaive() {
        BookTextParser bookParser = new ParallelStreamsNaiveBookTextParser();
        bookParser.countWords(sentences);
    }

    @Benchmark
    public void parallelStreams() {
        BookTextParser bookParser = new ParallelStreamsBookTextParser();
        bookParser.countWords(sentences);
    }

    @Benchmark
    public void parallelStreamsWithOnlyFlatmap() {
        BookTextParser bookParser = new ParallelStreamsWithOnlyFlatmapBookTextParser();
        bookParser.countWords(sentences);
    }

    @Benchmark
    public void parallelStreamsToMap() {
        BookTextParser bookParser = new ParallelStreamsToMapBookTextParser();
        bookParser.countWords(sentences);
    }

    @Benchmark
    public void parallelStreamsGroupingBy() {
        BookTextParser bookParser = new ParallelStreamsGroupingByBookTextParser();
        bookParser.countWords(sentences);
    }
}
