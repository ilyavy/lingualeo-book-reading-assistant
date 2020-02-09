package jila.model.api;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A representation of LL profile.
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

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getExpLevel() {
        return expLevel;
    }

    public void setExpLevel(int expLevel) {
        this.expLevel = expLevel;
    }

    public int getHungryPct() {
        return hungryPct;
    }

    public void setHungryPct(int hungryPct) {
        this.hungryPct = hungryPct;
    }

    public int getWordsCount() {
        return wordsCount;
    }

    public void setWordsCount(int wordsCount) {
        this.wordsCount = wordsCount;
    }

    public int getWordsKnown() {
        return wordsKnown;
    }

    public void setWordsKnown(int wordsKnown) {
        this.wordsKnown = wordsKnown;
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
