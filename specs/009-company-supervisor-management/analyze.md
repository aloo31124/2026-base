# Specification Analysis Report：公司主管管理

## 目錄

- [Findings](#findings) — 跨文件一致性
- [Coverage Summary](#coverage-summary) — FR 與 task 覆蓋
- [Constitution Alignment](#constitution-alignment) — 必要原則
- [Metrics](#metrics) — 覆蓋統計

## Findings

| ID | Category | Severity | Location | Summary | Recommendation |
|---|---|---|---|---|---|
| A1 | Assumption | LOW | spec.md §Assumptions | 員工綁定 UI 不在第 12–15 列 | 保留通用唯一資料模型，延後 UI |
| T1 | Terminology | LOW | Sheet row 14 | 原文「邦定」與「綁定」混用 | 文件與程式統一使用「綁定」 |

## Coverage Summary

| Requirement | Has Task | Task IDs | Notes |
|---|---|---|---|
| FR-001–FR-004 | Yes | T004, T007, T010–T014 | 公司 CRUD 與關聯刪除 |
| FR-005–FR-009 | Yes | T005, T008, T010, T015–T018 | 主管與既有使用者 |
| FR-010–FR-014 | Yes | T006, T009, T019–T022 | 綁定、唯一性與查詢 |
| FR-015–FR-018 | Yes | T023–T026, T027–T031 | API/頁面權限與 E2E |
| SC-006 | Yes | T027–T034 | 測試、報告、建置與 Sheet |

## Constitution Alignment

- 分層架構：BO → DAO → Service → Controller → React 任務完整，無跨層存取。
- 測試必要性：每個 User Story 均先有整合測試，並有 Postman/Cypress。
- MVP：不實作 Sheet 第 16 列後未標狀態的任務指派功能。
- 向後相容：僅新增 API、頁面與資料表。
- 文件註解：規格與新增方法均要求繁體中文。

## Metrics

- Total Requirements：18
- Total Tasks：34
- Coverage：100%
- Ambiguity Count：0
- Duplication Count：0
- Critical Issues：0
