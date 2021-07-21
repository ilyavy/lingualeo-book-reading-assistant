package com.github.ilyavy.model;

public class Cookie {
    private int id;
    private int user_id;
    private String name;
    private String value;

    public Cookie() {
    }

    public Cookie(int user_id, String name, String value) {
        this.user_id = user_id;
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

    public int getUser_id() {
        return user_id;
    }

    public Cookie setUser_id(int user_id) {
        this.user_id = user_id;
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
