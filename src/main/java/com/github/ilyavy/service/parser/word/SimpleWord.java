package com.github.ilyavy.service.parser.word;

import com.github.ilyavy.model.Word;

/**
 * A simple implementation of {@link Word}, not thread-safe for counting.
 */
public class SimpleWord extends Word {

    /**
     * How much word has been found in the text.
     */
    private long count = 0;

    public SimpleWord(final String word) {
        super(word);
    }

    public SimpleWord(final String word, final String context) {
        super(word, context);
    }

    public long getCount() {
        return count;
    }

    @Override
    public Word setCount(final long newCount) {
        count = newCount;
        return this;
    }

    @Override
    public long incrementCount() {
        return ++count;
    }
}
