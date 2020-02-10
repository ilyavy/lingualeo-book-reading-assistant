package jila.model;

import java.util.Comparator;
import java.util.Objects;

/**
 * Represents word entity.
 * It is an abstract class - logic for counting should be implemented by ancestors.
 */
public abstract class Word implements Comparable<Word> {
    /**
     * Id of the word.
     */
    private int id;

    /**
     * String value of word entity.
     */
    private String word = "";

    /**
     * The translation of the word.
     */
    private String translate = "";

    /**
     * A sentence, in which the word is used.
     */
    private String context = "";

    /**
     * The flag, which shows either the word is known by the user or not.
     */
    private boolean known = false;

    /**
     * The default constructor is not supported.
     */
    private Word() {
        throw new UnsupportedOperationException("A word cannot be created without its string value.");
    }

    /**
     * Creates the word entity by its string value.
     *
     * @param word string representation of the word
     */
    public Word(final String word) {
        setWord(word);
    }

    /**
     * Creates the word entity by its string value and a context.
     *
     * @param word    string representation of the word
     * @param context sentence, in which the word is used
     */
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

    public String getWord() {
        return word;
    }

    public String getTranslate() {
        return translate;
    }

    public String getContext() {
        return context;
    }

    public Word setContext(final String context) {
        this.context = context;
        return this;
    }

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
     * Should not be given public access. Supposed to be used only inside the class.
     *
     * @param word a string representation of the word
     */
    protected Word setWord(final String word) {
        this.word = word.toLowerCase();
        return this;
    }

    public Word setTranslate(final String translate) {
        this.translate = translate;
        return this;
    }

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

    /**
     * Increments the value of the inner counter.
     *
     * @return resulting value of the counter
     */
    public abstract long incrementCount();

    @Override
    public String toString() {
        return word + " :: " + getCount();
    }

    /**
     * Compares the string representations of this word with another one.
     *
     * @param that the word to be compared with
     * @return the value {@code 0} if the argument word's string representation is equal to this word's string
     *         representation; a value less than {@code 0} if this word's string representation is lexicographically
     *         less than the word's string representation argument; and a value greater than {@code 0} otherwise.
     */
    @Override
    public int compareTo(final Word that) {
        return word.compareTo(that.word);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Word)) {
            return false;
        }
        Word that = (Word) obj;
        return word.equals(that.word);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word);
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
     * Comparator for words, which uses count value of the words in order to compare them.
     */
    protected static class CountComparator implements Comparator<Word> {
        /**
         * Compares the word entities by their field count, i.e. the word is bigger
         * if it has been found more often in the source text.
         *
         * @param o1 first word
         * @param o2 second word
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
