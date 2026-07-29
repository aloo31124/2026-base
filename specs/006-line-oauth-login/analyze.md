# Analyze：LINE OAuth 註冊登入

## Traceability

| Sheet 列 | MUST 功能 | FR | Tasks | Tests |
| --- | --- | --- | --- | --- |
| 25 | LINE OAuth 即時登入、不需密碼 | FR-001–006、009、014 | T007–T015 | JUnit、Postman、Cypress |
| 26 | 首次綁定並顯示 LINE 官方登入頁 | FR-001–006、012–014 | T004–T015 | JUnit、Postman、Cypress |
| 27 | 首次註冊寫入資料庫 | FR-007–009 | T001–T003、T010 | JUnit、Postman |
| 28 | 成功與失敗過程存入資料庫 | FR-010–011 | T002、T005、T009、T016 | JUnit、Postman |

## Risk Review

- CSRF / callback replay：state hash、唯一鍵、單次終態與逾時。
- Authorization code interception：PKCE S256。
- 偽造 ID token：LINE Verify ID token endpoint + channel ID + nonce。
- 敏感資料洩漏：token/code/secret 不落庫、不進 Log、不進報告。
- 首次註冊競態：LINE user ID unique constraint + transaction。
- 測試誤用 mock：正式預設 false，mock controller/bean 僅在 property=true 建立。

## Readiness

規格已涵蓋 DB、DAO、Service、BO、Controller、React page、Postman、Cypress 與 Sheet 回寫門檻，無 NEEDS CLARIFICATION。
