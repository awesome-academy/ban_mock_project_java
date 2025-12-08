package com.sun.expense_management.exception;

/**
 * Exception thrown when rate limit is exceeded
 */
public class RateLimitExceededException extends RuntimeException {

    private final int lockoutDurationMinutes;

    public RateLimitExceededException(String message, int lockoutDurationMinutes) {
        super(message);
        this.lockoutDurationMinutes = lockoutDurationMinutes;
    }

    public int getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }
}
