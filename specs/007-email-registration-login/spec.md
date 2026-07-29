# Feature Specification：信箱註冊登入

**來源**：Google Sheet「註冊登入驗證」第 22–26 列
**子模組**：信箱註冊登入

## User Stories

1. 使用者可取得一次性信箱驗證碼，驗證後首次建立帳號並登入。
2. 信箱帳號以信箱與密碼登入，沿用既有 JWT session。
3. 使用者忘記密碼時可經信箱驗證後設定新密碼。
4. 寄信、驗證、成功與失敗均可追蹤，且不保存明文驗證碼或 SMTP 憑證。

## Functional Requirements

- **FR-001（Sheet 22）**：註冊前 MUST 寄送並驗證一次性信箱驗證碼。
- **FR-002（Sheet 23）**：正式環境 MUST 由環境變數設定 SMTP，並可實際寄出驗證信。
- **FR-003（Sheet 24）**：首次驗證成功 MUST 在同一交易建立 app_user 與 EMPLOYEE user_role。
- **FR-004（Sheet 25）**：信箱 MUST 作為帳號，並可使用密碼登入。
- **FR-005（Sheet 26）**：忘記密碼 MUST 經一次性信箱驗證後才能更新密碼。
- **FR-006**：驗證碼為六位數、十分鐘到期、最多錯誤五次、六十秒內不得重寄、單次使用。
- **FR-007**：所有新密碼 MUST 符合 password_policy；既有密碼不立即失效。
- **FR-008**：自動測試郵件攔截器 MUST 僅在明確設定啟用時存在。
- **FR-009**：密碼、驗證碼、SMTP secret MUST NOT 出現在 Log、資料庫或測試報告。

## Success Criteria

- Sheet 第 22–26 列、tasks 與 checklist 完成率 100%。
- Gradle build、Postman/Newman、Cypress 通過率 100%。
