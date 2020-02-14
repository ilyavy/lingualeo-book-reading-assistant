package com.github.ilyavy.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A representation of Lingualeo profile.
 */
public class LingualeoProfile {

    @JsonProperty("user_id")
    private int userId;

    private String nickname;

    @JsonProperty("xp_level")
    private int expLevel;

    @JsonProperty("hungry_pct")
    private int hungryPct;

    @JsonProperty("words_cnt")
    private int wordsCount;

    @JsonProperty("words_known")
    private int wordsKnown;

    public int getUserId() {
        return userId;
    }

    public LingualeoProfile setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public LingualeoProfile setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public int getExpLevel() {
        return expLevel;
    }

    public LingualeoProfile setExpLevel(int expLevel) {
        this.expLevel = expLevel;
        return this;
    }

    public int getHungryPct() {
        return hungryPct;
    }

    public LingualeoProfile setHungryPct(int hungryPct) {
        this.hungryPct = hungryPct;
        return this;
    }

    public int getWordsCount() {
        return wordsCount;
    }

    public LingualeoProfile setWordsCount(int wordsCount) {
        this.wordsCount = wordsCount;
        return this;
    }

    public int getWordsKnown() {
        return wordsKnown;
    }

    public LingualeoProfile setWordsKnown(int wordsKnown) {
        this.wordsKnown = wordsKnown;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LingualeoProfile that = (LingualeoProfile) obj;
        return userId == that.userId &&
                expLevel == that.expLevel &&
                hungryPct == that.hungryPct &&
                wordsCount == that.wordsCount &&
                wordsKnown == that.wordsKnown &&
                Objects.equals(nickname, that.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, nickname, expLevel, hungryPct, wordsCount, wordsKnown);
    }
}
