# LINE OAuth Postman 測試報告

## 結論

- 測試日期：2026-07-28（Asia/Taipei）
- 子模組：註冊登入驗證 / LINE OAuth 註冊登入
- Collection：`postman/line-oauth.postman_collection.json`
- 執行方式：Postman collection 透過 Newman 6.2.2 實際呼叫本機 Spring Boot API
- 環境：Spring profile `h2`、`LINE_OAUTH_MOCK_ENABLED=true`
- Request：12 / 12 通過
- Assertion：15 / 15 通過
- 失敗：0
- 完成率：**100%**
- 原始機器結果：`backend/build/newman-line-oauth-result.json`

## API 與 Response 驗證

| # | API 情境 | 預期 | 結果 |
| --- | --- | --- | --- |
| 1 | `GET /api/auth/line/authorize` | 200、授權 URL、逾時、state，不洩漏 verifier | 通過 |
| 2 | `POST /api/auth/line/callback` 首次登入 | 200、Bearer JWT、EMPLOYEE、LINE 顯示名稱 | 通過 |
| 3 | 查詢成功稽核 | SUCCESS / SUCCESS / completed | 通過 |
| 4 | 重播 callback | 401、state 已使用 | 通過 |
| 5 | 建立再次登入流程 | 200、新 state | 通過 |
| 6 | 同一 LINE 使用者再次登入 | 200、重用相同本地 username | 通過 |
| 7 | 建立 provider 失敗流程 | 200、新 state | 通過 |
| 8 | provider token/verify 失敗 | 502、安全錯誤，不回顯 authorization code | 通過 |
| 9 | 查詢失敗稽核 | FAILED / PROVIDER_ERROR / completed | 通過 |
| 10 | 建立取消授權流程 | 200、新 state | 通過 |
| 11 | 使用者取消授權 | 400、明確取消訊息 | 通過 |
| 12 | 查詢取消稽核 | DENIED / ACCESS_DENIED / completed | 通過 |

## Sheet MUST Traceability

| Sheet 列 | MUST 功能 | Postman 證據 | 完成率 |
| --- | --- | --- | --- |
| 25 | LINE OAuth 驗證後即時登入、不需本地密碼 | #1–2，取得 JWT session | 100% |
| 26 | 首次綁定與 LINE 登入流程 | #1–2、#5–6，首次與再次登入 | 100% |
| 27 | 首次註冊資料入庫 | #2、#6，相同 LINE 身分重用本地帳號 | 100% |
| 28 | 成功與失敗過程入庫 | #3、#9、#12 | 100% |

## 補充驗證

- `./backend/gradlew build`：成功。
- JUnit/MockMvc：7 個後端測試全數通過，其中 LINE OAuth 3 個整合測試涵蓋首次/再次登入、provider 失敗、取消與重播。
- 真實 LINE channel secret、access token、ID token、authorization code 與原始 state 均未寫入本報告。
