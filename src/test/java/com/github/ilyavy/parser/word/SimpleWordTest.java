package com.github.ilyavy.parser.word;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleWordTest {

    @Test
    void getAndSetCountWorkAsExpected() {
        var word = new SimpleWord("test");

        assertEquals(0, word.getCount());
        word.setCount(10);
        assertEquals(10, word.getCount());
    }

    @Test
    void incrementCountIncrementsOnlyOnOne() {
        var word = new SimpleWord("test", "context");

        assertEquals(0, word.getCount());
        assertEquals(1, word.incrementCount());
        word.incrementCount();
        assertEquals(2, word.getCount());
    }
}