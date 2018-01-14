package jila.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class. Allows to parse a text of a specified book.
 */
public final class BookParser4 extends BookParser {
    protected Map<String, AbstractWord> specWords = new HashMap<>();
    
    
    public BookParser4() {
        map = new WordsTrieT();
        WordsTrieT.LOGGING = false;
    }
    
    @Override
    protected Map<String, AbstractWord> getWords(final String text) {
        Pattern splitter = Pattern.compile(PATTERN);
        Matcher m = splitter.matcher(text);

        List<Word> sentence = new ArrayList<>();
        List<Word> needContext = new ArrayList<>();

        while (m.find()) {
            String wordStr = m.group().toLowerCase();
            if (wordStr.length() > WORD_LENGTH_THRESHOLD) {
                Word word = new Word(wordStr);

                ((WordsTrieT) map).add(word);
                word = ((WordsTrieT)map).get(wordStr);
                
                if (word == null) {
                    //System.out.println("the word wasn't added: " + wordStr);
                    word = new Word(wordStr);
                    specWords.put(wordStr, word);
                    sentence.add((Word) specWords.get(wordStr));
                    
                } else {
                    sentence.add(word);
                    needContext.add(word);
                }

            } else {                
                Word word = (Word) specWords.get(wordStr);
                if (word == null) {
                    word = new Word(wordStr);
                    specWords.put(wordStr, word);
                }
                sentence.add(word);
            }
        }
        
        Word[] context = new Word[sentence.size()];
        context = sentence.toArray(context);
        for (Word w : needContext) {
            Word[] curContext = w.getContextArray();
            if (curContext == null 
                    || (context.length > 3 && context.length < curContext.length)) {
                w.setContext(context);
            }
        }
        
        return map;
    }
}
 