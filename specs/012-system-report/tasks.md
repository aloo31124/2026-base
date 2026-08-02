# Tasks：系統報表

**Input**：`specs/012-system-report/` 的 spec、plan、research、data-model 與 contract。  
**Tests**：依使用者要求採 TDD，JUnit、Postman/Newman 與 Cypress 皆為必要工作。

## 目錄

- [Phase 1 - Speckit 與測試先行](#phase-1---speckit-與測試先行) — 4 項
- [Phase 2 - DB 與 DAO](#phase-2---db-與-dao) — 2 項
- [Phase 3 - Service、BO 與 REST API](#phase-3---servicebo-與-rest-api) — 4 項
- [Phase 4 - React 系統報表 Page](#phase-4---react-系統報表-page) — 4 項
- [Phase 5 - Postman、Cypress 與完整驗收](#phase-5---postmancypress-與完整驗收) — 7 項
- [Dependencies & Execution Order](#dependencies--execution-order) — TDD 與分層順序
- [FR Coverage](#fr-coverage) — 15 條 FR 對照

## Phase 1 - Speckit 與測試先行

- [x] T001 建立 `specs/012-system-report/` 的 spec、clarify、checklist、plan、research、data-model、contract、quickstart、tasks 與 analyze 文件
- [x] T002 逐項通過 `specs/012-system-report/checklists/requirements.md` 的 CHK001–CHK024
- [x] T003 [P] [US1] [US2] [US3] [US4] 建立失敗先行的 `Backend/src/test/java/com/agentflow/base/SystemReportIntegrationTest.java`
- [x] T004 [P] [US1] [US2] [US3] [US4] 建立 `Frontend/cypress/e2e/system-report.cy.ts` E2E 規格

## Phase 2 - DB 與 DAO

- [x] T005 [US1] [US2] 於 `specs/012-system-report/data-model.md` 核對既有 `company`、`company_membership`、`assigned_task` 表與報表關聯，不新增快照表
- [x] T006 [US1] [US2] [US3] 擴充 `Backend/src/main/java/com/agentflow/base/dao/AssignedTaskDao.java` 的公司與期間唯讀查詢

## Phase 3 - Service、BO 與 REST API

- [x] T007 [P] [US1] [US2] [US3] 建立 `Backend/src/main/java/com/agentflow/base/model/dto/SystemReportDtos.java` 公司選項與趨勢回應 model
- [x] T008 [US1] [US2] [US3] [US4] 建立 `Backend/src/main/java/com/agentflow/base/service/SystemReportService.java` 日期驗證、公司篩選、台北日界線、彙總與補零邏輯
- [x] T009 [US1] [US2] [US3] [US4] 建立 `Backend/src/main/java/com/agentflow/base/controller/SystemReportController.java` 管理 REST API
- [x] T010 [US4] 更新 `Backend/src/main/java/com/agentflow/base/config/SecurityConfig.java` 的系統報表模組 403 訊息並跑綠 T003

## Phase 4 - React 系統報表 Page

- [x] T011 [P] [US1] [US4] 建立 `Frontend/src/components/TaskTrendChart.tsx` 可存取 SVG 折線圖、座標與空資料摘要
- [x] T012 [US1] [US2] [US3] [US4] 建立 `Frontend/src/pages/SystemReportPage.tsx` 預設一年、公司/日期篩選與狀態 UI
- [x] T013 [US1] 更新 `Frontend/src/App.tsx` 與 `Frontend/src/components/AppShell.tsx` 的管理員路由及側欄入口
- [x] T014 [US1] [US2] [US3] [US4] 更新 `Frontend/src/styles.css`，沿用 `uiux/0.共用樣式` 的頁首、標籤、卡片與篩選設計

## Phase 5 - Postman、Cypress 與完整驗收

- [x] T015 [P] 建立 `postman/system-report.postman_collection.json` 並更新 `postman/package.json` 執行指令
- [x] T016 更新 `Frontend/package.json` 的系統報表 Cypress 指令並跑綠 T004
- [x] T017 執行 `./Backend/gradlew -p Backend clean test` 與 `npm --prefix Frontend run build` 至綠燈
- [x] T018 實際啟動後端並執行 Newman/Postman，驗證 response 且產出 `report/test/result-20260802-system-report-postman.md`
- [x] T019 實際啟動前後端並執行 Cypress，產出 `report/test/result-20260802-system-report-cypress.md0`
- [x] T020 核對 FR-001–FR-015、CHK001–CHK024、所有 task 與 `specs/012-system-report/analyze.md`，確認完成率 100%
- [x] T021 完成所有驗收後將 Google Sheet「任務報表」`B13:B14` 從「預計開發」更新為「開發完成」並重讀驗證

## Dependencies & Execution Order

- T001–T002 先完成；T003 與 T004 可平行建立測試規格。
- T005–T006 完成後才能進入 Service/API；T007 可與 DAO 工作分檔進行。
- T008 依賴 T006–T007，T009 依賴 T008，T010 完成後後端測試必須綠燈。
- T011 可獨立建立，T012 依賴 API 契約與 T011；T013–T014 完成後才能跑 Cypress。
- T017–T020 全部通過後才可執行 T021；Google Sheet 狀態不得提前更新。

## FR Coverage

| Requirements | Task IDs |
|---|---|
| FR-001–FR-002 | T003、T009–T013 |
| FR-003–FR-006 | T003、T005–T009、T011–T012、T018–T019 |
| FR-007–FR-009 | T003、T006、T008、T012、T018–T019 |
| FR-010 | T003、T007、T009、T015、T018 |
| FR-011–FR-012 | T004、T011–T014、T019 |
| FR-013 | T005–T010 |
| FR-014 | T011–T014 |
| FR-015 | T003–T004、T015–T020 |
