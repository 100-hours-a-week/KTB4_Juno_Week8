<p align="center">
  <img
    width="128"
    height="128"
    alt="favicon"
    src="https://github.com/user-attachments/assets/58d5d1a4-bbe1-4984-b9fc-2462057f19ab"
  />
</p>

# 마라보자 Backend (See Mara)

마라보자는 마라탕이나 훠궈를 먹을 때 찍어 먹는 다양한 소스 조합을 사용자들이 직접 작성하고 모아볼 수 있는 커뮤니티 서비스입니다.

이 저장소는 **마라보자 서비스의 백엔드 서버**입니다.

Spring Boot를 기반으로 회원 인증, 게시글, 댓글, 북마크, 카테고리, 검색, 정렬, 이미지 업로드 API를 제공하며, MySQL과 Amazon S3를 이용해 데이터를 관리합니다.

[프론트엔드 레포지토리 바로가기](https://github.com/100-hours-a-week/KTB4_Juno_Week10)

---

## 1. 프로젝트 소개

마라탕/훠궈 소스는 사람마다 취향이 강하게 갈리고, 같은 재료라도 비율이나 조합에 따라 맛이 크게 달라집니다.

마라보자는 사용자가 자신만의 소스 조합을 게시글로 공유하고, 다른 사용자가 검색과 정렬, 카테고리, 북마크 기능을 통해 원하는 조합을 다시 찾아볼 수 있도록 만든 커뮤니티 서비스입니다.

백엔드에서는 단순 CRUD API를 제공하는 것뿐 아니라 다음과 같은 부분에 집중했습니다.

- 인증된 사용자만 필요한 기능에 접근할 수 있도록 인증/인가 처리
- 검색, 정렬, 페이지네이션을 서버에서 처리해 게시글 탐색 지원
- 게시글과 카테고리의 다대다 관계 관리
- 사용자별 북마크 상태와 게시글 북마크 수 관리
- 게시글 조회수 중복 증가 제어
- Docker 환경과 실제 배포 환경에서도 유지 가능한 이미지 저장 구조
- API 응답 형식과 예외 처리 일관성 유지

### 기술 선택 이유

- **Spring Boot**: Controller → Service → Repository 구조로 역할을 분리하고 REST API를 구현하기 위해 사용했습니다.
- **Spring Data JPA**: Entity 중심으로 관계형 데이터를 관리하고 반복적인 CRUD 코드를 줄이기 위해 사용했습니다.
- **MySQL**: 사용자, 게시글, 댓글, 카테고리, 북마크처럼 데이터 간 관계가 명확해 관계형 데이터베이스가 적합하다고 판단했습니다.
- **Spring Security**: 인증 여부와 사용자 권한에 따라 API 접근을 제어하기 위해 사용했습니다.
- **JWT**: 프론트엔드와 백엔드가 분리된 환경에서 서버 세션에 의존하지 않고 사용자 인증 상태를 전달하기 위해 사용했습니다.
- **Amazon S3**: 초기 로컬 파일 저장 방식이 Docker 컨테이너와 EC2 파일 시스템에 의존하는 문제를 해결하기 위해 도입했습니다.
- **AWS IAM Role**: EC2 서버에 Access Key를 직접 저장하지 않고 S3에 접근할 수 있도록 구성했습니다.
- **Docker / Docker Compose**: Backend, Frontend, MySQL의 실행 환경을 통일하고 실제 EC2 배포 환경을 구성하기 위해 사용했습니다.
- **GitHub Actions**: 테스트, 빌드, EC2 배포 과정을 자동화하기 위해 사용했습니다.
- **Testcontainers**: 테스트 환경에서 실제 MySQL과 유사한 환경을 사용하기 위해 적용했습니다.

### 추후 추가하고 싶은 기능

- **1:1 채팅**: 게시글 작성자에게 소스 재료나 비율을 직접 질문할 수 있도록 WebSocket 기반의 실시간 채팅 기능을 추가하고 싶습니다.
- 채팅 메시지 DB 저장
- 이전 메시지 조회
- 읽음/안 읽음 상태 관리
- WebSocket 연결 종료 후 재연결
- 오프라인 사용자에게 전달되지 않은 메시지 처리

---

## 2. 주요 핵심 기능

### 1. JWT 기반 인증 / 인가

#### Why

게시글 작성, 댓글, 북마크, 사용자 정보 수정 등은 로그인한 사용자만 사용할 수 있어야 합니다.

또한 프론트엔드와 백엔드가 서로 분리되어 있기 때문에 서버 세션에 강하게 의존하지 않는 인증 방식이 필요했습니다.

#### How

로그인 성공 시 JWT Access Token을 발급하고, 이후 요청에서는 `Authorization` Header를 통해 전달합니다.

```http
Authorization: Bearer {accessToken}
```

Spring Security의 JWT Filter가 요청마다 토큰을 검증하고, 인증된 사용자 정보를 Security Context에 저장합니다.

```text
Client
  ↓
Authorization Header
  ↓
JWT Filter
  ↓
Token 검증
  ↓
Security Context
  ↓
Controller
```

인증이 필요한 API와 공개 API는 `SecurityConfig`에서 구분합니다.

#### What

- 회원가입
- 로그인
- JWT Access Token 발급
- 인증된 사용자 정보 조회
- Spring Security 기반 API 접근 제어
- 인증 실패 / 권한 부족 예외 처리

---

### 2. 게시글 검색 + 정렬 + 페이지네이션

#### Why

게시글 수가 증가할수록 전체 데이터를 한 번에 내려주는 방식은 비효율적입니다.

또한 사용자가 기억하는 재료나 맛 표현으로 게시글을 찾을 수 있어야 하고, 최신 글뿐 아니라 북마크 수, 조회수, 댓글 수를 기준으로 탐색할 수 있어야 했습니다.

#### How

게시글 목록 API에서 다음 파라미터를 함께 처리합니다.

```http
GET /posts?keyword=&sort=&page=&size=
```

검색어가 존재하면 제목 또는 본문에 키워드가 포함된 게시글을 조회합니다.

```java
lower(p.title) like lower(concat('%', :keyword, '%'))
or lower(p.content) like lower(concat('%', :keyword, '%'))
```

정렬 기준은 요청의 `sort` 값을 기준으로 서버에서 결정합니다.

페이지네이션은 Spring Data의 `Pageable`을 활용하여 처리합니다.

응답에는 게시글 목록뿐 아니라 다음 페이지 정보를 함께 반환합니다.

```text
number
size
totalPages
totalElements
isFirst
isLast
hasNext
hasPrevious
```

#### What

- 제목 + 본문 통합 검색
- 최신순 정렬
- 북마크순 정렬
- 조회수순 정렬
- 댓글순 정렬
- 페이지네이션
- 검색어가 없는 경우 전체 게시글 조회

---

### 3. 북마크 기능

#### Why

마라보자에서 북마크는 단순한 좋아요보다 사용자가 나중에 다시 먹어보고 싶은 소스 조합을 저장하는 기능에 가깝습니다.

한 사용자가 같은 게시글을 여러 번 북마크하거나 북마크 수가 실제 데이터와 달라지는 문제를 방지할 필요가 있었습니다.

#### How

사용자와 게시글의 관계를 별도의 `post_bookmarks` 테이블로 관리합니다.

```text
User
  ↓
PostBookmark
  ↓
Post
```

사용자 ID와 게시글 ID를 이용한 복합키를 사용하여 동일 사용자가 같은 게시글을 중복 북마크하지 못하도록 구성했습니다.

북마크 등록/취소 시 게시글의 `bookmarkCount`도 함께 갱신합니다.

#### What

- 북마크 등록
- 북마크 취소
- 중복 북마크 방지
- 게시글별 북마크 수 관리
- 사용자별 북마크 여부 조회
- 내 북마크 게시글 목록 조회

---

### 4. 카테고리 다중 선택

#### Why

하나의 소스 조합은 단순히 하나의 맛으로만 표현하기 어렵습니다.

예를 들어 매운맛과 고소한 맛을 동시에 가진 조합처럼 하나의 게시글에 여러 개의 카테고리가 포함될 수 있기 때문에 게시글과 카테고리의 다대다 관계가 필요했습니다.

#### How

게시글과 카테고리를 직접 다대다 관계로 연결하지 않고 `PostCategory` 중간 Entity를 사용했습니다.

```text
Post
  ↓
PostCategory
  ↓
Category
```

사용자는 서버에서 제공하는 카테고리 중 최대 3개까지 선택할 수 있습니다.

카테고리 예시:

```text
매운맛
달콤고소
새콤상큼
간장짭짤
참깨고소
연예인 추천
```

여러 카테고리를 선택한 경우 선택한 카테고리를 모두 포함하는 게시글을 조회할 수 있도록 구성했습니다.

#### What

- 서버 관리형 카테고리
- 게시글당 최대 3개 선택
- 게시글 ↔ 카테고리 관계 저장
- 카테고리 기반 게시글 필터링
- 카테고리 대표 이미지 제공

---

### 5. Amazon S3 이미지 관리

#### Why

초기에는 이미지를 EC2 또는 Docker Container 내부의 `/uploads` 디렉터리에 저장했습니다.

하지만 이 방식은 다음 문제가 있었습니다.

- 컨테이너가 이미지 파일에 의존함
- 컨테이너 재생성 시 별도의 Docker Volume 필요
- 애플리케이션 서버와 이미지 저장소의 역할이 결합됨
- 서버를 확장할 경우 이미지 파일 공유가 어려움

이 문제를 해결하기 위해 이미지 저장소를 Amazon S3로 분리했습니다.

#### How

이미지 업로드 시 서버가 S3에 파일을 저장하고 DB에는 전체 URL 대신 Object Key만 저장합니다.

```text
Frontend
  ↓
POST /images
  ↓
ImageService
  ↓
Amazon S3
  ↓
Object Key 반환
```

DB 저장 예시:

```text
images/550e8400-e29b-41d4-a716-446655440000.jpeg
categories/spicy.webp
```

이미지 조회 시에는 Object Key를 이용해 S3 Presigned URL을 생성합니다.

```text
MySQL
  ↓
Object Key
  ↓
ImageService
  ↓
S3Presigner
  ↓
Presigned URL
  ↓
Frontend
```

Presigned URL의 유효 시간은 현재 10분입니다.

EC2에서는 Access Key를 직접 저장하지 않고 IAM Role을 사용해 S3에 접근합니다.

#### What

- 게시글 이미지 S3 업로드
- 프로필 이미지 S3 관리
- 카테고리 대표 이미지 S3 관리
- DB에 Object Key 저장
- Private S3 Bucket 사용
- Presigned URL 기반 이미지 조회
- EC2 IAM Role 기반 S3 접근

---

### 6. 게시글 조회수 관리

#### Why

사용자가 같은 게시글을 새로고침할 때마다 조회수가 증가하면 실제 조회수와 큰 차이가 발생할 수 있습니다.

따라서 동일 사용자의 반복적인 접근으로 조회수가 과도하게 증가하지 않도록 제어할 필요가 있었습니다.

#### How

게시글 조회 기록을 별도의 `PostView` Entity로 관리하고 일정 시간 동안 동일 사용자의 반복 조회를 제한하는 방식으로 처리했습니다.

현재 기준으로 동일 사용자의 조회수 증가는 일정 시간 단위로 제한합니다.

#### What

- 게시글 조회 기록 관리
- 반복 조회에 의한 조회수 과도한 증가 방지
- 게시글별 조회수 관리

---

## 3. 빠른 시작

### 사전 요구 사항

로컬에서 프로젝트를 실행하려면 다음 환경이 필요합니다.

- Java 17
- MySQL 8.4
- Docker
- AWS Credentials
- Git

Testcontainers 기반 테스트를 실행하려면 Docker가 실행 중이어야 합니다.

---

### 저장소 Clone

```bash
git clone <BACKEND_REPOSITORY_URL>
cd KTB4_Juno_Week8
```

---

### MySQL 설정

MySQL에서 데이터베이스를 생성합니다.

```sql
CREATE DATABASE community_db;
```

---

### 환경 변수 설정

로컬 실행 환경에 다음 값이 필요합니다.

```env
JWT_SECRET=your_jwt_secret
JWT_EXPIRATION=3600000

AWS_REGION=ap-northeast-2
AWS_S3_BUCKET=your_bucket_name
```

DB 접속 정보는 사용하는 Spring Profile의 설정에 맞게 설정합니다.

AWS Credentials는 코드나 `.env`에 Access Key를 직접 작성하지 않고 AWS SDK의 `DefaultCredentialsProvider`를 사용합니다.

AWS CLI를 사용하는 경우:

```bash
aws configure
```

설정 확인:

```bash
aws sts get-caller-identity
```

---

### Backend 실행

개발 Profile로 실행합니다.

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

기본 서버 주소:

```text
http://localhost:8080
```

---

### 테스트

```bash
./gradlew clean test
```

테스트 환경에서는 Testcontainers를 통해 MySQL 8.4 컨테이너를 사용합니다.

---

### Build

```bash
./gradlew clean build
```

빌드 결과:

```text
build/libs/
```

---

### Docker Compose 실행

Docker Compose는 프론트엔드 저장소 `KTB4_Juno_Week10`에서 관리합니다.

두 저장소가 같은 상위 디렉터리에 있어야 합니다.

```text
Desktop
├── KTB4_Juno_Week8
└── KTB4_Juno_Week10
```

프론트엔드 저장소에서 실행합니다.

```bash
cd ../KTB4_Juno_Week10
docker compose up -d --build
```

상태 확인:

```bash
docker compose ps
```

실행되는 컨테이너:

```text
community-frontend
community-backend
community-mysql
```

---

## 4. 디렉토리 구조

```text
src
├── main
│   ├── java/com/example/demo
│   │   ├── config           # Security, CORS, AWS S3 등 설정
│   │   ├── controller       # HTTP 요청 / 응답 처리
│   │   ├── dto              # Request / Response DTO
│   │   ├── entity           # JPA Entity
│   │   ├── exception        # 예외 및 Global Exception 처리
│   │   ├── repository       # Spring Data JPA Repository
│   │   ├── security         # JWT 인증 관련 처리
│   │   └── service          # 비즈니스 로직
│   │
│   └── resources
│       ├── application.properties
│       ├── application-dev.properties
│       ├── application-docker.properties
│       └── application-test.properties
│
└── test
    └── java/com/example/demo
```

백엔드는 다음 계층 구조를 기본으로 사용합니다.

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Entity
    ↓
MySQL
```

---

## 5. 아키텍처 개요

```text
                         Internet
                            │
                            ▼
                     AWS EC2 :80
                            │
                            ▼
                ┌─────────────────────┐
                │       Nginx         │
                │ community-frontend  │
                └──────────┬──────────┘
                           │
                        /api/**
                           │
                           ▼
               ┌──────────────────────┐
               │     Spring Boot      │
               │ community-backend    │
               │       :8080          │
               └─────────┬────────────┘
                         │
          ┌──────────────┴──────────────┐
          │                             │
          ▼                             ▼
 ┌─────────────────┐          ┌─────────────────┐
 │    MySQL 8.4    │          │   Amazon S3     │
 │  community_db   │          │  Private Bucket │
 └─────────────────┘          └─────────────────┘
                                       ▲
                                       │
                                 EC2 IAM Role
```

### Backend 내부 구조

```text
HTTP Request
     ↓
Spring Security
     ↓
JWT Filter
     ↓
Controller
     ↓
Service
     ↓
Repository
     ↓
Spring Data JPA
     ↓
MySQL
```

이미지 요청은 다음과 같이 분리됩니다.

```text
Controller
    ↓
ImageService
    ↓
AWS SDK
    ↓
Amazon S3
```

### Docker 배포 구조

```text
AWS EC2
└── Docker Compose
    ├── community-frontend
    │   └── Nginx :80
    │
    ├── community-backend
    │   └── Spring Boot :8080
    │
    └── community-mysql
        └── MySQL :3306
```

MySQL 데이터는 Docker Volume으로 유지합니다.

```text
mysql-data
```

이미지는 Docker Volume을 사용하지 않고 Amazon S3에 저장합니다.

---
