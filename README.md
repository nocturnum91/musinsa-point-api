# 무료 포인트 시스템 API
Java 21 · Spring Boot 3 · H2 DB

---

## 1. 개요

본 프로젝트는 다음 기능을 제공하는 **무료 포인트 시스템 API** 구현 과제입니다.

- 포인트 적립
- 포인트 적립 취소
- 포인트 사용
- 포인트 사용 취소
- 포인트 이력 조회

요구사항에서 제시한 **정책 기반 제어**, **만료 정책**, **적립·사용 추적**,  
**사용 우선순위(수기 → 만료 임박 → 적립순)** 를 모두 충족하도록 설계되었습니다.

---

## 2. 개발 환경

- Java 21
- Spring Boot 3.5.x
- Spring Data JPA
- H2 Database
- Gradle 8.x

---

## 3. 프로젝트 구조

```text
src
 └── main
     ├── java/com/musinsa/point
     │   ├── application/point          # PointService / PointQueryService
     │   ├── domain/member              # Member Entity / Repository
     │   ├── domain/point               # PointSave / PointUse / History 등 핵심 도메인
     │   │   ├── entity
     │   │   ├── model                  # Enum 등
     │   │   └── repository
     │   ├── global
     │   │   ├── config                 # JPA/H2 설정
     │   │   ├── exception                  # ErrorCode / Exception
     │   │   └── entity                 # BaseDateTimeEntity
     │   └── interfaces/point           # Controller / Request / Response DTO
     │
     └── resources
         ├── application.yml
         ├── schema.sql                 # 테이블 스키마
         └── data.sql                   # 테스트 데이터
```

## 4. ERD

/resources/point_system_erd.png 참고

구성 요소:
- member
- point_item
- point_event
- point_save
- point_use
- point_use_detail
- point_history
- point_system_policy
- member_point_limit

## 5. 아키텍처 구성 (AWS 가정)


## 6. 주요 정책
### 6.1 적립 정책

- 1회 적립 가능 포인트는 정책값 기반 제어
(MAX_SAVE_PER_REQUEST)
- 회원별 최대 보유 포인트 한도 존재
(MAX_FREE_POINT_BALANCE + member_point_limit)
- 적립은 point_item 기반으로 이루어짐
- 수기 지급 여부 is_manual_yn 저장
- 만료일은 2가지 방식:
  - FIXED_DATE
  - RELATIVE_DAYS

### 6.2 사용 정책
- 주문에서만 사용 가능
- 어떤 saveNo → useNo 로 포인트가 이동했는지 전부 추적
(point_use_detail)
- 사용 우선순위:
  - 수기 지급 포인트
  - 만료 임박 순
  - 적립 시각 빠른 순

### 6.3 적립 취소 정책
- save 단위 전체 취소만 가능
- 이미 일부라도 사용된 save 는 취소 불가
- 취소 시:
  - available_amount = 0
  - history(SAVE_CANCEL) 기록

### 6.4 사용 취소 정책
- 전체/부분 취소 가능
- 원래 사용된 saveNo 단위로 롤백
- saveNo 가 만료되었으면 신규 save 로 재적립
(요구사항: "만료된 포인트 취소 시 그 금액만큼 신규 적립 처리")

## 7. API 명세
### 7.1 포인트 적립 (/save)

POST /api/points/save
```
{
  "memberId": "test_user1",
  "itemNo": 1,
  "amount": 3000
}
```

응답:
```
{
  "saveNo": 10,
  "pointKey": 55,
  "amount": 3000,
  "expireAt": "2026-01-01T00:00:00",
  "balanceAfter": 12000
}
```
### 7.2 적립 취소 (/save-cancel)

POST /api/points/save-cancel
```
{
  "memberId": "test_user1",
  "saveNo": 10
}
```
### 7.3 포인트 사용 (/use)
```
{
  "memberId": "test_user1",
  "orderNo": "A1234",
  "useAmount": 1200
}
```
### 7.4 포인트 사용 취소 (/use-cancel)
```
{
  "memberId": "test_user1",
  "useNo": 33,
  "cancelAmount": 1100
}
```

### 7.5 포인트 이력 조회 (/history)
```
{
  "memberId": "test_user1"
}
```
응답:
```
[
  {
    "pointKey": 72,
    "historyType": "USE_CANCEL",
    "amount": 1100,
    "balanceAfter": 1400,
    "description": "주문 취소로 인한 포인트 반환",
    "occurredAt": "2025-11-17T10:00:00"
  }
]
```

## 8. 실행 방법
### 8.1 빌드
```bash
./gradlew clean build      # 프로젝트 클린 및 빌드
```

### 8.2 실행
```bash
./gradlew bootRun          # 애플리케이션 실행 (포트 8080)
```

### 8.3 H2 콘솔
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:pointdb`
- Username: `point`
- Password: `point1234`

## 9. 테스트
```bash
./gradlew test             # 모든 테스트 실행
./gradlew test --tests PointServiceSavePointTest      # 특정 테스트 클래스 실행
./gradlew test --tests PointServiceUsePointTest       # 포인트 사용 테스트
./gradlew test --tests PointServiceSaveCancelTest     # 적립 취소 테스트
./gradlew test --tests PointServiceUseCancelTest      # 사용 취소 테스트
```

## 10. 설계 포인트
- 저장 구조를 save / use / useDetail / history 로 분리해
“포인트 원장(ledger)” 역할을 수행
- save 단위로 분리해
원 적립 → 사용 → 취소 → 이력 조회 를 모두 추적 가능
- 이벤트(event)와 아이템(item)을 분리
→ 비즈니스 확장성 확보
- 전역 정책(policy) + 회원별 정책(member limit)
→ 운영자 정책 변경 용이
- 만료된 saveNo 사용취소 시 신규 save 생성
→ 문제 요구사항 핵심 충족

## 11. 라이선스
- 과제 제출용. 별도 라이선스 없음.