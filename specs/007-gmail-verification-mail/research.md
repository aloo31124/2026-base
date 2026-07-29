# Research：Gmail SMTP 信箱驗證

## 目錄

- [參考專案發現](#參考專案發現) — Nodemailer 與 Gmail SMTP 做法
- [本專案決策](#本專案決策) — Spring Boot 對應實作
- [安全考量](#安全考量) — 憑證與驗證碼

## 參考專案發現

- `2025-newshop` 使用 Nodemailer 的 `service: gmail` 與帳密環境變數寄送純文字驗證碼。
- 註冊與忘記密碼共用寄信入口，但驗證碼保存在程序記憶體且沒有過期或刪除機制。
- 本功能只採用 Gmail SMTP 與環境變數設定概念，不複製記憶體驗證碼清單、錯誤吞沒或敏感日誌行為。

## 本專案決策

- 使用 Spring Boot Starter Mail，由 `JavaMailSender` 透過 `smtp.gmail.com:587` 與 STARTTLS 寄送。
- 使用 `MailGateway` 隔離外部 SMTP，讓 Service 單元測試與 API 整合測試不需真實 Gmail。
- 使用 `SecureRandom` 產生 6 位數字碼；目前只放入信件，不持久化。
- API 路徑置於 `/api/admin/**` 並以方法層 `SYSTEM_ADMIN` 權限再次保護。

## 安全考量

- `EMAIL_USER` 與 `EMAIL_PASSWORD` 只由執行環境注入。
- API 回應只包含遮罩信箱與時間，禁止回傳驗證碼。
- 日誌不得記錄驗證碼、完整收件信箱或 SMTP 密碼。
- 自動測試使用 mock，不連線 Gmail。
