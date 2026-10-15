package com.uth.newsmanagement.common.constant;

public final class AppConstants {

    private AppConstants() {
        // Prevent instantiation
    }

    // Pagination Constants
    public static final String DEFAULT_PAGE_NUMBER = "1";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "desc";

    // Security & Auth Header
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    // API Routes
    public static final String API_V1_PREFIX = "/api/v1";
    public static final String PUBLIC_PATH_PATTERN = "/api/v1/public/**";
    public static final String AUTH_PATH_PATTERN = "/api/v1/auth/**";
}
