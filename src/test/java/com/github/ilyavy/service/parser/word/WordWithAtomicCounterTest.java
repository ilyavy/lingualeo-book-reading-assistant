package com.github.ilyavy.service.parser.word;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WordWithAtomicCounterTest {

    @Test
    void getAndSetCountWorkAsExpected() {
        var word = new WordWithAtomicCounter("test");

        assertEquals(0, word.getCount());
        word.setCount(10);
        assertEquals(10, word.getCount());
    }

    @Test
    void incrementCountIncrementsOnlyOnOne() {
        var word = new WordWithAtomicCounter("test", "context");

        assertEquals(0, word.getCount());
        assertEquals(1, word.incrementCount());
        word.incrementCount();
        assertEquals(2, word.getCount());
    }
}