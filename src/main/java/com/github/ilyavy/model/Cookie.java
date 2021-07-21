package com.github.ilyavy.model;

/**
 * Representation of cookie returned by LinguaLeo API.
 */
public class Cookie {
    private int id;
    private int userId;
    private String name;
    private String value;

    public Cookie() {
    }

    /**
     * Creates new cookie with the provided parameters.
     * @param userId user's id
     * @param name name
     * @param value the value of cookie (returned by LinguaLeo API)
     */
    public Cookie(int userId, String name, String value) {
        this.userId = userId;
        this.name = name;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public Cookie setId(int id) {
        this.id = id;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Cookie setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public String getValue() {
        return value;
    }

    public Cookie setValue(String value) {
        this.value = value;
        return this;
    }
}
