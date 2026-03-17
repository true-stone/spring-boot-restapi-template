package com.example.api.service;

import com.example.api.dto.*;
import com.example.api.entity.User;
import com.example.api.exception.BusinessException;
import com.example.api.exception.ErrorCode;
import com.example.api.repository.UserRepository;
import com.example.api.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원 가입
     */
    @Transactional
    public void signUp(UserCreateRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodePassword = passwordEncoder.encode(request.password());

        User user = User.create(
                request.username(),
                encodePassword,
                request.name(),
                request.email()
        );
        userRepository.save(user);
    }

    /**
     * 프로필 조회
     */
    public UserResponse readMyProfile(UUID publicId) {
        return userRepository.findWithRolesByPublicId(publicId)
                .map(UserResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 프로필 수정
     */
    @Transactional
    public UserResponse updateMyProfile(UserUpdateRequest request) {
        User user = userRepository.findWithRolesByPublicId(SecurityUtils.getUserPublicId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.getEmail().equals(request.email()) &&
                userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        user.update(request.name(), request.email());
        return UserResponse.from(user);
    }

    /**
     * 사용자 목록 조회
     */
    public PageResponse<UserResponse> readUsers(UserPageParam pageParam) {
        return PageResponse.from(userRepository.findAll(pageParam.toPageable()).map(UserResponse::from));
    }
}
