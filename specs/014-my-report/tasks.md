# Tasks: 我的報表

## 目錄

- [Phase 1: Setup & Specification](#phase-1-setup--specification) — 4 項
- [Phase 2: Foundational Tests](#phase-2-foundational-tests) — 2 項
- [Phase 3: User Story 1 - 個人任務總覽](#phase-3-user-story-1---個人任務總覽) — 5 項
- [Phase 4: User Story 2 - 任務趨勢](#phase-4-user-story-2---任務趨勢) — 4 項
- [Phase 5: User Story 3 - 任務狀態比](#phase-5-user-story-3---任務狀態比) — 3 項
- [Phase 6: Frontend Integration](#phase-6-frontend-integration) — 4 項
- [Phase 7: Acceptance & Closeout](#phase-7-acceptance--closeout) — 7 項
- [Dependencies](#dependencies) — 執行順序
- [Parallel Opportunities](#parallel-opportunities) — 可平行工作
- [Implementation Strategy](#implementation-strategy) — TDD 與單子模組交付

## Phase 1: Setup & Specification

- [x] T001 建立 `specs/014-my-report/spec.md` 並逐條收錄 Sheet 第 23–25 列需求
- [x] T002 [P] 建立 `specs/014-my-report/checklists/requirements.md` 並完成 27 項需求品質檢核
- [x] T003 [P] 建立 `specs/014-my-report/plan.md`、`research.md`、`data-model.md` 與 `quickstart.md`
- [x] T004 [P] 建立 `specs/014-my-report/contracts/openapi.yaml` 與 `analyze.md`

## Phase 2: Foundational Tests

- [x] T005 新增失敗中的本人隔離、篩選、狀態比例與錯誤情境測試於 `backend/src/test/java/com/agentflow/base/MyReportIntegrationTest.java`
- [x] T006 確認 T005 在尚未實作 `/api/my/reports` 時紅燈，並記錄 TDD 起點於 `specs/014-my-report/tasks.md`

## Phase 3: User Story 1 - 個人任務總覽

- [x] T007 [US1] 在 `backend/src/main/java/com/agentflow/base/model/dto/MyReportDtos.java` 建立 BO/DTO records
- [x] T008 [US1] 在 `backend/src/main/java/com/agentflow/base/dao/AssignedTaskDao.java` 新增本人報表 JPA projection 查詢
- [x] T009 [US1] 在 `backend/src/main/java/com/agentflow/base/service/MyReportService.java` 實作本人身份、日期、狀態與資料隔離規則
- [x] T010 [US1] 在 `backend/src/main/java/com/agentflow/base/controller/MyReportController.java` 實作 filters/report REST API
- [x] T011 [US1] 執行 `MyReportIntegrationTest` 驗證 Sheet 第 23 列個人總覽與跨員工隔離

## Phase 4: User Story 2 - 任務趨勢

- [x] T012 [US2] 在 `MyReportService.java` 依 Asia/Taipei 補齊 0 值日期並限制 366 天
- [x] T013 [US2] 在 `MyReportIntegrationTest.java` 驗證預設一年、日期與工作狀態篩選及非法條件
- [x] T014 [US2] 在 `frontend/src/pages/MyReportPage.tsx` 建立任務趨勢標籤、篩選、摘要及空／載入／錯誤狀態
- [x] T015 [US2] 在 `frontend/cypress/e2e/my-report.cy.ts` 驗證 Sheet 第 24 列折線圖與篩選 request

## Phase 5: User Story 3 - 任務狀態比

- [x] T016 [US3] 在 `MyReportService.java` 實作三種狀態桶及合計 100% 的一位小數比例
- [x] T017 [US3] 在 `frontend/src/components/TaskStatusPieChart.tsx` 支援可配置 test id，並於 `MyReportPage.tsx` 建立任務狀態比標籤
- [x] T018 [US3] 在 `MyReportIntegrationTest.java` 與 `my-report.cy.ts` 驗證 Sheet 第 25 列圓餅圖、圖例與空資料

## Phase 6: Frontend Integration

- [x] T019 在 `frontend/src/App.tsx` 新增受登入 Guard 保護的 `/my-reports` route
- [x] T020 在 `frontend/src/components/AppShell.tsx` 新增「我的報表」導覽入口
- [x] T021 在 `frontend/src/styles.css` 沿用並微調 uiux 共用報表 RWD 樣式（若既有樣式已滿足則保留不改）
- [x] T022 在 `frontend/package.json` 新增專屬 Cypress script 並完成 production build

## Phase 7: Acceptance & Closeout

- [x] T023 建立並實際執行 `postman/my-report.postman_collection.json`，確認 response 值與 Newman 綠燈
- [x] T024 實際啟動前後端並執行 `frontend/cypress/e2e/my-report.cy.ts` 至綠燈
- [x] T025 產出 `report/test/result-20260802-my-report-postman.md`，明確記錄完成率
- [x] T026 產出 `report/test/result-20260802-my-report-cypress.md0`，明確記錄完成率
- [x] T027 執行後端完整測試、前端 production build 並確認所有 task/checklist 完成
- [x] T028 建立 `skills/business-logic/my-report/SKILL.md` 並更新 `skills/SKILLS_INDEX.md`
- [x] T029 僅將 Google Sheet「任務報表」B23:B25 從「預計開發」更新為「開發完成」，驗證後執行 git commit

## Dependencies

`T001 → T005 → T007 → T008 → T009 → T010 → T011 → T012–T018 → T019–T022 → T023–T029`

- US1 是資料隔離與 API 基礎，阻擋 US2、US3。
- US2 與 US3 共用同一 filters/report response，後端聚合需依序完成以避免同檔衝突。
- Postman 與 Cypress 必須在實作及 build 成功後執行。
- Sheet 狀態只在所有測試與報告完成後回寫。

## Parallel Opportunities

- T002、T003、T004 可在 spec 定稿後針對不同文件平行進行。
- API contract/collection 與 Cypress 測試骨架可在後端／前端介面確定後平行撰寫。
- Postman 與 Cypress 報告內容必須等待各自真實執行結果，不可預填成功。

## Implementation Strategy

先以整合測試建立紅燈，再依 DB/DAO → Service/BO → Controller → React Page → Postman → Cypress 的順序完成。此 Sheet 僅有「我的報表」一個待開發子模組，因此單一 `specs/014-my-report` 目錄與單一 commit 完成交付，不建立第二對話。
