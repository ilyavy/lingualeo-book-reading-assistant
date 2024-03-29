package com.github.ilyavy.service.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.ilyavy.model.Word;
import com.github.ilyavy.service.parser.word.SimpleWord;

/**
 * Book parser, which uses parallel streams to parallelize the job..
 */
public class ParallelStreamsGroupingByBookTextParser extends BookTextParser {

    @Override
    public Map<String, Word> countWords(List<String> sentences) {
        return sentences
                .parallelStream()
                .map(this::parseSentence)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingByConcurrent(Word::getWord,
                        Collectors.reducing(null, (w1, w2) -> {
                            if (w1 == null) {
                                return w2;
                            }
                            if (w2 == null) {
                                return w1;
                            }
                            w1.incrementCount();
                            return w1;
                        })));
    }

    /**
     * Parses a sentence into a list of words.
     *
     * @param sentence a sentence
     * @return list of words
     */
    public List<Word> parseSentence(final String sentence) {
        Pattern splitter = Pattern.compile(PATTERN);
        Matcher matcher = splitter.matcher(sentence);
        List<Word> list = new ArrayList<>();

        while (matcher.find()) {
            String wordStr = matcher.group().toLowerCase();
            if (wordStr.length() > WORD_LENGTH_THRESHOLD) {
                Word word = new SimpleWord(wordStr, sentence);
                word.incrementCount();
                list.add(word);
            }
        }
        return list;
    }
}
