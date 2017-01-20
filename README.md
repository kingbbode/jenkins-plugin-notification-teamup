TeamUP plugin for Jenkins
-------------------------

### Jenkins plugin to send a alert via TeamUP.

### Version History

-	1.0.0 (2017-01-18)
	-	첫 버전 완성
-	1.0.1 (2017-01-18)
	-	코드 정리
-	1.0.2 (2017-01-19)
	-	한글화
-	1.0.3 (2017-01-19)
	-	오타 수정

### TeamUP이란?

이스트소프트의 기업용 메신저 [팀업(TeamUP)](http://tmup.com)은

-	사내 메신저
-	프로젝트별 그룹피드(게시판)
-	문서 등 자료 중앙관리
-	대용량 파일 전송

등 다양한 업무 도구를 제공해 빠른 커뮤니케이션(소통)을 통한 업무 효율을 향상시켜주는 기업용 통합 커뮤니케이션 플랫폼입니다.

[![팀업](/images/teamup.jpg)](https://tmup.com/)

자세한 내용은 [팀업 소개 페이지](https://tmup.com/main/function)로!

Install Instructions
--------------------

### 1. API Key 신청

[팀업 Developer Center](https://tmup.com/main/developer)로 접속하여 API Key 신청.

![팀업](/images/developer_center.png)

### 2. Jenkins Plugin 설치

아직 정식 플러그인으로 등록되지 않았기 때문에 빌드 후 hpi 파일 생성

```
mvn install
```

젠킨스 관리 > 플러그인 관리 > 고급 > 플러그인 올리기

![업로드](/images/upload.png)

### 3. Global Config 등록

젠킨스 관리 > 시스템 설정

![업로드](/images/global_config.png)

-	발급받은 `Client ID`와 `Client Secret` 작성
-	User : 메시지를 보낼 주체(팀업 계정) 정보 작성

`Test Connection` 버튼을 누르면 성공 실패 메시지를 노출시키며, 성공시 메신저로 테스트 메시지를 보내게 됩니다.

![테스트](/images/test.png)

### 4. 빌드 후 조치 추가

특정 아이템의 빌드 후 조치로 추가합니다.

![추가](/images/add.png)

원하는 설정 체크를 합니다.

![추가](/images/config.png)

-	Room ID : 팀업 방 번호

`Test Connection` 버튼을 누르면 해당 팀업 방으로 테스트 메시지를 보내게 됩니다.

Exemple
-------

![추가](/images/ex.png)

---

본 플러그인은 Jenkins Slack 플러그인을 참고로 만들어졌습니다. 아직 부족한 점이 많습니다. 어떠한 문의나 Pull Request도 환영입니다.

감사합니다.
