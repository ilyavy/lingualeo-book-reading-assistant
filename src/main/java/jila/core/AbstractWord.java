package jila.core;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Represents word entity.
 * It is an abstract class. It does not implement functionality
 * related to contexts (the sentence, where the word has been used).
 * How to store and work with context should be decided in the concrete
 * implementations. It can be array, list, map or anything else.
 */
public abstract class AbstractWord
        implements Comparable<AbstractWord>, Jsonable {
    /**
     * String value of word entity.
     */
    private String word = "";

    /**
     * The translation of the word.
     */
    private String translate = "";

    /**
     * The flag, which shows either the word is known by the user or not.
     */
    private boolean known = false;

    /**
     * How much word has been found in the text.
     */
    private long count = 1;

    /**
     * The default constructor.
     */
    public AbstractWord() {
    }

    /**
     * Creates the word entity by its string value.
     * @param word  string representation of the word
     */
    public AbstractWord(final String word) {
        setWord(word);
    }

    /**
     * Creates the word entity by its string value and translation.
     * @param word  string representation of the word
     * @param translate the translation of the word
     */
    public AbstractWord(final String word, final String translate) {
        this(word);
        setTranslate(translate);
    }


    /**
     * Returns the string value of the word.
     * @return  string representation of the word
     */
    public String getWord() {
        return word;
    }

    /**
     * Returns the translation of the word.
     * @return  the translation of the word
     */
    public String getTranslate() {
        return translate;
    }

    /**
     * Returns the sentence, where the word has been used.
     * How to implement the storage and usage
     * of the context is dependent on the concrete class.
     * @return  context
     */
    public abstract String getContext();

    /**
     * Returns the value of the known flag.
     * @return boolean, either the word is already
     * known by a user or not
     */
    public boolean isKnown() {
        return known;
    }

    /**
     * Returns the value of the count.
     * @return how much times the word has been found in the text
     */
    public long getCount() {
        return count;
    }

    /**
     * Sets the new value for string representation of the word.
     * Should not be given public access. Supposed to be used only
     * inside the class.
     * @param word  a string representation of the word
     */
    protected void setWord(final String word) {
        this.word = word.toLowerCase();
    }

    /**
     * Sets the new translation of the word.
     * @param translate the tranlsation of the word
     */
    public void setTranslate(final String translate) {
        this.translate = translate;
    }

    /**
     * Sets the new value for the known flag.
     * @param known boolean value of the known flag
     */
    public void setKnown(final boolean known) {
        this.known = known;
    }

    /**
     * Sets new value for the count field.
     * @param newCount  a new value for the cound field
     */
    public void setCount(final long newCount) {
        count = newCount;
    }

    /**
     * Transforms the word into a string.
     * @return  the string form of the word entity
     */
    @Override
    public String toString() {
        return word;
    }

    /**
     * Compares the string representations of this word with another one.
     * @param that  the word to be compared with
     * @return  -1 - this < that, 0 - equal, +1 - this > that
     */
    @Override
    public int compareTo(final AbstractWord that) {
        return word.compareTo(that.word);
    }

    /**
     * Returns true if this and that word are equal in the
     * meaning of their string representations and the count values.
     * @param o the word to be compared with
     * @return  boolean, equal or not
     */
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof AbstractWord)) {
            return false;
        }
        AbstractWord that = (AbstractWord) o;
        if (word.equals(that.word) && count == that.count) {
            return true;
        }
        return false;
    }

    /**
     * Creates {@link JsonObject} without special attributes.
     * @return  JSON representation of the word with its fields
     */
    @Override
    public JsonObject toJsonObject() {
        return toJsonObject(null);
    }

    /**
     * Creates {@link JsonObject} with special attributes.
     * @param attributes    the map of the attributes to be used with the
     *                      resulting JsonObject
     * @return  JSON representation of the word with its fields
     */
    @Override
    public JsonObject toJsonObject(final Map<String, String> attributes) {
        JsonBuilderFactory factory = Json.createBuilderFactory(null);
        JsonObjectBuilder builder = factory.createObjectBuilder();

        builder.add("word", getWord());
        builder.add("translate", getTranslate());
        builder.add("context", getContext());
        builder.add("known", isKnown());
        builder.add("count", getCount());

        if (attributes != null) {
            for (Entry<String, String> entry : attributes.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }

        return builder.build();
    }


    /**
     * Creates new CountComparator.
     * @return new CountComparator
     */
    public static Comparator<AbstractWord> countOrder() {
        return new CountComparator();
    }


    /**
     * Comparator for words, which uses count value of the words
     * in order to compare them.
     */
    protected static class CountComparator
            implements Comparator<AbstractWord> {
        /**
         * Compares the word entities by their field count, i.e. the word
         * is bigger if it has been found more often in the source text.
         * @param o1
         * @param o2
         * @return -1 - o1 < o2, 0 - equal, +1 - o2 > o1
         */
        @Override
        public int compare(final AbstractWord o1, final AbstractWord o2) {
            Long c1 = Long.valueOf(o1.count);
            Long c2 = Long.valueOf(o2.count);
            return c1.compareTo(c2);
        }
    }
}
