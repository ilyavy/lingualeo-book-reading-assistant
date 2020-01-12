package jila.perfomance;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jila.parser.BookTextParser;
import jila.parser.ConcurrentMapWithAtomicWordCountersUsingForkJoinBookTextParser;
import jila.parser.ConcurrentMapWithAtomicWordCountersUsingThreadsAndPhaser;
import jila.parser.ForkJoinBookTextParser;
import jila.parser.FuturesBookTextParser;
import jila.parser.ParallelStreamsBookTextParser;
import jila.parser.SimpleSequentialBookTextParser;
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
    public void concurrentMapWithAtomicsUsingFuturesAndPhasers() {
        BookTextParser bookParser = new ConcurrentMapWithAtomicWordCountersUsingThreadsAndPhaser();
        bookParser.countWords(sentences);
    }

    @Benchmark
    public void parallelStreams() {
        BookTextParser bookParser = new ParallelStreamsBookTextParser();
        bookParser.countWords(sentences);
    }
}
