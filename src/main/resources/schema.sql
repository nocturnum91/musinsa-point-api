------------------------------------------------------------
-- MEMBER : 무료 포인트를 보유하는 회원
------------------------------------------------------------
CREATE TABLE member
(
    member_no  BIGINT PRIMARY KEY AUTO_INCREMENT, -- 내부 PK
    member_id  VARCHAR(100) NOT NULL UNIQUE,      -- 외부 회원 ID
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);

------------------------------------------------------------
-- POINT_SYSTEM_POLICY : 전역 포인트 정책
--  예) 회원 최대 보유 포인트, 1회 최대 적립 포인트 등
------------------------------------------------------------
CREATE TABLE point_system_policy
(
    policy_code  VARCHAR(50) PRIMARY KEY, -- 예: MAX_FREE_POINT_BALANCE
    policy_value VARCHAR(100) NOT NULL,   -- 문자열로 저장, 코드에서 파싱
    description  VARCHAR(255)             -- 설명
);

------------------------------------------------------------
-- MEMBER_POINT_LIMIT : 회원별 개별 한도 (옵션)
------------------------------------------------------------
CREATE TABLE member_point_limit
(
    member_no   BIGINT PRIMARY KEY, -- FK → member
    max_balance BIGINT    NOT NULL, -- 최대 보유 포인트
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,

    CONSTRAINT fk_member_point_limit_member
        FOREIGN KEY (member_no) REFERENCES member (member_no)
);

------------------------------------------------------------
-- POINT_ITEM : 포인트 정책/아이템 (금액, 만료 규칙, 유형)
--  point_type : PURCHASE / EVENT / COMPENSATION 등
--  expire_type : RELATIVE_DAYS / FIXED_DATE
------------------------------------------------------------
CREATE TABLE point_item
(
    item_no         BIGINT PRIMARY KEY AUTO_INCREMENT, -- 아이템 PK
    point_type      VARCHAR(20)  NOT NULL,             -- 포인트 유형
    item_name       VARCHAR(100) NOT NULL,             -- 아이템 명
    default_amount  BIGINT       NOT NULL,             -- 기본 적립 포인트
    expire_type     VARCHAR(20)  NOT NULL,             -- RELATIVE_DAYS / FIXED_DATE
    fixed_expire_dt TIMESTAMP NULL,                    -- FIXED_DATE일 때 사용
    expire_days     INT NULL,                          -- RELATIVE_DAYS일 때 사용
    is_manual_yn    CHAR(1)      NOT NULL,             -- 수기 지급용 아이템 여부 (Y/N)
    description     VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL
);

------------------------------------------------------------
-- POINT_EVENT : 사용자에게 노출되는 캠페인/이벤트
--  - 어떤 POINT_ITEM으로 적립할지 item_no로 연결
--  - display_yn : 사용자 노출 여부
--  - use_yn     : 적립 가능 여부
------------------------------------------------------------
CREATE TABLE point_event
(
    event_no    BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_no     BIGINT       NOT NULL,        -- FK → point_item
    event_code  VARCHAR(50)  NOT NULL UNIQUE, -- 이벤트 코드 (SIGNUP_BONUS 등)
    event_name  VARCHAR(100) NOT NULL,
    display_yn  CHAR(1)      NOT NULL,        -- 사용자 노출 여부 (Y/N)
    use_yn      CHAR(1)      NOT NULL,        -- 이벤트 활성 여부 (Y/N)
    start_dt    TIMESTAMP    NOT NULL,        -- 이벤트 시작 시각
    end_dt      TIMESTAMP NULL,               -- 종료 시각 (NULL=무기한)
    description VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,

    CONSTRAINT fk_point_event_item
        FOREIGN KEY (item_no) REFERENCES point_item (item_no)
);

