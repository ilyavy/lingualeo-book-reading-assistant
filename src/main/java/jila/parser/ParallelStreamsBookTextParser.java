package jila.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Book parser, which uses parallel streams to parallelize the job..
 */
public class ParallelStreamsBookTextParser extends BookTextParser {

    @Override
    public Map<String, Word> countWords(List<String> sentences) {
        Map<String, Word> wordsMap = sentences
                .stream()
                .map(this::parseSentence)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingByConcurrent(Word::getWord,
                        Collectors.reducing(null, (w1, w2) -> {
                            if (w1 == null) return w2;
                            if (w2 == null) return w1;

                            w1.incrementCount();
                            return w1;
                        })));

        return wordsMap;
    }

    private List<Word> parseSentence(final String sentence) {
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
    }
}
