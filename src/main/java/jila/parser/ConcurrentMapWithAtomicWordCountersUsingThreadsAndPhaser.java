package jila.parser;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Phaser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jila.model.Word;
import jila.parser.word.WordWithAtomicCounter;

/**
 * Book parser, which is similar to {@link ConcurrentMapWithAtomicWordCountersUsingForkJoinBookTextParser},
 * but uses simple threads instead of ForkJoin Framework to parallelize the job, and a phaser to sync the result.
 */
public class ConcurrentMapWithAtomicWordCountersUsingThreadsAndPhaser extends BookTextParser {

    @Override
    public Map<String, Word> countWords(final List<String> sentences) {
        double numberOfProcessors = Runtime.getRuntime().availableProcessors();
        int step = (int) Math.ceil(sentences.size() / numberOfProcessors);
        final Phaser phaser = new Phaser((int) numberOfProcessors + 1);

        Map<String, Word> wordsMap = new ConcurrentHashMap<>();

        for (int i = 0; i < sentences.size(); i = i + step) {
            int lo = i;
            int hi = Math.min(i + step, sentences.size());
            new Thread(() -> {
                for (int j = lo; j < hi; j++) {
                    parseSentence(sentences.get(j), wordsMap);
                }
                phaser.arriveAndDeregister();
            }).start();
        }

        phaser.arriveAndAwaitAdvance();
        return wordsMap;
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
}
