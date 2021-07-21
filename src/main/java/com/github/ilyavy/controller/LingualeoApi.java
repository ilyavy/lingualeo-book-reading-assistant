package com.github.ilyavy.controller;

import com.github.ilyavy.model.Cookie;
import com.github.ilyavy.model.LingualeoProfile;
import com.github.ilyavy.model.Word;
import com.github.ilyavy.model.api.LoginResponse;
import com.github.ilyavy.model.api.TranslateResponse;
import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static org.springframework.data.r2dbc.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

/**
 * Wrapper around lingualeo.com API. Uses non-blocking communication with Lingualeo web-service,
 * some responses can be cached.
 */
public class LingualeoApi {

    private static final Logger logger = LoggerFactory.getLogger(LingualeoApi.class);

    private static final String PERSISTENCE_FILE = "~/book-reading-assistant/localdata";

    private static final String BASE_URL = "https://api.lingualeo.com/";

    /** The name of the cookie, responsible for a user's session. */
    private static final String COOKIE_NAME = "remember";

    private volatile String sessionCookie;

    /** An active user's profile. */
    private LingualeoProfile lingualeoProfile;

    private R2dbcEntityTemplate dbTemplate;

    /**
     * Creates LingualeoApi object. If the persistence is not available due to access rights or some other reason,
     * the constructor will not fail, it will just make a record to the log and continue work without persistence.
     * The work of the constructor is blocking.
     */
    public LingualeoApi() {
        try {
            H2ConnectionFactory connectionFactory = new H2ConnectionFactory(H2ConnectionConfiguration.builder()
                    .file(PERSISTENCE_FILE)
                    .build());
            logger.debug("-" + connectionFactory.getMetadata().getName() + "-");
            dbTemplate = new R2dbcEntityTemplate(connectionFactory);

            dbTemplate.getDatabaseClient().sql("""
                    CREATE TABLE IF NOT EXISTS lingualeo_profile
                    (id INT PRIMARY KEY, nickname VARCHAR(255), exp_level INT, hungry_pct INT,
                    words_count INT, words_known INT)
                    """)
                    .fetch()
                    .rowsUpdated()
                    .then(dbTemplate.getDatabaseClient().sql("""
                            CREATE TABLE IF NOT EXISTS cookies
                            (id INT PRIMARY KEY AUTO_INCREMENT, user_id INT, name VARCHAR(255), value VARCHAR(255));
                            ALTER TABLE cookies ADD FOREIGN KEY (user_id) REFERENCES lingualeo_profile(id);
                            """)
                            .fetch()
                            .rowsUpdated())
                    .then(dbTemplate
                            .select(LingualeoProfile.class)
                            .first())
                    .doOnSuccess(p -> lingualeoProfile = p)
                    .flatMap(p -> dbTemplate
                            .select(Cookie.class)
                            .from("cookies")
                            .matching(query(where("user_id").is(p.getId())
                                    .and("name").is(COOKIE_NAME)))
                            .first()
                            .map(Cookie::getValue)
                    )
                    .subscribe(v -> sessionCookie = v, e -> logger.error("Persistence is unavailable", e));

        } catch (Exception e) {
            logger.error("Cannot open persistence file {}", PERSISTENCE_FILE, e);
        }
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
                .doOnSuccess(p -> lingualeoProfile = p)
                .doOnSuccess(p -> persistProfile());
    }

    /**
     * Persists the active (current) profile together with the session cookie, the call is non-blocking.
     */
    void persistProfile() {
        dbTemplate.select(LingualeoProfile.class)
                //.from(LingualeoProfile.class)
                .matching(query(where("id").is(lingualeoProfile.getId())))
                //.fetch()
                .first()
                .switchIfEmpty(dbTemplate
                        .insert(LingualeoProfile.class)
                        //.into(LingualeoProfile.class)
                        .using(lingualeoProfile)
                        .then()
                        .map(v -> lingualeoProfile))
                .flatMap(ignored -> dbTemplate
                        .insert(Cookie.class)
                        .using(new Cookie(lingualeoProfile.getId(), COOKIE_NAME, sessionCookie))
                        .then())
                .subscribeOn(Schedulers.single())
                .subscribe(ignored -> logger.info(
                        "The user's `{}` lingualeo profile and the session cookie are persisted", lingualeoProfile),
                        e -> logger.error("The user's profile cannot be persisted", e));
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
