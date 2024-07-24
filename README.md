# Potato Mall

![MainLogo](/assets/Logo.svg)

## 📖 프로젝트 소개

- 실무에서 구현한 결제 모듈 연동을 다시 한번 경험해보고자 시작한 프로젝트입니다.
- 토스페이먼츠 결제 모듈을 연동해 모의 결제를 진행해볼 수 있는 E-Commerce 사이트입니다.
- [Potato Mall 바로가기](https://potato-woong.site/)
    - 테스트 계정
        - ID : `test`
        - PW : `test1234`

---

## 🛠️ 기술 스택

### Back-end

- Language : Java 17
- Framework : SpringBoot 3.3.0
- DB : MariaDB
- ORM : JPA, QueryDSL

### Front-end

- Framework : React
- Library : Redux, Axios

---

## ⭐️ 핵심 내용

### 결제 기능

- 토스페이먼츠 결제 모듈을 연동해 모의 결제를 진행할 수 있습니다.
- 결제 전 재고량을 검사하고, 결제 도중 오류 발생 시 망취소를 구현하여 데이터 일관성을 유지합니다. [🌐 link](https://velog.io/@woong99/SpringBoot-%EA%B2%B0%EC%A0%9C-%EB%A1%9C%EC%A7%811-%EC%A0%84%EC%B2%B4%EC%A0%81%EC%9D%B8-%ED%9D%90%EB%A6%84)
- 재고량 수정 시 분산락을 사용해 동시성 문제를 해결하였습니다. [🌐 link](https://velog.io/@woong99/SpringBoot-%EA%B2%B0%EC%A0%9C-%EB%A1%9C%EC%A7%816-%EB%B6%84%EC%82%B0%EB%9D%BD)
- 트랜잭션을 분리하여 데드락 발생 가능성을 줄였습니다. [🌐 link](https://velog.io/@woong99/SpringBoot-%EA%B2%B0%EC%A0%9C-%EB%A1%9C%EC%A7%81-REQUIRESNEW)
- 트랜잭션 분리에 따른 롤백 문제를 해결하기 위해 유사 MSA Saga 패턴을 적용하였습니다. [🌐 link](https://velog.io/@woong99/SpringBoot-%EA%B2%B0%EC%A0%9C-%EB%A1%9C%EC%A7%815-%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%98-%EB%B6%84%EB%A6%AC%EC%97%90-%EB%94%B0%EB%A5%B8-%EB%A1%A4%EB%B0%B1)

### 로그인 기능

- Spring Security를 사용해 폼/SNS 로그인 기능을 구현했습니다.
- JWT 방식을 사용해 토큰 기반 인증을 구현하였습니다.
    - Access Token : 유효 시간을 짧게 설정하여 보안성을 강화했습니다.
    - Refresh Token : 토큰이 탈취되었을 경우를 대비하여 Redis에 저장했습니다. 또한, Refresh Token은 서버에서 직접 쿠키에 저장하도록 구현하여, HttpOnly와 Secure 속성을 사용해 보안을 강화했습니다.

### 실시간 검색어 기능

- ELK Stack을 이용해 NginX Access Log를 실시간으로 수집하고, 검색어 순위를 보여주는 기능을 구현했습니다.

### Rest Docs

- Rest Docs를 사용해 API 문서를 자동화했습니다. [🌐 link](https://api.potato-woong.site/docs/index.html)

### Github Actions

- Github Actions를 사용해 배포 자동화를 구현했습니다.

---

## ⚙️ 인프라

[![image](/assets/Infra.png)](https://github.com/user-attachments/assets/3ec7eca3-098b-42ef-b705-3e1b033ab33d)

---

## 🗒️ ERD

[![ERD drawio](/assets/ERD.png)](https://github.com/user-attachments/assets/d4153605-80dc-46bd-bf33-54289dad0ebc)

---

## 🎥 Preview

### 로그인 기능

<p align="center">
  <img src="/assets/login.png" alt="로그인 기능"/>
</p>

### 실시간 검색어 기능

<p align="center">
  <img src="/assets/search.png" alt="실시간 검색어 기능 "/>
</p>

### 결제 기능

<p align="center">
  <img src="/assets/pay.gif" alt="결제"/>
</p>

---
