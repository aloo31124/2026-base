# Quickstart：信箱註冊登入

## 目錄

- [啟動後端](#啟動後端) — H2 與 mock SMTP
- [啟動前端](#啟動前端) — Vite 指向驗收 API
- [Postman 驗收](#postman-驗收) — 12 requests、18 assertions
- [Cypress 驗收](#cypress-驗收) — 唯一一支子模組規格
- [正式環境](#正式環境) — Gmail SMTP 秘密

## 啟動後端

```bash
SERVER_PORT=18080 SPRING_PROFILES_ACTIVE=h2 ./backend/gradlew -p backend bootRun
```

H2 profile 明確啟用 mock SMTP 與固定測試碼 `123456`，只供本機自動驗收。

## 啟動前端

```bash
cd frontend
VITE_API_URL=http://localhost:18080/api npm run dev
```

開啟 `http://localhost:5173/login`，可進入信箱註冊或忘記密碼頁。

## Postman 驗收

```bash
cd postman
npm run test:email -- --env-var baseUrl=http://localhost:18080
```

collection：`postman/email-registration-login.postman_collection.json`。

## Cypress 驗收

```bash
cd frontend
npm run test:e2e:email
```

規格：`frontend/cypress/e2e/email-registration-login.cy.ts`。

## 正式環境

正式環境必須提供 `EMAIL_USER`、`EMAIL_PASSWORD`，並維持 `EMAIL_MOCK_ENABLED=false`、`EMAIL_TEST_CODE` 空白；憑證由 Secret Manager 或等效服務注入。
