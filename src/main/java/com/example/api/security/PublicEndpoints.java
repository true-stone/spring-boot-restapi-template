package com.example.api.security;

public final class PublicEndpoints {

    public PublicEndpoints() {
    }

    public static final String[] COMMON = {
            "/common/**"
    };

    public static final String[] AUTH = {
            "/api/v1/auth/**"
    };

    public static final String[] USER = {
            "/api/v1/users"
    };

}
