package com.alert.news.repository;

import com.alert.news.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 고객사 데이터 접근을 위한 Repository 인터페이스
 * 
 * JPA를 활용하여 고객사 데이터의 CRUD 작업을 수행합니다.
 * 토큰 기반 인증 및 연결 상태 관리에 필요한 쿼리들을 제공합니다.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {

    /**
     * 토큰으로 고객사 조회
     */
    Optional<Customer> findByToken(String token);

    /**
     * 활성 상태인 고객사 조회
     */
    List<Customer> findByIsActiveTrue();

    /**
     * 연결된 고객사 조회
     */
    List<Customer> findByConnectionIdIsNotNull();

    /**
     * 특정 연결 ID를 가진 고객사 조회
     */
    Optional<Customer> findByConnectionId(String connectionId);

    /**
     * 토큰이 만료되지 않은 활성 고객사 조회
     */
    @Query("SELECT c FROM Customer c WHERE c.isActive = true AND c.tokenExpiresAt > :now")
    List<Customer> findActiveCustomersWithValidToken(@Param("now") LocalDateTime now);

    /**
     * 연결된 활성 고객사 조회
     */
    @Query("SELECT c FROM Customer c WHERE c.isActive = true AND c.connectionId IS NOT NULL")
    List<Customer> findConnectedActiveCustomers();

    /**
     * 토큰이 곧 만료될 고객사 조회 (24시간 이내)
     */
    @Query("SELECT c FROM Customer c WHERE c.tokenExpiresAt BETWEEN :now AND :expiryThreshold")
    List<Customer> findCustomersWithExpiringToken(
            @Param("now") LocalDateTime now,
            @Param("expiryThreshold") LocalDateTime expiryThreshold
    );

    /**
     * 고객사명으로 검색
     */
    List<Customer> findByNameContainingIgnoreCase(String name);

    /**
     * 토큰 존재 여부 확인
     */
    boolean existsByToken(String token);

    /**
     * 연결 ID 존재 여부 확인
     */
    boolean existsByConnectionId(String connectionId);
}
