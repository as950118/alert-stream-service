package com.alert.news.repository;

import com.alert.news.model.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 뉴스 데이터 접근을 위한 Repository 인터페이스
 * 
 * JPA를 활용하여 뉴스 데이터의 CRUD 작업을 수행합니다.
 * 페이징, 정렬, 커스텀 쿼리 등을 지원합니다.
 */
@Repository
public interface NewsRepository extends JpaRepository<News, String> {

    /**
     * 특정 기간 내의 뉴스를 발행일시 순으로 조회
     */
    @Query("SELECT n FROM News n WHERE n.publishedAt BETWEEN :startDate AND :endDate ORDER BY n.publishedAt DESC")
    List<News> findByPublishedAtBetweenOrderByPublishedAtDesc(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 최근 N일간의 뉴스를 페이징하여 조회
     */
    @Query("SELECT n FROM News n WHERE n.publishedAt >= :since ORDER BY n.publishedAt DESC")
    Page<News> findRecentNews(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 제목에 특정 키워드가 포함된 뉴스 검색
     */
    @Query("SELECT n FROM News n WHERE n.title LIKE %:keyword% ORDER BY n.publishedAt DESC")
    Page<News> findByTitleContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 내용에 특정 키워드가 포함된 뉴스 검색
     */
    @Query("SELECT n FROM News n WHERE n.content LIKE %:keyword% ORDER BY n.publishedAt DESC")
    Page<News> findByContentContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 특정 날짜의 뉴스 조회
     */
    @Query("SELECT n FROM News n WHERE DATE(n.publishedAt) = DATE(:date) ORDER BY n.publishedAt DESC")
    List<News> findByPublishedDate(@Param("date") LocalDateTime date);

    /**
     * 최신 뉴스 N개 조회
     */
    @Query("SELECT n FROM News n ORDER BY n.publishedAt DESC")
    List<News> findTopNByOrderByPublishedAtDesc(Pageable pageable);

    /**
     * 뉴스 존재 여부 확인
     */
    boolean existsById(String id);
}
