package com.alert.news.controller;

import com.alert.news.dto.NewsDto;
import com.alert.news.model.News;
import com.alert.news.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 뉴스 REST API 컨트롤러
 * 
 * 뉴스 조회, 검색 등의 REST API를 제공합니다.
 * Swagger/OpenAPI 문서화를 위한 어노테이션을 포함합니다.
 */
@RestController
@RequestMapping("/api/v1/news")
@Tag(name = "News", description = "뉴스 관련 API")
public class NewsController {

    private final NewsService newsService;

    @Autowired
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    /**
     * 특정 뉴스 조회
     */
    @GetMapping("/{id}")
    @Operation(summary = "뉴스 조회", description = "ID로 특정 뉴스를 조회합니다.")
    public ResponseEntity<NewsDto> getNewsById(
            @Parameter(description = "뉴스 ID", required = true)
            @PathVariable String id) {
        
        News news = newsService.getNewsById(id);
        if (news == null) {
            return ResponseEntity.notFound().build();
        }
        
        NewsDto newsDto = newsService.convertToDto(news);
        return ResponseEntity.ok(newsDto);
    }

    /**
     * 뉴스 목록 조회 (페이징)
     */
    @GetMapping
    @Operation(summary = "뉴스 목록 조회", description = "페이징을 지원하는 뉴스 목록을 조회합니다.")
    public ResponseEntity<Page<NewsDto>> getNewsList(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        Page<News> newsPage = newsService.getNewsList(page, size);
        Page<NewsDto> newsDtoPage = newsPage.map(newsService::convertToDto);
        
        return ResponseEntity.ok(newsDtoPage);
    }

    /**
     * 최근 뉴스 조회
     */
    @GetMapping("/recent")
    @Operation(summary = "최근 뉴스 조회", description = "최근 N개의 뉴스를 조회합니다.")
    public ResponseEntity<List<NewsDto>> getRecentNews(
            @Parameter(description = "조회할 뉴스 개수", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        
        List<News> recentNews = newsService.getRecentNews(limit);
        List<NewsDto> recentNewsDto = recentNews.stream()
                .map(newsService::convertToDto)
                .toList();
        
        return ResponseEntity.ok(recentNewsDto);
    }

    /**
     * 기간별 뉴스 조회
     */
    @GetMapping("/period")
    @Operation(summary = "기간별 뉴스 조회", description = "특정 기간의 뉴스를 조회합니다.")
    public ResponseEntity<List<NewsDto>> getNewsByPeriod(
            @Parameter(description = "시작 날짜", example = "2025-01-01T00:00:00")
            @RequestParam LocalDateTime startDate,
            
            @Parameter(description = "종료 날짜", example = "2025-01-31T23:59:59")
            @RequestParam LocalDateTime endDate) {
        
        List<News> newsList = newsService.getNewsByPeriod(startDate, endDate);
        List<NewsDto> newsDtoList = newsList.stream()
                .map(newsService::convertToDto)
                .toList();
        
        return ResponseEntity.ok(newsDtoList);
    }

    /**
     * 키워드로 뉴스 검색
     */
    @GetMapping("/search")
    @Operation(summary = "키워드 검색", description = "제목에 키워드가 포함된 뉴스를 검색합니다.")
    public ResponseEntity<Page<NewsDto>> searchNewsByKeyword(
            @Parameter(description = "검색 키워드", required = true)
            @RequestParam String keyword,
            
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        Page<News> newsPage = newsService.searchNewsByKeyword(keyword, page, size);
        Page<NewsDto> newsDtoPage = newsPage.map(newsService::convertToDto);
        
        return ResponseEntity.ok(newsDtoPage);
    }

    /**
     * 뉴스 통계 조회
     */
    @GetMapping("/statistics")
    @Operation(summary = "뉴스 통계", description = "뉴스 관련 통계 정보를 조회합니다.")
    public ResponseEntity<NewsService.NewsStatistics> getNewsStatistics() {
        NewsService.NewsStatistics statistics = newsService.getNewsStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 뉴스 존재 여부 확인
     */
    @GetMapping("/{id}/exists")
    @Operation(summary = "뉴스 존재 여부 확인", description = "특정 ID의 뉴스가 존재하는지 확인합니다.")
    public ResponseEntity<Boolean> existsNews(
            @Parameter(description = "뉴스 ID", required = true)
            @PathVariable String id) {
        
        boolean exists = newsService.existsNews(id);
        return ResponseEntity.ok(exists);
    }

    /**
     * 뉴스 저장 (테스트용)
     */
    @PostMapping
    @Operation(summary = "뉴스 저장", description = "새로운 뉴스를 저장합니다. (테스트용)")
    public ResponseEntity<NewsDto> saveNews(@RequestBody NewsDto newsDto) {
        try {
            News news = new News();
            news.setId(newsDto.getId());
            news.setTitle(newsDto.getTitle());
            news.setContent(newsDto.getContent());
            news.setPublishedAt(newsDto.getPublishedAt());
            
            News savedNews = newsService.saveNews(news);
            NewsDto savedNewsDto = newsService.convertToDto(savedNews);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedNewsDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
