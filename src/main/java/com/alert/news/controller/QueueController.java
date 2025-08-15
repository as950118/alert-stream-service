package com.alert.news.controller;

import com.alert.news.queue.NewsQueueProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 메시지 큐 REST API 컨트롤러
 * 
 * 메시지 큐 상태 조회 및 테스트를 위한 REST API를 제공합니다.
 * Swagger/OpenAPI 문서화를 위한 어노테이션을 포함합니다.
 */
@RestController
@RequestMapping("/api/v1/queue")
@Tag(name = "Queue", description = "메시지 큐 관련 API")
public class QueueController {

    private final NewsQueueProcessor newsQueueProcessor;

    @Autowired
    public QueueController(NewsQueueProcessor newsQueueProcessor) {
        this.newsQueueProcessor = newsQueueProcessor;
    }

    /**
     * 큐 상태 조회
     */
    @GetMapping("/status")
    @Operation(summary = "큐 상태 조회", description = "메시지 큐의 현재 상태를 조회합니다.")
    public ResponseEntity<NewsQueueProcessor.QueueStatus> getQueueStatus() {
        NewsQueueProcessor.QueueStatus status = newsQueueProcessor.getQueueStatus();
        return ResponseEntity.ok(status);
    }

    /**
     * 뉴스 ID를 큐에 추가 (테스트용)
     */
    @PostMapping("/news")
    @Operation(summary = "뉴스 ID 큐 추가", description = "테스트를 위해 뉴스 ID를 메시지 큐에 추가합니다.")
    public ResponseEntity<String> enqueueNewsId(
            @Parameter(description = "뉴스 ID", required = true)
            @RequestParam String newsId) {
        
        try {
            newsQueueProcessor.enqueueNewsId(newsId);
            return ResponseEntity.ok("뉴스 ID가 큐에 추가되었습니다: " + newsId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("뉴스 ID 큐 추가 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 큐 통계 정보
     */
    @GetMapping("/statistics")
    @Operation(summary = "큐 통계", description = "메시지 큐의 상세 통계 정보를 조회합니다.")
    public ResponseEntity<QueueStatistics> getQueueStatistics() {
        NewsQueueProcessor.QueueStatus status = newsQueueProcessor.getQueueStatus();
        
        QueueStatistics statistics = new QueueStatistics(
            status.getCurrentSize(),
            status.getCapacity(),
            status.getRemainingCapacity(),
            status.getUtilizationRate(),
            System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(statistics);
    }

    /**
     * 큐 통계 정보를 담는 내부 클래스
     */
    public static class QueueStatistics {
        private final int currentSize;
        private final int capacity;
        private final int remainingCapacity;
        private final double utilizationRate;
        private final long timestamp;

        public QueueStatistics(int currentSize, int capacity, int remainingCapacity, 
                             double utilizationRate, long timestamp) {
            this.currentSize = currentSize;
            this.capacity = capacity;
            this.remainingCapacity = remainingCapacity;
            this.utilizationRate = utilizationRate;
            this.timestamp = timestamp;
        }

        // Getter
        public int getCurrentSize() { return currentSize; }
        public int getCapacity() { return capacity; }
        public int getRemainingCapacity() { return remainingCapacity; }
        public double getUtilizationRate() { return utilizationRate; }
        public long getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return "QueueStatistics{" +
                    "currentSize=" + currentSize +
                    ", capacity=" + capacity +
                    ", remainingCapacity=" + remainingCapacity +
                    ", utilizationRate=" + utilizationRate +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}
