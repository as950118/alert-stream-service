package com.alert.news.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정 클래스
 * 
 * STOMP 메시징을 위한 WebSocket 설정을 담당합니다.
 * 클라이언트와의 실시간 통신을 위한 엔드포인트와 메시지 브로커를 구성합니다.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결을 위한 엔드포인트 등록
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // CORS 설정 (개발 환경용)
                .withSockJS();  // SockJS 지원 (WebSocket을 지원하지 않는 브라우저 대응)
        
        // SockJS 없이 직접 WebSocket 연결
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트로부터 서버로 메시지를 보낼 때 사용할 prefix
        registry.setApplicationDestinationPrefixes("/app");
        
        // 서버에서 클라이언트로 메시지를 보낼 때 사용할 prefix
        // /topic: 모든 구독자에게 브로드캐스트
        // /queue: 특정 사용자에게만 전송
        registry.enableSimpleBroker("/topic", "/queue");
        
        // 사용자별 메시지 전송을 위한 prefix 설정
        registry.setUserDestinationPrefix("/user");
    }
}
