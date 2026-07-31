# Feature Specification：我的任務

**來源**：Google Sheet「任務指派」第 29–39 列，狀態皆為「預計開發」  
**建立日期**：2026-07-31

## 目錄

- [Clarifications](#clarifications) — 自動決策狀態、附件與流程語意
- [User Scenarios & Testing](#user-scenarios--testing) — 5 個可獨立驗收故事
- [Functional Requirements](#functional-requirements) — 17 條 FR
- [Key Entities](#key-entities) — 任務進度與附件
- [Assumptions](#assumptions) — MVP 安全邊界
- [Success Criteria](#success-criteria) — 5 項量測成果

## Clarifications

### Session 2026-07-31

- 工作狀態採「待處理、進行中、已完成」三態，與指派生命週期分離，避免破壞既有撤回／退回語意。
- 附件以資料庫保存檔名、媒體類型、大小與內容；單檔上限 10 MB，只允許圖片、影片與一般附件 MIME。
- 「提交審核」只要求工作狀態為已完成，不要求進度 100%；提交後交回原指派者，包含自己指派給自己的任務。
- 「退回」沿用既有受派人退回指派者流程；「申請延期」保存原因並交由指派者後續審核，本 feature 不擴張主管審核 UI。

## User Scenarios & Testing

### User Story 1 - 查詢與排序我的任務 (Priority: P1)

員工可依任務名稱、指派日期範圍、期限日期範圍查詢收到的任務並排序。

**Independent Test**：各條件能縮小結果，排序方向正確，且不顯示他人的任務。

### User Story 2 - 編輯工作進度 (Priority: P1)

員工可從列表進入編輯頁，更新工作狀態、進度內容及 10% 間距的視覺化進度。

**Independent Test**：更新後重讀仍保留狀態、內容與進度。

### User Story 3 - 上傳任務附件 (Priority: P1)

員工可上傳圖片、一般附件與影片並在任務中查看附件資訊。

**Independent Test**：合法檔案可保存；過大或空白檔案被拒絕。

### User Story 4 - 提交審核與退回 (Priority: P1)

員工可在列表或編輯頁提交審核或退回；未完成時提交會顯示提醒。

**Independent Test**：僅已完成狀態可提交，進度可小於 100%；退回交回指派者。

### User Story 5 - 申請延期 (Priority: P1)

員工可填寫非空原因申請延期，供指派者審核。

**Independent Test**：原因與申請時間被保存，空白原因被拒絕。

## Functional Requirements

- **FR-001**：員工 MUST 只能查閱自己收到的任務。
- **FR-002**：列表 MUST 支援任務名稱、指派日期範圍與期限日期範圍查詢。
- **FR-003**：列表 MUST 支援受控欄位升冪與降冪排序。
- **FR-004**：每筆列表資料 MUST 提供提交審核、退回、延期與編輯操作。
- **FR-005**：編輯操作 MUST 開啟對應任務的編輯頁。
- **FR-006**：工作狀態 MUST 僅包含待處理、進行中、已完成。
- **FR-007**：員工 MUST 可填寫最長 4000 字的工作進度內容。
- **FR-008**：進度 MUST 只允許 10 至 100 的 10% 間距值。
- **FR-009**：員工 MUST 可上傳圖片、附件與影片，單檔上限 10 MB。
- **FR-010**：附件 MUST 保存檔名、媒體類型、大小與內容。
- **FR-011**：提交審核 MUST 驗證工作狀態為已完成。
- **FR-012**：提交審核 MUST NOT 強制進度為 100%。
- **FR-013**：提交審核 MUST 將工作交付原指派者，包含自己指派給自己的任務。
- **FR-014**：受派人 MUST 可退回任務給指派者。
- **FR-015**：受派人 MUST 可填寫非空、最長 500 字原因申請延期。
- **FR-016**：後端 MUST 依 DB 表、JPA DAO、Service、BO 與 REST Controller 分層。
- **FR-017**：功能 MUST 具整合、Postman/Newman 與 Cypress 測試證據。

## Key Entities

- **AssignedTask**：新增工作狀態、進度內容、進度百分比、提交與延期申請資訊。
- **TaskAttachment**：所屬任務、上傳者、檔名、媒體類型、大小與二進位內容。

## Assumptions

- 僅實作第 29–39 列，不處理工作表下方尚無「預計開發」狀態的子模組。
- 既有 `ASSIGNED/RETURNED/WITHDRAWN` 完整保留；提交與延期以時間及原因欄位表達，避免破壞既有狀態契約。
- 檔案儲存採資料庫 MVP，未新增物件儲存服務。

## Success Criteria

- **SC-001**：員工可在 1 分鐘內查到並開啟指定任務。
- **SC-002**：狀態、內容與進度更新成功率為 100%。
- **SC-003**：未完成任務提交審核的攔截率為 100%。
- **SC-004**：跨使用者任務操作的拒絕率為 100%。
- **SC-005**：建置、整合測試、Postman/Newman 與 Cypress 全數通過。
