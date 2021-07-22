package com.github.ilyavy.model;

import org.springframework.data.annotation.Id;

public class Cookie {
    @Id // todo: workaround for Spring, the actual primary key is (userId, name)
    private int userId;

    private String name;

    private String value;

    public Cookie() {

    }

    /**
     * Constructs cookie representation with the specified parameters.
     * @param userId user's id
     * @param name name of the cookie
     * @param value cookie's value
     */
    public Cookie(int userId, String name, String value) {
        this.userId = userId;
        this.name = name;
        this.value = value;
    }

    public int getUserId() {
        return userId;
    }

    public Cookie setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public String getName() {
        return name;
    }

    public Cookie setName(String name) {
        this.name = name;
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

