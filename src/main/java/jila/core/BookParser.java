package jila.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class. Allows to parse a text of a specified book.
 */
public abstract class BookParser {
    protected final String PATTERN = "[a-zA-Z]+";
    protected final int WORD_LENGTH_THRESHOLD = 3;
    protected Map<String, Word> map;

    public BookParser() {
        map = new HashMap<String, Word>();
    }
    
    
    /**
     * Parse a text of a specified book.
     * @param filePath  - absolute path to a file of a book
     * @return List<Word> list of words
     */
    public List<Word> parse(final String filePath) {
        String text = getTextFromTxt(filePath);
        
        List<String> sentences = getSentences(text);

        for (String sentence : sentences) {
            getWords(sentence);
        }
        
        List<Word> words = new ArrayList<Word>(map.values());
        
        //List<Word> words = new ArrayList<Word>(getWords(text).values());
        Collections.sort(words, Collections.reverseOrder(Word.countOrder()));
        
        System.out.println("parse done");
        return words;
    }
    
    
    /**
     * Extracts a text from a TXT file.
     * @param filePath  - absolute path to a file.
     * @return  String - text of a book.
     */
    protected String getTextFromTxt(final String filePath) {
        StringBuilder sb = new StringBuilder();
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + " ");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
    
    
    /**
     * 
     * @param text
     * @return
     */
    protected List<String> getSentences(final String text) {
        List<String> sentences = new ArrayList<String>(); 
        Pattern splitter = Pattern.compile("[^!?.]+");
        Matcher m = splitter.matcher(text);
        
        while (m.find()) {
            String sentence = m.group();
            sentences.add(sentence);
        }
        
        return sentences;
    }
    
    
    /** Returns the tokens that match the regex pattern from the document 
     * text string.
     * @param pattern A regular expression string specifying the 
     *   token pattern desired
     * @param pattern A string
     * @return A List of tokens from the document text that match the regex 
     *   pattern
     */
    protected abstract Map<String, Word> getWords(final String text);   
    
    
    /** return the Flesch readability score of this document */
    public double getFleschScore(final long numSentences,
            final long numWords, final long numSyllables) {
        double secArg = (double) numWords/numSentences;
        double thirdArg = (double) numSyllables/numWords;
        double result = 206.835 - 1.015*secArg - 84.6*thirdArg;
        return result;
    }
    
    
 // This is a helper function that returns the number of syllables
    // in a word.  You should write this and use it in your 
    // BasicDocument class.
    // You will probably NOT need to add a countWords or a countSentences method
    // here.  The reason we put countSyllables here because we'll use it again
    // next week when we implement the EfficientDocument class.
    protected int countSyllables(String word)
    {
        int counter = 0;
        char[] tokens = word.toCharArray();
        if (isVowel(tokens[0])) {
            counter = 1;
        }
        for (int i = 1; i < tokens.length; i++) {
            if (i == tokens.length - 1 && Character.toLowerCase(tokens[i]) == 'e') {
                if (!isVowel(tokens[i-1])) {
                    if (counter == 0) {
                        counter = 1;
                    }
                }
            } else if (!isVowel(tokens[i-1]) && isVowel(tokens[i])) {
                counter++;
            }
        }
        
        return counter;
    }
    
    
    /**
     * Is char symbol a vowel.
     * @param  char letter
     * @return
     */
    protected boolean isVowel(char letter) {
        char[] vowels = new char[6];
        vowels[0] = 'a';
        vowels[1] = 'e';
        vowels[2] = 'i';
        vowels[3] = 'o';
        vowels[4] = 'u';
        vowels[5] = 'y';
        
        for (int i = 0; i < 6; i++) {
            if (Character.toLowerCase(letter) == vowels[i]) {
                return true;
            }
        }
        return false;
    }
    
    
    
    public static void main(String[] args) {
        BookParser bp = new BookParser4();
        String text = bp.getTextFromTxt("war-peace.txt");
        
        List<String> sentences = bp.getSentences(text);
        text = null;
        
        for (int i = 0; i < (int) sentences.size(); i++) {
            String sentence = sentences.get(i);
            bp.getWords(sentence);
        }
        sentences = null;
        
/*        List<Word> words = new ArrayList<Word>(bp.map.values());
        
        System.out.println(words.size());*/
        System.out.println(bp.map.values().size());
        System.out.println("finished");
    }
}
 