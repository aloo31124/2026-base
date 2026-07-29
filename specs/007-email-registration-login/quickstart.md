# Quickstart：信箱註冊登入

```bash
./backend/gradlew -p backend build
SPRING_PROFILES_ACTIVE=test ./backend/gradlew -p backend bootRun
cd postman && npm run test:email
cd ../frontend && npm run test:e2e:email
```

正式寄信需提供 `EMAIL_USER` 與輪替後的 `EMAIL_PASSWORD`，不得提交至 Git。
