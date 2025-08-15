package com.alert.news.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 번역된 뉴스 엔티티
 * 
 * AI가 번역한 뉴스 정보를 저장하는 엔티티입니다.
 * 메시지 큐를 통해 전달받은 뉴스 ID로 조회하여
 * WebSocket을 통해 실시간으로 고객사에게 전송합니다.
 */
@Entity
@Table(name = "translated_news")
public class News {

    @Id
    @Column(name = "id", length = 100)
    @NotBlank(message = "뉴스 ID는 필수입니다")
    private String id;

    @Column(name = "title", nullable = false, length = 500)
    @NotBlank(message = "뉴스 제목은 필수입니다")
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "뉴스 내용은 필수입니다")
    private String content;

    @Column(name = "published_at", nullable = false)
    @NotNull(message = "발행일시는 필수입니다")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 기본 생성자
    public News() {
        this.createdAt = LocalDateTime.now();
    }

    // 생성자
    public News(String id, String title, String content, LocalDateTime publishedAt) {
        this();
        this.id = id;
        this.title = title;
        this.content = content;
        this.publishedAt = publishedAt;
    }

    // Getter & Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
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

    // JPA 생명주기 콜백
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "News{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", publishedAt=" + publishedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
