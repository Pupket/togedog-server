<h1 align="center">Welcome to 같이걷개 <img src="https://raw.githubusercontent.com/MartinHeinz/MartinHeinz/master/wave.gif" width="48px"></h1>
<p>
</p>

<div style="text-align: center;">
    <img width="400" alt="스크린샷 2024-10-03 오후 8 47 00" src="https://github.com/user-attachments/assets/08ec61f3-63ca-41fe-a283-392ea026a971">
</div>

> 같이걷개 (비사이드 프로젝트)


## ✨ Description

---

"반려견이 유일한 내 가족" 이라는 펫미(Pet = Me)족이 증가하고 있는 요즘 직장인이나 학생들은 낮에 혼자 있을 반려동물을 걱정하면서 반려견 양육비를 월 15만원 이상 투자하고 있습니다. 낮에 강아지를 돌봐주거나 호텔에 맡기면서 발생하는 비용을 최소화하고 색다른 일자리로 반려 동물을 산책해주는 사람을 빠르게 구할 수 없을까 하는 요구사항을 해소시키고자 이렇게 반려동물을 위한 산책 메이트 매칭 플랫폼을 기획하게 되었습니다.

## Tech Stack

---

- Java , Spring Boot, Spring Security, OAuth2, JPA, JWT, Redis, WebSocket  
- AWS EC2, S3, RDS(MySQL), ACM, Router53, ALB, FCM
- Git Actions, Docker


## System Architecture

---

<div style="text-align: center;">
<img width="624" alt="image" src="https://github.com/user-attachments/assets/798d9494-8ac3-479d-a178-43fac259f585
">
</div>


