#!/bin/bash

# Alert Stream Service 시스템 테스트 스크립트
# 이 스크립트는 README.md에 명시된 주요 기능들이 제대로 구현되었는지 테스트합니다.

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 로그 함수
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 테스트 결과 카운터
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 테스트 함수
run_test() {
    local test_name="$1"
    local test_command="$2"
    local expected_status="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    log_info "테스트 실행: $test_name"
    
    if eval "$test_command" > /dev/null 2>&1; then
        if [ "$expected_status" = "success" ]; then
            log_success "✓ $test_name - 성공"
            PASSED_TESTS=$((PASSED_TESTS + 1))
        else
            log_error "✗ $test_name - 예상과 다른 결과 (성공했지만 실패해야 함)"
            FAILED_TESTS=$((FAILED_TESTS + 1))
        fi
    else
        if [ "$expected_status" = "success" ]; then
            log_error "✗ $test_name - 실패"
            FAILED_TESTS=$((FAILED_TESTS + 1))
        else
            log_success "✓ $test_name - 예상된 실패 (성공)"
            PASSED_TESTS=$((PASSED_TESTS + 1))
        fi
    fi
}

# 헬스체크 함수
check_health() {
    local max_attempts=30
    local attempt=1
    
    log_info "애플리케이션 헬스체크 대기 중..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s http://localhost:8080/actuator/health | grep -q '"status":"UP"'; then
            log_success "애플리케이션이 정상적으로 시작되었습니다."
            return 0
        fi
        
        log_info "시도 $attempt/$max_attempts - 애플리케이션 시작 대기 중..."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    log_error "애플리케이션이 시작되지 않았습니다."
    return 1
}

# 메인 테스트 실행
main() {
    log_info "🚀 Alert Stream Service 시스템 테스트 시작"
    log_info "=========================================="
    
    # 1. 기본 헬스체크
    log_info "1️⃣ 기본 헬스체크 테스트"
    if check_health; then
        run_test "애플리케이션 헬스체크" "curl -s http://localhost:8080/actuator/health | grep -q '\"status\":\"UP\"'" "success"
        run_test "데이터베이스 헬스체크" "curl -s http://localhost:8080/actuator/health/db | grep -q '\"status\":\"UP\"'" "success"
    else
        log_error "애플리케이션이 실행되지 않아 테스트를 중단합니다."
        exit 1
    fi
    
    # 2. Actuator 엔드포인트 테스트
    log_info "2️⃣ Actuator 엔드포인트 테스트"
    run_test "Actuator info 엔드포인트" "curl -s http://localhost:8080/actuator/info | grep -q 'alert-stream-service'" "success"
    run_test "Actuator metrics 엔드포인트" "curl -s http://localhost:8080/actuator/metrics | grep -q 'jvm'" "success"
    run_test "Prometheus 메트릭 엔드포인트" "curl -s http://localhost:8080/actuator/prometheus | grep -q 'jvm_memory_used_bytes'" "success"
    
    # 3. 고객사 API 테스트
    log_info "3️⃣ 고객사 API 테스트"
    run_test "새 고객사 생성" "curl -s -X POST 'http://localhost:8080/api/v1/customers?name=테스트고객사' | grep -q '테스트고객사'" "success"
    
    # 고객사 ID와 토큰 추출
    CUSTOMER_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/customers?name=API테스트고객사")
    CUSTOMER_ID=$(echo "$CUSTOMER_RESPONSE" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
    TOKEN=$(echo "$CUSTOMER_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    
    if [ -n "$CUSTOMER_ID" ] && [ -n "$TOKEN" ]; then
        log_info "테스트 고객사 생성됨: ID=$CUSTOMER_ID, 토큰=$TOKEN"
        
        run_test "고객사 정보 조회" "curl -s http://localhost:8080/api/v1/customers/$CUSTOMER_ID | grep -q '$CUSTOMER_ID'" "success"
        run_test "고객사 인증" "curl -s -X POST \"http://localhost:8080/api/v1/customers/auth?customerId=$CUSTOMER_ID&token=$TOKEN\" | grep -q '$CUSTOMER_ID'" "success"
        run_test "활성 고객사 목록 조회" "curl -s http://localhost:8080/api/v1/customers/active | grep -q '$CUSTOMER_ID'" "success"
    else
        log_warning "고객사 생성에 실패하여 일부 테스트를 건너뜁니다."
    fi
    
    # 4. 뉴스 API 테스트
    log_info "4️⃣ 뉴스 API 테스트"
    run_test "뉴스 저장" "curl -s -X POST 'http://localhost:8080/api/v1/news' -H 'Content-Type: application/json' -d '{\"id\":\"test-news-001\",\"title\":\"테스트뉴스\",\"content\":\"테스트내용\",\"publishedAt\":\"2025-01-15T10:00:00\"}' | grep -q 'test-news-001'" "success"
    run_test "뉴스 조회" "curl -s http://localhost:8080/api/v1/news/test-news-001 | grep -q '테스트뉴스'" "success"
    run_test "뉴스 목록 조회" "curl -s 'http://localhost:8080/api/v1/news?page=0&size=5' | grep -q 'content'" "success"
    run_test "뉴스 통계 조회" "curl -s http://localhost:8080/api/v1/news/statistics | grep -q 'totalCount'" "success"
    
    # 5. 큐 API 테스트
    log_info "5️⃣ 메시지 큐 API 테스트"
    run_test "큐 상태 조회" "curl -s http://localhost:8080/api/v1/queue/status | grep -q 'capacity'" "success"
    run_test "뉴스 ID 큐 추가" "curl -s -X POST 'http://localhost:8080/api/v1/queue/news?newsId=test-news-001' | grep -q '추가되었습니다'" "success"
    run_test "큐 통계 조회" "curl -s http://localhost:8080/api/v1/queue/statistics | grep -q 'currentSize'" "success"
    
    # 6. Swagger UI 접근 테스트
    log_info "6️⃣ Swagger UI 접근 테스트"
    run_test "Swagger UI 접근" "curl -s http://localhost:8080/swagger-ui.html | grep -q 'Swagger UI'" "success"
    
    # 7. WebSocket 연결 테스트 (기본)
    log_info "7️⃣ WebSocket 기본 연결 테스트"
    run_test "WebSocket 엔드포인트 접근" "curl -s -I http://localhost:8080/ws | grep -q 'HTTP/1.1'" "success"
    
    # 8. 데이터베이스 연결 테스트
    log_info "8️⃣ 데이터베이스 연결 테스트"
    if command -v docker > /dev/null; then
        run_test "PostgreSQL 컨테이너 상태" "docker ps | grep -q 'alert-news-postgres'" "success"
        run_test "Redis 컨테이너 상태" "docker ps | grep -q 'alert-news-redis'" "success"
    else
        log_warning "Docker가 설치되지 않아 컨테이너 상태 테스트를 건너뜁니다."
    fi
    
    # 테스트 결과 요약
    log_info "=========================================="
    log_info "📊 테스트 결과 요약"
    log_info "총 테스트: $TOTAL_TESTS"
    log_info "성공: $PASSED_TESTS"
    log_info "실패: $FAILED_TESTS"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        log_success "🎉 모든 테스트가 성공했습니다!"
        exit 0
    else
        log_error "❌ $FAILED_TESTS개의 테스트가 실패했습니다."
        exit 1
    fi
}

# 스크립트 실행
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
    main "$@"
fi
