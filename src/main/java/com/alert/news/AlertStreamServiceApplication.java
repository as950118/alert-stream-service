package com.alert.news;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Alert Stream Service 메인 애플리케이션 클래스
 * 
 * 실시간 뉴스 전송 시스템을 위한 Spring Boot 애플리케이션입니다.
 * WebSocket, 메시지 큐, JPA 등을 활용하여 실시간 뉴스 전송 서비스를 제공합니다.
 */
@SpringBootApplication
@EnableAsync
public class AlertStreamServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlertStreamServiceApplication.class, args);
    }
}
