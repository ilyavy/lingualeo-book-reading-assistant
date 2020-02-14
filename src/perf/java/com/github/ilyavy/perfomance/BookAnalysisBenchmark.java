package com.github.ilyavy.perfomance;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.ilyavy.parser.BookTextParser;
import com.github.ilyavy.parser.ConcurrentMapWithAtomicWordCountersUsingForkJoinBookTextParser;
import com.github.ilyavy.parser.ConcurrentMapWithAtomicWordCountersUsingThreadsAndPhaser;
import com.github.ilyavy.parser.ForkJoinBookTextParser;
import com.github.ilyavy.parser.FuturesBookTextParser;
import com.github.ilyavy.parser.ParallelStreamsBookTextParser;
import com.github.ilyavy.parser.ParallelStreamsGroupingByBookTextParser;
import com.github.ilyavy.parser.ParallelStreamsNaiveBookTextParser;
import com.github.ilyavy.parser.ParallelStreamsToMapBookTextParser;
import com.github.ilyavy.parser.ParallelStreamsWithOnlyFlatmapBookTextParser;
import com.github.ilyavy.parser.SimpleSequentialBookTextParser;
import com.github.ilyavy.parser.SingleStreamNaiveBookTextParser;
import com.github.ilyavy.reader.BookFileReader;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@BenchmarkMode({Mode.SampleTime, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, warmups = 5, jvmArgs = {"-Xms2G", "-Xmx2G"})
public class BookAnalysisBenchmark {

    private List<String> sentences;

    @Setup
    public void setup() throws IOException {
        String text = BookFileReader.createInstance(new File("../book-samples/war-peace.txt")).readIntoString();

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
