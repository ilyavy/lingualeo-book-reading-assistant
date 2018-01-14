package jila.core;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    /**
     * Log4j v.2 logger.
     */
    protected static Logger logger;

    /**
     * Regex pattern for finding words in the text.
     */
    protected final String PATTERN = "[a-zA-Z]+";

    /**
     * Minimal length of the word, which is accounted for.
     */
    protected final int WORD_LENGTH_THRESHOLD = 3;


    protected Map<String, AbstractWord> map;

    /**
     * The default constructor.
     */
    public BookParser() {
        logger = LogManager.getLogger();
        map = new HashMap<String, AbstractWord>();
    }

    /**
     * Parse a text of a specified book.
     * @param filePath  - absolute path to a file of a book.
     * @return List<AbstractWord> list of words.
     */
    public List<AbstractWord> parse(final String filePath) {
        // TODO: Add support for another book formats.
        String text = getTextFromTxt(filePath);

        List<String> sentences = getSentences(text);

        for (String sentence : sentences) {
            getWords(sentence);
        }

        List<AbstractWord> words = new ArrayList<>(map.values());
        //List<AbstractWord> words = new ArrayList<AbstractWord>(getWords(text).values());
        Collections.sort(words, Collections.reverseOrder(AbstractWord.countOrder()));

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
     * Returns the list of strings, each of which is a sentence
     * from the provided text.
     * @param text  text to analyze.
     * @return  list of sentences.
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


    protected abstract Map<String, AbstractWord> getWords(final String text);

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
     * @param  letter   provided char symbol
     * @return  boolean: false or true.
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

/*        List<AbstractWord> words = new ArrayList<AbstractWord>(bp.map.values());

        System.out.println(words.size());*/
        logger.log(Level.INFO, bp.map.values().size());
        logger.log(Level.INFO, "finished");
    }
}
