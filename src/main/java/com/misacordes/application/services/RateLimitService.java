package com.misacordes.application.services;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    // Cache de buckets por IP
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    
    // Límites por tipo de operación
    private static final int LOGIN_ATTEMPTS_PER_MINUTE = 5;
    private static final int API_REQUESTS_PER_MINUTE = 60;
    private static final int REGISTRATION_ATTEMPTS_PER_HOUR = 3;

    public boolean isLoginAllowed(String ipAddress) {
        String key = "login:" + ipAddress;
        Bucket bucket = cache.computeIfAbsent(key, k -> createLoginBucket());
        return bucket.tryConsume(1);
    }

    public boolean isRegistrationAllowed(String ipAddress) {
        String key = "register:" + ipAddress;
        Bucket bucket = cache.computeIfAbsent(key, k -> createRegistrationBucket());
        return bucket.tryConsume(1);
    }

    public boolean isApiRequestAllowed(String ipAddress) {
        String key = "api:" + ipAddress;
        Bucket bucket = cache.computeIfAbsent(key, k -> createApiRequestBucket());
        return bucket.tryConsume(1);
    }

    public void reset(String ipAddress) {
        cache.remove("login:" + ipAddress);
        cache.remove("register:" + ipAddress);
        cache.remove("api:" + ipAddress);
    }

    public void resetAll() {
        cache.clear();
    }


    private Bucket createLoginBucket() {
        // 5 intentos de login por minuto por IP
        Bandwidth limit = Bandwidth.classic(LOGIN_ATTEMPTS_PER_MINUTE, 
            Refill.intervally(LOGIN_ATTEMPTS_PER_MINUTE, Duration.ofMinutes(1)));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    private Bucket createRegistrationBucket() {
        Bandwidth limit = Bandwidth.classic(REGISTRATION_ATTEMPTS_PER_HOUR,
            Refill.intervally(REGISTRATION_ATTEMPTS_PER_HOUR, Duration.ofHours(1)));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    private Bucket createApiRequestBucket() {
        Bandwidth limit = Bandwidth.classic(API_REQUESTS_PER_MINUTE,
            Refill.intervally(API_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    public long getLoginAttemptsRemaining(String ipAddress) {
        String key = "login:" + ipAddress;
        Bucket bucket = cache.get(key);
        return bucket != null ? bucket.getAvailableTokens() : LOGIN_ATTEMPTS_PER_MINUTE;
    }
}

