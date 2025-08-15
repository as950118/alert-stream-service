package com.alert.news;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Alert Stream Service 애플리케이션 통합 테스트
 * 
 * Spring Boot 애플리케이션이 정상적으로 시작되는지 확인합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
class AlertStreamServiceApplicationTests {

    @Test
    void contextLoads() {
        // 애플리케이션 컨텍스트가 정상적으로 로드되는지 확인
    }
}
