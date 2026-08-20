package com.waimai.platform.security;

public interface LoginRateLimiter {

    void checkAllowed(String username, String clientAddress);

    void recordFailure(String username, String clientAddress);

    void clear(String username, String clientAddress);
}
