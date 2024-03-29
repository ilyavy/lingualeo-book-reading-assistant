package com.github.ilyavy.service.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.ilyavy.model.Word;
import com.github.ilyavy.service.parser.word.SimpleWord;

/**
 * Book parser, which uses parallel streams to parallelize the job..
 */
public class ParallelStreamsWithOnlyFlatmapBookTextParser extends BookTextParser {

    @Override
    public Map<String, Word> countWords(List<String> sentences) {
        Map<String, Word> wordsMap = sentences
                .parallelStream()
                .flatMap(s -> this.parseSentence(s).stream())
                .collect(Collectors.toConcurrentMap(Word::getWord, Function.identity(), (w1, w2) -> {
                    w1.incrementCount();
                    return w1;
                }));

        return wordsMap;
    }

    /**
     * Parses a sentence into a list of words.
     * @param sentence  a sentence
     * @return  list of words
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
