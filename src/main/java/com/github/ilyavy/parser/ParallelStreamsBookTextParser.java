package com.github.ilyavy.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.ilyavy.model.Word;
import com.github.ilyavy.parser.word.SimpleWord;

/**
 * Book parser, which uses parallel streams to parallelize the job..
 */
public class ParallelStreamsBookTextParser extends BookTextParser {

    @Override
    public Map<String, Word> countWords(List<String> sentences) {
        Map<String, Word> wordsMap = sentences
                .parallelStream()
                .unordered()
                .map(this::parseSentence)
                .flatMap(Collection::stream)
                .collect(Collectors.toConcurrentMap(Word::getWord, Function.identity(), (w1, w2) -> {
                    w1.incrementCount();
                    return w1;
                }));


        // TODO: test all the parsers around the boundary -- N = 10000 sentences and probably around N = 8000 sentences
        // https://developer.ibm.com/articles/j-java-streams-5-brian-goetz/

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
