# Specification Analysis Report：系統報表

## 目錄

- [結論](#結論) — 無阻斷問題
- [Coverage Summary](#coverage-summary) — FR 全數具有 task
- [Constitution Alignment](#constitution-alignment) — 六項原則通過
- [Implementation Verification](#implementation-verification) — 分層、FR 與測試證據
- [Metrics](#metrics) — 覆蓋率與問題統計

## 結論

spec、plan 與 tasks 對「系統報表、任務趨勢、公司篩選、最近一年」的用語一致；資料歸屬、時區、範圍、空資料、權限與測試證據皆有明確規則，無 CRITICAL、HIGH 或未覆蓋需求。

## Coverage Summary

| Requirement Key | Has Task | Task IDs | Notes |
|---|---|---|---|
| FR-001–FR-002 | 是 | T003、T009–T013 | 管理員權限、預設標籤與導覽 |
| FR-003–FR-006 | 是 | T003、T005–T012、T018–T019 | 全公司、單一公司、圖表與摘要 |
| FR-007–FR-009 | 是 | T003、T006、T008、T012、T018–T019 | 預設一年、邊界與補零 |
| FR-010 | 是 | T003、T007、T009、T015、T018 | 標準 API envelope 與 Postman response |
| FR-011–FR-012 | 是 | T004、T011–T014、T019 | 可存取圖表與 UI 狀態 |
| FR-013–FR-014 | 是 | T005–T014 | DB/DAO/Service/BO/Controller/React/Page 分層 |
| FR-015 | 是 | T003–T004、T015–T020 | 三層測試、建置與報告 |

## Constitution Alignment

- 分層架構：Controller → Service → DAO，無跨層存取。
- 測試必要性：先建立整合與 E2E 規格，包含邊界、錯誤與權限。
- MVP：沿用三張表並採原生 SVG，無報表快照與新套件。
- 業務正確性：任務依受派人唯一公司歸屬，避免重複。
- 向後相容：僅新增 API、page 與 DAO 查詢，不改既有回應。
- 文件與註解：Speckit 與新增方法均要求繁體中文。

## Implementation Verification

| 層級 / 需求 | 實作位置 | 驗證證據 |
|---|---|---|
| DB 表與關聯 | `company`、`company_membership`、`assigned_task` | JUnit 以兩家公司建立任務並驗證唯一歸屬 |
| DAO | `AssignedTaskDao.findTaskTrendSources` | 單一公司與全部公司查詢計數正確 |
| Service | `SystemReportService` | 預設一年、台北日界線、366 天、補零、404/400 |
| BO/DTO | `SystemReportDtos` | Newman 驗證公司、摘要與 points response |
| Controller/API | `SystemReportController` | MockMvc 3/3、Newman 30/30 assertions |
| React/Page | `SystemReportPage`、`TaskTrendChart` | production build 成功、Cypress 1/1 |
| FR-001–FR-015 | tasks T003–T020 | 覆蓋率 100%，無未映射需求 |

## Metrics

- Total Requirements：15
- Total Tasks：21
- Requirement Coverage：100%
- Checklist Items：24/24 passed
- Ambiguity Count：0
- Duplication Count：0
- Critical Issues：0
- Unmapped Tasks：0
