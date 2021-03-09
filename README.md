# espoc

## 개발 환경
- [amazonaws corretto jdk11](https://docs.aws.amazon.com/ko_kr/corretto/latest/corretto-11-ug/what-is-corretto-11.html) 을 사용합니다
```bash
$ brew install homebrew/cask-versions/corretto11 --cask
$ jenv add /Library/Java/JavaVirtualMachines/amazon-corretto-11.jdk/Contents/Home
$ jenv versions
```
- jhipster-uaa.zip 파일 압축을 풀고 도커 이미지를 빌드합니다. 이 과정은 최초 한번만 실행하면 됩니다
```bash
~ $ cp msa-starter/jhipster-uaa.zip ./
~ $ unzip jhipster-uaa.zip && cd jhipster-uaa && ./gradlew jibDockerBuild
```
- 아래 명령으로 MySQL(3306), Kafka(9092), jhipster-uaa(9999) 등을 구동합니다
```bash
~/espoc $ ./gradlew clusterUp
# Ctrl + c to quit
```
- 애플리케이션을 구동합니다
```bash
~/espoc $ export SPRING_PROFILES_ACTIVE=local; export USER_TIMEZONE="Asia/Seoul"; ./gradlew clean bootRun
$ curl -s http://localhost:8080/management/health
```
- [Postman Collection & Environment](./postman)를 import하여 Example 및 UAA API를 작동해볼 수 있습니다

### 계정
docker service|username|password
---|---|---
mysql|root|secret

