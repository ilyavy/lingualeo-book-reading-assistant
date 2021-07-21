package com.github.ilyavy.model;

/**
 * Representation of cookie returned by LinguaLeo API.
 * @param userId user's id
 * @param name name of the cookie
 * @param value cookie's value
 */
public record Cookie(int userId, String name, String value) {

}
