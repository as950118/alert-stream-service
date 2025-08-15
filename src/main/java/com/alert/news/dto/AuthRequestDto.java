package com.alert.news.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 고객사 인증 요청 DTO
 * 
 * 고객사가 WebSocket 연결 시 인증을 위해 전송하는 요청 정보입니다.
 */
public class AuthRequestDto {

    @NotBlank(message = "고객사 ID는 필수입니다")
    private String customerId;

    @NotBlank(message = "인증 토큰은 필수입니다")
    private String token;

    // 기본 생성자
    public AuthRequestDto() {}

    // 생성자
    public AuthRequestDto(String customerId, String token) {
        this.customerId = customerId;
        this.token = token;
    }

    // Getter & Setter
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "AuthRequestDto{" +
                "customerId='" + customerId + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
