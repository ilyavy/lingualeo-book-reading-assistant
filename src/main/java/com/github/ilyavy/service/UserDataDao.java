package com.github.ilyavy.service;

import com.github.ilyavy.model.Cookie;
import com.github.ilyavy.model.LingualeoProfile;
import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static org.springframework.data.r2dbc.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

/**
 * Data layer for the access to user's info persisted to the DB.
 */
@Service
public class UserDataDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDataDao.class);

    private static final String PERSISTENCE_FILE = "~/book-reading-assistant/localdata";

    private R2dbcEntityTemplate dbTemplate;

    /**
     * Creates the necessary tables if they don't exist.
     * @return just Mono to continue the pipeline
     */
    public Mono<?> initializeTablesIfNecessary() {
        ConnectionFactory connectionFactory = new H2ConnectionFactory(H2ConnectionConfiguration.builder()
                .file(PERSISTENCE_FILE)
                .build());
        logger.debug("-" + connectionFactory.getMetadata().getName() + "-");
        dbTemplate = new R2dbcEntityTemplate(connectionFactory);

        return dbTemplate.getDatabaseClient().sql("""
                    CREATE TABLE IF NOT EXISTS lingualeo_profile
                    (id INT PRIMARY KEY, nickname VARCHAR(255), exp_level INT, hungry_pct INT,
                    words_count INT, words_known INT)
                    """)
                .fetch()
                .rowsUpdated()
                .then(dbTemplate.getDatabaseClient().sql("""
                            CREATE TABLE IF NOT EXISTS cookie
                            (user_id INT, name VARCHAR(255), value VARCHAR(255),
                            PRIMARY KEY(user_id, name));
                            ALTER TABLE cookie ADD FOREIGN KEY (user_id) REFERENCES lingualeo_profile(id);
                            """)
                        .fetch()
                        .rowsUpdated());
    }

    /**
     * Retrieves user's profile from the DB.
     * @return lingualeo user's profile
     */
    public Mono<LingualeoProfile> getUserProfile() {
        return dbTemplate
                .select(LingualeoProfile.class)
                .first(); // todo: Add the logic to manage several users at the same PC
    }

    /**
     * Retrieves user's session cookie previously persisted.
     * @param profileId user's id
     * @param cookieName the name of the cookie of interest
     * @return user's session cookie
     */
    public Mono<String> getCookie(final int profileId, final String cookieName) {
        return dbTemplate
                .select(Cookie.class)
                .from("cookie")
                .matching(query(where("user_id").is(profileId)
                        .and("name").is(cookieName)))
                .first()
                .map(Cookie::getValue);
    }

    /**
     * Persists lingualeo profile, the call is non-blocking.
     * @param lingualeoProfile user's profile to persist
     */
    public void persistProfile(LingualeoProfile lingualeoProfile) {
        dbTemplate.select(LingualeoProfile.class)
                .matching(query(where("id").is(lingualeoProfile.getId())))
                .first()
                .switchIfEmpty(dbTemplate
                        .insert(LingualeoProfile.class)
                        .using(lingualeoProfile))
                .subscribeOn(Schedulers.single())
                .subscribe(ignored -> logger.info(
                        "The user's `{}` lingualeo profile is persisted", lingualeoProfile),
                        e -> logger.error("The user's profile cannot be persisted", e));
    }

    /**
     * Persists lingualeo profile, the call is non-blocking.
     * @param profile user's profile, to which the cookie belongs
     * @param cookieName cookie's name
     * @param cookieValue cookie's value
     */
    public void persistCookie(final LingualeoProfile profile, final String cookieName, final String cookieValue) {
        dbTemplate
                .insert(Cookie.class)
                .using(new Cookie(profile.getId(), cookieName, cookieValue))
                .subscribeOn(Schedulers.single())
                .subscribe(ignored -> logger.info(
                        "The user's `{}` session cookie is persisted", profile.getId()),
                        e -> logger.error("The user's cookie cannot be persisted", e));
    }
}
