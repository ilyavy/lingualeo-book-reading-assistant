package jila.model;

/**
 * Represents word entity.
 * It is an abstract class. It does not implement functionality
 * related to contexts (the sentence, where the word has been used).
 * How to store and work with context should be decided in the concrete
 * implementations. It can be array, list, map or anything else.
 */
public class SimpleWord extends Word {

    /**
     * How much word has been found in the text.
     */
    private long count = 0;

    /**
     * The default constructor.
     */
    public SimpleWord() {
    }

    /**
     * Creates the word entity by its string value.
     *
     * @param word string representation of the word
     */
    public SimpleWord(final String word) {
        super(word);
    }

    public SimpleWord(final String word, final String context) {
        super(word, context);
    }

    /**
     * Returns the value of the count.
     *
     * @return how much times the word has been found in the text
     */
    public long getCount() {
        return count;
    }

    /**
     * Sets new value for the count field.
     *
     * @param newCount a new value for the cound field
     */
    @Override
    public Word setCount(final long newCount) {
        count = newCount;
        return this;
    }

    @Override
    public long incrementCount() {
        return count++;
    }

    /**
     * Transforms the word into a string.
     *
     * @return the string form of the word entity
     */
    @Override
    public String toString() {
        return getWord() + " :: " + getCount();
    }
}
