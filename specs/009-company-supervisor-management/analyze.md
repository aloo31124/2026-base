# Specification Analysis Report：公司主管管理

## 目錄

- [Findings](#findings) — 跨文件一致性
- [Coverage Summary](#coverage-summary) — FR、SC 與 task 覆蓋
- [Constitution Alignment](#constitution-alignment) — 必要原則
- [Metrics](#metrics) — 覆蓋統計

## Findings

| ID | Category | Severity | Location | Summary | Recommendation |
|---|---|---|---|---|---|
| S1 | Scope | LOW | spec.md §Assumptions | 既有第 13–15 列已完成內容與本次第 16–17 列增量共用同一子模組文件 | 保留既有內容，只新增可追溯的增量 FR 與 task |
| T1 | Terminology | LOW | Sheet row 14 | 原文「邦定」與「綁定」混用 | 文件與程式統一使用「綁定」 |
| A2 | API Compatibility | LOW | plan.md §Sheet 第 16–17 列增量 | 員工綁定若改寫既有 payload 會破壞主管呼叫端 | 採新增 `/employee-bindings` 並保留 `/bindings` |

## Coverage Summary

| Requirement | Has Task | Task IDs | Notes |
|---|---|---|---|
| FR-001–FR-004 | Yes | T004, T007, T010–T014 | 公司 CRUD 與關聯刪除 |
| FR-005–FR-009 | Yes | T005, T008, T010, T015–T018 | 主管與既有使用者 |
| FR-010–FR-014 | Yes | T006, T009, T019–T022 | 綁定、唯一性與查詢 |
| FR-015–FR-018 | Yes | T023–T026, T027–T031 | API/頁面權限與 E2E |
| FR-019 | Yes | T035–T038 | Unicode 映射、遷移與驗證 |
| FR-020 | Yes | T048–T051 | 綁定公司標籤與類型切換 |
| FR-021–FR-024 | Yes | T043–T050, T052–T054 | 員工資格、綁定、查詢、取消與一人一公司 |
| SC-006 | Yes | T027–T034 | 測試、報告、建置與 Sheet |
| SC-008–SC-009 | Yes | T048–T055 | 增量 UI、Postman、Cypress 與 Sheet |

## Constitution Alignment

- 分層架構：BO → DAO → Service → Controller → React 任務完整，無跨層存取。
- 測試必要性：每個 User Story 均先有整合測試，並有 Postman/Cypress。
- MVP：不實作 Sheet 第 18 列後未標狀態的任務指派功能。
- 向後相容：僅新增 API、頁面與資料表。
- 文件註解：規格與新增方法均要求繁體中文。

## Metrics

- Total Requirements：24
- Total Tasks：55
- Coverage：100%
- Ambiguity Count：0
- Duplication Count：0
- Critical Issues：0
