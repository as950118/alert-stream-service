package com.alert.news.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 고객사 엔티티
 * 
 * 뉴스 구독 고객사의 정보와 WebSocket 연결 상태를 관리하는 엔티티입니다.
 * 토큰 기반 인증을 통해 연결을 제한하고, 고객사별 개별 연결을 관리합니다.
 */
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @Column(name = "id", length = 100)
    @NotBlank(message = "고객사 ID는 필수입니다")
    private String id;

    @Column(name = "name", nullable = false, length = 200)
    @NotBlank(message = "고객사명은 필수입니다")
    private String name;

    @Column(name = "token", nullable = false, length = 500, unique = true)
    @NotBlank(message = "인증 토큰은 필수입니다")
    private String token;

    @Column(name = "connection_id", length = 100)
    private String connectionId;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "token_expires_at", nullable = false)
    @NotNull(message = "토큰 만료일시는 필수입니다")
    private LocalDateTime tokenExpiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 기본 생성자
    public Customer() {
        this.createdAt = LocalDateTime.now();
    }

    // 생성자
    public Customer(String id, String name, String token, LocalDateTime tokenExpiresAt) {
        this();
        this.id = id;
        this.name = name;
        this.token = token;
        this.tokenExpiresAt = tokenExpiresAt;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // 비즈니스 메서드
    public boolean isTokenExpired() {
        return LocalDateTime.now().isAfter(tokenExpiresAt);
    }

    public boolean isConnected() {
        return connectionId != null && !connectionId.isEmpty();
    }

    public void connect(String connectionId) {
        this.connectionId = connectionId;
        this.updatedAt = LocalDateTime.now();
    }

    public void disconnect() {
        this.connectionId = null;
        this.updatedAt = LocalDateTime.now();
    }

    // JPA 생명주기 콜백
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", token='" + token + '\'' +
                ", connectionId='" + connectionId + '\'' +
                ", isActive=" + isActive +
                ", tokenExpiresAt=" + tokenExpiresAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
