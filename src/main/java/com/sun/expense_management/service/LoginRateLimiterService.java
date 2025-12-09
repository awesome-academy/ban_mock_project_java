package com.sun.expense_management.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Service to handle rate limiting for login attempts
 * Uses Bucket4j token bucket algorithm with Caffeine cache
 */
@Service
public class LoginRateLimiterService {

    @Value("${app.security.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.security.login.lockout-duration-minutes:15}")
    private int lockoutDurationMinutes;

    // Cache to store buckets per IP address
    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(10000)
            .build();

    /**
     * Check if the IP address can attempt login
     * @param ipAddress Client IP address
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean allowLogin(String ipAddress) {
        Bucket bucket = resolveBucket(ipAddress);
        return bucket.tryConsume(1);
    }

    /**
     * Get remaining attempts for an IP address
     * @param ipAddress Client IP address
     * @return Number of remaining login attempts
     */
    public long getRemainingAttempts(String ipAddress) {
        Bucket bucket = resolveBucket(ipAddress);
        return bucket.getAvailableTokens();
    }

    /**
     * Reset rate limit for an IP address (e.g., after successful login)
     * @param ipAddress Client IP address
     */
    public void resetLimit(String ipAddress) {
        cache.invalidate(ipAddress);
    }

    /**
     * Get or create a bucket for the given IP address
     */
    private Bucket resolveBucket(String ipAddress) {
        return cache.get(ipAddress, key -> createNewBucket());
    }

    /**
     * Create a new bucket with configured limits
     * Uses token bucket algorithm:
     * - Initial capacity: maxAttempts tokens
     * - Refill: maxAttempts tokens every lockoutDuration
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(maxAttempts)
                .refillIntervally(maxAttempts, Duration.ofMinutes(lockoutDurationMinutes))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public int getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }
}
