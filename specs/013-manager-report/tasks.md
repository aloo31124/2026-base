# Tasks：主管報表

**Input**：`specs/013-manager-report/` 的 spec、plan、research、data-model 與 contract。  
**Tests**：依使用者要求採 TDD，JUnit、Postman/Newman 與 Cypress 皆為必要工作。

## 目錄

- [Phase 1 - Speckit 與測試先行](#phase-1---speckit-與測試先行) — 4 項
- [Phase 2 - 公司總覽與資料層](#phase-2---公司總覽與資料層) — 3 項
- [Phase 3 - 指派趨勢與狀態比例後端](#phase-3---指派趨勢與狀態比例後端) — 5 項
- [Phase 4 - React 主管報表 Page](#phase-4---react-主管報表-page) — 5 項
- [Phase 5 - Postman、Cypress 與完整驗收](#phase-5---postmancypress-與完整驗收) — 8 項
- [Dependencies & Execution Order](#dependencies--execution-order) — TDD 與分層順序
- [Parallel Opportunities](#parallel-opportunities) — 不同檔案的可平行工作
- [FR Coverage](#fr-coverage) — 20 條 FR 對照

## Phase 1 - Speckit 與測試先行

- [x] T001 建立 `specs/013-manager-report/` 的 spec、clarify、checklist、plan、research、data-model、contract、quickstart、tasks 與 analyze 文件
- [x] T002 逐項通過 `specs/013-manager-report/checklists/requirements.md` 的 CHK001–CHK026
- [x] T003 [P] [US1] [US2] [US3] [US4] 建立失敗先行的 `backend/src/test/java/com/agentflow/base/ManagerReportIntegrationTest.java`
- [x] T004 [P] [US1] [US2] [US3] [US4] 建立 `frontend/cypress/e2e/manager-report.cy.ts` E2E 規格

## Phase 2 - 公司總覽與資料層

- [x] T005 [US1] 於 `specs/013-manager-report/data-model.md` 核對 `company`、`company_membership`、`app_user`、`assigned_task` 表與關聯，不新增快照表
- [x] T006 [US1] [US2] [US3] 擴充 `backend/src/main/java/com/agentflow/base/dao/AssignedTaskDao.java` 的公司與主管任務唯讀查詢
- [x] T007 [P] [US1] [US2] [US3] 建立 `backend/src/main/java/com/agentflow/base/model/dto/ManagerReportDtos.java` filters、來源、趨勢與比例 model

## Phase 3 - 指派趨勢與狀態比例後端

- [x] T008 [US1] [US2] [US3] [US4] 建立 `backend/src/main/java/com/agentflow/base/service/ManagerReportService.java` 主管公司、日期、篩選、彙總、補零與比例邏輯
- [x] T009 [US1] [US2] [US3] [US4] 建立 `backend/src/main/java/com/agentflow/base/controller/ManagerReportController.java` 主管 REST API
- [x] T010 [US4] 更新 `backend/src/main/java/com/agentflow/base/config/SecurityConfig.java` 的主管報表專屬 403 訊息
- [x] T011 [US1] [US2] [US3] 執行 `ManagerReportIntegrationTest`，確認 filters、公司總覽、趨勢、比例與篩選綠燈
- [x] T012 [US4] 執行 `ManagerReportIntegrationTest`，確認非主管、無公司、非法日期／狀態／執行者綠燈

## Phase 4 - React 主管報表 Page

- [x] T013 [P] [US3] 建立 `frontend/src/components/TaskStatusPieChart.tsx` 可存取圓餅圖、圖例與零資料摘要
- [x] T014 [US1] [US2] [US3] [US4] 建立 `frontend/src/pages/ManagerReportPage.tsx` 公司摘要、共用篩選、兩標籤與狀態 UI
- [x] T015 [US1] [US4] 更新 `frontend/src/App.tsx` 與 `frontend/src/components/AppShell.tsx` 的主管路由、角色標示及側欄入口
- [x] T016 [US1] [US2] [US3] [US4] 更新 `frontend/src/styles.css`，沿用 `uiux/0.共用樣式` 的頁首、標籤、卡片、篩選、圖表與響應式設計
- [x] T017 [US1] [US2] [US3] [US4] 執行 TypeScript 與 Vite production build 至綠燈

## Phase 5 - Postman、Cypress 與完整驗收

- [x] T018 [P] 建立 `postman/manager-report.postman_collection.json` 並更新 `postman/package.json` 執行指令
- [x] T019 更新 `frontend/package.json` 的主管報表 Cypress 指令並跑綠 T004
- [x] T020 執行 `./backend/gradlew -p backend clean test` 與 `npm --prefix frontend run build` 至綠燈
- [x] T021 實際啟動後端並執行 Postman/Newman，驗證 response 且產出 `report/test/result-20260802-manager-report-postman.md`
- [x] T022 實際啟動前端並執行 Cypress，產出 `report/test/result-20260802-manager-report-cypress.md0`
- [x] T023 建立 `skills/business-logic/manager-report/SKILL.md` 並更新 `skills/SKILLS_INDEX.md`
- [x] T024 核對 FR-001–FR-020、CHK001–CHK026、所有 task 與 `specs/013-manager-report/analyze.md`，確認完成率 100%
- [x] T025 完成所有驗收後將 Google Sheet「任務報表」`B18:B20` 從「預計開發」更新為「開發完成」並重讀驗證

## Dependencies & Execution Order

- T001–T002 先完成；T003 與 T004 可在不同測試檔平行建立，且先於實作失敗。
- T005–T007 完成後才能進入 Service／API；T006 與 T007 可分檔進行。
- T008 依賴 T006–T007，T009 依賴 T008，T010–T012 完成後後端測試必須綠燈。
- T013 可獨立建立；T014 依賴 API 契約與圖表元件；T015–T017 完成後才能跑 Cypress。
- T020–T024 全部通過後才可執行 T025；Google Sheet 狀態不得提前更新。

## Parallel Opportunities

- T003 與 T004 位於後端／前端不同測試檔，可平行撰寫。
- T006 與 T007 分別修改 DAO 與 DTO，可平行進行。
- T013 與後端 Phase 3 不共用檔案，可在後端實作期間建立。
- T018 與前端 production build 不共用檔案，可平行準備。

## FR Coverage

| Requirements | Task IDs |
|---|---|
| FR-001–FR-004 | T003、T006–T012、T014–T016、T021–T022 |
| FR-005–FR-011 | T003–T004、T006–T009、T011、T014、T016、T021–T022 |
| FR-012–FR-016 | T003–T004、T006–T009、T011、T013–T016、T021–T022 |
| FR-017–FR-018 | T003、T006–T010、T018、T021 |
| FR-019 | T004、T013–T017、T019、T022 |
| FR-020 | T003–T004、T017–T024 |
