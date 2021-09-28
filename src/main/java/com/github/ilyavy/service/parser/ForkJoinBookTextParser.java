package com.github.ilyavy.service.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

import com.github.ilyavy.model.Word;

/**
 * Book parser, which recursively forks the sentences into chunks, the number of which is equal to available CPU cores,
 * and processes all the chunks in parallel, joining the results.
 */
public class ForkJoinBookTextParser extends BookTextParser {

    @Override
    public Map<String, Word> countWords(List<String> sentences) {
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        int sequentialThreshold = (int) Math.ceil(sentences.size() / Double.valueOf(numberOfCores));

        ParseSentencesTask task = new ParseSentencesTask(0, sentences.size(), sentences, sequentialThreshold);
        return task.compute();
    }

    /**
     * Recursive task's implementation, splits the job up to sequentialThreshold specified.
     */
    class ParseSentencesTask extends RecursiveTask<Map<String, Word>> {

        private int lo;

        private int hi;

        private List<String> sentences;

        private int sequentialThreshold;

        ParseSentencesTask(int lo, int hi, List<String> sentences, int sequentialThreshold) {
            this.lo = lo;
            this.hi = hi;
            this.sentences = sentences;
            this.sequentialThreshold = sequentialThreshold;
        }

        @Override
        protected Map<String, Word> compute() {
            if (hi - lo <= sequentialThreshold) {
                Map<String, Word> map = new HashMap<>();

                for (int i = lo; i < hi; i++) {
                    String sentence = sentences.get(i);
                    parseSentence(sentence, map);
                }
                return map;

            } else {
                int mid = (hi - lo) / 2;
                ParseSentencesTask left = new ParseSentencesTask(lo, lo + mid, sentences, sequentialThreshold);
                left.fork();
                ParseSentencesTask right = new ParseSentencesTask(lo + mid, hi, sentences, sequentialThreshold);

                return mergeMaps(right.compute(), left.join());
            }
        }

        private Map<String, Word> mergeMaps(Map<String, Word> left, Map<String, Word> right) {
            left.forEach((k, v) ->
                    right.merge(k, v, (w1, w2) -> {
                        w1.setCount(w1.getCount() + w2.getCount());
                        return w1;
                    }));

            return right;
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
