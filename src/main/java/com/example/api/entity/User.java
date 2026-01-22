package com.example.api.entity;

import com.example.api.converter.UuidToBytesConverter;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class User extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 외부 노출/연동용 식별자 */
    @Column( nullable = false, unique = true)
    @Convert(converter = UuidToBytesConverter.class)
    private UUID publicId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @PrePersist
    private void prePersist() {
        if (publicId == null) {
            publicId = UuidCreator.getTimeOrderedEpoch();   // UUIDv7 계열(시간 정렬)
        }
    }

    @Builder
    public User(String username, String password, String name, String email) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
    }

    public static User create(String username, String password, String name, String email) {
        return User.builder()
                .username(username)
                .password(password)
                .name(name)
                .email(email)
                .build();
    }
}
