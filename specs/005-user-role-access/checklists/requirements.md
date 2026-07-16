# Requirements Checklist：使用者角色權限

## 目錄

- [認證與安全](#認證與安全) — 5 項
- [管理流程](#管理流程) — 5 項

## 認證與安全

- [x] CHK001 四組初始帳密、角色與空表條件是否明確？[Spec §FR-001–002]
- [x] CHK002 密碼雜湊與不得洩漏需求是否明確？[Spec §FR-003]
- [x] CHK003 Authentication、Authorization、Filter Chain 與 JWT 到期是否完整？[Spec §FR-004–005]
- [x] CHK004 API 與前端路由雙層授權是否定義？[Spec §FR-012–013]
- [x] CHK005 停用帳號與重複角色的邊界是否涵蓋？[Edge Case]

## 管理流程

- [x] CHK006 一人多角色資料關係與預設 EMPLOYEE 是否完整？[Spec §FR-006–008]
- [x] CHK007 使用者列表欄位與操作是否明確？[Spec §FR-009]
- [x] CHK008 管理員新增標記與密碼長度是否可測？[Spec §FR-010]
- [x] CHK009 角色 tab 與附加 MANAGER 行為是否明確？[Spec §FR-011]
- [x] CHK010 每個 User Story 是否有 BO/DAO/Service/API/React/Test task？[Traceability]

