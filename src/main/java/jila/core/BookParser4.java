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
    protected Map<String, Word> specWords = new HashMap<>();
    
    
    public BookParser4() {
        map = new WordsTrieT();
        WordsTrieT.LOGGING = false;
    }
    
    @Override
    protected Map<String, Word> getWords(final String text) {
        Pattern splitter = Pattern.compile(PATTERN);
        Matcher m = splitter.matcher(text);

        List<Word2> sentence = new ArrayList<>();
        List<Word2> needContext = new ArrayList<>();

        while (m.find()) {
            String wordStr = m.group().toLowerCase();
            if (wordStr.length() > WORD_LENGTH_THRESHOLD) {
                Word2 word = new Word2(wordStr);

                ((WordsTrieT) map).add(word);
                word = ((WordsTrieT)map).get(wordStr);
                
                if (word == null) {
                    //System.out.println("the word wasn't added: " + wordStr);
                    word = new Word2(wordStr);
                    specWords.put(wordStr, word);
                    sentence.add((Word2) specWords.get(wordStr));
                    
                } else {
                    sentence.add(word);
                    needContext.add(word);
                }

            } else {                
                Word2 word = (Word2) specWords.get(wordStr);
                if (word == null) {
                    word = new Word2(wordStr);
                    specWords.put(wordStr, word);
                }
                sentence.add(word);
            }
        }
        
        Word2[] context = new Word2[sentence.size()];
        context = sentence.toArray(context);
        for (Word2 w : needContext) {
            Word2[] curContext = w.getContextArray();
            if (curContext == null 
                    || (context.length > 3 && context.length < curContext.length)) {
                w.setContext(context);
            }
        }
        
        return map;
    }
}
 