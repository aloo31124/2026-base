# Quickstart：Gmail 信箱驗證測試

## 目錄

- [Environment](#environment) — Gmail SMTP 環境變數
- [Run](#run) — 啟動方式
- [Manual Validation](#manual-validation) — 管理員頁面測試
- [Automated Validation](#automated-validation) — 自動測試與建置

## Environment

Gmail 憑證必須存在於執行 Spring Boot 的作業系統環境：

```bash
export EMAIL_USER="<Gmail 帳號>"
export EMAIL_PASSWORD="<Gmail 應用程式密碼>"
```

不得將真實值提交到 `.env.example`、`application.yml` 或前端設定。

## Run

```bash
./backend/gradlew -p backend bootRun
cd frontend
npm run dev
```

## Manual Validation

1. 以系統管理員登入。
2. 從側邊導覽進入「信箱驗證」。
3. 輸入可收信地址後按「寄送驗證碼」。
4. 確認頁面顯示成功與遮罩信箱，並在收件匣確認 6 位數驗證碼。

## Automated Validation

```bash
./backend/gradlew -p backend test
./backend/gradlew -p backend build
cd frontend && npm run build
```

自動測試使用 mock gateway，不會寄出真實 Gmail。
