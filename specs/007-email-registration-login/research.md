# Research：信箱註冊登入

- 採 Spring Mail SMTP STARTTLS，正式預設 Gmail SMTP，可由環境變數覆寫。
- 驗證碼採 SecureRandom 六位數與 BCrypt，避免資料庫外洩後直接取得明文。
- Newman/Cypress 使用 property-gated 記憶體郵件攔截器，不依賴真實收件匣。
- 信箱作為既有 username 欄位，欄位擴至 160 字元以維持登入 API 相容。
