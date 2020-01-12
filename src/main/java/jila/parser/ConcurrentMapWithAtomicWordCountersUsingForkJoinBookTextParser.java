package jila.parser;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Book parser, which is similar to {@link ForkJoinBookTextParser}, the differences are: word implementation
 * uses atomic counter and all the words are stored in the concurrent hash map.
 */
public class ConcurrentMapWithAtomicWordCountersUsingForkJoinBookTextParser extends BookTextParser {

    @Override
    public Map<String, Word> countWords(final List<String> sentences) {
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        int sequentialThreshold = (int) Math.ceil(sentences.size() / Double.valueOf(numberOfCores));

        ParseSentencesTaskWithConcurrentMap task =
                new ParseSentencesTaskWithConcurrentMap(0, sentences.size(), sentences, sequentialThreshold);

        return task.compute();
    }

    @Override
    protected Map<String, Word> parseSentence(final String sentence, final Map<String, Word> wordsMap) {
        Pattern splitter = Pattern.compile(PATTERN);
        Matcher matcher = splitter.matcher(sentence);

        while (matcher.find()) {
            String wordStr = matcher.group().toLowerCase();
            if (wordStr.length() > WORD_LENGTH_THRESHOLD) {
                WordWithAtomicCounter word = (WordWithAtomicCounter) wordsMap.get(wordStr);
                if (word == null) {
                    word = (WordWithAtomicCounter) wordsMap
                            .merge(wordStr, new WordWithAtomicCounter(wordStr, sentence), (w1, w2) -> w1);
                }
                word.incrementCount();
            }
        }

        return wordsMap;
    }

    class ParseSentencesTaskWithConcurrentMap extends RecursiveTask<Map<String, Word>> {

        private int lo;

        private int hi;

        private List<String> sentences;

        private int sequentialThreshold;

        private Map<String, Word> wordsMap;

        ParseSentencesTaskWithConcurrentMap(int lo, int hi, List<String> sentences, int sequentialThreshold) {
            this.lo = lo;
            this.hi = hi;
            this.sentences = sentences;
            this.sequentialThreshold = sequentialThreshold;
            this.wordsMap = new ConcurrentHashMap<>();
        }

        ParseSentencesTaskWithConcurrentMap(
                int lo, int hi, List<String> sentences, int sequentialThreshold, Map<String, Word> wordsMap) {

            this.lo = lo;
            this.hi = hi;
            this.sentences = sentences;
            this.sequentialThreshold = sequentialThreshold;
            this.wordsMap = wordsMap;
        }

        @Override
        protected Map<String, Word> compute() {

            if (hi - lo <= sequentialThreshold) {
                for (int i = lo; i < hi; i++) {
                    String sentence = sentences.get(i);
                    parseSentence(sentence, wordsMap);
                }

            } else {
                int mid = (hi - lo) / 2;

                ParseSentencesTaskWithConcurrentMap left = new ParseSentencesTaskWithConcurrentMap(
                        lo, lo + mid, sentences, sequentialThreshold, wordsMap);
                left.fork();

                ParseSentencesTaskWithConcurrentMap right = new ParseSentencesTaskWithConcurrentMap(
                        lo + mid, hi, sentences, sequentialThreshold, wordsMap);
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
}
