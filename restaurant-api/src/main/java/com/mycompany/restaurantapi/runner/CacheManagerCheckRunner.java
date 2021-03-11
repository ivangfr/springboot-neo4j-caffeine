package com.mycompany.restaurantapi.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CacheManagerCheckRunner implements CommandLineRunner {

    private final CacheManager cacheManager;

    @Override
    public void run(String... strings) {
        log.info("=> Using cache manager: {}", cacheManager.getClass().getName());
    }
}
