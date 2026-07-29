---
name: email-verification
description: 維護 AgentFlow Gmail SMTP 寄信、管理員信箱驗證頁、驗證碼信件或寄信 API 時使用。
---

# Gmail 信箱驗證

## 目錄

- [權限契約](#權限契約) — SYSTEM_ADMIN 雙層授權
- [寄信契約](#寄信契約) — Gmail SMTP 與驗證碼
- [安全契約](#安全契約) — 秘密、回應與日誌
- [測試契約](#測試契約) — 不連真實 Gmail

## 權限契約

`/email-verification` 與 `/api/admin/email-verification/**` 只允許 `SYSTEM_ADMIN`。前端 Guard 與後端 `@PreAuthorize` 必須同時存在；一般使用者不得看見側邊導覽項目。

## 寄信契約

使用 Spring Mail 連線 Gmail SMTP `smtp.gmail.com:587` 並強制 STARTTLS。驗證碼由 `SecureRandom` 產生 6 位數字，純文字信件需包含 AgentFlow 識別及 10 分鐘有效提示。

目前功能只測試寄送，不保存或核銷驗證碼。若新增正式驗證流程，必須另行設計雜湊保存、效期、單次使用與嘗試次數限制。

## 安全契約

Gmail 帳號與應用程式密碼只能由 `EMAIL_USER`、`EMAIL_PASSWORD` 環境變數或 Secret Manager 注入。API 回應僅含遮罩收件信箱與寄送時間；驗證碼、完整收件者與 SMTP 秘密不得寫入日誌或版本庫。

## 測試契約

單元與整合測試替換 `MailGateway`，不得連線真實 Gmail 或寄出測試信。測試至少涵蓋管理員成功、一般使用者禁止、無效 email、SMTP 失敗及設定缺漏。
