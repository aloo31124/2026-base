# Research：信箱註冊登入

## 目錄

- [驗證碼保存](#驗證碼保存) — BCrypt 與一次性狀態
- [登入相容性](#登入相容性) — 信箱即 username
- [寄送稽核](#寄送稽核) — 成功與失敗資料庫紀錄
- [自動測試郵件](#自動測試郵件) — 明確隔離的 mock profile
- [既有資料庫相容](#既有資料庫相容) — sent_at 與 status 欄位

## 驗證碼保存

採用 BCrypt 雜湊而非明碼，保留 10 分鐘效期、5 次錯誤限制及 `PENDING → VERIFIED → COMPLETED` 狀態。驗證成功只回傳資料列 UUID 作一次性票券，Service 再驗證 email、purpose、效期與未使用狀態。

## 登入相容性

新帳號將正規化 email 同時保存於 `username` 與 `email`，因此既有 `UserDetailsService`、JWT subject 與 `/api/auth/login` 不需 breaking change；原本 `admin`、`user` 等帳號仍可登入。

## 寄送稽核

新增 `email_delivery_log` 保存用途、遮罩收件者、狀態與安全失敗摘要。完整 email 僅留在資料庫供內部追蹤，API 不回傳；驗證碼與 SMTP 秘密不進入紀錄。

## 自動測試郵件

H2 與 test profile 可明確啟用 mock SMTP 並使用固定 `123456`；正式設定 `EMAIL_MOCK_ENABLED` 預設 false、`EMAIL_TEST_CODE` 預設空白，仍使用 Gmail SMTP 與 `SecureRandom`。此設計讓 Postman/Cypress 無需真實 Gmail 秘密。

## 既有資料庫相容

本機既有 `email_verification` 表包含必填 `sent_at` 與 `status`。新 BO 保留並填寫兩欄，讓 Hibernate `ddl-auto:update` 可直接升級，而非破壞或刪除既有資料。
