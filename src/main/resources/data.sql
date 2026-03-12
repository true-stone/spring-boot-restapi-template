-- ====================================================
-- 로컬 개발용 초기 데이터 (local 프로필 전용)
-- ====================================================

-- 어드민 계정 초기 삽입
-- 기본 비밀번호: Admin1234!
-- 해시 재생성: new BCryptPasswordEncoder().encode("Admin1234!")
-- public_id: 00000000-0000-0000-0000-000000000001 (UuidToBytesConverter: MSB+LSB 순서)
MERGE INTO users (public_id, username, password, name, email, create_date, modified_date)
KEY (username)
VALUES (
    X'00000000000000000000000000000001',
    'admin',
    '$2a$10$xY6EPdtdEtg126WCZCd.eOgN.9bT5u5hpucddaAppQWmSVCw22XUe',
    '관리자',
    'admin@example.com',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

MERGE INTO user_roles (user_id, role)
KEY (user_id, role)
SELECT id, 'USER' FROM users WHERE username = 'admin';

MERGE INTO user_roles (user_id, role)
KEY (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE username = 'admin';
