package com.github.ilyavy.service.parser.word;

import java.util.concurrent.atomic.AtomicLong;

import com.github.ilyavy.model.Word;

/**
 * A thread-safe for counting implementation of {@link Word}.
 */
public class WordWithAtomicCounter extends Word {

    /**
     * How much word has been found in the text.
     */
    private final AtomicLong count = new AtomicLong(0);

    public WordWithAtomicCounter(final String word) {
        super(word);
    }

    public WordWithAtomicCounter(final String word, final String context) {
        super(word, context);
    }

    @Override
    public long getCount() {
        return count.get();
    }

    @Override
    public WordWithAtomicCounter setCount(final long newCount) {
        count.set(newCount);
        return this;
    }

    @Override
    public long incrementCount() {
        return count.incrementAndGet();
    }
}
