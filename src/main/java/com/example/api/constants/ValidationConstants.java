package com.example.api.constants;

public final class ValidationConstants {

    private ValidationConstants() {
    }

    // Username 관련 상수
    public static final String USERNAME_PATTERN = "^[a-z0-9_.]+$";
    public static final int USERNAME_MIN_LENGTH = 4;
    public static final int USERNAME_MAX_LENGTH = 20;

    // Password 관련 상수
    public static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,72}$";
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 72;
}
