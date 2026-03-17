package com.example.api.util;

import com.example.api.exception.BusinessException;
import com.example.api.exception.ErrorCode;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * 현재 인증된 사용자의 publicId(UUIDv7)를 반환합니다.
     * JWT subject에서 추출하며 DB 조회가 발생하지 않습니다.
     */
    public static UUID getUserPublicId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }
    }
}
