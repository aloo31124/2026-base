# Feature Specification: 我的報表

**Feature Branch**: `主管員工公司綁定`
**Created**: 2026-08-02
**Status**: Ready
**Input**: Google Sheet「任務報表」第 23–25 列，僅處理狀態為「預計開發」的「任務報表／我的報表」。

## 目錄

- [User Scenarios & Testing](#user-scenarios--testing) — 3 條可獨立驗收的員工報表旅程
  - [User Story 1 - 檢視個人任務總覽 (Priority: P1)](#user-story-1---檢視個人任務總覽-priority-p1) — 對應 Sheet 第 23 列
  - [User Story 2 - 檢視任務趨勢 (Priority: P1)](#user-story-2---檢視任務趨勢-priority-p1) — 對應 Sheet 第 24 列
  - [User Story 3 - 檢視任務狀態比例 (Priority: P1)](#user-story-3---檢視任務狀態比例-priority-p1) — 對應 Sheet 第 25 列
  - [Edge Cases](#edge-cases) — 空資料、非法條件與資料隔離
- [Requirements](#requirements) — 11 條可測試功能需求
  - [Functional Requirements](#functional-requirements) — API、圖表、篩選、授權與狀態
  - [Key Entities](#key-entities) — 沿用使用者與指派任務
- [Clarifications](#clarifications) — 3 項自動決策紀錄
  - [自動決策紀錄](#自動決策紀錄) — 儲存、資料邊界與執行者篩選
- [Success Criteria](#success-criteria) — 5 項量化完成標準
  - [Measurable Outcomes](#measurable-outcomes) — 正確率、完成率與回應時間
- [Assumptions](#assumptions) — 既有任務與身分機制假設
- [Out of Scope](#out-of-scope) — 本子模組不處理的事項

## User Scenarios & Testing

### User Story 1 - 檢視個人任務總覽 (Priority: P1)

已登入員工可進入「我的報表」頁面，看到自己受派任務的總數、日期範圍與目前篩選範圍，不會看到其他員工資料。

**Why this priority**: 個人任務資料隔離與總覽是兩種圖表的共同基礎，也是 Sheet 第 23 列的 MUST 功能。

**Independent Test**: 建立兩位員工的受派任務後，以其中一位登入查詢，回應總數只包含該員工任務，頁面摘要同步呈現相同數值。

**Acceptance Scenarios**:

1. **Given** 員工已有受派任務，**When** 開啟我的報表，**Then** 僅顯示自己任務的總數與預設最近一年範圍。
2. **Given** 另一位員工也有任務，**When** 目前員工查詢，**Then** 另一位員工的任務不會計入或出現在回應。
3. **Given** 未登入請求，**When** 呼叫我的報表 API，**Then** 回傳統一 401 response。

---

### User Story 2 - 檢視任務趨勢 (Priority: P1)

員工可在「任務趨勢」標籤查看自己任務量的每日折線圖，並用執行者、執行狀態及日期範圍篩選，預設為最近一年。

**Why this priority**: 對應 Sheet 第 24 列，讓員工理解個人工作量在時間上的分布。

**Independent Test**: 以指定日期與工作狀態查詢，折線圖必須補齊範圍內每日資料點並只統計符合條件的自己任務。

**Acceptance Scenarios**:

1. **Given** 員工在不同日期有任務，**When** 套用日期條件，**Then** 折線圖依台北日期呈現連續每日資料點。
2. **Given** 員工有不同工作狀態任務，**When** 選擇「已完成」，**Then** 僅已完成任務計入趨勢與摘要。
3. **Given** 使用預設查詢，**When** 頁面初始化，**Then** 起日為迄日往前一年且範圍最多 366 天。

---

### User Story 3 - 檢視任務狀態比例 (Priority: P1)

員工可在「任務狀態比」標籤查看自己任務依待處理、進行中、已完成分類的圓餅圖與百分比，並沿用相同篩選條件。

**Why this priority**: 對應 Sheet 第 25 列，提供個人工作狀態分布的即時摘要。

**Independent Test**: 建立三種工作狀態資料後查詢，狀態數量須正確、百分比合計 100%；空資料時三項均為 0%。

**Acceptance Scenarios**:

1. **Given** 員工具有三種狀態任務，**When** 切換至任務狀態比，**Then** 圓餅圖與文字圖例呈現正確數量及一位小數百分比。
2. **Given** 篩選後沒有任務，**When** 查看任務狀態比，**Then** 頁面顯示空狀態且三種比例均為 0%。
3. **Given** 已在趨勢標籤套用篩選，**When** 切換狀態比標籤，**Then** 相同條件持續套用，不額外發散資料範圍。

### Edge Cases

- 員工沒有任何受派任務時，回應必須包含連續零值趨勢與三個零值狀態桶。
- 起日晚於迄日、範圍超過 366 天或未知工作狀態時，API 必須回傳明確 400 錯誤。
- 執行者識別不是登入員工本人時，API 必須拒絕請求，避免 UUID 探測或跨員工查詢。
- 既有任務的 `work_status` 為空時，報表視為 `PENDING`，維持舊資料相容性。
- 時間邊界以 `Asia/Taipei` 含起日、含迄日計算，避免 UTC 跨日誤差。

## Requirements

### Functional Requirements

- **FR-001**: 系統 MUST 提供登入員工專用「我的報表」頁面，顯示自己受派任務資料，不要求主管或系統管理員角色。
- **FR-002**: 系統 MUST 透過既有 JPA 指派任務資料，以登入帳號對應的受派人作為不可覆寫的資料邊界。
- **FR-003**: 系統 MUST 提供報表篩選選項 API，包含登入員工本人、三種工作狀態與最近一年預設日期。
- **FR-004**: 系統 MUST 提供綜合報表 API，回傳篩選條件、個人任務總數、連續每日趨勢與三種狀態比例。
- **FR-005**: 系統 MUST 支援執行者、工作狀態、含起日及含迄日篩選；執行者只能是登入員工本人或未指定。
- **FR-006**: 系統 MUST 將預設日期設為台北當日往前一年至當日，且單次查詢不得超過 366 天。
- **FR-007**: 趨勢折線圖 MUST 補齊選定日期範圍內所有無資料日期並以 0 呈現。
- **FR-008**: 狀態圓餅圖 MUST 顯示待處理、進行中、已完成三種數量及一位小數百分比；有資料時合計為 100%。
- **FR-009**: 前端 MUST 依 `uiux/0.共用樣式` 與既有報表操作流程，提供載入、錯誤、空資料、重新整理、篩選與標籤切換狀態。
- **FR-010**: Controller MUST 僅處理 HTTP 參數與標準 response，業務規則置於 Service，資料存取置於 DAO。
- **FR-011**: 每一個 Sheet MUST 功能 MUST 納入後端整合測試、Postman/Newman 驗證、Cypress E2E、task 與 requirements checklist 對照。

### Key Entities

- **UserAccount**: 登入員工；以帳號定位不可跨越的報表資料邊界，含識別、姓名與帳號。
- **AssignedTask**: 既有 `assigned_task` 表中的受派任務，提供受派人、指派時間與工作狀態；本功能不新增重複報表表。
- **MyReportFilter**: 非持久化查詢值物件，包含執行者、工作狀態、含起日、含迄日。
- **MyReport**: 非持久化綜合結果，包含個人任務總數、趨勢資料點與狀態資料桶。

## Clarifications

### 自動決策紀錄

- **議題**: 報表是否需要新增 DB 表
  - **候選方案**: 新增報表快照表 / 即時計算既有 `assigned_task` / 僅前端聚合
  - **採用方案**: 即時計算既有 `assigned_task`
  - **採用理由**: 報表資料量屬 MVP 範圍，沿用既有資料來源可避免同步與一致性風險，且仍完整經過 JPA DAO。
  - **影響章節**: FR-002、Key Entities、data-model、tasks
- **議題**: 員工報表的資料權限範圍
  - **候選方案**: 可查看全公司 / 可指定任意員工 / 僅登入員工本人
  - **採用方案**: 僅登入員工本人
  - **採用理由**: 「我的報表」明確要求自己的任務，最小權限可避免個資與工作資料外洩。
  - **影響章節**: US1、FR-001、FR-002、FR-005
- **議題**: Sheet 要求執行者篩選但報表僅能看本人
  - **候選方案**: 移除執行者欄 / 提供本人單一選項 / 開放同公司員工
  - **採用方案**: 提供本人單一選項與「全部執行者」語意
  - **採用理由**: 保留 Sheet 指定的操作元件並維持資料隔離；任何非本人識別由後端拒絕。
  - **影響章節**: US2、FR-003、FR-005、前端與 API contract

## Success Criteria

### Measurable Outcomes

- **SC-001**: 3 個 Sheet MUST 功能各有至少一個後端整合斷言與一個 Cypress E2E 斷言，驗收完成率為 100%。
- **SC-002**: 建立其他員工任務的隔離測試中，登入員工的報表統計錯誤納入數為 0 筆。
- **SC-003**: 日期範圍 366 天內的查詢可在測試環境 2 秒內完成並回傳完整連續資料點。
- **SC-004**: 有資料時狀態比例合計精確為 100.0%，空資料時三個狀態皆為 0.0%。
- **SC-005**: 後端 Gradle 測試、前端 production build、Newman collection 與專屬 Cypress spec 全數通過。

## Assumptions

- 登入 JWT 的 principal 名稱可用 `UserAccountDao.findByUsername` 找到使用者。
- `AssignedTask.assignee` 是「自己任務」的唯一歸屬依據；任務建立者不影響員工可見範圍。
- 既有三種 `WorkStatus` 為 `PENDING`、`IN_PROGRESS`、`COMPLETED`。
- 目前資料量可由限定 366 天範圍的 JPA 查詢即時聚合，尚不需要報表快取或排程快照。

## Out of Scope

- 不提供主管或系統管理員跨員工檢視；其報表維持既有頁面與 API。
- 不新增任務、修改進度或匯出檔案。
- 不建立額外報表資料表、排程或分析倉儲。
- 不修改 Google Sheet「任務報表」之外的任何標籤。
