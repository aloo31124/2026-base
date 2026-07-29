# Feature Specification：LINE OAuth 註冊登入

**來源**：Google Sheet「註冊登入驗證」第 25–28 列
**子模組**：LINE OAuth 註冊登入

## User Stories

### User Story 1 - 以 LINE 官方頁面登入 (Priority: P1)

使用者在登入頁點選「使用 LINE 登入」後，瀏覽器前往 LINE Login v2.1 官方授權頁；驗證成功後回到 AgentFlow 並取得既有 JWT session，不需輸入 AgentFlow 密碼。

### User Story 2 - 首次 LINE 登入自動註冊 (Priority: P1)

通過 LINE 驗證且尚未綁定的 LINE 使用者，自動建立 AgentFlow 使用者、LINE 綁定資料及 EMPLOYEE 角色；同一 LINE 使用者再次登入不得重複建立帳號。

### User Story 3 - 留存 OAuth 稽核結果 (Priority: P1)

每次開始 LINE 登入都建立稽核紀錄；成功、拒絕、state 無效、逾時或 LINE API 失敗時，都必須更新結果與可追蹤的失敗代碼，不得保存 access token、ID token、channel secret 或 authorization code。

### User Story 4 - 顯示可操作的錯誤狀態 (Priority: P2)

LINE 驗證失敗或使用者取消時，前端 callback 頁回到登入畫面並顯示明確訊息；已成功的 callback state 不可重播。

## Functional Requirements

- **FR-001**：登入頁 MUST 提供「使用 LINE 登入」按鈕並導向 LINE Login v2.1 授權頁。
- **FR-002**：授權請求 MUST 使用 `response_type=code`、固定 callback URL、`profile openid email` scope、不可預測的 `state` 與 `nonce`。
- **FR-003**：授權流程 MUST 使用 PKCE S256；code verifier 僅在流程未完成期間保存，完成後清除。
- **FR-004**：callback MUST 比對單次使用的 state 並拒絕未知、已使用或逾時 state。
- **FR-005**：後端 MUST 以 authorization code 與 code verifier 呼叫 LINE token endpoint。
- **FR-006**：後端 MUST 使用 LINE Verify ID token endpoint 驗證 ID token、channel ID 與 nonce，並以已驗證的 `sub` 作為 LINE 使用者識別。
- **FR-007**：首次登入 MUST 在同一 transaction 建立 `app_user`、`line_oauth_account` 與 EMPLOYEE `user_role`。
- **FR-008**：同一 LINE user ID 再次登入 MUST 重用原帳號，不得建立重複使用者或角色。
- **FR-009**：成功登入 MUST 簽發與帳密登入相同格式及到期規則的 AgentFlow JWT。
- **FR-010**：每次授權開始 MUST 建立 `line_oauth_attempt`；成功或失敗 MUST 寫入終態、完成時間及非敏感結果碼。
- **FR-011**：Log 與資料庫 MUST NOT 保存 channel secret、access token、ID token、authorization code、原始 state 或原始 LINE API response。
- **FR-012**：channel ID、channel secret 與 callback URL MUST 由環境變數提供；正式模式缺少設定時回傳明確服務設定錯誤。
- **FR-013**：自動測試 MUST 使用僅在 `LINE_OAUTH_MOCK_ENABLED=true` 時啟用的本機 mock provider；正式預設 MUST 關閉。
- **FR-014**：React MUST 提供 callback page，將 query 中的 code/state/error 以 POST 傳給後端，成功後保存 session 並導向授權頁面。
- **FR-015**：所有 Sheet 第 25–28 列 MUST 功能均須有可追溯 task、checklist、Postman 與 Cypress 驗證。

## Key Entities

- **LineOAuthAccount**：AgentFlow 使用者與 LINE user ID 的一對一綁定。
- **LineOAuthAttempt**：state hash、PKCE/nonce 暫存、逾時、成功或失敗結果及關聯使用者。
- **UserAccount / UserRole**：首次 LINE 註冊建立的本地帳號與 EMPLOYEE 角色。

## Edge Cases

- state 不存在、已使用、過期或遭重播時回 401，且不交換 token。
- 使用者拒絕授權時寫入 `DENIED` 後回到登入頁。
- LINE token 或 ID token 驗證失敗時寫入 `PROVIDER_ERROR`，API 不回傳供應者敏感內容。
- LINE 未提供 email 時以不可登入的系統保留 email 建立帳號；不得偽造可收信地址。
- 同一 LINE user ID 的並行首次登入由唯一鍵與 transaction 防止重複綁定。

## Success Criteria

- **SC-001**：Sheet 第 25–28 列功能、tasks 與 checklist 完成率 100%。
- **SC-002**：首次登入建立本地使用者、LINE 綁定與 EMPLOYEE 角色正確率 100%。
- **SC-003**：再次登入不重複建立帳號正確率 100%。
- **SC-004**：成功、拒絕與 provider 失敗稽核落庫正確率 100%。
- **SC-005**：Gradle build、Postman/Newman 與 Cypress 通過率 100%。

## Assumptions

- Google Sheet 提供的 callback URL 是前端 SPA 路由 `/api/auth/line/callback`；React 收到 query 後再呼叫後端 callback API。
- LINE channel 必須在 LINE Developers Console 設定相同 callback URL。
- 自動測試不使用真實 LINE 帳號或真實密鑰。
