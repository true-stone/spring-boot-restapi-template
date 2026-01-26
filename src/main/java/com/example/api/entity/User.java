package com.example.api.entity;

import com.example.api.converter.UuidToBytesConverter;
import com.example.api.dto.UserRole;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseTime {

    /**
     * 내부 시스템용 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 외부 노출/연동용 식별자(UUIDv7)
     */
    @Column(nullable = false, unique = true)
    @Convert(converter = UuidToBytesConverter.class)
    private UUID publicId;

    /**
     * 로그인용 아이디
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * 암호화된 비밀번호
     */
    @Column(nullable = false)
    private String password;

    /**
     * 사용자의 실명 (성명)
     */
    @Column(nullable = false)
    private String name;

    /**
     * 이메일 주소
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * 사용자의 권한(역할)
     * <pre>
     * - {@code ElementCollection}은 별도 테이블(user_roles)에 저장됨
     * - {@code EnumType.STRING}으로 저장해야 enum 순서 변경에 안전
     * </pre>
     */
    @Getter(AccessLevel.NONE)
    @ElementCollection(fetch = FetchType.EAGER) // 권한은 보통 즉시 로딩
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_user_roles_user_id_role", columnNames = {"user_id", "role"})
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private Set<UserRole> roles = new HashSet<>();

    /**
     * 권한 목록 조회용(읽기 전용)
     * <pre>
     * - 내부 컬렉션의 가변 참조를 그대로 노출하면 외부에서 권한이 임의로 변경될 수 있으므로 수정 불가능한 뷰를 반환한다.
     * </pre>
     */
    public Set<UserRole> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    @PrePersist
    private void prePersist() {
        if (publicId == null) {
            publicId = UuidCreator.getTimeOrderedEpoch();   // UUIDv7 계열(시간 정렬)
        }

        // 혹시라도 권한이 비어 저장되는 것을 방지
        if (roles.isEmpty()) {
            roles.add(UserRole.USER);
        }
    }

    @Builder(access = AccessLevel.PRIVATE)
    public User(String username, String password, String name, String email) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
    }

    /**
     * 도메인 팩토리 메서드 - 기본 권한(USER) 부여
     */
    public static User create(String username, String password, String name, String email) {
        User user = User.builder()
                .username(username)
                .password(password)
                .name(name)
                .email(email)
                .build();

        user.addRole(UserRole.USER);
        return user;
    }

    /**
     * 권한 변경은 도메인 메서드로만 수행하도록 제공
     */
    public void addRole(UserRole role) {
        if (role == null) return;
        this.roles.add(role);
    }

    public void removeRole(UserRole role) {
        if (role == null) return;
        this.roles.remove(role);
    }

    public boolean hasRole(UserRole role) {
        return role != null && this.roles.contains(role);
    }
}
