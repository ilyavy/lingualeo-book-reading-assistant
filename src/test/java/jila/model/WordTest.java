package jila.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WordTest {

    @Test
    void createWordTransformedIntoLowerCase() {
        var word = new WordForTests("Test");

        assertEquals("test", word.getWord());
    }

    @Test
    void toStringShowsOnlyWordAndCounter() {
        var word = new WordForTests("test")
                .setCount(0)
                .setContext("context")
                .setTranslate("translate")
                .setKnown(true);

        assertEquals("test :: 0", word.toString());
    }

    @Test
    void compareToAndEqualsAndHashCodeUseOnlyWordValue() {
        var word1 = new WordForTests("test", "context")
                .setId(1)
                .setCount(0)
                .setTranslate("translate")
                .setKnown(true);

        var word2 = new WordForTests("test", "different context")
                .setId(2)
                .setCount(100)
                .setTranslate("different translate")
                .setKnown(false);

        var word3 = new WordForTests("differentword")
                .setId(1)
                .setCount(0)
                .setContext("context")
                .setTranslate("translate")
                .setKnown(true);

        assertEquals(word1, word2);

        assertTwoWordsHaveAllFieldsEqualExceptWordValue(word1, word3);
        assertNotEquals(word1, word3);

        assertTrue(word1.compareTo(word3) > 0);
        assertTrue(word3.compareTo(word1) < 0);
        assertEquals(0, word1.compareTo(word2));

        assertEquals(word1.hashCode(), word2.hashCode());
        assertNotEquals(word1.hashCode(), word3.hashCode());
    }

    private void assertTwoWordsHaveAllFieldsEqualExceptWordValue(Word oneWord, Word otherWord) {
        assertEquals(oneWord.getId(), otherWord.getId());
        assertEquals(oneWord.getCount(), otherWord.getCount());
        assertEquals(oneWord.getContext(), otherWord.getContext());
        assertEquals(oneWord.getTranslate(), otherWord.getTranslate());
        assertEquals(oneWord.isKnown(), otherWord.isKnown());
        assertNotEquals(oneWord.getWord(), otherWord.getWord());
    }

    @Test
    void equalsComparesOnlyWordInstances() {
        var word = new WordForTests("word");
        assertFalse(word.equals("word"));
    }

    @Test
    void countOrderCorrectlyComparesByCountValue() {
        var comparator = Word.countOrder();

        var word1 = new WordForTests("word1").setCount(5);
        var word2 = new WordForTests("anotherword").setCount(10);
        var word3 = new WordForTests("thesamecounter").setCount(5);

        assertEquals(-1, comparator.compare(word1, word2));
        assertEquals(1, comparator.compare(word2, word1));
        assertEquals(0, comparator.compare(word1, word3));
    }

    public static class WordForTests extends Word {

        private long count;

        public WordForTests(String word) {
            super(word);
        }

        public WordForTests(String word, String context) {
            super(word, context);
        }

        @Override
        public long getCount() {
            return count;
        }

        @Override
        public Word setCount(long newCount) {
            this.count = newCount;
            return this;
        }

        @Override
        public long incrementCount() {
            return ++count;
        }
    }
}