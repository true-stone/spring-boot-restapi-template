package com.example.api.dto;

import lombok.Getter;

@Getter
public enum UserRole {
    /** 일반 회원 */
    USER,
    /** 관리자 */
    ADMIN;

    public String authority() {
        return "ROLE_" + name();
    }
}
