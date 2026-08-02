# Quickstart：登入工作階段倒數驗收

## 目錄

- [Prerequisites](#prerequisites) — 前端依賴
- [Scenario 1：正常倒數](#scenario-1正常倒數) — 120 秒測試 token
- [Scenario 2：到期登出](#scenario-2到期登出) — 清除與導頁
- [Scenario 3：窄螢幕](#scenario-3窄螢幕) — timer 可見性
- [Commands](#commands) — build 與 Cypress

## Prerequisites

- Node.js 與 `frontend/node_modules` 已安裝。
- 前端 Vite 服務運行於 Cypress `baseUrl`。
- 專屬測試攔截使用者清單 API，因此不需要後端服務或真實帳密。

## Scenario 1：正常倒數

1. 固定瀏覽器目前時間。
2. 注入 `exp = now + 120s` 的 SYSTEM_ADMIN session。
3. 進入 `/users`，確認右上角為 `00:02:00`。
4. 推進一秒，確認為 `00:01:59`。

## Scenario 2：到期登出

1. 注入 `exp = now + 2s` 的 session。
2. 推進至到期點。
3. 確認 URL 為 `/login`，localStorage 的 `session`、`token` 皆為 null。
4. 確認登入頁顯示「登入時間已到，請重新登入。」。

## Scenario 3：窄螢幕

1. 將 viewport 設為 390 × 844。
2. 注入有效 session 並進入 `/users`。
3. 確認 timer 數值可見且符合 `HH:MM:SS`。

## Commands

```bash
cd frontend
npm run build
npm run test:e2e:login-session-countdown
```
