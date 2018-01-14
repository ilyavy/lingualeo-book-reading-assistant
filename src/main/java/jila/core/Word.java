package jila.core;

/**
 * The concrete implementation of AbstractWord class.
 * The context is implemented as an array of Word objects.
 */
public class Word extends AbstractWord {
    /**
     * The context is stored as an array of Word objects.
     * The order of the words in the array is the same as
     * the order in the original sentence.
     */
    private Word[] context = null;

    /**
     * The default constructor.
     */
    public Word() {
    }

    /**
     * Creates the word entity by its string value.
     * @param word  string representation of the word
     */
    public Word(final String word) {
        super(word);
    }

    /**
     * Creates the word entity by its string value and translation.
     * @param word  string representation of the word
     * @param translate the translation of the word
     */
    public Word(final String word, final String translate) {
        super(word, translate);
    }

    /**
     * Returns the context in the form, in which it is stored -
     * array of Word objects. The method returns the reference
     * to the field of the Word object, so the sentence can be
     * manipulated directly.
     * @return  the reference to the context field of the Word object
     */
    public Word[] getContextArray() {
        return context;
    }

    /**
     * Returns the context (the sentence, where the word
     * has been used) as a string object.
     * @return
     */
    @Override
    public String getContext() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < context.length; i++) {
            String w = context[i].getWord();
            if (i == 0) {
                w = Character.toUpperCase(w.charAt(0)) + w.substring(1);
            }
            sb.append(w + " ");
        }

        return sb.toString();
    }

    /**
     * Sets a context to the word.
     * @param context   the array of Word objects, forming the
     *                  sentence, where the word has been used.
     */
    public void setContext(final Word[] context) {
        this.context = context;
    }
}
