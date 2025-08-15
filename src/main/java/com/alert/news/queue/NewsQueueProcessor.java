package com.alert.news.queue;

import com.alert.news.model.News;
import com.alert.news.service.NewsService;
import com.alert.news.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 뉴스 ID 메시지 큐 프로세서
 * 
 * 내부 큐(LinkedBlockingQueue)를 통해 뉴스 ID를 받아서
 * 해당 뉴스를 조회하고 WebSocket으로 실시간 전송하는 역할을 담당합니다.
 * 
 * 현재는 내부 큐를 사용하지만, 향후 AWS SQS 등으로 확장 가능하도록 설계되었습니다.
 */
@Component
public class NewsQueueProcessor {

    private static final Logger logger = LoggerFactory.getLogger(NewsQueueProcessor.class);

    @Value("${queue.capacity:1000}")
    private int queueCapacity;

    @Value("${queue.poll-timeout:1000}")
    private long pollTimeout;

    private final BlockingQueue<String> newsQueue;
    private final NewsService newsService;
    private final WebSocketService webSocketService;

    @Autowired
    public NewsQueueProcessor(NewsService newsService, WebSocketService webSocketService) {
        this.newsService = newsService;
        this.webSocketService = webSocketService;
        this.newsQueue = new LinkedBlockingQueue<>(queueCapacity);
        
        // 큐 프로세서 시작
        startQueueProcessor();
    }

    /**
     * 뉴스 ID를 큐에 추가
     */
    public void enqueueNewsId(String newsId) {
        try {
            if (newsQueue.offer(newsId)) {
                logger.info("뉴스 ID가 큐에 추가되었습니다: {}", newsId);
            } else {
                logger.warn("큐가 가득 차서 뉴스 ID를 추가할 수 없습니다: {}", newsId);
            }
        } catch (Exception e) {
            logger.error("뉴스 ID 큐 추가 중 오류 발생: {}", newsId, e);
        }
    }

    /**
     * 큐 프로세서 시작
     */
    private void startQueueProcessor() {
        Thread processorThread = new Thread(() -> {
            logger.info("뉴스 큐 프로세서가 시작되었습니다. 큐 용량: {}", queueCapacity);
            
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 큐에서 뉴스 ID를 가져옴 (타임아웃 설정)
                    String newsId = newsQueue.poll(pollTimeout, TimeUnit.MILLISECONDS);
                    
                    if (newsId != null) {
                        processNewsId(newsId);
                    }
                } catch (InterruptedException e) {
                    logger.info("뉴스 큐 프로세서가 중단되었습니다.");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("뉴스 큐 처리 중 오류 발생", e);
                }
            }
        });
        
        processorThread.setName("news-queue-processor");
        processorThread.setDaemon(true);
        processorThread.start();
    }

    /**
     * 뉴스 ID 처리
     */
    @Async
    protected void processNewsId(String newsId) {
        try {
            logger.debug("뉴스 ID 처리 시작: {}", newsId);
            
            // 뉴스 조회
            News news = newsService.getNewsById(newsId);
            if (news == null) {
                logger.warn("뉴스를 찾을 수 없습니다: {}", newsId);
                return;
            }
            
            // WebSocket을 통해 모든 연결된 고객사에게 전송
            webSocketService.broadcastNewsToAllCustomers(news);
            
            logger.info("뉴스 전송 완료: {} - {}", newsId, news.getTitle());
            
        } catch (Exception e) {
            logger.error("뉴스 ID 처리 중 오류 발생: {}", newsId, e);
        }
    }

    /**
     * 큐 상태 정보 반환
     */
    public QueueStatus getQueueStatus() {
        return new QueueStatus(
            newsQueue.size(),
            queueCapacity,
            newsQueue.remainingCapacity()
        );
    }

    /**
     * 큐 상태 정보를 담는 내부 클래스
     */
    public static class QueueStatus {
        private final int currentSize;
        private final int capacity;
        private final int remainingCapacity;

        public QueueStatus(int currentSize, int capacity, int remainingCapacity) {
            this.currentSize = currentSize;
            this.capacity = capacity;
            this.remainingCapacity = remainingCapacity;
        }

        // Getter
        public int getCurrentSize() { return currentSize; }
        public int getCapacity() { return capacity; }
        public int getRemainingCapacity() { return remainingCapacity; }
        public double getUtilizationRate() { 
            return capacity > 0 ? (double) currentSize / capacity * 100 : 0; 
        }
    }
}
