# Requirements Checklist：例外處理與 Log

## 目錄

- [錯誤契約](#錯誤契約) — 4 項
- [安全與追蹤](#安全與追蹤) — 4 項

## 錯誤契約

- [x] CHK001 是否定義驗證、業務、權限與未知例外？[Spec §FR-001–002]
- [x] CHK002 401 與 403 語意是否可區分？[Spec §FR-008]
- [x] CHK003 API 權限錯誤指定文字是否無 placeholder？[Spec §FR-006]
- [x] CHK004 頁面擋權導向與文字是否完整？[Spec §FR-007]

## 安全與追蹤

- [x] CHK005 Log 層級與必要層是否有定義？[Spec §FR-003–004]
- [x] CHK006 是否要求未知例外不洩漏 stack trace？[Spec §FR-005]
- [x] CHK007 JWT 格式錯誤與角色不足邊界是否涵蓋？[Edge Case]
- [x] CHK008 所有錯誤 FR 是否有後端或 Cypress task？[Traceability]

