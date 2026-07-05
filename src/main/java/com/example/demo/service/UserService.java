package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.dto.user.*;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public SignupResponse signup(SignupRequest request) {

        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            throw new ApiException(HttpStatus.CONFLICT, "중복된 이메일입니다.");
        });

        userRepository.findByNickname(request.getNickname()).ifPresent(user -> {
            throw new ApiException(HttpStatus.CONFLICT, "중복된 닉네임입니다.");
        });

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = userRepository.save(
                new User(
                        request.getEmail(),
                        encodedPassword,
                        request.getNickname(),
                        request.getProfileImage()
                )
        );

        return new SignupResponse(user.getUserId());
    }

    public SigninResponse signin(SigninRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "이메일 또는 비밀번호가 일치하지 않습니다."
                ));

        if (user.isDeleted()) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "이메일 또는 비밀번호가 일치하지 않습니다."
            );
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "이메일 또는 비밀번호가 일치하지 않습니다."
            );
        }

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getUserId(),
                user.getEmail()
        );

        return new SigninResponse(
                user.getUserId(),
                accessToken,
                "Bearer"
        );
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo(Long userId) {
        User user = findLoginUser(userId);

        return new UserInfoResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImage()
        );
    }

    public UpdateUserInfoResponse updateUserInfo(
            Long userId,
            UpdateUserInfoRequest request
    ) {
        User user = findLoginUser(userId);

        userRepository.findByNickname(request.getNickname()).ifPresent(foundUser -> {
            if (!foundUser.getUserId().equals(userId)) {
                throw new ApiException(HttpStatus.CONFLICT, "중복된 닉네임입니다.");
            }
        });

        user.updateUserInfo(
                request.getNickname(),
                request.getProfileImage()
        );

        return new UpdateUserInfoResponse(
                user.getNickname(),
                user.getProfileImage()
        );
    }

    public void updatePassword(
            Long userId,
            UpdatePasswordRequest request
    ) {
        User user = findLoginUser(userId);

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());

        user.updatePassword(encodedPassword);
    }

    public void deleteUser(Long userId) {
        User user = findLoginUser(userId);

        user.withdraw();
    }

    private User findLoginUser(Long userId) {
        if (userId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "로그인이 필요합니다."
                ));

        if (user.isDeleted()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        return user;
    }
}