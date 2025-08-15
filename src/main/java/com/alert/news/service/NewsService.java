package com.alert.news.service;

import com.alert.news.dto.NewsDto;
import com.alert.news.model.News;
import com.alert.news.repository.NewsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 뉴스 서비스
 * 
 * 뉴스 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 뉴스 조회, 검색, 페이징 등의 기능을 제공합니다.
 */
@Service
@Transactional
public class NewsService {

    private static final Logger logger = LoggerFactory.getLogger(NewsService.class);

    private final NewsRepository newsRepository;

    @Autowired
    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    /**
     * ID로 뉴스 조회
     */
    @Transactional(readOnly = true)
    public News getNewsById(String id) {
        try {
            Optional<News> news = newsRepository.findById(id);
            if (news.isPresent()) {
                logger.debug("뉴스 조회 성공: {}", id);
                return news.get();
            } else {
                logger.warn("뉴스를 찾을 수 없습니다: {}", id);
                return null;
            }
        } catch (Exception e) {
            logger.error("뉴스 조회 중 오류 발생: {}", id, e);
            throw new RuntimeException("뉴스 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 뉴스 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<News> getNewsList(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
            Page<News> newsPage = newsRepository.findAll(pageable);
            
            logger.debug("뉴스 목록 조회 완료: 페이지 {}, 크기 {}, 총 {}개", 
                        page, size, newsPage.getTotalElements());
            
            return newsPage;
        } catch (Exception e) {
            logger.error("뉴스 목록 조회 중 오류 발생", e);
            throw new RuntimeException("뉴스 목록 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 최근 뉴스 조회
     */
    @Transactional(readOnly = true)
    public List<News> getRecentNews(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedAt"));
            List<News> recentNews = newsRepository.findTopNByOrderByPublishedAtDesc(pageable);
            
            logger.debug("최근 뉴스 조회 완료: {}개", recentNews.size());
            
            return recentNews;
        } catch (Exception e) {
            logger.error("최근 뉴스 조회 중 오류 발생", e);
            throw new RuntimeException("최근 뉴스 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 특정 기간의 뉴스 조회
     */
    @Transactional(readOnly = true)
    public List<News> getNewsByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<News> newsList = newsRepository.findByPublishedAtBetweenOrderByPublishedAtDesc(startDate, endDate);
            
            logger.debug("기간별 뉴스 조회 완료: {} ~ {}, {}개", startDate, endDate, newsList.size());
            
            return newsList;
        } catch (Exception e) {
            logger.error("기간별 뉴스 조회 중 오류 발생: {} ~ {}", startDate, endDate, e);
            throw new RuntimeException("기간별 뉴스 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 키워드로 뉴스 검색
     */
    @Transactional(readOnly = true)
    public Page<News> searchNewsByKeyword(String keyword, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
            Page<News> newsPage = newsRepository.findByTitleContaining(keyword, pageable);
            
            logger.debug("키워드 검색 완료: '{}', 페이지 {}, 크기 {}, 총 {}개", 
                        keyword, page, size, newsPage.getTotalElements());
            
            return newsPage;
        } catch (Exception e) {
            logger.error("키워드 검색 중 오류 발생: '{}'", keyword, e);
            throw new RuntimeException("키워드 검색 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 뉴스 저장
     */
    public News saveNews(News news) {
        try {
            News savedNews = newsRepository.save(news);
            logger.info("뉴스 저장 완료: {} - {}", savedNews.getId(), savedNews.getTitle());
            return savedNews;
        } catch (Exception e) {
            logger.error("뉴스 저장 중 오류 발생: {}", news.getId(), e);
            throw new RuntimeException("뉴스 저장 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 뉴스 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean existsNews(String id) {
        try {
            return newsRepository.existsById(id);
        } catch (Exception e) {
            logger.error("뉴스 존재 여부 확인 중 오류 발생: {}", id, e);
            throw new RuntimeException("뉴스 존재 여부 확인 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * News 엔티티를 NewsDto로 변환
     */
    public NewsDto convertToDto(News news) {
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
     * 뉴스 통계 정보 조회
     */
    @Transactional(readOnly = true)
    public NewsStatistics getNewsStatistics() {
        try {
            long totalCount = newsRepository.count();
            long todayCount = newsRepository.findByPublishedDate(LocalDateTime.now()).size();
            long thisWeekCount = newsRepository.findByPublishedDate(
                LocalDateTime.now().minusWeeks(1)).size();
            
            NewsStatistics stats = new NewsStatistics(totalCount, todayCount, thisWeekCount);
            logger.debug("뉴스 통계 조회 완료: {}", stats);
            
            return stats;
        } catch (Exception e) {
            logger.error("뉴스 통계 조회 중 오류 발생", e);
            throw new RuntimeException("뉴스 통계 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 뉴스 통계 정보를 담는 내부 클래스
     */
    public static class NewsStatistics {
        private final long totalCount;
        private final long todayCount;
        private final long thisWeekCount;

        public NewsStatistics(long totalCount, long todayCount, long thisWeekCount) {
            this.totalCount = totalCount;
            this.todayCount = todayCount;
            this.thisWeekCount = thisWeekCount;
        }

        // Getter
        public long getTotalCount() { return totalCount; }
        public long getTodayCount() { return todayCount; }
        public long getThisWeekCount() { return thisWeekCount; }

        @Override
        public String toString() {
            return "NewsStatistics{" +
                    "totalCount=" + totalCount +
                    ", todayCount=" + todayCount +
                    ", thisWeekCount=" + thisWeekCount +
                    '}';
        }
    }
}
