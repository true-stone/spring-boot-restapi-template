package com.example.api.repository;

import com.example.api.entity.User;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 회원 조회
     */
    @QueryHints({
            @QueryHint(name = "org.hibernate.comment", value = "공개아이디로 회원 기본 정보 조회")
    })
    Optional<User> findByPublicId(@Param("publicId") UUID publicId);

    /**
     * 회원 및 권한 조회
     */
    @QueryHints({
            @QueryHint(name = "org.hibernate.comment", value = "공개아이디로 회원 및 권한 조회")
    })
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findWithRolesByPublicId(@Param("publicId") UUID publicId);

    /**
     * 로그인용 아이디로 회원 조회
     */
    @QueryHints({
            @QueryHint(name = "org.hibernate.comment", value = "로그인용 아이디로 회원 조회")
    })
    Optional<User> findByUsername(@Param("username") String username);

    boolean existsByUsername(@Param("username") String username);

    boolean existsByEmail(@Param("email") String email);
}
