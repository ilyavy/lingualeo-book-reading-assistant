package jila.model.api;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The model of a translation of a word.
 */
public class Translation {

    private int id;

    @JsonProperty("pic_url")
    private String pictureUrl;

    private String value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Translation that = (Translation) obj;
        return id == that.id &&
                Objects.equals(pictureUrl, that.pictureUrl) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pictureUrl, value);
    }
}
