---
name: email-verification
description: 維護 AgentFlow Gmail SMTP、信箱註冊登入、忘記密碼、驗證碼、寄送紀錄或相關 API／頁面時使用。
---

# 信箱註冊登入與 Gmail 驗證

## 目錄

- [管理員契約](#管理員契約) — SMTP 測試與寄送紀錄
- [註冊契約](#註冊契約) — 首次信箱、驗證碼與建帳
- [密碼契約](#密碼契約) — 登入與忘記密碼
- [安全契約](#安全契約) — 雜湊、效期、秘密與角色
- [測試契約](#測試契約) — JUnit、Postman 與 Cypress

## 管理員契約

`/email-verification` 與 `/api/admin/email-verification/**` 只允許 `SYSTEM_ADMIN`。測試寄信成功或失敗都必須寫入 `email_delivery_log`；管理頁只顯示遮罩收件者、用途、狀態、時間與安全失敗摘要。

## 註冊契約

註冊前以 trim 後小寫信箱檢查唯一性，重複時回覆「此信箱已註冊。」。驗證碼為 6 位數、有效 10 分鐘、最多錯誤 5 次，只接受最新有效流程。核銷成功回傳一次性票券，建帳時 `username=email`、密碼使用 BCrypt、`registration_method=信箱註冊`，並只賦予 `EMPLOYEE`。

## 密碼契約

既有 `/api/auth/login` 同時支援原帳號與新信箱帳號。忘記密碼須先完成 `PASSWORD_RESET` 信箱驗證，再以未使用票券更新密碼；成功後票券立即失效。

## 安全契約

Gmail 憑證只能由 `EMAIL_USER`、`EMAIL_PASSWORD` 或 Secret Manager 注入。驗證碼只保存 BCrypt 雜湊，不得出現在 API、應用程式日誌或寄送紀錄。正式環境 `EMAIL_MOCK_ENABLED` 預設 false、`EMAIL_TEST_CODE` 必須留空。

## 測試契約

JUnit／MockMvc 至少涵蓋首次註冊、重複信箱、錯碼、一次性票券、信箱登入、忘記密碼、管理員紀錄及錯密碼 401。Postman collection 與 `email-registration-login.cy.ts` 必須在 H2 mock SMTP 環境通過；不得連線真實 Gmail 或保存真實秘密。
