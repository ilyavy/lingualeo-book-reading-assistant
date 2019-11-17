package jila.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jila.reader.BookReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class. Allows to parse a text of a specified book.
 */
public class BookTextParser {
    protected static Logger logger = LoggerFactory.getLogger(BookTextParser.class);

    /**
     * Regex pattern for finding words in the text.
     */
    protected final String PATTERN = "[a-zA-Z]+";

    /**
     * Minimal length of the word, which is accounted for.
     */
    protected final int WORD_LENGTH_THRESHOLD = 3;


    protected Map<String, Word> map = new HashMap<>();

    /**
     * The default constructor.
     */
    public BookTextParser() {

    }

    /**
     * Parse a text of a specified book.
     *
     * @param bookText - the text of the book
     * @return List<Word> list of words.
     */
    public List<Word> parse(final String bookText) {
        List<String> sentences = getSentences(bookText);

        for (String sentence : sentences) {
            getWords(sentence);
        }

        List<Word> words = new ArrayList<>(map.values());
        Collections.sort(words, Collections.reverseOrder(Word.countOrder()));

        return words;
    }


    /**
     * Returns the list of strings, each of which is a sentence
     * from the provided text.
     *
     * @param text text to analyze.
     * @return list of sentences.
     */
    protected List<String> getSentences(final String text) {
        List<String> sentences = new ArrayList<>();
        Pattern splitter = Pattern.compile("[^!?.]+");
        Matcher m = splitter.matcher(text);

        while (m.find()) {
            String sentence = m.group();
            sentences.add(sentence);
        }

        return sentences;
    }


    protected Map<String, Word> getWords(final String text) {
        Pattern splitter = Pattern.compile(PATTERN);
        Matcher m = splitter.matcher(text);

        while (m.find()) {
            String wordStr = m.group().toLowerCase();
            if (wordStr.length() > WORD_LENGTH_THRESHOLD) {
                Word word = (Word) map.get(wordStr);
                if (word == null) {
                    word = new Word(wordStr);
                } else {
                    word.setCount(word.getCount() + 1);
                }
                if (text != null) {
                    if (word.getContext() == null || word.getContext().length() < 2 ||
                            word.getContext().length() > text.length()) {

                        word.setContext(text);
                    }
                }
                map.put(wordStr, word);
            }
        }

        return map;
    }

    /**
     * Return the Flesch readability score of this document.
     */
    public double getFleschScore(final long numSentences,
                                 final long numWords, final long numSyllables) {
        double secArg = (double) numWords / numSentences;
        double thirdArg = (double) numSyllables / numWords;
        double result = 206.835 - (1.015 * secArg) - (84.6 * thirdArg);
        return result;
    }

    /**
     * This is a helper function that returns the number of syllables
     * in a word.  You should write this and use it in your
     * BasicDocument class.
     * You will probably NOT need to add a countWords or a countSentences method
     * here.  The reason we put countSyllables here because we'll use it again
     * next week when we implement the EfficientDocument class.
     */
    protected int countSyllables(String word) {
        int counter = 0;
        char[] tokens = word.toCharArray();
        if (isVowel(tokens[0])) {
            counter = 1;
        }
        for (int i = 1; i < tokens.length; i++) {
            if (i == tokens.length - 1 && Character.toLowerCase(tokens[i]) == 'e') {
                if (!isVowel(tokens[i - 1])) {
                    if (counter == 0) {
                        counter = 1;
                    }
                }
            } else if (!isVowel(tokens[i - 1]) && isVowel(tokens[i])) {
                counter++;
            }
        }

        return counter;
    }


    /**
     * Returns is a given char symbol a vowel or not.
     *
     * @param letter provided char symbol
     * @return boolean: false or true.
     */
    protected boolean isVowel(final char letter) {
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


    public static void main(String[] args) throws IOException {
        BookTextParser bp = new BookTextParser();
        String text = BookReader.createInstance("war-peace.txt").readIntoString();

        List<String> sentences = bp.getSentences(text);
        text = null;

        for (int i = 0; i < (int) sentences.size(); i++) {
            String sentence = sentences.get(i);
            bp.getWords(sentence);
        }
        sentences = null;

        List<Word> words = new ArrayList<>(bp.map.values());

        System.out.println(words.size());
    }
}
