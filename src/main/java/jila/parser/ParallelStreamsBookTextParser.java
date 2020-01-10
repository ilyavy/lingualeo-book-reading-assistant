package jila.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParallelStreamsBookTextParser extends BookTextParser {

    @Override
    public Map<String, Word> countWords(List<String> sentences) {
        Map<String, Word> wordsMap = sentences.stream()
                .parallel()
                .map(sentence -> parseSentence(sentence, new HashMap<String, Word>()))
                .reduce(this::mergeMaps)
                .orElse(new HashMap<>());

        return wordsMap;
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
