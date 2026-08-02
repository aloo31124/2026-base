# Requirements Quality Checklist: 我的報表

**Purpose**: 驗證 Google Sheet 第 23–25 列、資料隔離、篩選、圖表與測試需求是否完整、清楚、一致且可量測。
**Created**: 2026-08-02
**Feature**: [spec.md](../spec.md)

## 目錄

- [Google Sheet MUST 覆蓋](#google-sheet-must-覆蓋) — 3 項
- [Requirement Completeness](#requirement-completeness) — 5 項
- [Requirement Clarity](#requirement-clarity) — 4 項
- [Requirement Consistency](#requirement-consistency) — 3 項
- [Acceptance Criteria Quality](#acceptance-criteria-quality) — 4 項
- [Scenario & Edge Case Coverage](#scenario--edge-case-coverage) — 5 項
- [Dependencies & Assumptions](#dependencies--assumptions) — 3 項
- [Notes](#notes) — 驗證結論

## Google Sheet MUST 覆蓋

- [x] CHK001 是否明確定義員工在「我的報表」檢視自己所有任務的總覽與資料隔離？ [Completeness, Spec §US1, §FR-001–002, Sheet Row 23]
- [x] CHK002 是否明確定義「任務趨勢」折線圖、執行者／狀態／日期篩選與最近一年預設？ [Completeness, Spec §US2, §FR-003–007, Sheet Row 24]
- [x] CHK003 是否明確定義「任務狀態比」圓餅圖、三種狀態、比例精度與共用篩選？ [Completeness, Spec §US3, §FR-005–008, Sheet Row 25]

## Requirement Completeness

- [x] CHK004 是否記載完整 DB 資料來源、JPA DAO、Service、BO/DTO、Controller、React Page 與測試層級？ [Completeness, Spec §FR-002, §FR-010–011, Plan]
- [x] CHK005 是否定義 filters 與 report 兩種 API 回應所需欄位及標準 response？ [Completeness, Spec §FR-003–004, Contract]
- [x] CHK006 是否定義載入、錯誤、空資料、重新整理、篩選與標籤切換狀態？ [Completeness, Spec §FR-009]
- [x] CHK007 是否定義工作狀態空值的向後相容規則？ [Completeness, Spec §Edge Cases]
- [x] CHK008 是否定義每一個 MUST 功能與 Postman、Cypress、task、checklist 的追溯關係？ [Traceability, Spec §FR-011]

## Requirement Clarity

- [x] CHK009 「自己任務」是否明確量化為 `AssignedTask.assignee` 等於登入員工？ [Clarity, Spec §FR-002]
- [x] CHK010 執行者篩選是否明確限制為未指定或登入員工本人？ [Clarity, Spec §FR-005, Clarifications]
- [x] CHK011 最近一年是否明確定義台北當日、含起迄日與最多 366 天？ [Clarity, Spec §FR-006]
- [x] CHK012 狀態百分比是否明確定義一位小數且有資料時合計 100.0%？ [Clarity, Spec §FR-008, §SC-004]

## Requirement Consistency

- [x] CHK013 頁面、API 與 DAO 的資料範圍是否一致限制為登入員工本人？ [Consistency, Spec §US1, §FR-001–005]
- [x] CHK014 兩個圖表標籤是否一致共用執行者、狀態與日期條件？ [Consistency, Spec §US2–US3]
- [x] CHK015 是否避免與既有主管報表、系統報表的角色及路徑定義衝突？ [Consistency, Spec §Out of Scope]

## Acceptance Criteria Quality

- [x] CHK016 每條 User Story 是否具有可獨立執行且客觀判定的 acceptance scenarios？ [Measurability, Spec §US1–US3]
- [x] CHK017 是否有可量測的跨員工錯誤納入數 0 筆標準？ [Measurability, Spec §SC-002]
- [x] CHK018 是否有可量測的趨勢資料完整性、狀態比例與完成率標準？ [Measurability, Spec §SC-001, §SC-004]
- [x] CHK019 是否有 Gradle、production build、Newman 與 Cypress 全數通過的完成訊號？ [Measurability, Spec §SC-005]

## Scenario & Edge Case Coverage

- [x] CHK020 是否涵蓋有資料、空資料、未登入與跨員工識別四種情境？ [Coverage, Spec §US1, §Edge Cases]
- [x] CHK021 是否涵蓋非法日期順序、超長日期範圍與未知狀態？ [Coverage, Spec §Edge Cases]
- [x] CHK022 是否涵蓋無資料日期補 0 與台北時區跨日邊界？ [Coverage, Spec §US2, §Edge Cases]
- [x] CHK023 是否涵蓋既有空工作狀態視為待處理的相容規則？ [Coverage, Spec §Edge Cases]
- [x] CHK024 是否涵蓋圖表篩選條件在標籤切換後保持一致？ [Coverage, Spec §US3]

## Dependencies & Assumptions

- [x] CHK025 是否明確記載登入 principal、既有 `assigned_task` 與三種工作狀態依賴？ [Assumption, Spec §Assumptions]
- [x] CHK026 是否說明沿用既有表而不建立報表快照表的理由與邊界？ [Decision, Spec §Clarifications]
- [x] CHK027 是否明確排除跨員工報表、任務異動、匯出及其他 Sheet 標籤？ [Boundary, Spec §Out of Scope]

## Notes

- 27/27 項通過；需求已具備實作與測試所需完整度。
- 所有項目均含 Spec、Sheet、Gap、Assumption 或 Decision 追溯標記，追溯率 100%。
