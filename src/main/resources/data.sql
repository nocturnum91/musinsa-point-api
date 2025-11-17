------------------------------------------------------------
-- TEST MEMBER : 테스트용 회원 2명
------------------------------------------------------------
INSERT INTO member (member_no, member_id, created_at, updated_at)
VALUES (1, 'test_user1', '2025-11-01 00:00:00', CURRENT_TIMESTAMP);

INSERT INTO member (member_no, member_id, created_at, updated_at)
VALUES (2, 'test_user2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


------------------------------------------------------------
-- POINT SYSTEM POLICY : 전역 정책 예시
------------------------------------------------------------

INSERT INTO point_system_policy (policy_code, policy_value, description)
VALUES ('MAX_FREE_POINT_BALANCE', '15000', '최대 보유 가능 무료 포인트');

INSERT INTO point_system_policy (policy_code, policy_value, description)
VALUES ('MAX_SAVE_PER_REQUEST', '10000', '1회 요청당 최대 적립 포인트');


------------------------------------------------------------
-- MEMBER POINT LIMIT : 회원별 개별 한도 예시
------------------------------------------------------------

INSERT INTO member_point_limit (member_no, max_balance, created_at, updated_at)
VALUES (2, 5000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
-- test_user2 최대

------------------------------------------------------------
-- POINT_ITEM : 이벤트별 포인트 정책/아이템
-- point_type = 'EVENT' 기준으로 구성
------------------------------------------------------------

-- 1. 회원 가입 축하 적립 (1년 만료, RELATIVE_DAYS 365)
INSERT INTO point_item (item_no, point_type, item_name, default_amount,
                        expire_type, fixed_expire_dt, expire_days,
                        is_manual_yn, description, created_at, updated_at)
VALUES (1,
        'EVENT',
        '회원 가입 축하 포인트',
        3000, -- 기본 3,000P
        'RELATIVE_DAYS',
        NULL,
        365, -- 적립일 기준 1년 후 만료
        'N',
        '신규 가입 회원에게 1회 지급되는 포인트',
        '2025-10-01 00:00:00',
        CURRENT_TIMESTAMP);

-- 2. 출석체크 적립 (3개월 만료, RELATIVE_DAYS 90)
INSERT INTO point_item (item_no, point_type, item_name, default_amount,
                        expire_type, fixed_expire_dt, expire_days,
                        is_manual_yn, description, created_at, updated_at)
VALUES (2,
        'EVENT',
        '출석체크 포인트',
        100, -- 기본 100P
        'RELATIVE_DAYS',
        NULL,
        90, -- 적립일 기준 3개월(90일) 후 만료
        'N',
        '매일 출석 시 적립되는 포인트',
        '2025-10-01 00:00:00',
        CURRENT_TIMESTAMP);

-- 3. 블랙프라이데이 쇼핑 지원금 (이벤트 종료일 고정 만료)
INSERT INTO point_item (item_no, point_type, item_name, default_amount,
                        expire_type, fixed_expire_dt, expire_days,
                        is_manual_yn, description, created_at, updated_at)
VALUES (3,
        'EVENT',
        '블랙프라이데이 쇼핑 지원 포인트',
        5000, -- 기본 5,000P
        'FIXED_DATE',
        TIMESTAMP '2025-11-30 23:59:59',-- 캠페인 종료일에 일괄 만료
        NULL,
        'N',
        '블랙프라이데이 기간 쇼핑 지원 포인트',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- 4. 깜짝 쇼핑 지원금 (1일 만료, RELATIVE_DAYS 1)
INSERT INTO point_item (item_no, point_type, item_name, default_amount,
                        expire_type, fixed_expire_dt, expire_days,
                        is_manual_yn, description, created_at, updated_at)
VALUES (4,
        'EVENT',
        '깜짝 쇼핑 지원 포인트',
        2000, -- 기본 2,000P
        'RELATIVE_DAYS',
        NULL,
        '1',
        'N',
        '깜짝 쇼핑 지원 포인트',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- 5. 수기 적립용 아이템 (1개월 만료, RELATIVE_DAYS 30)
INSERT INTO point_item (item_no, point_type, item_name, default_amount,
                        expire_type, fixed_expire_dt, expire_days,
                        is_manual_yn, description, created_at, updated_at)
VALUES (5,
        'EVENT',
        '보상 포인트',
        0, -- 기본 0P (수기 적립 시 별도 지정)
        'RELATIVE_DAYS',
        NULL,
        30, -- 적립일 기준 1개월(30일) 후 만료
        'Y',
        '관리자 수기 적립용 포인트 아이템',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

------------------------------------------------------------
-- POINT_EVENT : 이벤트 4건 정의
-- 1: 회원 가입 축하 적립
-- 2: 출석체크 적립
-- 3: 블랙프라이데이 쇼핑 지원금
-- 4: 깜짝 쇼핑 지원금
------------------------------------------------------------

-- 1. 회원 가입 축하 적립 이벤트
INSERT INTO point_event (event_no, item_no, event_code, event_name,
                         display_yn, use_yn, start_dt, end_dt,
                         description, created_at, updated_at)
VALUES (1,
        1, -- item_no=1 (회원 가입 축하 아이템)
        'SIGNUP_BONUS',
        '회원 가입 축하 적립 이벤트',
        'N', -- 비노출
        'Y', -- 활성
        '2025-10-01 00:00:00',
        NULL, -- 종료일 없음
        '신규 가입 시 3,000P 적립',
        '2025-10-01 00:00:00',
        CURRENT_TIMESTAMP);

-- 2. 출석체크 적립 이벤트
INSERT INTO point_event (event_no, item_no, event_code, event_name,
                         display_yn, use_yn, start_dt, end_dt,
                         description, created_at, updated_at)
VALUES (2,
        2, -- item_no=2 (출석체크 아이템)
        'ATTEND_DAILY',
        '출석체크 적립 이벤트',
        'Y', -- 노출
        'Y',
        '2025-10-01 00:00:00',
        NULL,
        '매일 출석 시 100P 적립 (3개월 만료)',
        '2025-10-01 00:00:00',
        CURRENT_TIMESTAMP);

-- 3. 블랙프라이데이 쇼핑 지원금 이벤트
INSERT INTO point_event (event_no, item_no, event_code, event_name,
                         display_yn, use_yn, start_dt, end_dt,
                         description, created_at, updated_at)
VALUES (3,
        3, -- item_no=3 (블랙프라이데이 아이템)
        'BLACK_FRIDAY_2025',
        '블랙프라이데이 쇼핑 지원금 이벤트',
        'Y',
        'Y',
        TIMESTAMP '2025-11-10 00:00:00',
        TIMESTAMP '2025-11-30 23:59:59',
        '블랙프라이데이 기간 5,000P 쇼핑 지원금 지급',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- 4. 깜짝 쇼핑 지원금 이벤트
INSERT INTO point_event (event_no, item_no, event_code, event_name,
                         display_yn, use_yn, start_dt, end_dt,
                         description, created_at, updated_at)
VALUES (4,
        4, -- item_no=4 (깜짝 쇼핑 지원금)
        'DAY_SHOPPING_SUPPORT',
        '깜짝 쇼핑 지원금 이벤트',
        'N',
        'Y',
        CURRENT_TIMESTAMP,
        NULL,
        '당일 사용 가능한 2,000P 쇼핑 지원 포인트 지급',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

------------------------------------------------------------
-- POINT_SAVE : 실제 적립 단위 예시
-- 1: test_user1 - 회원 가입 축하 포인트 적립
-- 2: test_user1 - 출석체크 포인트 적립
------------------------------------------------------------


------------------------------------------------------------
-- POINT_SAVE : 실제 적립 단위 예시
-- save_no 1,2는 테스트 샘플
------------------------------------------------------------

-- 1. test_user1 - 회원 가입 축하 포인트 적립
INSERT INTO point_save (save_no, member_no, item_no, point_type,
                        amount, available_amount, expire_at, is_manual_yn,
                        event_code, created_at, updated_at)
VALUES (1,
        1,         -- test_user1
        1,         -- item_no=1 (회원가입 축하)
        'EVENT',   -- point_type
        3000,      -- 적립 포인트
        3000,      -- 사용 가능 잔액
        DATEADD('DAY', 365, TIMESTAMP '2025-11-01 00:00:00'), -- 만료 = 기준일 + 365일
        'N',       -- 수기 아님
        'SIGNUP_BONUS', -- 어떤 이벤트로 적립됐는지 (메타)
        TIMESTAMP '2025-11-01 00:00:00',
        CURRENT_TIMESTAMP);

-- 2. test_user1 - 출석체크 포인트 적립
INSERT INTO point_save (save_no, member_no, item_no, point_type,
                        amount, available_amount, expire_at, is_manual_yn,
                        event_code, created_at, updated_at)
VALUES (2,
        1,         -- test_user1
        2,         -- item_no=2 (출석체크)
        'EVENT',   -- point_type
        100,       -- 적립 포인트
        100,       -- 사용 가능 잔액
        DATEADD('DAY', 90, TIMESTAMP '2025-11-01 00:05:00'), -- 만료 = 기준일 + 90일
        'N',
        'ATTEND_DAILY',
        TIMESTAMP '2025-11-01 00:05:00',
        CURRENT_TIMESTAMP);

------------------------------------------------------------
-- POINT_HISTORY : POINT_SAVE 예시 2건에 대한 이력
-- history_no = 1 : 회원 가입 축하 3,000P 적립
-- history_no = 2 : 출석체크 100P 적립
------------------------------------------------------------

INSERT INTO point_history (history_no, member_no, history_type,
                           ref_save_no, ref_use_no,
                           amount, balance_after, description,
                           occurred_at, created_at)
VALUES (
           1,
           1,                      -- member_no (test_user1)
           'SAVE',                 -- HistoryType.SAVE
           1,                      -- ref_save_no (POINT_SAVE.save_no = 1)
           NULL,                   -- ref_use_no
           3000,                   -- 적립 금액
           3000,                   -- 적립 후 총 잔액
           '회원 가입 축하 포인트',
           TIMESTAMP '2025-11-01 00:00:00', -- 발생 시각
           CURRENT_TIMESTAMP
       );

INSERT INTO point_history (history_no, member_no, history_type,
                           ref_save_no, ref_use_no,
                           amount, balance_after, description,
                           occurred_at, created_at)
VALUES (
           2,
           1,                      -- member_no (test_user1)
           'SAVE',
           2,                      -- ref_save_no (POINT_SAVE.save_no = 2)
           NULL,
           100,                    -- 적립 금액
           3100,                   -- 3,000 + 100
           '출석체크 포인트',
           TIMESTAMP '2025-11-01 00:05:00', -- 예시로 5분 후
           CURRENT_TIMESTAMP
       );