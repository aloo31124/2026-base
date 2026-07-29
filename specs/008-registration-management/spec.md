# Feature Specification：註冊登入管理

**來源**：Google Sheet「註冊登入驗證」第 18–22 列  
**狀態範圍**：僅處理「預計開發」

## 目錄

- [Clarifications](#clarifications) — 自動釐清密碼政策、稽核紀錄與權限訊息
- [User Scenarios & Testing](#user-scenarios--testing) — 4 個可獨立驗收的 User Story
- [Functional Requirements](#functional-requirements) — 12 條 FR
- [Key Entities](#key-entities) — 密碼政策與註冊紀錄
- [Edge Cases](#edge-cases) — 權限、空資料與政策變更
- [Assumptions](#assumptions) — 最小可行範圍決策
- [Success Criteria](#success-criteria) — 5 項可量測成果

## Clarifications

### Session 2026-07-29

- Q：密碼長度與複雜度如何設定？ → A：長度 8–72，英文字母與數字可分別啟閉。
- Q：密碼政策套用在哪些流程？ → A：信箱註冊與忘記密碼的新密碼。
- Q：LINE 與信箱註冊紀錄如何定義？ → A：只記錄首次成功建帳，既有帳號登入不重複記錄。
- Q：無權限訊息中的模組名稱為何？ → A：統一使用「註冊登入管理」。

## User Scenarios & Testing

### User Story 1 - 管理密碼政策 (Priority: P1)

系統管理員可在後台「註冊登入管理」頁面讀取及更新最小密碼長度、是否要求英文字母、是否要求數字。

**Independent Test**：管理員更新政策後重新讀取，值一致；不合法長度被拒絕。

### User Story 2 - 套用密碼政策 (Priority: P1)

新使用者進行信箱註冊或忘記密碼時，輸入的密碼必須符合目前政策。

**Independent Test**：不符合政策的密碼得到明確說明，符合者可完成既有流程。

### User Story 3 - 檢視註冊紀錄 (Priority: P1)

系統管理員可檢視 LINE 與信箱首次註冊紀錄、識別資料、時間與成功狀態。

**Independent Test**：分別完成信箱與 LINE 首次註冊後，管理頁可看到兩種方式且顯示成功。

### User Story 4 - 前後端雙層權限 (Priority: P1)

非系統管理員不得呼叫管理 API，亦不得直接進入管理頁面。

**Independent Test**：一般使用者呼叫 API 得到 403 與指定訊息；進入頁面時被導向無權限頁並顯示指定訊息。

## Functional Requirements

- **FR-001**：系統 MUST 保存一份有效密碼政策，最小長度範圍為 8–72。
- **FR-002**：系統 MUST 允許系統管理員獨立設定是否要求至少一個英文字母。
- **FR-003**：系統 MUST 允許系統管理員獨立設定是否要求至少一個數字。
- **FR-004**：系統 MUST 對信箱註冊與忘記密碼套用目前密碼政策。
- **FR-005**：密碼不符合政策時，系統 MUST 拒絕請求並說明缺少的條件。
- **FR-006**：系統 MUST 保存信箱與 LINE 首次成功建帳的註冊紀錄。
- **FR-007**：註冊紀錄 MUST 包含註冊方式、識別資料、成功狀態與完成時間。
- **FR-008**：既有 LINE 帳號再次登入 MUST NOT 建立重複註冊紀錄。
- **FR-009**：管理 API MUST 僅允許 `SYSTEM_ADMIN`。
- **FR-010**：無管理 API 權限時 MUST 回覆「[註冊登入管理] [api] 無系統管理員權限。」。
- **FR-011**：前端管理路由 MUST 僅允許 `SYSTEM_ADMIN`。
- **FR-012**：無管理頁權限時 MUST 顯示「[註冊登入管理] [頁面] 無系統管理員權限。」。

## Key Entities

- **PasswordPolicy**：最小長度、要求英文字母、要求數字、建立與更新時間。
- **RegistrationRecord**：使用者、註冊方式、識別資料、成功狀態、完成時間。

## Edge Cases

- 尚無註冊紀錄時顯示空狀態，不視為錯誤。
- 政策更新只影響之後設定的新密碼，不強迫既有密碼立即重設。
- 密碼同時缺少多個條件時，一次回覆全部缺少條件。
- 不合法的最小長度不得寫入資料庫。

## Assumptions

- 本次只涵蓋 Google Sheet 指定的「註冊登入管理」子模組。
- 註冊紀錄聚焦首次成功建帳；失敗登入或取消 OAuth 已由既有流程紀錄，不擴張為新的登入稽核功能。
- 管理頁沿用 `uiux/0.共用樣式` 與 `uiux/1.1.使用者分權` 的卡片、表格、標籤與側欄操作。

## Success Criteria

- **SC-001**：管理員能在 1 分鐘內完成政策讀取、修改與確認。
- **SC-002**：100% 不符合已啟用條件的信箱新密碼會被拒絕。
- **SC-003**：完成 LINE 或信箱首次註冊後，紀錄於 2 秒內可查詢。
- **SC-004**：管理 API 與頁面的非管理員拒絕案例通過率為 100%。
- **SC-005**：後端整合測試、Postman collection、前端建置與 Cypress 規格全部通過。
