package com.example.api.service;

import com.example.api.dto.UserCreateRequest;
import com.example.api.dto.UserResponse;
import com.example.api.entity.User;
import com.example.api.exception.BusinessException;
import com.example.api.exception.ErrorCode;
import com.example.api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
                request.email()
        );

        userRepository.save(user);
    }

    public UserResponse me(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }
}
