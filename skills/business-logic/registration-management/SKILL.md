---
name: registration-management
description: 維護 AgentFlow 註冊登入管理、動態密碼政策、LINE／信箱首次註冊稽核、管理 API 或 React 管理頁時使用。
---

# 註冊登入管理

## 目錄

- [密碼政策契約](#密碼政策契約) — 長度、字母與數字
- [登入效期契約](#登入效期契約) — 後台分鐘數與新 JWT 生效
- [註冊稽核契約](#註冊稽核契約) — EMAIL 與 LINE
- [權限契約](#權限契約) — API 與頁面雙層保護
- [測試契約](#測試契約) — JUnit、Postman、Cypress

## 密碼政策契約

政策保存於 `password_policy`，最小長度僅允許 8–72；`requireLetter` 與 `requireNumber` 可獨立啟閉。政策套用於信箱註冊與忘記密碼；不強迫既有密碼立即重設。拒絕時一次列出全部缺少條件。

## 登入效期契約

登出時間保存於 `session_timeout_policy`，只允許 5–1440 整數分鐘；資料尚不存在時沿用 `JWT_EXPIRATION_MINUTES`（預設 120）建立。管理入口位於「註冊登入管理」，更新後只套用新簽發的 JWT；既有 token 保留原 `exp`，不得由前端改寫或提前延長。

## 註冊稽核契約

`registration_record` 只記錄 EMAIL 或 LINE 首次成功建帳；同一 `app_user` 最多一筆，既有 LINE 帳號再次登入不得重複新增。管理查詢最多回傳最近 100 筆。

## 權限契約

`/api/admin/registration-management/**` 與 `/registration-management` 只允許 `SYSTEM_ADMIN`。API 拒絕訊息為「[註冊登入管理] [api] 無系統管理員權限。」；頁面拒絕訊息為「[註冊登入管理] [頁面] 無系統管理員權限。」。

## 測試契約

JUnit／MockMvc 必須涵蓋密碼與登入效期政策讀寫、效期邊界、新 JWT claims、密碼拒絕、Email/LINE 稽核與 403；Postman 與 Cypress 必須在 H2、mock mail、mock LINE 環境通過。
