package jila.model;

import java.util.Comparator;
import java.util.Objects;

/**
 * Represents word entity.
 * It is an abstract class. It does not implement functionality
 * related to contexts (the sentence, where the word has been used).
 * How to store and work with context should be decided in the concrete
 * implementations. It can be array, list, map or anything else.
 */
public abstract class Word implements Comparable<Word> {
    private int id;

    /**
     * String value of word entity.
     */
    private String word = "";

    /**
     * The translation of the word.
     */
    private String translate = "";

    private String context = "";

    /**
     * The flag, which shows either the word is known by the user or not.
     */
    private boolean known = false;

    /**
     * The default constructor.
     */
    public Word() {
    }

    /**
     * Creates the word entity by its string value.
     *
     * @param word string representation of the word
     */
    public Word(final String word) {
        setWord(word);
    }

    public Word(final String word, final String context) {
        this(word);
        setContext(context);
    }

    public int getId() {
        return id;
    }

    public Word setId(int id) {
        this.id = id;
        return this;
    }

    /**
     * Returns the string value of the word.
     *
     * @return string representation of the word
     */
    public String getWord() {
        return word;
    }

    /**
     * Returns the translation of the word.
     *
     * @return the translation of the word
     */
    public String getTranslate() {
        return translate;
    }

    /**
     * Returns the context (the sentence, where the word
     * has been used) as a string object.
     *
     * @return
     */
    public String getContext() {
        return context;
    }

    /**
     * Sets a context to the word.
     *
     * @param context the array of Word objects, forming the
     *                sentence, where the word has been used.
     */
    public Word setContext(final String context) {
        this.context = context;
        return this;
    }

    /**
     * Returns the value of the known flag.
     *
     * @return boolean, either the word is already known by a user or not
     */
    public boolean isKnown() {
        return known;
    }

    /**
     * Returns the value of the count.
     *
     * @return how much times the word has been found in the text
     */
    public abstract long getCount();

    /**
     * Sets the new value for string representation of the word.
     * Should not be given public access. Supposed to be used only
     * inside the class.
     *
     * @param word a string representation of the word
     */
    protected Word setWord(final String word) {
        this.word = word.toLowerCase();
        return this;
    }

    /**
     * Sets the new translation of the word.
     *
     * @param translate the tranlsation of the word
     */
    public Word setTranslate(final String translate) {
        this.translate = translate;
        return this;
    }

    /**
     * Sets the new value for the known flag.
     *
     * @param known boolean value of the known flag
     */
    public Word setKnown(final boolean known) {
        this.known = known;
        return this;
    }

    /**
     * Sets new value for the count field.
     *
     * @param newCount a new value for the cound field
     */
    public abstract Word setCount(long newCount);

    public abstract long incrementCount();

    /**
     * Transforms the word into a string.
     *
     * @return the string form of the word entity
     */
    @Override
    public String toString() {
        return word + " :: " + getCount();
    }

    /**
     * Compares the string representations of this word with another one.
     *
     * @param that the word to be compared with
     * @return -1 - this < that, 0 - equal, +1 - this > that
     */
    @Override
    public int compareTo(final Word that) {
        return word.compareTo(that.word);
    }

    /**
     * Returns true if this and that word are equal in the
     * meaning of their string representations and the count values.
     *
     * @param obj the word to be compared with
     * @return boolean, equal or not
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Word)) {
            return false;
        }
        Word that = (Word) obj;
        if (word.equals(that.word) && getCount() == that.getCount()) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, getCount());
    }

    /**
     * Creates new CountComparator.
     *
     * @return new CountComparator
     */
    public static Comparator<Word> countOrder() {
        return new CountComparator();
    }


    /**
     * Comparator for words, which uses count value of the words
     * in order to compare them.
     */
    protected static class CountComparator
            implements Comparator<Word> {
        /**
         * Compares the word entities by their field count, i.e. the word
         * is bigger if it has been found more often in the source text.
         *
         * @param o1
         * @param o2
         * @return -1 - o1 < o2, 0 - equal, +1 - o2 > o1
         */
        @Override
        public int compare(final Word o1, final Word o2) {
            Long c1 = o1.getCount();
            Long c2 = o2.getCount();
            return c1.compareTo(c2);
        }
    }
}
