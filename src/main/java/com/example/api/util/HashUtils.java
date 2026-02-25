package com.example.api.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 문자열/바이트 배열을 대상으로 해시 값을 생성하기 위한 유틸리티 클래스
 */
public final class HashUtils {

    private HashUtils() {
    }

    /**
     * 입력 문자열을 UTF-8 바이트로 변환한 뒤 SHA-256 해시를 계산하고, 결과를 소문자 16진수 문자열(hex)로 반환한다.
     *
     * @param value 해시를 계산할 원문 문자열 (null 가능)
     * @return SHA-256 hex 문자열. 입력이 null이거나 내부 예외가 발생하면 "na"를 반환한다.
     */
    public static String sha256Hex(String value) {
        return sha256Hex(value, StandardCharsets.UTF_8);
    }

    /**
     * 입력 문자열을 지정한 charset으로 바이트 변환한 뒤 SHA-256 해시를 계산하고, 결과를 소문자 16진수 문자열(hex)로 반환한다.
     *
     * @param value 해시를 계산할 원문 문자열 (null 가능)
     * @param charset 문자열을 바이트로 변환할 문자셋 (null 불가 권장)
     * @return SHA-256 hex 문자열. 실패 시 "na".
     */
    public static String sha256Hex(String value, Charset charset) {
        if (value == null) return "na";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(charset));
            return toHex(digest);
        } catch (Exception e) {
            return "na";
        }
    }

    private static String toHex(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        final char[] digits = "0123456789abcdef".toCharArray();

        int i = 0;
        for (byte b : bytes) {
            int v = b & 0xFF;
            hex[i++] = digits[v >>> 4];
            hex[i++] = digits[v & 0x0F];
        }
        return new String(hex);
    }

}
