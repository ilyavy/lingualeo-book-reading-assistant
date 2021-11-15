package com.github.ilyavy.service;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;

import com.github.ilyavy.dao.UserDataDao;
import com.github.ilyavy.model.Cookie;
import com.github.ilyavy.model.LingualeoProfile;
import com.github.ilyavy.repository.LingualeoProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Schedulers;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserDataDao userDataDao;

    private final LingualeoProfileRepository lingualeoProfileRepository;

    private final AtomicReference<LingualeoProfile> lingualeoProfile = new AtomicReference<>();

    private final AtomicReference<String> sessionCookie = new AtomicReference<>();

    @Autowired
    public UserService(LingualeoProfileRepository lingualeoProfileRepository, UserDataDao userDataDao) {
        this.lingualeoProfileRepository = lingualeoProfileRepository;
        this.userDataDao = userDataDao;
    }

    /** Initializes DB tables. Should be replaced with proper migrations. */
    @PostConstruct
    public void initialize() {
        /* If the persistence is not available due to access rights or some other reason,
        the code will not fail, it will just make a record to the log and continue work without persistence.
        The work of this part is blocking. */
        userDataDao.initializeTablesIfNecessary()
                .then(userDataDao.getUserProfile()) // todo: for now only one active profile is supported
                .doOnSuccess(lingualeoProfile::set)
                .flatMap(p -> userDataDao.getCookie(p.getId(), LingualeoService.COOKIE_NAME))
                .subscribe(sessionCookie::set, e -> logger.error("Persistence is unavailable", e));
    }

    /**
     * Persists lingualeo profile.
     * @param lingualeoProfile the profile to persist
     */
    public void persistProfile(LingualeoProfile lingualeoProfile) {
        lingualeoProfileRepository
                .save(lingualeoProfile)
                .subscribeOn(Schedulers.single())
                .subscribe(ignored -> logger.info(
                                "The user's `{}` lingualeo profile is persisted", lingualeoProfile),
                        e -> logger.error("The user's profile cannot be persisted", e));
    }

    public void persistCookie(final LingualeoProfile profile, final String cookieName, final String cookieValue) {
        // userDataDao.persistCookie(profile, cookieName, cookieValue);
    }

    public LingualeoProfile getLingualeoProfile() {
        return lingualeoProfileRepository.findAll().blockFirst();
    }

    /**
     * Returns user's cookie.
     * @return
     */
    public Cookie getCookie() {
        if (lingualeoProfile.get() != null && sessionCookie.get() != null) {
            return new Cookie(lingualeoProfile.get().getId(), LingualeoService.COOKIE_NAME, sessionCookie.get());
        } else {
            return null;
        }
    }
}
