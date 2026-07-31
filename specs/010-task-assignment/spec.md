# Feature Specification：任務指派

**來源**：Google Sheet「任務指派」第 20–26 列，狀態皆為「預計開發」  
**建立日期**：2026-07-30

## 目錄

- [Clarifications](#clarifications) — 自動決策任務粒度、公司綁定與撤回語意
- [User Scenarios & Testing](#user-scenarios--testing) — 5 個獨立驗收故事
- [Functional Requirements](#functional-requirements) — 18 條 FR
- [Key Entities](#key-entities) — 主管員工綁定與任務
- [Assumptions](#assumptions) — MVP 邊界
- [Success Criteria](#success-criteria) — 6 項量測成果

## Clarifications

### Session 2026-07-30

- Q：一筆任務可指派幾人？ → A：一筆任務一位受派人；需要多人時建立多筆任務，保持 CRUD 與狀態清晰。
- Q：註冊後輸入公司名稱如何處理？ → A：只可精確比對既有公司主檔並建立自身綁定，不允許建立新公司。
- Q：撤回與刪除差異？ → A：撤回保留稽核資料並標記 `WITHDRAWN`；刪除僅允許尚未退回的本人建立任務。
- Q：誰可成為受派人？ → A：建立者本人、同公司主管，以及已綁定在該主管下且同公司的員工。
- Q：退回後是否可修改？ → A：退回保留原因；主管可修改後重新指派同一受派人，狀態回到 `ASSIGNED`。

## User Scenarios & Testing

### User Story 1 - 綁定公司與主管員工 (Priority: P1)

主管或員工登入後可輸入既有公司名稱完成自身公司綁定；主管可依員工信箱搜尋同公司員工並建立或取消主管員工綁定。

**Independent Test**：未綁公司者可依名稱綁定；主管可綁定同公司員工，跨公司與重複綁定被拒絕。

### User Story 2 - 建立與指派任務 (Priority: P1)

主管可設定名稱、內容、期限，並指派給自己、同公司其他主管或旗下員工。

**Independent Test**：合法受派人建立成功；跨公司與非旗下員工被拒絕。

### User Story 3 - 任務 CRUD、查詢與排序 (Priority: P1)

主管可查詢自己建立的任務，依名稱、員工帳號、指派日期範圍、期限日期範圍篩選並排序，亦可修改或刪除。

**Independent Test**：每個條件皆可縮小結果，排序方向正確，其他主管任務不可見。

### User Story 4 - 撤回與退回 (Priority: P1)

主管可撤回自己建立的任務；受派主管或員工可退回任務並填寫原因。

**Independent Test**：撤回與退回狀態、原因與權限皆正確，非本人操作被拒絕。

### User Story 5 - React 操作頁 (Priority: P1)

頁面沿用 `uiux/0.共用樣式` 的側欄、標籤、卡片、篩選列、表格與響應式流程，提供任務管理、我的任務及成員綁定。

**Independent Test**：Cypress 可完成公司綁定、員工綁定、任務新增查詢、退回與撤回流程。

### Edge Cases

- 公司不存在、使用者已綁公司或跨公司綁定時拒絕。
- 員工信箱忽略大小寫精確搜尋；找不到時回傳空集合。
- 截止日早於目前時間時拒絕新增或重新指派。
- 退回原因去除空白後不得為空，最長 500 字。
- 已撤回任務不得再退回；已退回任務不得撤回。
- 查詢日期起日不得晚於迄日；排序欄位只接受白名單。

## Functional Requirements

- **FR-001**：系統 MUST 允許已登入主管與員工依既有公司名稱綁定自身公司。
- **FR-002**：自身已有公司綁定時 MUST NOT 再次綁定。
- **FR-003**：主管 MUST 可依同公司員工信箱搜尋員工。
- **FR-004**：主管 MUST 可建立及取消主管員工綁定，且同一員工最多一位主管。
- **FR-005**：主管 MUST 可指派任務給自己、同公司主管或旗下同公司員工。
- **FR-006**：任務 MUST 保存名稱、內容、期限、建立者、受派人、指派時間與狀態。
- **FR-007**：任務名稱不得空白且最長 160 字；內容最長 4000 字。
- **FR-008**：主管 MUST 可修改自己建立的任務。
- **FR-009**：主管 MUST 可刪除自己建立且未退回的任務。
- **FR-010**：主管 MUST 可撤回自己建立且狀態為 `ASSIGNED` 的任務。
- **FR-011**：受派人 MUST 可退回狀態為 `ASSIGNED` 的任務並填寫原因。
- **FR-012**：任務狀態 MUST 包含 `ASSIGNED`、`RETURNED`、`WITHDRAWN`。
- **FR-013**：主管 MUST 可依任務名稱、受派人帳號、指派日期與期限日期範圍查詢。
- **FR-014**：任務列表 MUST 支援受控欄位升冪與降冪排序。
- **FR-015**：主管只能查閱與操作自己建立的任務；收件匣只能顯示自己收到的任務。
- **FR-016**：後端 MUST 依 DB 表、JPA DAO、Service、BO、REST Controller 分層。
- **FR-017**：前端 MUST 提供 React 對應頁面並符合 `uiux/` 操作與視覺慣例。
- **FR-018**：所有功能 MUST 具整合測試、Postman/Newman 測試與 Cypress E2E 證據。

## Key Entities

- **SupervisorEmployeeBinding**：主管與員工唯一綁定；員工唯一，保存建立時間。
- **AssignedTask**：任務名稱、內容、期限、建立者、受派人、指派時間、狀態、退回原因與退回時間。
- **CompanyMembership**：沿用既有公司成員綁定，作為同公司授權依據。

## Assumptions

- 僅處理 Google Sheet 第 20–26 列，不變更其他列的功能。
- 公司綁定為註冊後首次登入的補充流程；既有已綁定使用者直接進入任務頁。
- 任務不做附件、留言、多人共同受派或排程通知，避免超出 Sheet 要求。

## Success Criteria

- **SC-001**：主管可在 2 分鐘內完成員工搜尋、綁定及取消。
- **SC-002**：主管可在 1 分鐘內建立一筆含名稱、內容與期限的任務。
- **SC-003**：100% 跨公司或非所屬員工指派被拒絕。
- **SC-004**：100% 退回任務保存非空退回原因。
- **SC-005**：五類查詢或排序條件皆有自動化驗證。
- **SC-006**：後端建置、整合測試、Postman/Newman 與 Cypress 全數通過。
