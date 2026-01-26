package com.example.api.security;

import com.example.api.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

public record UserDetailsImpl(User user) implements UserDetails {

    public UUID publicId() {
        return user.getPublicId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.authority()))
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * 사용자의 계정이 만료되었는지 여부를 나타냅니다. 만료된 계정은 인증할 수 없습니다.
     *
     * @return 계정이 유효하면 {@code true}, 만료되었으면 {@code false}
     */
    @Override
    public boolean isAccountNonExpired() {
        return true; // Or logic from User entity
    }

    /**
     * 사용자가 잠겨 있는지 여부를 나타냅니다. 잠긴 사용자는 인증할 수 없습니다.
     *
     * @return 사용자가 잠겨 있지 않으면 {@code true}, 잠겨 있으면 {@code false}
     */
    @Override
    public boolean isAccountNonLocked() {
        return true; // Or logic from User entity
    }

    /**
     * 사용자의 자격 증명(비밀번호)이 만료되었는지 여부를 나타냅니다. 만료된 자격 증명은 인증할 수 없습니다.
     *
     * @return 자격 증명이 유효하면 {@code true}, 만료되었으면 {@code false}
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Or logic from User entity
    }

    /**
     * 사용자가 활성화되었는지 비활성화되었는지 여부를 나타냅니다. 비활성화된 사용자는 인증할 수 없습니다.
     *
     * @return 사용자가 활성화되어 있으면 {@code true}, 비활성화되어 있으면 {@code false}
     */
    @Override
    public boolean isEnabled() {
        return true; // Or logic from User entity
    }
}
