package com.github.ilyavy.model.lingualeo.api;

import java.util.Arrays;

import com.github.ilyavy.model.Translation;

/**
 * Response of LL api call to get a translation of a word.
 */
public class TranslateResponse {

    private Translation[] translate;

    public Translation[] getTranslate() {
        return translate;
    }

    public void setTranslate(Translation[] translate) {
        this.translate = translate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TranslateResponse that = (TranslateResponse) obj;
        return Arrays.equals(translate, that.translate);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(translate);
    }
}
