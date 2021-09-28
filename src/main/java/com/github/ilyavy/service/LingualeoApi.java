package com.github.ilyavy.service;

import com.github.ilyavy.model.LingualeoProfile;
import com.github.ilyavy.model.Word;
import com.github.ilyavy.model.lingualeo.api.LoginResponse;
import com.github.ilyavy.model.lingualeo.api.TranslateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Wrapper around lingualeo.com API. Uses non-blocking communication with Lingualeo web-service,
 * some responses can be cached.
 */
@Component
public class LingualeoApi {

    private static final Logger logger = LoggerFactory.getLogger(LingualeoApi.class);

    private static final String BASE_URL = "https://api.lingualeo.com/";

    /** The name of the cookie, responsible for a user's session. */
    public static final String COOKIE_NAME = "remember";

    private volatile String sessionCookie;

    /** An active user's profile. */
    private LingualeoProfile lingualeoProfile;

    /**
     * Creates LingualeoApi object.
     */
    public LingualeoApi() {

    }

    /**
     * Creates LingualeoApi object with the specified parameters.
     * @param lingualeoProfile previously persisted user's profile
     * @param sessionCookie user's session cookie
     */
    public LingualeoApi(LingualeoProfile lingualeoProfile, String sessionCookie) {
        this.lingualeoProfile = lingualeoProfile;
        this.sessionCookie = sessionCookie;
    }

    /**
     * Authenticate user, using his email and password, the call is not blocking and works in a separate thread.
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
                .doOnSuccess(response -> sessionCookie = response.cookies().getFirst(COOKIE_NAME).getValue())
                .flatMap(response -> response.bodyToMono(LoginResponse.class))
                .map(LoginResponse::getUser)
                .doOnSuccess(p -> lingualeoProfile = p);
    }

    /**
     * Checks if the active user is authenticated already.
     * @return boolean true - if authenticated, false - otherwise
     */
    public boolean isUserAuthenticated() {
        return !(sessionCookie == null || sessionCookie.isBlank());
    }

    /**
     * Returns a lingualeo profile of the active user.
     * @return Lingualeo profile
     */
    public LingualeoProfile getLingualeoProfile() {
        return lingualeoProfile;
    }

    public String getSessionCookie() {
        return sessionCookie;
    }

    /**
     * Returns a translation of the specified word, the call is non-blocking.
     * @param word word to translate
     * @return Mono of the word with the translation
     */
    public Mono<Word> requestAndSetTranslation(Word word) {
        return WebClient.create(BASE_URL)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("gettranslates")
                        .queryParam("word", word)
                        .build())
                .cookie(COOKIE_NAME, sessionCookie)
                .retrieve()
                .bodyToMono(TranslateResponse.class)
                .map(r -> word.setTranslate(r.getTranslate()[0].getValue()));
    }

    /**
     * Adds the specified word with a translation and a context into user's dictionary, the call is non-blocking.
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
                .cookie(COOKIE_NAME, sessionCookie)
                .retrieve()
                .bodyToMono(String.class)
                .map(r -> word);
    }
}
