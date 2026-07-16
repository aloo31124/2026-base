# Requirements Checklist：整合測試

## 目錄

- [測試範圍](#測試範圍) — 5 項
- [報告品質](#報告品質) — 4 項

## 測試範圍

- [x] CHK001 是否定義 test 表完整 CRUD 堆疊？[Spec §FR-001]
- [x] CHK002 是否定義固定路由 `/test/testTemp/`？[Spec §FR-002]
- [x] CHK003 是否區分隔離自動測試與實際 MSSQL API 測試？[Spec §FR-003]
- [x] CHK004 Postman assertions 是否包含狀態與重要 Response 值？[Spec §FR-004]
- [x] CHK005 Cypress 是否涵蓋登入、權限、使用者與 CRUD？[Spec §FR-006]

## 報告品質

- [x] CHK006 是否定義兩份報告的精確路徑與檔名？[Spec §FR-005, FR-007]
- [x] CHK007 完成率是否定義為通過數／總數？[Spec §FR-008]
- [x] CHK008 測試重跑的唯一資料與清理需求是否涵蓋？[Edge Case]
- [x] CHK009 每個 P1 情境是否有對應測試 task？[Traceability]

