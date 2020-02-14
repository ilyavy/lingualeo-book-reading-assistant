package com.github.ilyavy.controller;

import com.github.ilyavy.model.LingualeoProfile;
import com.github.ilyavy.model.Word;
import com.github.ilyavy.model.api.LoginResponse;
import com.github.ilyavy.model.api.TranslateResponse;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Provide the way to work with lingualeo api.
 */
public class LingualeoApi {
    private static final String BASE_URL = "https://api.lingualeo.com/";
    private volatile String cookie;

    /**
     * Create LingualeoApi object,
     * using default connector (with no proxy).
     */
    public LingualeoApi() {
    }

    /**
     * Create LingualeoApi object, using cookie.
     * Invoke of login method is not necessary after that.
     *
     * @param cookie - cookie string.
     */
    public LingualeoApi(final String cookie) {
        this.cookie = cookie;
    }

    /**
     * Authenticate user, using his email and password.
     *
     * @param email    - user's email
     * @param password - user's password. Should not be encrypted.
     * @return JsonObject, containing user's profile data.
     */
    public Mono<LingualeoProfile> login(final String email, final String password) {
        return WebClient.create(BASE_URL)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("login")
                        .queryParam("email", email)
                        .queryParam("password", password)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .doOnSuccess(response -> cookie = response.cookies().getFirst("remember").getValue())
                .flatMap(response -> response.bodyToMono(LoginResponse.class))
                .map(LoginResponse::getUser);
    }

    /**
     * Returns a translation of the specified word.
     *
     * @param word word to translate
     * @return
     */
    public Mono<Word> requestAndSetTranslation(Word word) {
        return WebClient.create(BASE_URL)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("gettranslates")
                        .queryParam("word", word)
                        .build())
                .cookie("remember", cookie)
                .retrieve()
                .bodyToMono(TranslateResponse.class)
                .map(r -> word.setTranslate(r.getTranslate()[0].getValue()));
    }

    /**
     * Adds the specified word with a translation and a context into user's dictionary.
     *
     * @param word word to add to a dictionary
     */
    public Mono<Word> addWordToDictionary(Word word) {
        return WebClient.create(BASE_URL)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("addword")
                        .queryParam("word", word.getWord())
                        .queryParam("translate", word.getTranslate())
                        .queryParam("context", word.getContext())
                        .build())
                .cookie("remember", cookie)
                .retrieve()
                .bodyToMono(String.class)
                .map(r -> word);
    }
}
