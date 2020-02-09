package jila.model;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents word entity.
 * It is an abstract class. It does not implement functionality
 * related to contexts (the sentence, where the word has been used).
 * How to store and work with context should be decided in the concrete
 * implementations. It can be array, list, map or anything else.
 */
public class WordWithAtomicCounter extends Word {

    /**
     * How much word has been found in the text.
     */
    private final AtomicLong count = new AtomicLong(0);

    /**
     * Creates the word entity by its string value.
     *
     * @param word string representation of the word
     */
    public WordWithAtomicCounter(final String word) {
        super(word);
    }

    public WordWithAtomicCounter(final String word, final String context) {
        super(word, context);
    }

    /**
     * Returns the value of the count.
     *
     * @return how much times the word has been found in the text
     */
    @Override
    public long getCount() {
        return count.get();
    }

    /**
     * Sets new value for the count field.
     *
     * @param newCount a new value for the cound field
     */
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
