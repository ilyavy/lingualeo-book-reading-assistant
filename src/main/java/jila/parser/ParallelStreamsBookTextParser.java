package jila.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Book parser, which uses parallel streams to parallelize the job..
 */
public class ParallelStreamsBookTextParser extends BookTextParser {

    @Override
    public Map<String, Word> countWords(List<String> sentences) {
        /*long before = System.nanoTime();
        Map<String, Word> wordsMap = sentences.parallelStream()
                .map(sentence -> parseSentence(sentence, new HashMap<String, Word>()))
                .reduce(this::mergeMaps)
                .orElse(new HashMap<>());
        long after = System.nanoTime();
        long result = after - before;
        System.out.println("time: " + result/1_000_000);*/

        long before = System.nanoTime();
        Map<String, Optional<Word>> wordsMap = sentences
                .parallelStream()
                .map(sentence -> {
                    Pattern splitter = Pattern.compile(PATTERN);
                    Matcher m = splitter.matcher(sentence);
                    List<Word> list = new ArrayList<>();

                    while (m.find()) {
                        String wordStr = m.group().toLowerCase();
                        if (wordStr.length() > WORD_LENGTH_THRESHOLD) {
                            Word word = new SimpleWord(wordStr, sentence);
                            word.incrementCount();
                            list.add(word);
                        }
                    }
                    return list;
                })
                .flatMap(Collection::stream)
                // .sorted() -> should not be used if groupingByConcurrent is used
                // is collect is ok for parallel? (effective java)
                // here it seems ok if concurrent alt is used:
                // https://docs.oracle.com/javase/tutorial/collections/streams/parallelism.html
                .collect(Collectors.groupingByConcurrent(Word::getWord, Collectors.reducing((w1, w2) -> {
                    w1.incrementCount();
                    return w1;
                })));

        long after = System.nanoTime();
        long result = after - before;
        System.out.println("time: " + result/1_000_000);

        System.out.println(wordsMap);

        return null;
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
