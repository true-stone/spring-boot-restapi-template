package com.example.api.service;

import com.example.api.dto.PageResponse;
import com.example.api.dto.UserCreateRequest;
import com.example.api.dto.UserPageParam;
import com.example.api.dto.UserResponse;
import com.example.api.entity.User;
import com.example.api.exception.BusinessException;
import com.example.api.exception.ErrorCode;
import com.example.api.repository.UserRepository;
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
                request.name(),
                request.email()
        );
        userRepository.save(user);
    }

    public PageResponse<UserResponse> readUsers(UserPageParam pageParam) {
        return PageResponse.from(userRepository.findAll(pageParam.toPageable()).map(UserResponse::from));
    }
}
