### 개발 환경에서 실행

프로젝트 루트 디렉토리에서 터미널을 열고 다음 명령어를 실행합니다.

```bash
docker compose up --build
```
- 이 명령어는 Dockerfile.dev를 사용하여 애플리케이션 이미지를 빌드하고, docker-compose.yml에 정의된 app, oracle, redis 서비스와 함께 모든 컨테이너를 실행합니다.
- 로컬에서 코드를 수정하면 spring-boot-devtools가 변경 사항을 감지하여 컨테이너 내부의 애플리케이션을 자동으로 재시작합니다.
- IDE에서 localhost:5005로 원격 디버거를 연결하여 컨테이너 내부의 애플리케이션을 디버깅할 수 있습니다.

### 프로덕션 이미지 빌드 및 실행
프로덕션 환경에 배포할 이미지를 빌드하려면 Dockerfile.prod를 직접 사용합니다.

#### 프로덕션 이미지 빌드

```bash
docker build -f Dockerfile.prod -t your-app-name:latest .
```
#### 빌드된 이미지 실행

```bash
docker run -p 8080:8080 your-app-name:latest
```