package com.github.ilyavy.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.ilyavy.model.Word;

/**
 * Book parser, which divides the sentences into chunks, the number of which is equal to available CPU cores,
 * and then processes them in parallel using CompletableFutures and a blocking queue storing the results of
 * computations. The results are merged in parallel to chunks processing right in the moment they become available.
 */
public class FuturesBookTextParser extends BookTextParser {

    @Override
    public Map<String, Word> countWords(List<String> sentences) {
        final int availableProcessors = Runtime.getRuntime().availableProcessors();

        final ArrayBlockingQueue<Map<String, Word>> futuresResultsQueue = new ArrayBlockingQueue<>(availableProcessors);
        final AtomicInteger parsedSentencesChunksCounter = new AtomicInteger(availableProcessors);

        final int step = (int) Math.ceil(sentences.size() / Double.valueOf(availableProcessors));

        for (int i = 0; i < sentences.size(); i = i + step) {
            int lo = i;
            int hi = Math.min(i + step, sentences.size());
            CompletableFuture
                    .supplyAsync(() -> parseSentencesChunk(sentences, lo, hi))
                    .thenAccept(map -> {
                        futuresResultsQueue.add(map);
                        parsedSentencesChunksCounter.decrementAndGet();
                    });
        }

        Map<String, Word> result = null;
        try {
            result = CompletableFuture.supplyAsync(() ->
                    mergeMapsFromQueue(futuresResultsQueue, parsedSentencesChunksCounter)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private Map<String, Word> parseSentencesChunk(List<String> sentences, int lo, int hi) {
        Map<String, Word> map = new HashMap<>();

        for (int i = lo; i < hi; i++) {
            String sentence = sentences.get(i);
            parseSentence(sentence, map);
        }
        return map;
    }

    private Map<String, Word> mergeMapsFromQueue(ArrayBlockingQueue<Map<String, Word>> futuresResultsQueue,
                                                 AtomicInteger parsedSentencesChunksCounter) {

        try {
            Map<String, Word> left = futuresResultsQueue.take();
            Map<String, Word> right = futuresResultsQueue.take();

            Map<String, Word> result = mergeMaps(left, right);

            if (parsedSentencesChunksCounter.get() == 0 && futuresResultsQueue.size() == 0) {
                return result;
            }
            futuresResultsQueue.put(result);

            return mergeMapsFromQueue(futuresResultsQueue, parsedSentencesChunksCounter);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
}
