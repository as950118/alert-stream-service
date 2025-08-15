package com.alert.news.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 고객사 데이터 전송 객체 (DTO)
 * 
 * 고객사 인증 및 연결 상태 정보를 전송하는 DTO입니다.
 * 민감한 정보(토큰)는 제외하고 필요한 정보만 노출합니다.
 */
public class CustomerDto {

    @NotBlank(message = "고객사 ID는 필수입니다")
    private String id;

    @NotBlank(message = "고객사명은 필수입니다")
    private String name;

    private String connectionId;

    private boolean isActive;

    @NotNull(message = "토큰 만료일시는 필수입니다")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime tokenExpiresAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // 기본 생성자
    public CustomerDto() {}

    // 생성자
    public CustomerDto(String id, String name, String connectionId, boolean isActive, 
                      LocalDateTime tokenExpiresAt, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.connectionId = connectionId;
        this.isActive = isActive;
        this.tokenExpiresAt = tokenExpiresAt;
        this.createdAt = createdAt;
    }

    // Getter & Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CustomerDto{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", connectionId='" + connectionId + '\'' +
                ", isActive=" + isActive +
                ", tokenExpiresAt=" + tokenExpiresAt +
                ", createdAt=" + createdAt +
                '}';
    }
}