[//]: # ([![stackticon]&#40;https://firebasestorage.googleapis.com/v0/b/stackticon-81399.appspot.com/o/images%2F1732620980855?alt=media&token=0aa05b9b-ea57-49d5-88ca-63142b98bb70&#41;]&#40;https://github.com/msdio/stackticon&#41;)


## :pencil2: ERD

---

<div style="text-align: center;">
<img width="600" alt="image" src="https://github.com/user-attachments/assets/6e7903a5-78ed-4bc6-8726-90bb85769e65">
</div>


# :mag: 서비스 기능

---

## 계정
- 소셜로그인만 가능
- 중복 로그인 시 가장 최근 로그인 유저만 유지

## 공통 기능
### 알림함
- 생성일 기준 14일 동안 유지 후 자동 삭제.
- 주요 알림:
    - 산책 매칭 요청 및 수락 알림.
    - 산책 관련 알림 (예: 산책 시간 변경, 취소).
- 알림은 제목과 상세 내용을 포함하여 사용자에게 제공.

### 프로필 카드
- 등록된 산책 메이트 또는 반려견의 프로필을 랜덤으로 노출.
- 선택하여 세부 정보를 확인하거나 매칭 진행 가능.

---

## 채팅

### 채팅방
- **정렬 기준**:
    - 1순위: 읽지 않은 메시지가 있는 채팅방.
    - 2순위: 읽은 메시지가 있는 채팅방.
- **안 읽은 메시지 표시**:
    - 채팅방 목록과 채팅방 내부에서 안 읽은 메시지 개수 제공.
- **채팅방 삭제**:
    - 사용자가 선택한 특정 채팅방 삭제 가능.

### 메시지 기능
- **텍스트 메시지 입력**:
    - 사용자 간 텍스트 기반 메시지 송수신.
- **메시지 보낸 시간 표시**:
    - 각 메시지에 발송 시간 표시.

### 파일 전송
- **사진 전송**:
    - 채팅방 내 카메라 기능으로 실시간 사진 전송 (최대 2MB).
- **카메라 접근 및 사용**:
    - 채팅방에서 카메라 아이콘 클릭으로 사진 촬영 및 전송 가능.

---

## 보호자 모드
### 홈
- **홈 배너**: 반려견의 산책 활동을 슬라이드 배너로 시각적으로 확인.
- **산책 메이트 프로필 카드 목록**: 등록된 산책 메이트 프로필을 랜덤으로 노출.

### 산책 관리
- **산책 스케줄 관리**: 반려견의 산책 일정을 등록, 수정, 삭제 가능.
- **산책 기록 보기**: 날짜별로 과거 산책 기록 조회.

### 매칭 관리
- **산책 메이트 매칭 요청**: 원하는 산책 메이트에게 매칭 요청 가능.
- **산책 메이트 검색** : 초성 입력시 자동 키워드 완성 가능.
- **매칭 내역 관리**: 과거 매칭 내역 확인 및 진행 중인 매칭 상태 조회.

### 반려견 프로필 관리
- **프로필 등록/수정**: 반려견의 이름, 나이, 품종, 특이사항 등을 입력/수정.
- **사진 업로드**: 반려견 사진 추가로 프로필 완성 가능.

---

## 산책 메이트 모드
### 홈
- **홈 배너**: 산책 메이트의 산책 활동을 슬라이드 배너로 시각적으로 확인.
- **반려견 프로필 카드 목록**: 등록된 반려견 프로필을 랜덤으로 노출.
  - 반려견 견종 초성 입력시 자동 키워드 완성

### 산책 관리
- **산책 제안**: 보호자에게 산책 제안을 보내 날짜와 시간 조율.
- **산책 기록 관리**: 참여한 산책 기록 조회 및 관리 가능.

### 매칭 관리
- **매칭 요청 확인**: 보호자에게 받은 매칭 요청을 수락/거절.
- **매칭 상태 관리**: 진행 중인 매칭 상태를 확인하고 보호자와 소통.

### 프로필 관리
- **프로필 설정**: 이름, 활동 가능 시간, 지역 등의 정보를 등록/수정.
- **사진 업로드**: 자신의 사진 추가로 프로필 완성 가능.




# 주요과정

---

## 협업 과정

- 애자일 방식의 회의 및 피그마 와 엑셀을 사용한 문서 공유
  ([프로젝트 명세서](https://docs.google.com/spreadsheets/d/1VBapu7mr89ujvRpVGxLbJw6wNkxU-_CEoCnUFgFYw0M/edit?usp=sharing) / 
[피그마 명세서](https://www.figma.com/design/7X1mnbaM8zmJY1Kc02SOfv/%EA%B0%99%EC%9D%B4%EA%B1%B7%EA%B0%9C?node-id=0-1&t=406YOQg0NFmoyYaX-1))
  - 팀원이 주차별 팀원간 목표 달성 정도를 공유
  - 이슈 발생 부분에 대해 토론하고 정책에 대해 결정
- swagger를 사용한 API 문서 명세화
- 백엔드 팀원과 깃허브를 사용한 이슈 관리
  - main과 develop 브랜치를 분기, 파생한 브랜치로 각 팀원간 맡은 기능을 구현
  - 이슈를 작성하여 작업중인 건 확인
  - PR을 통한 코드 리뷰 및 소통

## 개발 과정

- 레이어 아키텍처의 강한 의존성 문제 개선([개선과정 블로그 기록](https://sunro1994.tistory.com/255))
  - 어댑터 패턴을 사용하여 각 레이어의 의존성 약화
- 예외 응답을 처리할 수 있는 클래스 구현, ExceptionHandler를 사용하여 예외처리 로직 공통 처리
- Spring Security와 Jwt를 사용한 인증 방식 구현
- AOP를 사용한 각 컨트롤러 및 서비스 레이어의 로깅 공통 로직 중복 제거
- OAuth2를 사용한 소셜 로그인 구현
- 중복 로그인을 처리할 수 있는 Filter와 인증을 거칠 수 있는 Filter를 OncePerRequestFilter를 상속받아 구현
- Redis의 ZSet과 트라이 구조를 사용한 실시간 초성 검색 자동완성 기능 개발
  - 견종과 유저 닉네임 검색을 위한 음절 분리 및 저장 과정을 RDB에서 Redis의 ZSet을 사용하여 저장 속도 개선(15초->2초) 
  - Redis Read Through 패턴과 Write Around 조합을 사용하여 정합성 문제 해결
- STOMP와 Websocket을 사용한 실시간 채팅 기능 개발
  - 유저의 마지막 접속 시간을 체크하여 미수신 메시지 반환 
  - EventListener와 JWT를 사용하여 유저 로그인 상태 실시간 확인
  - 미접속 유저는 FCM을 사용한 실시간 알림

## 배포 과정

- AWS EC2와 Docker를 사용한 CI 파이프라인 구성
- Git Actions를 사용한 CD 파이프라인 구성
- AWS Router 53을 사용한 도메인 네임 서버 구성
- AWS ACM, AWS ALB를 사용한 DNS Verification 수행 및 포트 리다이렉트
