-- 초기 데이터베이스 테이블 생성
-- V1__Create_initial_tables.sql

-- 번역된 뉴스 테이블
CREATE TABLE translated_news (
    id VARCHAR(100) PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    published_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 고객사 테이블
CREATE TABLE customers (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    connection_id VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    token_expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_translated_news_published_at ON translated_news(published_at);
CREATE INDEX idx_translated_news_created_at ON translated_news(created_at);
CREATE INDEX idx_customers_token ON customers(token);
CREATE INDEX idx_customers_connection_id ON customers(connection_id);
CREATE INDEX idx_customers_is_active ON customers(is_active);

-- 초기 테스트 데이터 삽입 (선택사항)
INSERT INTO customers (id, name, token, token_expires_at) VALUES 
('test-customer-1', '테스트 고객사 1', 'test-token-1', CURRENT_TIMESTAMP + INTERVAL '24 hours'),
('test-customer-2', '테스트 고객사 2', 'test-token-2', CURRENT_TIMESTAMP + INTERVAL '24 hours');

-- 샘플 뉴스 데이터 삽입 (선택사항)
INSERT INTO translated_news (id, title, content, published_at) VALUES 
('news-1', 'AI 기술 발전으로 인한 일자리 변화', '인공지능 기술의 급속한 발전으로 많은 산업 분야에서 일자리 구조가 변화하고 있습니다...', CURRENT_TIMESTAMP - INTERVAL '1 hour'),
('news-2', '지속가능한 에너지 전환의 중요성', '기후 변화 대응을 위한 지속가능한 에너지 전환이 전 세계적으로 중요한 이슈로 부상하고 있습니다...', CURRENT_TIMESTAMP - INTERVAL '30 minutes');
