# Quickstart：LINE OAuth 註冊登入

## Environment

```text
LINE_CHANNEL_ID=<LINE Login channel id>
LINE_CHANNEL_SECRET=<LINE Login channel secret>
LINE_CALLBACK_URL=http://localhost:5173/api/auth/line/callback
LINE_OAUTH_MOCK_ENABLED=false
```

正式環境不得啟用 mock。自動測試可使用：

```text
SPRING_PROFILES_ACTIVE=h2
LINE_OAUTH_MOCK_ENABLED=true
LINE_CALLBACK_URL=http://localhost:5173/api/auth/line/callback
```

## Run

```bash
./backend/gradlew -p backend bootRun
cd frontend && npm run dev
```

開啟 `/login`，點選「使用 LINE 登入」。成功後 callback page 將 session 寫入 Redux/localStorage 並導向使用者可存取頁面。

## Validation

```bash
./backend/gradlew -p backend build
cd postman && npm run test:line
cd frontend && npm run test:e2e:line
```
