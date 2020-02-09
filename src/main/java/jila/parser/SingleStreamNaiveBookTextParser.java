package jila.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import jila.model.Word;

/**
 * Naive implementation of BookTextParser, using serial stream of sentences.
 */
public class SingleStreamNaiveBookTextParser extends BookTextParser {

    @Override
    public Map<String, Word> countWords(List<String> sentences) {
        return getSentencesStream(sentences)
                .map(sentence -> parseSentence(sentence, new HashMap<String, Word>()))
                .reduce(this::mergeMaps)
                .orElse(new HashMap<>());
    }

    Stream<String> getSentencesStream(List<String> sentences) {
        return sentences.stream();
    }

    private Map<String, Word> mergeMaps(Map<String, Word> left, Map<String, Word> right) {
        left.forEach((k, v) ->
                right.merge(k, v, (w1, w2) -> {
                    w1.setCount(w1.getCount() + w2.getCount());
                    return w1;
                }));

        return right;
    }
}
