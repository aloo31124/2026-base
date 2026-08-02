---
name: login-session-countdown
description: 維護 AgentFlow 登入倒數、JWT 到期、自動登出、逾時登入提示或相關 Cypress 測試時使用。
---

# 登入工作階段倒數

## 目錄

- [時間契約](#時間契約) — JWT exp 單一來源與絕對校時
- [顯示契約](#顯示契約) — 右上角、格式、警示與 RWD
- [登出契約](#登出契約) — 清除登入狀態與 replace 導頁
- [相容契約](#相容契約) — 無 exp token 與後端授權
- [測試契約](#測試契約) — Cypress 與 production build

## 時間契約

登入倒數只使用目前 JWT payload 的 `exp`，不得另設固定分鐘數、因操作延長或在前端建立滑動 session。後台登出時間更新由伺服器套用於後續新 JWT，既有 JWT 保留原 `exp`。每次更新以 `exp - Date.now()` 重算；每秒、分頁恢復可見及視窗 focus 都必須校時。

## 顯示契約

有效 JWT 在 AppShell 右上角顯示「登入倒數」與 `HH:MM:SS`；五分鐘以下使用 warning 樣式。根節點維持 `role="timer"`，720px 以下可隱藏標籤但不得隱藏時間值。

## 登出契約

倒數歸零時只觸發一次 `logout('session-expired')`，清除 Redux session、`localStorage.session` 與 `localStorage.token`，再以 replace 導向 `/login`。登入頁顯示「登入時間已到，請重新登入。」；手動登出使用 `logout(undefined)` 且不顯示逾時訊息。

## 相容契約

前端解析 JWT 僅供顯示，不驗證簽章或取代後端授權。token 無法解析或沒有有效 `exp` 時不得顯示虛假倒數；既有 API 仍負責拒絕無效憑證，並保留 opaque mock token 的測試相容性。

## 測試契約

`login-session-countdown.cy.ts` 至少涵蓋逐秒更新、到期清除與導頁、focus 校時、初載已過期及窄螢幕；`session-timeout-settings.cy.ts` 驗證後台讀寫契約。後端必須解析新 JWT claims 驗證動態分鐘數；`npm run build` 與專屬 Cypress 必須全數通過。
