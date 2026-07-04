package com.example.demo.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SigninResponse {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    public SigninResponse(Long userId, String accessToken, String tokenType) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.tokenType = tokenType;
    }

    public Long getUserId() {
        return userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }
}