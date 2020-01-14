package jila.parser;

import java.util.List;
import java.util.stream.Stream;

/**
 * Naive streams implementation, with stream been parallelized.
 */
public class ParallelStreamsNaiveBookTextParser extends SingleStreamNaiveBookTextParser {

    @Override
    Stream<String> getSentencesStream(List<String> sentences) {
        return sentences.parallelStream();
    }
}
