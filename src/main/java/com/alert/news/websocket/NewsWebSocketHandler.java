package com.alert.news.websocket;

import com.alert.news.dto.AuthRequestDto;
import com.alert.news.dto.AuthResponseDto;
import com.alert.news.service.CustomerService;
import com.alert.news.service.WebSocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * WebSocket 메시지 핸들러
 * 
 * 클라이언트로부터 받은 WebSocket 메시지를 처리하는 컨트롤러입니다.
 * 고객사 인증, 연결 관리 등의 메시지를 처리합니다.
 */
@Controller
public class NewsWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(NewsWebSocketHandler.class);

    private final CustomerService customerService;
    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;

    @Autowired
    public NewsWebSocketHandler(CustomerService customerService, 
                               WebSocketService webSocketService,
                               ObjectMapper objectMapper) {
        this.customerService = customerService;
        this.webSocketService = webSocketService;
        this.objectMapper = objectMapper;
    }

    /**
     * 고객사 인증 요청 처리
     * 
     * 클라이언트가 WebSocket 연결 시 고객사 인증을 요청합니다.
     * 인증 성공 시 연결을 등록하고, 실패 시 오류 메시지를 반환합니다.
     */
    @MessageMapping("/auth")
    @SendToUser("/queue/auth")
    public AuthResponseDto handleAuthentication(@Payload AuthRequestDto authRequest,
                                             SimpMessageHeaderAccessor headerAccessor) {
        try {
            String connectionId = headerAccessor.getSessionId();
            String customerId = authRequest.getCustomerId();
            String token = authRequest.getToken();

            logger.info("고객사 인증 요청: 고객사 ID: {}, 연결 ID: {}", customerId, connectionId);

            // 고객사 인증
            boolean isAuthenticated = customerService.authenticateCustomer(customerId, token);
            
            if (isAuthenticated) {
                // 인증 성공 시 연결 등록
                boolean isConnected = customerService.connectCustomer(customerId, connectionId);
                if (isConnected) {
                    // WebSocket 서비스에 연결 등록
                    webSocketService.registerConnection(connectionId, customerId);
                    
                    // 고객사 정보 조회
                    var customer = customerService.getCustomerById(customerId);
                    
                    AuthResponseDto response = new AuthResponseDto(
                        true, 
                        "인증 성공", 
                        customerId, 
                        customer.getName(),
                        customer.getTokenExpiresAt()
                    );
                    
                    logger.info("고객사 인증 성공: 고객사 ID: {}, 연결 ID: {}", customerId, connectionId);
                    return response;
                } else {
                    logger.warn("고객사 연결 실패: 고객사 ID: {}, 연결 ID: {}", customerId, connectionId);
                    return new AuthResponseDto(false, "연결 설정에 실패했습니다.");
                }
            } else {
                logger.warn("고객사 인증 실패: 고객사 ID: {}, 연결 ID: {}", customerId, connectionId);
                return new AuthResponseDto(false, "인증에 실패했습니다. 고객사 ID와 토큰을 확인해주세요.");
            }
        } catch (Exception e) {
            logger.error("고객사 인증 처리 중 오류 발생", e);
            return new AuthResponseDto(false, "인증 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 연결 해제 처리
     * 
     * 클라이언트가 연결을 해제할 때 호출됩니다.
     * 고객사 연결 상태를 업데이트하고 WebSocket 서비스에서 연결을 제거합니다.
     */
    @MessageMapping("/disconnect")
    public void handleDisconnect(SimpMessageHeaderAccessor headerAccessor) {
        try {
            String connectionId = headerAccessor.getSessionId();
            String customerId = webSocketService.getCustomerIdByConnection(connectionId);
            
            if (customerId != null) {
                logger.info("고객사 연결 해제 요청: 고객사 ID: {}, 연결 ID: {}", customerId, connectionId);
                
                // 고객사 연결 해제
                customerService.disconnectCustomer(connectionId);
                
                // WebSocket 서비스에서 연결 제거
                webSocketService.unregisterConnection(connectionId);
                
                logger.info("고객사 연결 해제 완료: 고객사 ID: {}, 연결 ID: {}", customerId, connectionId);
            } else {
                logger.warn("연결 ID에 해당하는 고객사가 없습니다: {}", connectionId);
            }
        } catch (Exception e) {
            logger.error("연결 해제 처리 중 오류 발생", e);
        }
    }

    /**
     * 연결 상태 확인
     * 
     * 클라이언트가 현재 연결 상태를 확인할 때 사용합니다.
     */
    @MessageMapping("/status")
    @SendToUser("/queue/status")
    public ConnectionStatus handleStatusRequest(SimpMessageHeaderAccessor headerAccessor) {
        try {
            String connectionId = headerAccessor.getSessionId();
            String customerId = webSocketService.getCustomerIdByConnection(connectionId);
            
            if (customerId != null) {
                var customer = customerService.getCustomerById(customerId);
                if (customer != null) {
                    return new ConnectionStatus(
                        customerId,
                        customer.getName(),
                        true,
                        customer.getTokenExpiresAt()
                    );
                }
            }
            
            return new ConnectionStatus(null, null, false, null);
        } catch (Exception e) {
            logger.error("연결 상태 확인 중 오류 발생", e);
            return new ConnectionStatus(null, null, false, null);
        }
    }

    /**
     * 연결 상태 정보를 담는 내부 클래스
     */
    public static class ConnectionStatus {
        private final String customerId;
        private final String customerName;
        private final boolean isConnected;
        private final LocalDateTime tokenExpiresAt;

        public ConnectionStatus(String customerId, String customerName, 
                              boolean isConnected, LocalDateTime tokenExpiresAt) {
            this.customerId = customerId;
            this.customerName = customerName;
            this.isConnected = isConnected;
            this.tokenExpiresAt = tokenExpiresAt;
        }

        // Getter
        public String getCustomerId() { return customerId; }
        public String getCustomerName() { return customerName; }
        public boolean isConnected() { return isConnected; }
        public LocalDateTime getTokenExpiresAt() { return tokenExpiresAt; }

        @Override
        public String toString() {
            return "ConnectionStatus{" +
                    "customerId='" + customerId + '\'' +
                    ", customerName='" + customerName + '\'' +
                    ", isConnected=" + isConnected +
                    ", tokenExpiresAt=" + tokenExpiresAt +
                    '}';
        }
    }
}
