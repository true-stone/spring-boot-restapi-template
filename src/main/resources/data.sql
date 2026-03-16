-- ====================================================
-- 로컬 개발용 초기 데이터 (local 프로필 전용)
-- ====================================================

-- 어드민 계정 초기 삽입
-- 기본 비밀번호: Admin1234!
-- 해시 재생성: new BCryptPasswordEncoder().encode("Admin1234!")
-- public_id: 00000000-0000-0000-0000-000000000001 (UuidToBytesConverter: MSB+LSB 순서)
insert INTO users (public_id, username, password, name, email, create_date, modified_date)
VALUES
    (X'019cf57229307ceab8b29927b06f1203','admin','$2a$10$xY6EPdtdEtg126WCZCd.eOgN.9bT5u5hpucddaAppQWmSVCw22XUe','관리자','admin@example.com',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
    (X'019cf57229307b63b14090975c354429', 'username1',  '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원1',  'username1@gmail.com',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'019cf57229307d65a805527044203d93', 'username2',  '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원2',  'username2@gmail.com',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'019cf57229307109a2b1fed75122a110', 'username3',  '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원3',  'username3@gmail.com',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'019cf57229307e5e918f4ada8de8e046', 'username4',  '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원4',  'username4@gmail.com',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'019cf57229307bb3a1f93be6f91846e7', 'username5',  '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원5',  'username5@gmail.com',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'019cf57229307bb482ea230275e7b040', 'username6',  '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원6',  'username6@gmail.com',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'019cf572293078679dcb6fdcf0f22d4b', 'username7',  '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원7',  'username7@gmail.com',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'019cf57229307b30b288e940dbedb4a7', 'username8',  '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원8',  'username8@gmail.com',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'019cf57229307985b3a9607efd9a0b97', 'username9',  '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원9',  'username9@gmail.com',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'019cf57229307d6c9aba35c8e060f8b9', 'username10', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원10', 'username10@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'019cf5722930758782db4628836e887d', 'username11', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원11', 'username11@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'019cf57229307e0a920f40c783e4b744', 'username12', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원12', 'username12@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'019cf57229307a9fb9b973742aa80338', 'username13', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원13', 'username13@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'019cf57229307dbc85b56d68afa0ac6f', 'username14', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원14', 'username14@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'019cf57229307d5aaae108b3dbb02437', 'username15', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원15', 'username15@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'019cf572293079aabf33f41870ab2eb2', 'username16', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원16', 'username16@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'019cf57229307ba9b5a872ca4bd9e7a9', 'username17', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원17', 'username17@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'019cf57229307927b0ee8e3775f75d21', 'username18', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원18', 'username18@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'019cf587bab073278cc7aa80dcc1246f', 'username19', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원19', 'username19@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (X'019cf587bab07e7888e25fbbbfb3938d', 'username20', '$2a$10$ULLWwRRcpArYoidGV8RCOe.PfrhzWLKSL0z51rXXvfv74ryNYknVi', '회원20', 'username20@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO user_roles (user_id, role)
KEY (user_id, role)
SELECT id, 'USER' FROM users;

MERGE INTO user_roles (user_id, role)
KEY (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE username = 'admin';
