# 信箱註冊登入 Postman 測試報告

## 結果

- 日期：2026-07-28
- API 自動案例：8
- 通過：8
- 失敗：0
- Newman assertions 完成率：100%
- 發佈閘門完成率：8/9（88.9%）

## 已完成

- Postman Desktop 12.21.1 已安裝、啟動並開啟 `email-registration-login.postman_collection.json`。
- Newman 對 H2 測試 profile 執行 8 個實際 HTTP request，8/8 assertions 通過。
- Newman 對 Docker SQL Server 2022 執行相同 collection，8/8 requests、8/8 assertions 通過。
- 已驗證寄送註冊碼、取得測試碼、首次註冊、信箱登入、寄送重設碼、取得重設碼、更新密碼與新密碼登入。
- JSON 原始結果：`backend/build/newman-email-result.json`。

## 未完成閘門

- 環境未提供輪替後的 `EMAIL_USER` 與 `EMAIL_PASSWORD`，因此未執行真實 Gmail SMTP 寄送與收件內容確認。
- 未使用 Google Sheet 中已公開的舊憑證，也未將任何密碼或驗證碼寫入本報告。

## 執行命令

```text
./backend/gradlew -p backend build
SPRING_PROFILES_ACTIVE=test ./backend/gradlew -p backend bootRun
cd postman && npm run test:email
docker compose up -d
bash ./scripts/init-database.sh
EMAIL_CAPTURE_ENABLED=true ./backend/gradlew -p backend bootRun
cd postman && npm run test:email
```

結論：API 與資料庫自動測試 100% 正確；真實 SMTP 閘門尚缺輪替憑證，故不得回寫 Sheet 為開發完成。
