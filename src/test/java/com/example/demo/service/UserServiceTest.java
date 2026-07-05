package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.dto.user.*;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입에 성공한다")
    void signupSuccess() {
        // given
        SignupRequest request = createSignupRequest(
                "test@example.com",
                "Password123!",
                "testUser",
                "/uploads/images/profile.png"
        );

        // when
        SignupResponse response = userService.signup(request);

        // then
        assertThat(response.getUserId()).isNotNull();

        User savedUser = userRepository.findByEmail("test@example.com")
                .orElseThrow();

        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("testUser");
        assertThat(savedUser.getProfileImage()).isEqualTo("/uploads/images/profile.png");

        assertThat(savedUser.getPassword()).isNotEqualTo("Password123!");
        assertThat(passwordEncoder.matches("Password123!", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("중복된 이메일이면 회원가입에 실패한다")
    void signupFailByDuplicatedEmail() {
        // given
        SignupRequest firstRequest = createSignupRequest(
                "test@example.com",
                "Password123!",
                "testUser1",
                "/uploads/images/profile1.png"
        );

        userService.signup(firstRequest);

        SignupRequest duplicatedEmailRequest = createSignupRequest(
                "test@example.com",
                "Password123!",
                "testUser2",
                "/uploads/images/profile2.png"
        );

        // when & then
        assertThatThrownBy(() -> userService.signup(duplicatedEmailRequest))
                .isInstanceOf(ApiException.class)
                .hasMessage("중복된 이메일입니다.");
    }

    @Test
    @DisplayName("중복된 닉네임이면 회원가입에 실패한다")
    void signupFailByDuplicatedNickname() {
        // given
        SignupRequest firstRequest = createSignupRequest(
                "test1@example.com",
                "Password123!",
                "testUser",
                "/uploads/images/profile1.png"
        );

        userService.signup(firstRequest);

        SignupRequest duplicatedNicknameRequest = createSignupRequest(
                "test2@example.com",
                "Password123!",
                "testUser",
                "/uploads/images/profile2.png"
        );

        // when & then
        assertThatThrownBy(() -> userService.signup(duplicatedNicknameRequest))
                .isInstanceOf(ApiException.class)
                .hasMessage("중복된 닉네임입니다.");
    }

    @Test
    @DisplayName("로그인에 성공하면 JWT 토큰을 발급한다")
    void signinSuccess() {
        // given
        SignupRequest signupRequest = createSignupRequest(
                "login@example.com",
                "Password123!",
                "loginUser",
                "/uploads/images/profile.png"
        );

        userService.signup(signupRequest);

        SigninRequest signinRequest = createSigninRequest(
                "login@example.com",
                "Password123!"
        );

        // when
        SigninResponse response = userService.signin(signinRequest);

        // then
        assertThat(response.getUserId()).isNotNull();
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패한다")
    void signinFailByWrongPassword() {
        // given
        SignupRequest signupRequest = createSignupRequest(
                "wrong-password@example.com",
                "Password123!",
                "wrongPasswordUser",
                "/uploads/images/profile.png"
        );

        userService.signup(signupRequest);

        SigninRequest signinRequest = createSigninRequest(
                "wrong-password@example.com",
                "WrongPassword123!"
        );

        // when & then
        assertThatThrownBy(() -> userService.signin(signinRequest))
                .isInstanceOf(ApiException.class)
                .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 이메일이면 로그인에 실패한다")
    void signinFailByNotFoundEmail() {
        // given
        SigninRequest signinRequest = createSigninRequest(
                "notfound@example.com",
                "Password123!"
        );

        // when & then
        assertThatThrownBy(() -> userService.signin(signinRequest))
                .isInstanceOf(ApiException.class)
                .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("내 정보 조회에 성공한다")
    void getMyInfoSuccess() {
        // given
        SignupRequest signupRequest = createSignupRequest(
                "myinfo@example.com",
                "Password123!",
                "myInfoUser",
                "/uploads/images/profile.png"
        );

        SignupResponse signupResponse = userService.signup(signupRequest);

        // when
        UserInfoResponse response = userService.getMyInfo(signupResponse.getUserId());

        // then
        assertThat(response.getUserId()).isEqualTo(signupResponse.getUserId());
        assertThat(response.getEmail()).isEqualTo("myinfo@example.com");
        assertThat(response.getNickname()).isEqualTo("myInfoUser");
        assertThat(response.getProfileImage()).isEqualTo("/uploads/images/profile.png");
    }

    @Test
    @DisplayName("회원정보 수정에 성공한다")
    void updateUserInfoSuccess() {
        // given
        SignupRequest signupRequest = createSignupRequest(
                "update@example.com",
                "Password123!",
                "beforeNickname",
                "/uploads/images/before.png"
        );

        SignupResponse signupResponse = userService.signup(signupRequest);

        UpdateUserInfoRequest updateRequest = createUpdateUserInfoRequest(
                "afterNickname",
                "/uploads/images/after.png"
        );

        // when
        UpdateUserInfoResponse response = userService.updateUserInfo(
                signupResponse.getUserId(),
                updateRequest
        );

        // then
        assertThat(response.getNickname()).isEqualTo("afterNickname");
        assertThat(response.getProfileImage()).isEqualTo("/uploads/images/after.png");

        User savedUser = userRepository.findById(signupResponse.getUserId())
                .orElseThrow();

        assertThat(savedUser.getNickname()).isEqualTo("afterNickname");
        assertThat(savedUser.getProfileImage()).isEqualTo("/uploads/images/after.png");
    }

    @Test
    @DisplayName("중복된 닉네임으로 회원정보 수정하면 실패한다")
    void updateUserInfoFailByDuplicatedNickname() {
        // given
        SignupRequest firstRequest = createSignupRequest(
                "first@example.com",
                "Password123!",
                "firstUser",
                "/uploads/images/first.png"
        );

        userService.signup(firstRequest);

        SignupRequest secondRequest = createSignupRequest(
                "second@example.com",
                "Password123!",
                "secondUser",
                "/uploads/images/second.png"
        );

        SignupResponse secondResponse = userService.signup(secondRequest);

        UpdateUserInfoRequest updateRequest = createUpdateUserInfoRequest(
                "firstUser",
                "/uploads/images/update.png"
        );

        // when & then
        assertThatThrownBy(() -> userService.updateUserInfo(
                secondResponse.getUserId(),
                updateRequest
        ))
                .isInstanceOf(ApiException.class)
                .hasMessage("중복된 닉네임입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 정보 조회에 실패한다")
    void getMyInfoFailByNotFoundUser() {
        // given
        Long notFoundUserId = 999L;

        // when & then
        assertThatThrownBy(() -> userService.getMyInfo(notFoundUserId))
                .isInstanceOf(ApiException.class)
                .hasMessage("로그인이 필요합니다.");
    }

    @Test
    @DisplayName("비밀번호 수정에 성공한다")
    void updatePasswordSuccess() {
        // given
        SignupRequest signupRequest = createSignupRequest(
                "password@example.com",
                "Password123!",
                "passwordUser",
                "/uploads/images/profile.png"
        );

        SignupResponse signupResponse = userService.signup(signupRequest);

        UpdatePasswordRequest updateRequest = createUpdatePasswordRequest("NewPassword123!");

        // when
        userService.updatePassword(signupResponse.getUserId(), updateRequest);

        // then
        User savedUser = userRepository.findById(signupResponse.getUserId())
                .orElseThrow();

        assertThat(savedUser.getPassword()).isNotEqualTo("NewPassword123!");
        assertThat(passwordEncoder.matches("NewPassword123!", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("비밀번호 수정 후 새 비밀번호로 로그인에 성공한다")
    void signinSuccessAfterPasswordUpdate() {
        // given
        SignupRequest signupRequest = createSignupRequest(
                "new-password-login@example.com",
                "Password123!",
                "newPasswordLoginUser",
                "/uploads/images/profile.png"
        );

        SignupResponse signupResponse = userService.signup(signupRequest);

        UpdatePasswordRequest updateRequest = createUpdatePasswordRequest("NewPassword123!");

        userService.updatePassword(signupResponse.getUserId(), updateRequest);

        SigninRequest signinRequest = createSigninRequest(
                "new-password-login@example.com",
                "NewPassword123!"
        );

        // when
        SigninResponse response = userService.signin(signinRequest);

        // then
        assertThat(response.getUserId()).isEqualTo(signupResponse.getUserId());
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("비밀번호 수정 후 기존 비밀번호로 로그인하면 실패한다")
    void signinFailWithOldPasswordAfterPasswordUpdate() {
        // given
        SignupRequest signupRequest = createSignupRequest(
                "old-password-login@example.com",
                "Password123!",
                "oldPasswordLoginUser",
                "/uploads/images/profile.png"
        );

        SignupResponse signupResponse = userService.signup(signupRequest);

        UpdatePasswordRequest updateRequest = createUpdatePasswordRequest("NewPassword123!");

        userService.updatePassword(signupResponse.getUserId(), updateRequest);

        SigninRequest signinRequest = createSigninRequest(
                "old-password-login@example.com",
                "Password123!"
        );

        // when & then
        assertThatThrownBy(() -> userService.signin(signinRequest))
                .isInstanceOf(ApiException.class)
                .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("회원탈퇴에 성공한다")
    void deleteUserSuccess() {
        // given
        SignupRequest signupRequest = createSignupRequest(
                "delete@example.com",
                "Password123!",
                "deleteUser",
                "/uploads/images/profile.png"
        );

        SignupResponse signupResponse = userService.signup(signupRequest);

        // when
        userService.deleteUser(signupResponse.getUserId());

        // then
        User savedUser = userRepository.findById(signupResponse.getUserId())
                .orElseThrow();

        assertThat(savedUser.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("회원탈퇴 후 로그인에 실패한다")
    void signinFailAfterDeleteUser() {
        // given
        SignupRequest signupRequest = createSignupRequest(
                "deleted-login@example.com",
                "Password123!",
                "deletedLoginUser",
                "/uploads/images/profile.png"
        );

        SignupResponse signupResponse = userService.signup(signupRequest);

        userService.deleteUser(signupResponse.getUserId());

        SigninRequest signinRequest = createSigninRequest(
                "deleted-login@example.com",
                "Password123!"
        );

        // when & then
        assertThatThrownBy(() -> userService.signin(signinRequest))
                .isInstanceOf(ApiException.class)
                .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("회원탈퇴 후 내 정보 조회에 실패한다")
    void getMyInfoFailAfterDeleteUser() {
        // given
        SignupRequest signupRequest = createSignupRequest(
                "deleted-myinfo@example.com",
                "Password123!",
                "deletedMyInfoUser",
                "/uploads/images/profile.png"
        );

        SignupResponse signupResponse = userService.signup(signupRequest);

        userService.deleteUser(signupResponse.getUserId());

        // when & then
        assertThatThrownBy(() -> userService.getMyInfo(signupResponse.getUserId()))
                .isInstanceOf(ApiException.class)
                .hasMessage("로그인이 필요합니다.");
    }

    private SignupRequest createSignupRequest(
            String email,
            String password,
            String nickname,
            String profileImage
    ) {
        SignupRequest request = new SignupRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setNickname(nickname);
        request.setProfileImage(profileImage);
        return request;
    }

    private SigninRequest createSigninRequest(String email, String password) {
        SigninRequest request = new SigninRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private UpdateUserInfoRequest createUpdateUserInfoRequest(
            String nickname,
            String profileImage
    ) {
        UpdateUserInfoRequest request = new UpdateUserInfoRequest();
        request.setNickname(nickname);
        request.setProfileImage(profileImage);
        return request;
    }

    private UpdatePasswordRequest createUpdatePasswordRequest(String newPassword) {
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setNewPassword(newPassword);
        return request;
    }
}