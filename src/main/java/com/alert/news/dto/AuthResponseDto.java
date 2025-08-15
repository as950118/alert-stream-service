package com.alert.news.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 고객사 인증 응답 DTO
 * 
 * 고객사 인증 결과를 전송하는 응답 정보입니다.
 */
public class AuthResponseDto {

    private boolean success;
    private String message;
    private String customerId;
    private String customerName;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime tokenExpiresAt;

    // 기본 생성자
    public AuthResponseDto() {}

    // 성공 응답 생성자
    public AuthResponseDto(boolean success, String message, String customerId, 
                          String customerName, LocalDateTime tokenExpiresAt) {
        this.success = success;
        this.message = message;
        this.customerId = customerId;
        this.customerName = customerName;
        this.tokenExpiresAt = tokenExpiresAt;
    }

    // 실패 응답 생성자
    public AuthResponseDto(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getter & Setter
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    @Override
    public String toString() {
        return "AuthResponseDto{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", customerId='" + customerId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", tokenExpiresAt=" + tokenExpiresAt +
                '}';
    }
}