------------------------------------------------------------
-- POINT_SAVE : 실제 적립 단위 (버킷)
--  - save_no : 한 번의 적립 행위(버킷) PK
--  - event_code : 어떤 이벤트 코드로 적립됐는지 (선택, FK 아님)
------------------------------------------------------------
CREATE TABLE point_save
(
    save_no          BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_no        BIGINT      NOT NULL,
    item_no          BIGINT      NOT NULL,
    point_type       VARCHAR(20) NOT NULL, -- PURCHASE / EVENT / COMPENSATION 등
    amount           BIGINT      NOT NULL, -- 최초 적립 포인트
    available_amount BIGINT      NOT NULL, -- 현재 사용 가능 잔액
    expire_at        TIMESTAMP   NOT NULL, -- 실제 만료 시각
    is_manual_yn     CHAR(1)     NOT NULL, -- 관리자 수기 지급 여부 (Y/N)
    event_code       VARCHAR(50),          -- 적립을 발생시킨 이벤트 코드(옵션)
    created_at       TIMESTAMP   NOT NULL,
    updated_at       TIMESTAMP   NOT NULL,

    CONSTRAINT fk_save_member
        FOREIGN KEY (member_no) REFERENCES member (member_no),

    CONSTRAINT fk_save_item
        FOREIGN KEY (item_no) REFERENCES point_item (item_no)
);

-- 포인트 사용 우선순위: 수기(Y) → 만료 임박 → 적립 순
CREATE INDEX idx_point_save_use_order
    ON point_save (member_no, is_manual_yn DESC, expire_at ASC, created_at ASC);


------------------------------------------------------------
-- POINT_USE : 포인트 사용 단위 (C, D 같은 키)
------------------------------------------------------------
CREATE TABLE point_use
(
    use_no      BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_no   BIGINT       NOT NULL,
    order_no    VARCHAR(100) NOT NULL, -- 주문 번호
    used_amount BIGINT       NOT NULL, -- 이 사용 행위의 총 사용 포인트
    status      VARCHAR(20)  NOT NULL, -- USED / PARTIAL_CANCEL / CANCEL
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,

    CONSTRAINT fk_use_member
        FOREIGN KEY (member_no) REFERENCES member (member_no)
);

CREATE INDEX idx_point_use_member_order
    ON point_use (member_no, order_no);

------------------------------------------------------------
-- POINT_USE_DETAIL : 사용 이력과 적립 SAVE 간 매핑
--  - 어느 SAVE에서 얼마가 빠져나갔는지 기록
------------------------------------------------------------
CREATE TABLE point_use_detail
(
    detail_no       BIGINT PRIMARY KEY AUTO_INCREMENT,
    use_no          BIGINT    NOT NULL,
    save_no         BIGINT    NOT NULL,
    used_amount     BIGINT    NOT NULL,
    canceled_amount BIGINT    NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL,

    CONSTRAINT fk_use_detail_use
        FOREIGN KEY (use_no) REFERENCES point_use (use_no),

    CONSTRAINT fk_use_detail_save
        FOREIGN KEY (save_no) REFERENCES point_save (save_no)
);

CREATE INDEX idx_point_use_detail_use
    ON point_use_detail (use_no);

CREATE INDEX idx_point_use_detail_save
    ON point_use_detail (save_no);

------------------------------------------------------------
-- POINT_HISTORY : 포인트 통합 이력 (적립/사용/취소/만료 조회용)
--  - 모든 비즈니스 이벤트를 ledger처럼 쌓는 테이블
--  - amount : 부호 포함 증감량 (+적립 / -사용/만료 등)
------------------------------------------------------------
CREATE TABLE point_history
(
    history_no    BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_no     BIGINT      NOT NULL,
    history_type  VARCHAR(20) NOT NULL, -- SAVE, SAVE_CANCEL, USE, USE_CANCEL, EXPIRE 등
    ref_save_no   BIGINT NULL,          -- 관련 save_no
    ref_use_no    BIGINT NULL,          -- 관련 use_no
    amount        BIGINT      NOT NULL, -- SAVE/USE_CANCEL > 0, USE/SAVE_CANCEL/EXPIRE < 0 등
    balance_after BIGINT NULL,          -- 이 거래 이후 잔액 (옵션)
    description   VARCHAR(255),
    occurred_at   TIMESTAMP   NOT NULL, -- 비즈니스 관점 발생 시각
    created_at    TIMESTAMP   NOT NULL, -- 레코드 생성 시각

    CONSTRAINT fk_history_member
        FOREIGN KEY (member_no) REFERENCES member (member_no),

    CONSTRAINT fk_history_save
        FOREIGN KEY (ref_save_no) REFERENCES point_save (save_no),

    CONSTRAINT fk_history_use
        FOREIGN KEY (ref_use_no) REFERENCES point_use (use_no)
);

CREATE INDEX idx_point_history_member_time
    ON point_history (member_no, occurred_at DESC);
