# 🧪 Alert Stream Service 테스트 가이드

이 문서는 README.md에 명시된 기능들이 제대로 구현되었는지 확인할 수 있는 테스트 방법을 제공합니다.

## 📋 목차

1. [환경 설정](#환경-설정)
2. [기본 기능 테스트](#기본-기능-테스트)
3. [WebSocket 실시간 통신 테스트](#websocket-실시간-통신-테스트)
4. [메시지 큐 테스트](#메시지-큐-테스트)
5. [고객사 인증 테스트](#고객사-인증-테스트)
6. [API 엔드포인트 테스트](#api-엔드포인트-테스트)
7. [성능 및 부하 테스트](#성능-및-부하-테스트)
8. [모니터링 및 헬스체크](#모니터링-및-헬스체크)

## 🚀 환경 설정

### 1. Docker 환경 실행

```bash
# Docker Compose로 전체 환경 실행
docker-compose up -d

# 서비스 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f app
```

### 2. 애플리케이션 실행

```bash
# Maven으로 빌드 및 실행
./mvnw clean package
./mvnw spring-boot:run

# 또는 JAR 파일로 직접 실행
java -jar target/alert-stream-service-1.0.0.jar
```

### 3. 환경 확인

```bash
# 데이터베이스 연결 확인
docker exec -it alert-news-postgres psql -U postgres -d alert_news -c "\dt"

# Redis 연결 확인
docker exec -it alert-news-redis redis-cli ping
```

## 🔍 기본 기능 테스트

### 1. 애플리케이션 시작 확인

```bash
# 애플리케이션 상태 확인
curl http://localhost:8080/actuator/health

# 예상 응답:
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"}
  }
}
```

### 2. 데이터베이스 스키마 확인

```bash
# PostgreSQL에 직접 연결하여 테이블 확인
docker exec -it alert-news-postgres psql -U postgres -d alert_news

# 테이블 목록 확인
\dt

# 테이블 구조 확인
\d translated_news
\d customers

# 샘플 데이터 확인
SELECT * FROM translated_news LIMIT 5;
SELECT * FROM customers LIMIT 5;
```

## 🌐 WebSocket 실시간 통신 테스트

### 1. WebSocket 연결 테스트

#### JavaScript 클라이언트 (브라우저 콘솔)

```javascript
// WebSocket 연결
const socket = new WebSocket('ws://localhost:8080/ws');

// 연결 이벤트
socket.onopen = function(event) {
    console.log('WebSocket 연결됨');
    
    // 고객사 인증 요청
    socket.send(JSON.stringify({
        destination: '/app/auth',
        body: JSON.stringify({
            customerId: 'test-customer-1',
            token: 'test-token-1'
        })
    }));
};

// 메시지 수신
socket.onmessage = function(event) {
    console.log('메시지 수신:', JSON.parse(event.data));
};

// 연결 해제
socket.onclose = function(event) {
    console.log('WebSocket 연결 해제됨');
};
```

#### cURL을 이용한 WebSocket 테스트

```bash
# WebSocket 연결 테스트 (wscat 필요)
wscat -c ws://localhost:8080/ws

# 연결 후 인증 메시지 전송
{"destination":"/app/auth","body":"{\"customerId\":\"test-customer-1\",\"token\":\"test-token-1\"}"}
```

### 2. 실시간 뉴스 전송 테스트

```bash
# 1. 고객사 생성
curl -X POST "http://localhost:8080/api/v1/customers?name=테스트고객사" \
  -H "Content-Type: application/json"

# 2. 뉴스 저장
curl -X POST "http://localhost:8080/api/v1/news" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "test-news-001",
    "title": "테스트 뉴스 제목",
    "content": "테스트 뉴스 내용입니다.",
    "publishedAt": "2025-01-15T10:00:00"
  }'

# 3. 뉴스 ID를 큐에 추가
curl -X POST "http://localhost:8080/api/v1/queue/news?newsId=test-news-001"

# 4. WebSocket 클라이언트에서 실시간 뉴스 수신 확인
```

## 📨 메시지 큐 테스트

### 1. 큐 상태 확인

```bash
# 큐 상태 조회
curl http://localhost:8080/api/v1/queue/status

# 예상 응답:
{
  "currentSize": 0,
  "capacity": 1000,
  "remainingCapacity": 1000,
  "utilizationRate": 0.0
}

# 큐 통계 조회
curl http://localhost:8080/api/v1/queue/statistics
```

### 2. 큐 처리 테스트

```bash
# 여러 뉴스 ID를 큐에 추가
for i in {1..5}; do
  curl -X POST "http://localhost:8080/api/v1/queue/news?newsId=test-news-00$i"
done

# 큐 상태 재확인
curl http://localhost:8080/api/v1/queue/status

# 로그에서 큐 처리 확인
docker-compose logs -f app | grep "뉴스 ID 처리"
```

## 🔐 고객사 인증 테스트

### 1. 고객사 생성 및 토큰 확인

```bash
# 새 고객사 생성
curl -X POST "http://localhost:8080/api/v1/customers?name=신규고객사"

# 응답에서 customerId와 token 확인
# 예상 응답:
{
  "id": "customer-xxxxxxxx",
  "name": "신규고객사",
  "token": "token-xxxxxxxxxxxxxxxx",
  "tokenExpiresAt": "2025-01-16T10:00:00",
  "createdAt": "2025-01-15T10:00:00"
}
```

### 2. 인증 테스트

```bash
# 올바른 인증 정보로 테스트
curl -X POST "http://localhost:8080/api/v1/customers/auth?customerId=신규고객사ID&token=신규고객사토큰"

# 잘못된 인증 정보로 테스트 (401 응답 확인)
curl -X POST "http://localhost:8080/api/v1/customers/auth?customerId=잘못된ID&token=잘못된토큰"

# 토큰 갱신 테스트
curl -X POST "http://localhost:8080/api/v1/customers/신규고객사ID/refresh-token"
```

### 3. 연결 상태 관리 테스트

```bash
# 활성 고객사 목록 조회
curl http://localhost:8080/api/v1/customers/active

# 연결된 고객사 목록 조회
curl http://localhost:8080/api/v1/customers/connected

# 특정 고객사 연결 상태 확인
curl http://localhost:8080/api/v1/customers/고객사ID/connections

# WebSocket 연결 상태 확인
curl http://localhost:8080/api/v1/customers/websocket/status
```

## 🌍 API 엔드포인트 테스트

### 1. 뉴스 API 테스트

```bash
# 뉴스 목록 조회 (페이징)
curl "http://localhost:8080/api/v1/news?page=0&size=10"

# 특정 뉴스 조회
curl http://localhost:8080/api/v1/news/test-news-001

# 최근 뉴스 조회
curl "http://localhost:8080/api/v1/news/recent?limit=5"

# 키워드 검색
curl "http://localhost:8080/api/v1/news/search?keyword=테스트&page=0&size=10"

# 기간별 뉴스 조회
curl "http://localhost:8080/api/v1/news/period?startDate=2025-01-01T00:00:00&endDate=2025-01-31T23:59:59"

# 뉴스 통계
curl http://localhost:8080/api/v1/news/statistics

# 뉴스 존재 여부 확인
curl http://localhost:8080/api/v1/news/test-news-001/exists
```

### 2. 고객사 API 테스트

```bash
# 고객사 정보 조회
curl http://localhost:8080/api/v1/customers/고객사ID

# 고객사 비활성화
curl -X POST "http://localhost:8080/api/v1/customers/고객사ID/deactivate"

# 고객사 활성화
curl -X POST "http://localhost:8080/api/v1/customers/고객사ID/activate"
```

### 3. Swagger UI를 통한 API 테스트

```
http://localhost:8080/swagger-ui.html
```

## 📊 성능 및 부하 테스트

### 1. 동시 연결 테스트

```bash
# 여러 고객사 동시 연결 시뮬레이션
for i in {1..10}; do
  # 고객사 생성
  CUSTOMER_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/customers?name=부하테스트고객사$i")
  CUSTOMER_ID=$(echo $CUSTOMER_RESPONSE | jq -r '.id')
  TOKEN=$(echo $CUSTOMER_RESPONSE | jq -r '.token')
  
  echo "고객사 $i 생성: $CUSTOMER_ID, 토큰: $TOKEN"
done
```

### 2. 큐 부하 테스트

```bash
# 대량의 뉴스 ID를 큐에 추가
for i in {1..100}; do
  curl -X POST "http://localhost:8080/api/v1/queue/news?newsId=bulk-test-news-$i" &
done
wait

# 큐 상태 확인
curl http://localhost:8080/api/v1/queue/status
```

### 3. 메모리 및 CPU 사용량 모니터링

```bash
# Docker 컨테이너 리소스 사용량 확인
docker stats alert-stream-service

# 애플리케이션 로그에서 성능 관련 메시지 확인
docker-compose logs -f app | grep -E "(처리시간|성능|메모리)"
```

## 📈 모니터링 및 헬스체크

### 1. Actuator 엔드포인트 테스트

```bash
# 애플리케이션 헬스체크
curl http://localhost:8080/actuator/health

# 데이터베이스 헬스체크
curl http://localhost:8080/actuator/health/db

# 애플리케이션 정보
curl http://localhost:8080/actuator/info

# 환경 변수
curl http://localhost:8080/actuator/env

# 메트릭
curl http://localhost:8080/actuator/metrics

# Prometheus 메트릭
curl http://localhost:8080/actuator/prometheus
```

### 2. WebSocket 메트릭 확인

```bash
# WebSocket 세션 메트릭
curl http://localhost:8080/actuator/metrics/websocket.sessions

# 큐 크기 메트릭
curl http://localhost:8080/actuator/metrics/queue.size
```

### 3. 로그 레벨 및 로그 확인

```bash
# 애플리케이션 로그 확인
docker-compose logs -f app

# 특정 로그 레벨 확인
docker-compose logs -f app | grep "DEBUG"
docker-compose logs -f app | grep "ERROR"

# 로그 파일 확인 (볼륨 마운트된 경우)
ls -la logs/
tail -f logs/spring.log
```

## 🐛 문제 해결 및 디버깅

### 1. 일반적인 문제들

#### 데이터베이스 연결 실패
```bash
# PostgreSQL 컨테이너 상태 확인
docker-compose ps postgres

# 데이터베이스 로그 확인
docker-compose logs postgres

# 네트워크 연결 테스트
docker exec alert-stream-service ping postgres
```

#### WebSocket 연결 실패
```bash
# WebSocket 설정 확인
curl http://localhost:8080/actuator/env | grep websocket

# CORS 설정 확인
curl -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: GET" \
     -H "Access-Control-Request-Headers: X-Requested-With" \
     -X OPTIONS http://localhost:8080/ws
```

#### 큐 처리 지연
```bash
# 큐 상태 확인
curl http://localhost:8080/api/v1/queue/status

# 스레드 상태 확인
curl http://localhost:8080/actuator/metrics | grep thread

# 로그에서 큐 처리 시간 확인
docker-compose logs -f app | grep "큐 처리"
```

### 2. 디버깅 모드 활성화

```yaml
# application.yml에 추가
logging:
  level:
    com.alert.news: DEBUG
    org.springframework.web.socket: DEBUG
    org.springframework.messaging: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

## 📝 테스트 체크리스트

### ✅ 기본 기능
- [ ] 애플리케이션 시작 및 헬스체크
- [ ] 데이터베이스 연결 및 스키마 생성
- [ ] Flyway 마이그레이션 실행

### ✅ WebSocket 기능
- [ ] WebSocket 연결 성공
- [ ] 고객사 인증 성공
- [ ] 실시간 뉴스 전송
- [ ] 연결 해제 처리

### ✅ 메시지 큐 기능
- [ ] 뉴스 ID 큐 추가
- [ ] 큐 처리 및 뉴스 전송
- [ ] 큐 상태 모니터링

### ✅ REST API 기능
- [ ] 뉴스 CRUD 작업
- [ ] 고객사 관리
- [ ] 큐 상태 조회
- [ ] Swagger UI 접근

### ✅ 모니터링 기능
- [ ] Actuator 엔드포인트 접근
- [ ] 헬스체크 응답
- [ ] 메트릭 수집
- [ ] 로그 출력

## 🎯 성공 기준

### 기능적 요구사항
- ✅ 실시간 뉴스 전송 시스템 동작
- ✅ 고객사별 개별 연결 관리
- ✅ 토큰 기반 인증 및 보안
- ✅ 메시지 큐를 통한 비동기 처리

### 성능 요구사항
- ✅ 동시 연결 처리 (10개 이상)
- ✅ 큐 처리 지연 시간 < 1초
- ✅ 메모리 사용량 < 1GB
- ✅ 응답 시간 < 100ms

### 안정성 요구사항
- ✅ 24시간 연속 운영 가능
- ✅ 장애 상황에서 자동 복구
- ✅ 로그 및 모니터링 체계 구축

---

**참고**: 이 테스트 가이드는 README.md에 명시된 모든 기능이 제대로 구현되었는지 확인하기 위한 것입니다. 각 테스트를 순차적으로 실행하여 시스템의 정상 동작을 검증하세요.
