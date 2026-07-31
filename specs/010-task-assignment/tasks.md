# Tasks：任務指派

## 目錄

- [Phase 1 - Setup](#phase-1---setup) — 2 項
- [Phase 2 - Foundational](#phase-2---foundational) — 4 項
- [Phase 3 - US1 綁定](#phase-3---us1-綁定) — 4 項
- [Phase 4 - US2 任務指派](#phase-4---us2-任務指派) — 4 項
- [Phase 5 - US3 查詢 CRUD](#phase-5---us3-查詢-crud) — 3 項
- [Phase 6 - US4 狀態](#phase-6---us4-狀態) — 3 項
- [Phase 7 - US5 前端與驗收](#phase-7---us5-前端與驗收) — 6 項

## Phase 1 - Setup

- [x] T001 建立 `specs/010-task-assignment/` 規格、設計與契約文件
- [x] T002 確認 `.gitignore` 與既有 Java/Node 忽略規則

## Phase 2 - Foundational

- [x] T003 [P] 建立 BO 於 `backend/src/main/java/com/agentflow/base/model/bo/`
- [x] T004 [P] 建立 JPA DAO 於 `backend/src/main/java/com/agentflow/base/dao/`
- [x] T005 [P] 建立 DTO 於 `backend/src/main/java/com/agentflow/base/model/dto/TaskAssignmentDtos.java`
- [x] T006 先建立整合測試於 `backend/src/test/java/com/agentflow/base/TaskAssignmentIntegrationTest.java`

## Phase 3 - US1 綁定

- [x] T007 [US1] 實作公司自助綁定於 `TaskAssignmentService.java`
- [x] T008 [US1] 實作員工信箱搜尋與主管員工綁定於 `TaskAssignmentService.java`
- [x] T009 [US1] 暴露綁定 REST 於 `TaskAssignmentController.java`
- [x] T010 [US1] 通過公司與員工綁定整合測試

## Phase 4 - US2 任務指派

- [x] T011 [US2] 實作合法受派人查詢於 `TaskAssignmentService.java`
- [x] T012 [US2] 實作任務建立與資格驗證於 `TaskAssignmentService.java`
- [x] T013 [US2] 暴露任務建立 REST 於 `TaskAssignmentController.java`
- [x] T014 [US2] 通過自己、同公司主管與旗下員工指派測試

## Phase 5 - US3 查詢 CRUD

- [x] T015 [US3] 實作條件查詢與白名單排序於 `AssignedTaskDao.java`
- [x] T016 [US3] 實作任務修改與刪除於 `TaskAssignmentService.java`
- [x] T017 [US3] 通過 CRUD、日期範圍與排序測試

## Phase 6 - US4 狀態

- [x] T018 [US4] 實作撤回與退回狀態轉換於 `AssignedTask.java`
- [x] T019 [US4] 實作收件匣與狀態 REST 於 `TaskAssignmentController.java`
- [x] T020 [US4] 通過權限、退回原因與非法狀態測試

## Phase 7 - US5 前端與驗收

- [x] T021 [US5] 建立 React 頁面於 `frontend/src/pages/TaskAssignmentPage.tsx`
- [x] T022 [US5] 更新路由、側欄與樣式於 `frontend/src/App.tsx`、`AppShell.tsx`、`styles.css`
- [x] T023 [US5] 建立 Postman collection 並產出 `report/test/result-20260730-task-assignment-postman.md`
- [x] T024 [US5] 建立 Cypress 並產出 `report/test/result-20260730-task-assignment-cypress.md`
- [x] T025 執行 `backend/gradlew -p backend clean build` 並修到綠燈
- [x] T026 完成 FR 對照、analyze、checklist 與 Google Sheet B20:B26 狀態回寫
