# Tasks：整合測試

## 目錄

- [Phase 1 - Backend Tests](#phase-1---backend-tests) — 3 項
- [Phase 2 - Postman](#phase-2---postman) — 3 項
- [Phase 3 - Cypress](#phase-3---cypress) — 3 項
- [Phase 4 - Reports](#phase-4---reports) — 3 項

## Phase 1 - Backend Tests

- [x] T001 [US1] 建立 H2 MSSQL mode 測試 profile 於 backend/src/main/resources/application-test.yml
- [x] T002 [US1] 撰寫登入、權限、使用者與 test CRUD 整合測試於 backend/src/test/java/com/agentflow/base/AgentFlowIntegrationTest.java
- [x] T003 [US1] 撰寫 DB 命名與 UUID 審計測試於 backend/src/test/java/com/agentflow/base/DatabaseConventionTest.java

## Phase 2 - Postman

- [x] T004 [US2] 建立登入、使用者與 test CRUD collection 於 postman/agentflow-base.postman_collection.json
- [x] T005 [US2] 初始化 MSSQL 並啟動 backend Spring Boot
- [x] T006 [US2] 使用 Newman 實際執行 Postman assertions 並保存結果

## Phase 3 - Cypress

- [x] T007 [US3] 建立 Cypress config 於 frontend/cypress.config.ts
- [x] T008 [US3] 依 task 與 FR 撰寫單一 E2E spec 於 frontend/cypress/e2e/base-system.cy.ts
- [x] T009 [US3] 同時啟動 backend/frontend 並執行 Cypress 至綠燈

## Phase 4 - Reports

- [x] T010 [US2] 產生完成率報告於 report/test/20260716-01-postman.md
- [x] T011 [US3] 產生完成率報告於 report/test/20260716-02-cypress.md
- [x] T012 核對兩份報告之通過數、總數、完成率與重現命令

