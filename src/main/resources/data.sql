-- ====================================================
-- 로컬 개발용 초기 데이터 (local 프로필 전용)
-- ====================================================

-- 어드민 계정 초기 삽입
-- 기본 비밀번호: Admin1234!
-- 해시 재생성: new BCryptPasswordEncoder().encode("Admin1234!")
-- public_id: 00000000-0000-0000-0000-000000000001 (UuidToBytesConverter: MSB+LSB 순서)
insert INTO users (public_id, username, password, name, email, create_date, modified_date)
VALUES
    (X'00000000000000000000000000000001','admin','$2a$10$xY6EPdtdEtg126WCZCd.eOgN.9bT5u5hpucddaAppQWmSVCw22XUe','관리자','admin@example.com',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000101', 'username1',  '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원1',  'username1@gmail.com',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000102', 'username2',  '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원2',  'username2@gmail.com',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000103', 'username3',  '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원3',  'username3@gmail.com',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000104', 'username4',  '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원4',  'username4@gmail.com',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000105', 'username5',  '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원5',  'username5@gmail.com',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000106', 'username6',  '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원6',  'username6@gmail.com',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000107', 'username7',  '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원7',  'username7@gmail.com',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000108', 'username8',  '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원8',  'username8@gmail.com',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000109', 'username9',  '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원9',  'username9@gmail.com',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000110', 'username10', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원10', 'username10@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000111', 'username11', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원11', 'username11@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000112', 'username12', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원12', 'username12@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000113', 'username13', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원13', 'username13@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000114', 'username14', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원14', 'username14@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000115', 'username15', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원15', 'username15@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000116', 'username16', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원16', 'username16@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000117', 'username17', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원17', 'username17@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000118', 'username18', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원18', 'username18@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000119', 'username19', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원19', 'username19@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'00000000000000000000000000000120', 'username20', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원20', 'username20@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO user_roles (user_id, role)
KEY (user_id, role)
SELECT id, 'USER' FROM users;

MERGE INTO user_roles (user_id, role)
KEY (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE username = 'admin';
