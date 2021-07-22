package com.github.ilyavy.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;

/**
 * A representation of Lingualeo profile.
 */
public class LingualeoProfile {

    @Id
    @JsonProperty("user_id")
    private int id;

    private String nickname = "";

    @JsonProperty("xp_level")
    private int expLevel;

    @JsonProperty("hungry_pct")
    private int hungryPct;

    @JsonProperty("words_cnt")
    private int wordsCount;

    @JsonProperty("words_known")
    private int wordsKnown;

    public int getId() {
        return id;
    }

    public LingualeoProfile() {
    }

    public LingualeoProfile setId(int id) {
        this.id = id;
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
    public String toString() {
        return "LingualeoProfile{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", expLevel=" + expLevel +
                '}';
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
        return id == that.id &&
                expLevel == that.expLevel &&
                hungryPct == that.hungryPct &&
                wordsCount == that.wordsCount &&
                wordsKnown == that.wordsKnown &&
                Objects.equals(nickname, that.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nickname, expLevel, hungryPct, wordsCount, wordsKnown);
    }
}
