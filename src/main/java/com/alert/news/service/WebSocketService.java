package com.alert.news.service;

import com.alert.news.dto.NewsDto;
import com.alert.news.model.Customer;
import com.alert.news.model.News;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * WebSocket 서비스
 * 
 * WebSocket을 통한 실시간 뉴스 전송을 담당하는 서비스 클래스입니다.
 * 고객사별 개별 연결 관리 및 뉴스 브로드캐스팅 기능을 제공합니다.
 */
@Service
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final CustomerService customerService;
    private final ObjectMapper objectMapper;

    // 연결 ID와 고객사 ID의 매핑을 관리
    private final ConcurrentMap<String, String> connectionToCustomerMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> customerToConnectionMap = new ConcurrentHashMap<>();

    @Autowired
    public WebSocketService(SimpMessagingTemplate messagingTemplate, 
                           CustomerService customerService,
                           ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.customerService = customerService;
        this.objectMapper = objectMapper;
    }

    /**
     * 고객사 연결 등록
     */
    public void registerConnection(String connectionId, String customerId) {
        try {
            // 기존 연결이 있다면 제거
            String existingConnectionId = customerToConnectionMap.get(customerId);
            if (existingConnectionId != null) {
                connectionToCustomerMap.remove(existingConnectionId);
                logger.info("기존 연결을 제거합니다. 고객사 ID: {}, 기존 연결 ID: {}", 
                           customerId, existingConnectionId);
            }

            // 새로운 연결 등록
            connectionToCustomerMap.put(connectionId, customerId);
            customerToConnectionMap.put(customerId, connectionId);
            
            logger.info("고객사 연결 등록 완료: 고객사 ID: {}, 연결 ID: {}", customerId, connectionId);
        } catch (Exception e) {
            logger.error("고객사 연결 등록 중 오류 발생: 고객사 ID: {}, 연결 ID: {}", customerId, connectionId, e);
        }
    }

    /**
     * 고객사 연결 해제
     */
    public void unregisterConnection(String connectionId) {
        try {
            String customerId = connectionToCustomerMap.remove(connectionId);
            if (customerId != null) {
                customerToConnectionMap.remove(customerId);
                logger.info("고객사 연결 해제 완료: 고객사 ID: {}, 연결 ID: {}", customerId, connectionId);
            } else {
                logger.warn("연결 ID에 해당하는 고객사가 없습니다: {}", connectionId);
            }
        } catch (Exception e) {
            logger.error("고객사 연결 해제 중 오류 발생: 연결 ID: {}", connectionId, e);
        }
    }

    /**
     * 특정 고객사에게 뉴스 전송
     */
    public void sendNewsToCustomer(String customerId, News news) {
        try {
            String connectionId = customerToConnectionMap.get(customerId);
            if (connectionId == null) {
                logger.warn("고객사가 연결되어 있지 않습니다: {}", customerId);
                return;
            }

            NewsDto newsDto = convertToDto(news);
            String message = objectMapper.writeValueAsString(newsDto);
            
            messagingTemplate.convertAndSendToUser(
                connectionId, 
                "/queue/news", 
                message
            );
            
            logger.debug("뉴스 전송 완료: 고객사 ID: {}, 뉴스 ID: {}, 연결 ID: {}", 
                        customerId, news.getId(), connectionId);
        } catch (JsonProcessingException e) {
            logger.error("뉴스 DTO 직렬화 중 오류 발생: 고객사 ID: {}, 뉴스 ID: {}", 
                        customerId, news.getId(), e);
        } catch (Exception e) {
            logger.error("뉴스 전송 중 오류 발생: 고객사 ID: {}, 뉴스 ID: {}", 
                        customerId, news.getId(), e);
        }
    }

    /**
     * 모든 연결된 고객사에게 뉴스 브로드캐스트
     */
    public void broadcastNewsToAllCustomers(News news) {
        try {
            List<Customer> connectedCustomers = customerService.getConnectedCustomers();
            if (connectedCustomers.isEmpty()) {
                logger.warn("연결된 고객사가 없어 뉴스를 전송할 수 없습니다. 뉴스 ID: {}", news.getId());
                return;
            }

            NewsDto newsDto = convertToDto(news);
            String message = objectMapper.writeValueAsString(newsDto);
            
            // 모든 연결된 고객사에게 전송
            for (Customer customer : connectedCustomers) {
                try {
                    messagingTemplate.convertAndSendToUser(
                        customer.getConnectionId(), 
                        "/queue/news", 
                        message
                    );
                    logger.debug("뉴스 브로드캐스트 완료: 고객사 ID: {}, 뉴스 ID: {}, 연결 ID: {}", 
                                customer.getId(), news.getId(), customer.getConnectionId());
                } catch (Exception e) {
                    logger.error("특정 고객사에게 뉴스 전송 중 오류 발생: 고객사 ID: {}, 뉴스 ID: {}", 
                                customer.getId(), news.getId(), e);
                }
            }
            
            logger.info("뉴스 브로드캐스트 완료: 뉴스 ID: {}, 대상 고객사: {}명", 
                       news.getId(), connectedCustomers.size());
        } catch (JsonProcessingException e) {
            logger.error("뉴스 DTO 직렬화 중 오류 발생: 뉴스 ID: {}", news.getId(), e);
        } catch (Exception e) {
            logger.error("뉴스 브로드캐스트 중 오류 발생: 뉴스 ID: {}", news.getId(), e);
        }
    }

    /**
     * 특정 고객사에게 메시지 전송
     */
    public void sendMessageToCustomer(String customerId, String message) {
        try {
            String connectionId = customerToConnectionMap.get(customerId);
            if (connectionId == null) {
                logger.warn("고객사가 연결되어 있지 않습니다: {}", customerId);
                return;
            }

            messagingTemplate.convertAndSendToUser(
                connectionId, 
                "/queue/message", 
                message
            );
            
            logger.debug("메시지 전송 완료: 고객사 ID: {}, 연결 ID: {}", customerId, connectionId);
        } catch (Exception e) {
            logger.error("메시지 전송 중 오류 발생: 고객사 ID: {}", customerId, e);
        }
    }

    /**
     * 모든 연결된 고객사에게 메시지 브로드캐스트
     */
    public void broadcastMessageToAllCustomers(String message) {
        try {
            List<Customer> connectedCustomers = customerService.getConnectedCustomers();
            if (connectedCustomers.isEmpty()) {
                logger.warn("연결된 고객사가 없어 메시지를 전송할 수 없습니다.");
                return;
            }

            for (Customer customer : connectedCustomers) {
                try {
                    messagingTemplate.convertAndSendToUser(
                        customer.getConnectionId(), 
                        "/queue/message", 
                        message
                    );
                } catch (Exception e) {
                    logger.error("특정 고객사에게 메시지 전송 중 오류 발생: 고객사 ID: {}", 
                                customer.getId(), e);
                }
            }
            
            logger.info("메시지 브로드캐스트 완료: 대상 고객사: {}명", connectedCustomers.size());
        } catch (Exception e) {
            logger.error("메시지 브로드캐스트 중 오류 발생", e);
        }
    }

    /**
     * 연결 상태 정보 조회
     */
    public ConnectionStatus getConnectionStatus() {
        return new ConnectionStatus(
            connectionToCustomerMap.size(),
            customerToConnectionMap.size()
        );
    }

    /**
     * 특정 고객사의 연결 상태 확인
     */
    public boolean isCustomerConnected(String customerId) {
        return customerToConnectionMap.containsKey(customerId);
    }

    /**
     * 특정 연결 ID의 고객사 확인
     */
    public String getCustomerIdByConnection(String connectionId) {
        return connectionToCustomerMap.get(connectionId);
    }

    /**
     * News 엔티티를 NewsDto로 변환
     */
    private NewsDto convertToDto(News news) {
        if (news == null) {
            return null;
        }
        
        NewsDto dto = new NewsDto();
        dto.setId(news.getId());
        dto.setTitle(news.getTitle());
        dto.setContent(news.getContent());
        dto.setPublishedAt(news.getPublishedAt());
        dto.setCreatedAt(news.getCreatedAt());
        
        return dto;
    }

    /**
     * 연결 상태 정보를 담는 내부 클래스
     */
    public static class ConnectionStatus {
        private final int totalConnections;
        private final int connectedCustomers;

        public ConnectionStatus(int totalConnections, int connectedCustomers) {
            this.totalConnections = totalConnections;
            this.connectedCustomers = connectedCustomers;
        }

        // Getter
        public int getTotalConnections() { return totalConnections; }
        public int getConnectedCustomers() { return connectedCustomers; }

        @Override
        public String toString() {
            return "ConnectionStatus{" +
                    "totalConnections=" + totalConnections +
                    ", connectedCustomers=" + connectedCustomers +
                    '}';
        }
    }
}
