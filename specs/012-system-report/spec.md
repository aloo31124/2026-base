# Feature Specification：系統報表

**來源**：Google Sheet「任務報表」第 13–14 列，狀態為「預計開發」  
**建立日期**：2026-08-02  
**Status**：Complete

## 目錄

- [Clarifications](#clarifications) — 統計歸屬、日期與空資料的自動決策
- [User Scenarios & Testing](#user-scenarios--testing) — 4 個可獨立驗收故事
  - [User Story 1 - 檢視跨公司任務趨勢 (Priority: P1)](#user-story-1---檢視跨公司任務趨勢-priority-p1) — 管理員查看全部公司趨勢
  - [User Story 2 - 依公司篩選趨勢 (Priority: P1)](#user-story-2---依公司篩選趨勢-priority-p1) — 切換公司範圍
  - [User Story 3 - 依時間範圍篩選 (Priority: P1)](#user-story-3---依時間範圍篩選-priority-p1) — 預設一年並可調整
  - [User Story 4 - 安全與例外狀態 (Priority: P2)](#user-story-4---安全與例外狀態-priority-p2) — 權限、空資料與錯誤回饋
  - [Edge Cases](#edge-cases) — 日期、公司與零資料邊界
- [Requirements](#requirements) — 15 條功能需求
  - [Functional Requirements](#functional-requirements) — 可測試的 MUST 規則
  - [Key Entities](#key-entities) — 報表唯讀資料模型
- [Assumptions](#assumptions) — MVP 範圍與相容前提
- [Success Criteria](#success-criteria) — 5 項量測成果

## Clarifications

### Session 2026-08-02

- **議題**：任務應歸屬哪一家公司。
  - **候選方案**：建立者公司、受派人公司、兩者都計入。
  - **採用方案**：以任務受派人在 `company_membership` 的公司歸屬計入一次。
  - **採用理由**：任務工作量由實際承接人所在公司負擔，且單一使用者只能綁定一家公司，可避免重複計數。
  - **影響章節**：FR-003、FR-006、Edge Cases。
- **議題**：「預設時間是一年內」的邊界。
  - **候選方案**：本曆年、最近 365 天、前 12 個完整月份。
  - **採用方案**：預設從今日往前推一年至今日，起迄日皆包含。
  - **採用理由**：符合一般「一年內」理解，且不因跨年而突然縮短資料區間。
  - **影響章節**：FR-007、FR-008、SC-001。
- **議題**：無任務日期是否顯示。
  - **候選方案**：略過、補零、只顯示週彙總。
  - **採用方案**：按日補零並以折線圖呈現。
  - **採用理由**：時間軸連續才可正確辨識任務量變化，也能明確表達無任務日。
  - **影響章節**：FR-009、FR-011、Edge Cases。

## User Scenarios & Testing

### User Story 1 - 檢視跨公司任務趨勢 (Priority: P1)

系統管理員可進入「系統報表」頁面，在「任務趨勢」標籤查看所有公司任務量的折線圖與合計數。

**Why this priority**：這是工作表兩項 MUST 功能的主要價值與管理入口。

**Independent Test**：以系統管理員登入後開啟頁面，預設即顯示全部公司的連續日趨勢、任務總數與公司數。

**Acceptance Scenarios**：

1. **Given** 多家公司具有任務，**When** 系統管理員開啟系統報表，**Then** 畫面顯示全部公司的任務折線趨勢與正確合計。
2. **Given** 使用者沒有系統管理員角色，**When** 嘗試存取頁面或 API，**Then** 系統拒絕並顯示模組專屬權限訊息。

### User Story 2 - 依公司篩選趨勢 (Priority: P1)

系統管理員可從公司篩選元件選擇全部公司或單一公司，重新取得該範圍任務趨勢。

**Why this priority**：跨公司總覽必須能下鑽至單一公司，才能支援管理判讀。

**Independent Test**：選擇特定公司後，合計與每日資料只包含該公司受派人的任務。

**Acceptance Scenarios**：

1. **Given** 公司甲與公司乙皆有任務，**When** 選擇公司甲並套用篩選，**Then** 只顯示公司甲資料。
2. **Given** 傳入不存在的公司，**When** 查詢趨勢，**Then** 系統回覆找不到公司，不回傳混合資料。

### User Story 3 - 依時間範圍篩選 (Priority: P1)

系統管理員可設定開始日與結束日；首次進入預設顯示最近一年，並可重新套用篩選。

**Why this priority**：明確期間是趨勢比較與查核的必要條件。

**Independent Test**：調整期間後，每個資料點皆落在含首尾的指定範圍，合計同步更新。

**Acceptance Scenarios**：

1. **Given** 首次開啟頁面，**When** 無自訂日期，**Then** 起日為今日往前一年、迄日為今日。
2. **Given** 起日晚於迄日，**When** 套用篩選，**Then** 系統拒絕並說明日期範圍錯誤。

### User Story 4 - 安全與例外狀態 (Priority: P2)

系統管理員在載入中、無資料或服務失敗時可獲得清楚且可操作的狀態回饋。

**Why this priority**：報表需避免空白畫面或誤解零資料為故障。

**Independent Test**：無任務期間顯示零值與空資料提示；API 失敗顯示錯誤訊息且保留篩選條件。

**Acceptance Scenarios**：

1. **Given** 所選期間無任務，**When** 查詢完成，**Then** 顯示總數 0、連續零值資料與空資料提示。
2. **Given** 查詢失敗，**When** 錯誤回傳，**Then** 顯示錯誤訊息且可再次套用篩選。

### Edge Cases

- 任務受派人沒有公司綁定時不計入公司趨勢，避免錯誤歸屬。
- 起日與迄日相同時回傳單日資料點，且含當日全部任務。
- 查詢期間最多 366 天；超出時拒絕，避免折線圖與查詢負載無界成長。
- 公司存在但期間內沒有任務時仍回傳該公司名稱、零總數與連續零值資料。
- 使用者時區採專案設定的 `Asia/Taipei`，日期切分不以 UTC 日界線呈現。

## Requirements

### Functional Requirements

- **FR-001**：系統 MUST 僅允許 `SYSTEM_ADMIN` 角色存取系統報表頁面與 API。
- **FR-002**：系統報表頁面 MUST 提供「任務趨勢」標籤並作為預設標籤。
- **FR-003**：任務趨勢 MUST 顯示所有公司受派任務的按日任務量折線圖。
- **FR-004**：頁面 MUST 顯示所選期間的任務總數、包含公司數與日期範圍摘要。
- **FR-005**：公司篩選 MUST 提供「全部公司」及所有既有公司的選項。
- **FR-006**：選擇單一公司後 MUST 只統計受派人綁定至該公司的任務。
- **FR-007**：首次進入 MUST 預設查詢今日往前一年至今日，且含首尾日期。
- **FR-008**：日期篩選 MUST 允許含首尾的 1 至 366 天範圍，起日不得晚於迄日。
- **FR-009**：回應 MUST 對查詢期間每一日提供資料點，無任務日數量為 0。
- **FR-010**：REST 回應 MUST 使用既有 `{ success, message, data, timestamp }` 格式。
- **FR-011**：折線圖 MUST 有可存取名稱、座標資訊與文字摘要，並在零資料時顯示明確提示。
- **FR-012**：載入、成功、空資料與錯誤狀態 MUST 可區分，錯誤後可重新查詢。
- **FR-013**：後端 MUST 依相關 DB 表、JPA DAO、Service、BO/DTO 與 REST Controller 分層。
- **FR-014**：前端 MUST 以 React 頁面實作，沿用 `uiux/0.共用樣式` 的頁首、標籤、卡片與篩選操作。
- **FR-015**：功能 MUST 具 JUnit 整合測試、真實 HTTP Postman/Newman 與 Cypress E2E 證據及完成率報告。

### Key Entities

- **Company**：公司篩選選項與報表歸屬主檔。
- **CompanyMembership**：使用者至公司的唯一歸屬；用於把任務受派人映射至公司。
- **AssignedTask**：以 `assignedAt` 作為任務量趨勢日期來源。
- **TaskTrendPoint**：唯讀回應模型，包含日期與任務數量，不建立持久化表。
- **TaskTrendReport**：唯讀回應模型，包含篩選條件、任務總數、公司數及連續資料點。

## Assumptions

- 本次只處理「任務報表」第 13–14 列的「系統報表」子模組；「主管報表」狀態空白，不在範圍。
- 不新增報表快照或彙總表，直接查詢既有 `company`、`company_membership`、`assigned_task`，確保資料即時且無同步風險。
- 任務趨勢以 `assignedAt` 表示新增任務量，不依後續狀態變更重分日期。
- 專案既有 JWT、標準 API response 與錯誤處理機制持續沿用。

## Success Criteria

- **SC-001**：系統管理員首次進入後，在 3 秒內看到最近一年的趨勢結果或明確空資料狀態。
- **SC-002**：全部公司與單一公司篩選的任務計數正確率為 100%。
- **SC-003**：指定期間的日期資料點完整率為 100%，無任務日皆明確為 0。
- **SC-004**：非系統管理員的頁面與 API 存取拒絕率為 100%。
- **SC-005**：後端建置與整合測試、前端建置、Postman/Newman 與 Cypress 全數通過，報告完成率為 100%。
