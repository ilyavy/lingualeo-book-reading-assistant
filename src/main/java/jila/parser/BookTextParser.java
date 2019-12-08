package jila.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Phaser;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jila.reader.BookFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

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
    public List<String> getSentences(final String text) {
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
                    word.setContext(text);
                } else {
                    word.setCount(word.getCount() + 1);
                }
                /*if (text != null) {
                    if (word.getContext() == null || word.getContext().length() < 2 ||
                            word.getContext().length() > text.length()) {

                        word.setContext(text);
                    }
                }*/
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

    public void sequential(List<String> sentences) throws IOException {
        for (int i = 0; i < (int) sentences.size(); i++) {
            String sentence = sentences.get(i);
            getWords(sentence);
        }

        List<Word> words = new ArrayList<>(map.values());
        System.out.println(words.size());
    }

    public void forkJoinWithThreshold(List<String> sentences) throws IOException {
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        System.out.println("available cores: " + numberOfCores);

        ParseTextTask task = new ParseTextTask(0, sentences.size(), sentences,
                (int) Math.ceil(sentences.size() / Double.valueOf(numberOfCores)));
        List<Word> words = new ArrayList<>(task.compute().values());

        print(words);
    }

    protected Map<String, Word> getWordss(final Map<String, Word> map, final String text) {
        Pattern splitter = Pattern.compile(PATTERN);
        Matcher m = splitter.matcher(text);

        while (m.find()) {
            String wordStr = m.group().toLowerCase();
            if (wordStr.length() > WORD_LENGTH_THRESHOLD) {
                Word word = (Word) map.get(wordStr);
                if (word == null) {
                    word = new Word(wordStr);
                    word.setContext(text);
                } else {
                    word.setCount(word.getCount() + 1);
                }
/*                if (text != null) {
                    if (word.getContext() == null || word.getContext().length() < 2 ||
                            word.getContext().length() > text.length()) {

                        word.setContext(text);
                    }
                }*/
                map.put(wordStr, word);
            }
        }

        return map;
    }

    protected Map<String, Word> mergeMaps(Map<String, Word> left, Map<String, Word> right) {
        left.forEach((k, v) ->
                right.merge(k, v, (w1, w2) -> {
                    w1.setCount(w1.getCount() + w2.getCount());

                    if (w2.getContext().length() < w1.getContext().length()) {
                        w1.setContext(w2.getContext());
                    }

                    return w1;
                }));

        return right;
    }

    private final ArrayBlockingQueue<Map<String, Word>> futuresQueue =
            new ArrayBlockingQueue<>(20);
    private final AtomicInteger counter = new AtomicInteger(0);

    public List<Word> futures(List<String> sentences) throws ExecutionException, InterruptedException {
        counter.set(Runtime.getRuntime().availableProcessors());

        final int step = (int) Math.ceil(sentences.size() / Double.valueOf(Runtime.getRuntime().availableProcessors()));

        for (int i = 0; i < sentences.size(); i = i + step) {
            int lo = i;
            int hi = Math.min(i + step, sentences.size());
            CompletableFuture
                    .supplyAsync(() -> getWordsInFuture(sentences, lo, hi))
                    .thenAccept(map -> {
                        futuresQueue.add(map);
                        counter.decrementAndGet();
                    });
        }

        Map<String, Word> result = CompletableFuture.supplyAsync(() -> {
            Map<String, Word> r = null;
            try {
                r = mergeFromQueue();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return r;
        }).get();

        return new ArrayList<>(result.values());
    }

    protected Map<String, Word> getWordsInFuture(List<String> sentences, int lo, int hi) {
        Map<String, Word> map = new HashMap<>();
        for (int i = lo; i < hi; i++) {
            String sentence = sentences.get(i);
            getWordss(map, sentence);
        }

        // System.out.println("!--- finished: " + lo + ", " + hi);

        return map;
    }

    public Map<String, Word> mergeFromQueue() throws InterruptedException {
        var left = futuresQueue.take();
        var right = futuresQueue.take();

        var result = mergeMaps(left, right);

        if (counter.get() == 0 && futuresQueue.size() == 0) {
            return result;
        }

        futuresQueue.put(result);
        return mergeFromQueue();
    }


    Map<String, WordWithAtomicCounter> wordsMap = new ConcurrentHashMap<>();

    public void concurrentMapWithAtomicsUsingForkJoin(List<String> sentences) {
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        // System.out.println("available cores: " + numberOfCores);

        ParseTextTaskWithConcurrentMap task = new ParseTextTaskWithConcurrentMap(0, sentences.size(), sentences,
                (int) Math.ceil(sentences.size() / Double.valueOf(numberOfCores)));
        List<WordWithAtomicCounter> words = new ArrayList<>(task.compute().values());

        printt(words);
    }

    Phaser phaser = new Phaser();

    public void concurrentMapWithAtomicsUsingFuturesAndPhasers(List<String> sentences) throws ExecutionException, InterruptedException {
        final int step = (int) Math.ceil(sentences.size() / Double.valueOf(Runtime.getRuntime().availableProcessors()));

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < sentences.size(); i = i + step) {
            int lo = i;
            int hi = Math.min(i + step, sentences.size());
            futures.add(CompletableFuture.runAsync(() -> getWordsInFutureWithConcurrentMap(sentences, lo, hi)));
        }

        for (CompletableFuture<Void> f : futures) {
            f.get();
        }

        printt(new ArrayList<>(wordsMap.values()));
    }


    protected void getWordsInFutureWithConcurrentMap(List<String> sentences, int lo, int hi) {
        Map<String, Word> map = new HashMap<>();
        for (int i = lo; i < hi; i++) {
            String sentence = sentences.get(i);
            getWordsWithConcurrentMap(sentence);
        }

        // System.out.println("!--- finished: " + lo + ", " + hi);
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        BookTextParser bookParser = new BookTextParser();

        // String text = BookFileReader.createInstance("little_red_riding_hood.txt").readIntoString();
        String text = BookFileReader.createInstance("war-peace.txt").readIntoString();

        List<String> sentences = bookParser.getSentences(text);
        System.out.println("The number of sentences: " + sentences.size());
        text = null;

        new BookTextParser().concurrentMapWithAtomicsUsingFuturesAndPhasers(sentences);

        /*CompletableFuture<List<Word>> f1 = CompletableFuture.supplyAsync(() -> {
            List<Word> result = null;
            try {
                result = new BookTextParser().futures(sentences);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        });

        CompletableFuture<List<Word>> f2 = CompletableFuture.supplyAsync(() -> {
            List<Word> result = null;
            try {
                result = new BookTextParser().futures(sentences);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        });

        CompletableFuture<List<Word>> f3 = CompletableFuture.supplyAsync(() -> {
            List<Word> result = null;
            try {
                result = new BookTextParser().futures(sentences);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        });

        CompletableFuture<List<Word>> f4 = CompletableFuture.supplyAsync(() -> {
            List<Word> result = null;
            try {
                result = new BookTextParser().futures(sentences);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        });

        CompletableFuture<List<Word>> f5 = CompletableFuture.supplyAsync(() -> {
            List<Word> result = null;
            try {
                result = new BookTextParser().futures(sentences);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        });*/

        /*assertThat(f1.get())
                .isEqualTo(f2.get())
                .isEqualTo(f3.get())
                .isEqualTo(f4.get())
                .isEqualTo(f5.get());*/

/*        print(f1.get());
        print(f2.get());
        print(f3.get());
        print(f4.get());
        print(f5.get());*/
    }

    public static void print(List<Word> words) {
        System.out.println("--SIZE: " + words.size());
        /*int numberOfPrintedWords = 20;
        for (int i = 0; i < (numberOfPrintedWords < words.size() ? numberOfPrintedWords : words.size()); i++) {
            System.out.println(words.get(i));
        }*/
    }

    public static void printt(List<WordWithAtomicCounter> words) {
        System.out.println("--SIZE: " + words.size());
        /*int numberOfPrintedWords = 20;
        for (int i = 0; i < (numberOfPrintedWords < words.size() ? numberOfPrintedWords : words.size()); i++) {
            System.out.println(words.get(i));
        }*/
    }


    protected Map<String, WordWithAtomicCounter> getWordsWithConcurrentMap(final String text) {
        Pattern splitter = Pattern.compile(PATTERN);
        Matcher m = splitter.matcher(text);

        while (m.find()) {
            String wordStr = m.group().toLowerCase();
            if (wordStr.length() > WORD_LENGTH_THRESHOLD) {

                WordWithAtomicCounter wordRes = wordsMap.get(wordStr);
                if (wordRes == null) {
                    WordWithAtomicCounter word = new WordWithAtomicCounter(wordStr);
                    word.setContext(text);
                    wordRes = wordsMap.merge(wordStr, word, (w1, w2) -> w1);
                }
                wordRes.incrementCount();

/*                if (text != null) {
                    if (word.getContext() == null || word.getContext().length() < 2 ||
                            word.getContext().length() > text.length()) {

                        word.setContext(text);
                    }
                }*/
            }
        }

        return wordsMap;
    }



    public class ParseTextTaskWithConcurrentMap extends RecursiveTask<Map<String, WordWithAtomicCounter>> {
        private int lo;
        private int hi;
        private List<String> sentences;
        private int sequentialThreshold;

        public ParseTextTaskWithConcurrentMap(int lo, int hi, List<String> sentences, int sequentialThreshold) {
            this.lo = lo;
            this.hi = hi;
            this.sentences = sentences;
            this.sequentialThreshold = sequentialThreshold;
        }

        @Override
        protected Map<String, WordWithAtomicCounter> compute() {

            if (hi - lo <= sequentialThreshold) {
                for (int i = lo; i < hi; i++) {
                    String sentence = sentences.get(i);
                    getWordsWithConcurrentMap(sentence);
                }

            } else {
                int mid = (hi - lo) / 2;

                ParseTextTaskWithConcurrentMap left =
                        new ParseTextTaskWithConcurrentMap(lo, lo + mid, sentences, sequentialThreshold);
                left.fork();

                ParseTextTaskWithConcurrentMap right =
                        new ParseTextTaskWithConcurrentMap(lo + mid, hi, sentences, sequentialThreshold);
                right.compute();
                left.join();
            }

            return wordsMap;
        }

        @Override
        public String toString() {
            return "ParseTextTask{" +
                    "lo=" + lo +
                    ", hi=" + hi +
                    ", sequentialThreshold=" + sequentialThreshold +
                    '}';
        }
    }



    public class ParseTextTask extends RecursiveTask<Map<String, Word>> {

        private int lo;
        private int hi;
        private List<String> sentences;
        private int sequentialThreshold;

        public ParseTextTask(int lo, int hi, List<String> sentences, int sequentialThreshold) {
            this.lo = lo;
            this.hi = hi;
            this.sentences = sentences;
            this.sequentialThreshold = sequentialThreshold;

            // System.out.println(this);
        }

        @Override
        protected Map<String, Word> compute() {

            if (hi - lo <= sequentialThreshold) {
                Map<String, Word> map = new HashMap<>();
                for (int i = lo; i < hi; i++) {
                    String sentence = sentences.get(i);
                    getWordss(map, sentence);
                }
                return map;

            } else {
                // TODO: set the ForkJoinPool's size

                int mid = (hi - lo) / 2;
                ParseTextTask left = new ParseTextTask(lo, lo + mid, sentences, sequentialThreshold);
                left.fork();
                ParseTextTask right = new ParseTextTask(lo + mid, hi, sentences, sequentialThreshold);

                return mergeMaps(right.compute(), left.join());
            }
        }

        @Override
        public String toString() {
            return "ParseTextTask{" +
                    "lo=" + lo +
                    ", hi=" + hi +
                    ", sequentialThreshold=" + sequentialThreshold +
                    '}';
        }
    }
}
