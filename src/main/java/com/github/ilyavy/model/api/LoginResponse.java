package com.github.ilyavy.model.api;

import com.github.ilyavy.model.LingualeoProfile;

/**
 * Response from Lingualeo API to the login request.
 */
public class LoginResponse {

    private LingualeoProfile user;

    public LingualeoProfile getUser() {
        return user;
    }

    public LoginResponse setUser(LingualeoProfile user) {
        this.user = user;
        return this;
    }
}
