# Quickstart：後台登出時間設定驗收

## 目錄

- [Scenario 1：管理政策](#scenario-1管理政策) — 讀寫 30 分鐘
- [Scenario 2：JWT 生效](#scenario-2jwt-生效) — exp 減 iat
- [Scenario 3：邊界與權限](#scenario-3邊界與權限) — 400 與 403
- [Scenario 4：前端倒數](#scenario-4前端倒數) — 沿用 exp 顯示
- [Commands](#commands) — Gradle、build 與 Cypress

## Scenario 1：管理政策

1. 系統管理員登入並進入「註冊登入管理」。
2. 在「登入工作階段」輸入 30 分鐘並儲存。
3. 確認成功訊息與重新讀取值皆為 30。

## Scenario 2：JWT 生效

1. 更新政策後重新登入。
2. 解析新 JWT 的 `iat` 與 `exp`。
3. 確認相差 30 分鐘；更新前 token 的 claims 不變。

## Scenario 3：邊界與權限

- 4、1441 分鐘回 400；5、1440 可保存。
- 一般使用者 GET/PUT 回註冊登入管理專屬 403。

## Scenario 4：前端倒數

新登入後進入任一受保護頁面，右上角倒數以 JWT exp 顯示約 30 分鐘；不另呼叫政策 API。

## Commands

```bash
cd backend
./gradlew test --tests com.agentflow.base.RegistrationManagementIntegrationTest
./gradlew test

cd ../frontend
npm run build
npm run test:e2e:session-timeout-settings
npm run test:e2e:login-session-countdown
```
