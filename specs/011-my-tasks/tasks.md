# Tasks：我的任務

## 目錄

- [Phase 1 - 規格與測試](#phase-1---規格與測試) — 4 項
- [Phase 2 - DB 與 DAO](#phase-2---db-與-dao) — 3 項
- [Phase 3 - Service 與 API](#phase-3---service-與-api) — 4 項
- [Phase 4 - React 頁面](#phase-4---react-頁面) — 3 項
- [Phase 5 - 驗收](#phase-5---驗收) — 5 項

## Phase 1 - 規格與測試

- [x] T001 建立 `specs/011-my-tasks/` 全套 speckit 文件
- [x] T002 完成 `specs/011-my-tasks/checklists/requirements.md` 逐項需求檢核
- [x] T003 [P] 擴充 `backend/src/test/java/com/agentflow/base/TaskAssignmentIntegrationTest.java` 我的任務測試
- [x] T004 [P] 建立 `frontend/cypress/e2e/my-tasks.cy.ts` E2E 測試

## Phase 2 - DB 與 DAO

- [x] T005 [US2] 擴充 `backend/src/main/java/com/agentflow/base/model/bo/AssignedTask.java`
- [x] T006 [P] [US3] 建立 `backend/src/main/java/com/agentflow/base/model/bo/TaskAttachment.java`
- [x] T007 [P] [US1] 擴充 `AssignedTaskDao.java` 並建立 `TaskAttachmentDao.java`

## Phase 3 - Service 與 API

- [x] T008 [US1] 擴充本人查詢與排序於 `TaskAssignmentService.java`
- [x] T009 [US2] 實作進度更新與本人權限於 `TaskAssignmentService.java`
- [x] T010 [US3] [US4] [US5] 實作附件、提交、退回與延期規則於 `TaskAssignmentService.java`
- [x] T011 [US1] [US2] [US3] [US4] [US5] 暴露 DTO 與 REST 於 `TaskAssignmentDtos.java`、`TaskAssignmentController.java`

## Phase 4 - React 頁面

- [x] T012 [US1] [US4] [US5] 建立 `frontend/src/pages/MyTasksPage.tsx` 列表頁
- [x] T013 [US2] [US3] [US4] [US5] 建立 `frontend/src/pages/MyTaskEditPage.tsx` 編輯頁
- [x] T014 更新 `frontend/src/App.tsx`、`AppShell.tsx`、`styles.css` 路由與 UIUX 樣式

## Phase 5 - 驗收

- [x] T015 擴充並通過 `postman/task-assignment.postman_collection.json` 我的任務案例
- [x] T016 執行後端與前端建置並修到綠燈
- [x] T017 執行 Cypress 並產出 `report/test/result-20260731-my-tasks-cypress.md0`
- [x] T018 產出 `report/test/result-20260731-my-tasks-postman.md`
- [x] T019 完成 FR 對照、checklist、analyze 並回寫 Google Sheet `B29:B39`
