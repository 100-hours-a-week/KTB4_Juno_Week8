package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.user.*;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        SignupResponse response = userService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입에 성공하였습니다.", response));
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<SigninResponse>> signin(
            @Valid @RequestBody SigninRequest request
    ) {
        SigninResponse response = userService.signin(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("로그인에 성공하였습니다.", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserInfoResponse response = userService.getMyInfo(userDetails.getUserId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("회원정보 조회에 성공하였습니다.", response));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UpdateUserInfoResponse>> updateUserInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateUserInfoRequest request
    ) {
        UpdateUserInfoResponse response = userService.updateUserInfo(
                userDetails.getUserId(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("회원정보 수정에 성공하였습니다.", response));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        userService.updatePassword(userDetails.getUserId(), request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("비밀번호 수정에 성공하였습니다.", null));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.deleteUser(userDetails.getUserId());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/signout")
    public ResponseEntity<Void> signout() {
        return ResponseEntity.noContent().build();
    }
}