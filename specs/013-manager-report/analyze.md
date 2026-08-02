# Specification Analysis Report：主管報表

## 目錄

- [結論](#結論) — 無阻斷問題
- [Coverage Summary](#coverage-summary) — FR 全數具有 task
- [Constitution Alignment](#constitution-alignment) — 六項原則通過
- [Implementation Verification](#implementation-verification) — 分層、FR 與測試證據追蹤
- [Metrics](#metrics) — 覆蓋率與問題統計

## 結論

spec、plan 與 tasks 對「公司所有任務總覽」、「目前主管自己指派的任務」、「執行狀態」、「預設一年」及共用篩選的用語一致；公司／主管隔離、時區、範圍、空資料、權限與驗收證據均有明確規則，無 CRITICAL、HIGH 或未覆蓋需求。

## Coverage Summary

| Requirement Key | Has Task | Task IDs | Notes |
|---|---|---|---|
| FR-001–FR-004 | 是 | T003、T006–T012、T014–T016、T021–T022 | 主管權限、公司範圍與摘要 |
| FR-005–FR-011 | 是 | T003–T004、T006–T009、T011、T014、T016、T021–T022 | 個人指派趨勢與三類篩選 |
| FR-012–FR-016 | 是 | T003–T004、T006–T009、T011、T013–T016、T021–T022 | 狀態比例、共用條件與台北時區 |
| FR-017–FR-018 | 是 | T003、T006–T010、T018、T021 | response、錯誤與嚴格分層 |
| FR-019 | 是 | T004、T013–T017、T019、T022 | React page、UIUX 與可存取圖表 |
| FR-020 | 是 | T003–T004、T017–T024 | 建置、三層測試、報告與技能文件 |

## Constitution Alignment

- 分層架構：Controller → Service → DAO，無跨層存取。
- 測試必要性：先建立整合與 E2E 規格，包含隔離、邊界、錯誤與權限。
- MVP：沿用四張表與原生視覺元件，不建立快照與新套件。
- 業務正確性：公司總覽與目前主管圖表採不同且明確的統計口徑。
- 向後相容：僅新增 API、page、DTO 與 query，不變更既有 response。
- 文件與註解：Speckit、方法、函式與測試報告均要求繁體中文。

## Implementation Verification

| 層級／需求 | 預定實作位置 | 驗證證據 |
|---|---|---|
| DB 表與關聯 | `company`、`company_membership`、`app_user`、`assigned_task` | JUnit 建立跨公司、跨主管與多狀態任務 |
| DAO | `AssignedTaskDao` 公司／主管 projection query | 單一公司與目前主管計數正確 |
| Service | `ManagerReportService` | 預設一年、台北日界線、366 天、執行者／狀態、補零與比例 |
| BO／DTO | `ManagerReportDtos` | Newman 驗證 filters、摘要、points 與 buckets response |
| Controller／API | `ManagerReportController` | MockMvc 3/3、Newman 32/32 assertions |
| React／Page | `ManagerReportPage`、兩個圖表元件 | production build 成功、Cypress 1/1 |
| FR-001–FR-020 | tasks T003–T024 | 實際覆蓋率 100%，無未映射需求 |

## Metrics

- Total Requirements：20
- Total Tasks：25
- Requirement Coverage：100%
- Checklist Items：26/26 passed
- Ambiguity Count：0
- Duplication Count：0
- Critical Issues：0
- Unmapped Tasks：0
