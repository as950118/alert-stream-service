package com.alert.news.service;

import com.alert.news.dto.CustomerDto;
import com.alert.news.model.Customer;
import com.alert.news.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 고객사 서비스
 * 
 * 고객사 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 고객사 인증, 토큰 관리, 연결 상태 관리 등의 기능을 제공합니다.
 */
@Service
@Transactional
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    @Value("${customer.token.expiry-hours:24}")
    private int tokenExpiryHours;

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * ID로 고객사 조회
     */
    @Transactional(readOnly = true)
    public Customer getCustomerById(String id) {
        try {
            Optional<Customer> customer = customerRepository.findById(id);
            if (customer.isPresent()) {
                logger.debug("고객사 조회 성공: {}", id);
                return customer.get();
            } else {
                logger.warn("고객사를 찾을 수 없습니다: {}", id);
                return null;
            }
        } catch (Exception e) {
            logger.error("고객사 조회 중 오류 발생: {}", id, e);
            throw new RuntimeException("고객사 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 토큰으로 고객사 조회
     */
    @Transactional(readOnly = true)
    public Customer getCustomerByToken(String token) {
        try {
            Optional<Customer> customer = customerRepository.findByToken(token);
            if (customer.isPresent()) {
                logger.debug("토큰으로 고객사 조회 성공: {}", token);
                return customer.get();
            } else {
                logger.warn("토큰에 해당하는 고객사를 찾을 수 없습니다: {}", token);
                return null;
            }
        } catch (Exception e) {
            logger.error("토큰으로 고객사 조회 중 오류 발생: {}", token, e);
            throw new RuntimeException("토큰으로 고객사 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 고객사 인증
     */
    @Transactional(readOnly = true)
    public boolean authenticateCustomer(String customerId, String token) {
        try {
            Customer customer = getCustomerByToken(token);
            if (customer == null) {
                logger.warn("인증 실패: 토큰에 해당하는 고객사가 없습니다. 토큰: {}", token);
                return false;
            }

            if (!customer.getId().equals(customerId)) {
                logger.warn("인증 실패: 고객사 ID가 일치하지 않습니다. 요청: {}, 토큰 소유자: {}", 
                           customerId, customer.getId());
                return false;
            }

            if (!customer.isActive()) {
                logger.warn("인증 실패: 비활성 고객사입니다. 고객사 ID: {}", customerId);
                return false;
            }

            if (customer.isTokenExpired()) {
                logger.warn("인증 실패: 토큰이 만료되었습니다. 고객사 ID: {}, 만료일시: {}", 
                           customerId, customer.getTokenExpiresAt());
                return false;
            }

            logger.info("고객사 인증 성공: {}", customerId);
            return true;
        } catch (Exception e) {
            logger.error("고객사 인증 중 오류 발생: 고객사 ID: {}, 토큰: {}", customerId, token, e);
            return false;
        }
    }

    /**
     * 고객사 연결
     */
    public boolean connectCustomer(String customerId, String connectionId) {
        try {
            Customer customer = getCustomerById(customerId);
            if (customer == null) {
                logger.warn("고객사 연결 실패: 고객사를 찾을 수 없습니다. 고객사 ID: {}", customerId);
                return false;
            }

            if (!customer.isActive()) {
                logger.warn("고객사 연결 실패: 비활성 고객사입니다. 고객사 ID: {}", customerId);
                return false;
            }

            if (customer.isTokenExpired()) {
                logger.warn("고객사 연결 실패: 토큰이 만료되었습니다. 고객사 ID: {}", customerId);
                return false;
            }

            // 이미 연결된 경우 기존 연결 해제
            if (customer.isConnected()) {
                logger.info("기존 연결을 해제하고 새로운 연결을 설정합니다. 고객사 ID: {}, 기존 연결: {}", 
                           customerId, customer.getConnectionId());
            }

            customer.connect(connectionId);
            customerRepository.save(customer);
            
            logger.info("고객사 연결 성공: 고객사 ID: {}, 연결 ID: {}", customerId, connectionId);
            return true;
        } catch (Exception e) {
            logger.error("고객사 연결 중 오류 발생: 고객사 ID: {}, 연결 ID: {}", customerId, connectionId, e);
            return false;
        }
    }

    /**
     * 고객사 연결 해제
     */
    public boolean disconnectCustomer(String connectionId) {
        try {
            Optional<Customer> customer = customerRepository.findByConnectionId(connectionId);
            if (customer.isPresent()) {
                Customer cust = customer.get();
                cust.disconnect();
                customerRepository.save(cust);
                
                logger.info("고객사 연결 해제 성공: 고객사 ID: {}, 연결 ID: {}", cust.getId(), connectionId);
                return true;
            } else {
                logger.warn("연결 ID에 해당하는 고객사를 찾을 수 없습니다: {}", connectionId);
                return false;
            }
        } catch (Exception e) {
            logger.error("고객사 연결 해제 중 오류 발생: 연결 ID: {}", connectionId, e);
            return false;
        }
    }

    /**
     * 활성 고객사 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Customer> getActiveCustomers() {
        try {
            List<Customer> activeCustomers = customerRepository.findByIsActiveTrue();
            logger.debug("활성 고객사 목록 조회 완료: {}개", activeCustomers.size());
            return activeCustomers;
        } catch (Exception e) {
            logger.error("활성 고객사 목록 조회 중 오류 발생", e);
            throw new RuntimeException("활성 고객사 목록 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 연결된 고객사 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Customer> getConnectedCustomers() {
        try {
            List<Customer> connectedCustomers = customerRepository.findConnectedActiveCustomers();
            logger.debug("연결된 고객사 목록 조회 완료: {}개", connectedCustomers.size());
            return connectedCustomers;
        } catch (Exception e) {
            logger.error("연결된 고객사 목록 조회 중 오류 발생", e);
            throw new RuntimeException("연결된 고객사 목록 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 고객사 저장
     */
    public Customer saveCustomer(Customer customer) {
        try {
            Customer savedCustomer = customerRepository.save(customer);
            logger.info("고객사 저장 완료: {} - {}", savedCustomer.getId(), savedCustomer.getName());
            return savedCustomer;
        } catch (Exception e) {
            logger.error("고객사 저장 중 오류 발생: {}", customer.getId(), e);
            throw new RuntimeException("고객사 저장 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 새로운 고객사 생성
     */
    public Customer createCustomer(String name) {
        try {
            String customerId = generateCustomerId();
            String token = generateToken();
            LocalDateTime tokenExpiresAt = LocalDateTime.now().plusHours(tokenExpiryHours);
            
            Customer customer = new Customer(customerId, name, token, tokenExpiresAt);
            Customer savedCustomer = saveCustomer(customer);
            
            logger.info("새로운 고객사 생성 완료: {} - {}, 토큰: {}", 
                       savedCustomer.getId(), savedCustomer.getName(), savedCustomer.getToken());
            
            return savedCustomer;
        } catch (Exception e) {
            logger.error("새로운 고객사 생성 중 오류 발생: {}", name, e);
            throw new RuntimeException("새로운 고객사 생성 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 토큰 갱신
     */
    public boolean refreshToken(String customerId) {
        try {
            Customer customer = getCustomerById(customerId);
            if (customer == null) {
                logger.warn("토큰 갱신 실패: 고객사를 찾을 수 없습니다. 고객사 ID: {}", customerId);
                return false;
            }

            String newToken = generateToken();
            LocalDateTime newTokenExpiresAt = LocalDateTime.now().plusHours(tokenExpiryHours);
            
            customer.setToken(newToken);
            customer.setTokenExpiresAt(newTokenExpiresAt);
            saveCustomer(customer);
            
            logger.info("토큰 갱신 완료: 고객사 ID: {}, 새 토큰: {}", customerId, newToken);
            return true;
        } catch (Exception e) {
            logger.error("토큰 갱신 중 오류 발생: 고객사 ID: {}", customerId, e);
            return false;
        }
    }

    /**
     * Customer 엔티티를 CustomerDto로 변환
     */
    public CustomerDto convertToDto(Customer customer) {
        if (customer == null) {
            return null;
        }
        
        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setConnectionId(customer.getConnectionId());
        dto.setActive(customer.isActive());
        dto.setTokenExpiresAt(customer.getTokenExpiresAt());
        dto.setCreatedAt(customer.getCreatedAt());
        
        return dto;
    }

    /**
     * 고객사 ID 생성
     */
    private String generateCustomerId() {
        return "customer-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 토큰 생성
     */
    private String generateToken() {
        return "token-" + UUID.randomUUID().toString().replace("-", "");
    }
}
