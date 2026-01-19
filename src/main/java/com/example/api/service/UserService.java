package com.example.api.service;

import com.example.api.dto.UserCreateRequest;
import com.example.api.dto.UserResponse;
import com.example.api.entity.User;
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
            throw new IllegalArgumentException("이미 존재하는 회원입니다.");
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
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        return UserResponse.from(user);
    }
}
