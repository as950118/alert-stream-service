package com.alert.news.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 뉴스 데이터 전송 객체 (DTO)
 * 
 * WebSocket을 통해 클라이언트에게 전송되는 뉴스 정보를 담는 DTO입니다.
 * JSON 직렬화/역직렬화를 위한 Jackson 어노테이션을 포함합니다.
 */
public class NewsDto {

    @NotBlank(message = "뉴스 ID는 필수입니다")
    private String id;

    @NotBlank(message = "뉴스 제목은 필수입니다")
    private String title;

    @NotBlank(message = "뉴스 내용은 필수입니다")
    private String content;

    @NotNull(message = "발행일시는 필수입니다")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime publishedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // 기본 생성자
    public NewsDto() {}

    // 생성자
    public NewsDto(String id, String title, String content, LocalDateTime publishedAt) {
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

    @Override
    public String toString() {
        return "NewsDto{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", publishedAt=" + publishedAt +
                ", createdAt=" + createdAt +
                '}';
    }
}
