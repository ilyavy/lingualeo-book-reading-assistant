package com.github.ilyavy.config;

import java.time.Duration;

import com.github.ilyavy.model.Cookie;
import com.github.ilyavy.service.LingualeoService;
import com.github.ilyavy.service.UserService;
import io.github.pepperkit.retry.BackoffFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import static io.github.pepperkit.retry.Retry.retry;

@Configuration
public class AppConfiguration {

    private final ApplicationContext applicationContext;

    @Bean
    @DependsOn("userService")
    public LingualeoService lingualeoService() {
        UserService userService = applicationContext.getBean(UserService.class);

        retry(5)
                .backoff(new BackoffFunction.Fixed())
                .delay(Duration.ofMillis(300))
                .run(() -> {
                    if (userService.getCookie() == null) {
                        // logger.debug("Reading user's cookie: {}", userService.getCookie());
                        throw new NullPointerException();
                    }
                });

        Cookie cookie = userService.getCookie();
        if (cookie != null) {
            return new LingualeoService(userService.getLingualeoProfile(), cookie.getValue());
        } else {
            return new LingualeoService();
        }
    }

    @Autowired
    public AppConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
