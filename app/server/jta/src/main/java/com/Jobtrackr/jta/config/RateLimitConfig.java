package com.Jobtrackr.jta.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class RateLimitConfig {

    @Value("${rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${rate-limit.ai-requests-per-hour:20}")
    private int aiRequestsPerHour;

    @Value("${rate-limit.login-attempts-per-minute:5}")
    private int loginAttemptsPerMinute;

    @Bean
    public Cache<String, Bucket> rateLimitCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(100_000)
                .build();
    }

    public Bucket createStandardBucket() {
        Bandwidth limit = Bandwidth.classic(requestsPerMinute, 
                Refill.intervally(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    public Bucket createAIBucket() {
        Bandwidth limit = Bandwidth.classic(aiRequestsPerHour, 
                Refill.intervally(aiRequestsPerHour, Duration.ofHours(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    public Bucket createLoginBucket() {
        Bandwidth limit = Bandwidth.classic(loginAttemptsPerMinute, 
                Refill.intervally(loginAttemptsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
