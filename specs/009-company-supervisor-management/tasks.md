# Tasks：公司主管管理

## 目錄

- [Phase 1 - Setup](#phase-1---setup) — 3 項
- [Phase 2 - Foundational](#phase-2---foundational) — 7 項
- [Phase 3 - User Story 1](#phase-3---user-story-1) — 4 項
- [Phase 4 - User Story 2](#phase-4---user-story-2) — 4 項
- [Phase 5 - User Story 3](#phase-5---user-story-3) — 4 項
- [Phase 6 - User Story 4](#phase-6---user-story-4) — 4 項
- [Phase 7 - Verification](#phase-7---verification) — 8 項
- [Phase 8 - Unicode Bug Fix](#phase-8---unicode-bug-fix) — 4 項
- [Phase 9 - Sheet 第 16–17 列 Speckit 增量](#phase-9---sheet-第-1617-列-speckit-增量) — 4 項
- [Phase 10 - User Story 4 員工綁定後端](#phase-10---user-story-4-員工綁定後端) — 5 項
- [Phase 11 - User Story 4–5 綁定公司前端](#phase-11---user-story-45-綁定公司前端) — 4 項
- [Phase 12 - 增量驗收與回寫](#phase-12---增量驗收與回寫) — 4 項
- [Dependencies](#dependencies) — DB → API → React → 驗收

## Phase 1 - Setup

- [x] T001 建立 `specs/009-company-supervisor-management/` 全套 Speckit 文件
- [x] T002 盤點 `uiux/0.共用樣式`、`uiux/1.1.使用者分權` 與既有 React 管理頁
- [x] T003 建立 TDD 整合測試骨架 `backend/src/test/java/com/agentflow/base/CompanySupervisorManagementIntegrationTest.java`

## Phase 2 - Foundational

- [x] T004 [P] 建立公司 BO `backend/src/main/java/com/agentflow/base/model/bo/Company.java`
- [x] T005 [P] 建立主管 BO `backend/src/main/java/com/agentflow/base/model/bo/SupervisorProfile.java`
- [x] T006 [P] 建立公司成員綁定 BO `backend/src/main/java/com/agentflow/base/model/bo/CompanyMembership.java`
- [x] T007 [P] 建立公司 DAO `backend/src/main/java/com/agentflow/base/dao/CompanyDao.java`
- [x] T008 [P] 建立主管 DAO `backend/src/main/java/com/agentflow/base/dao/SupervisorProfileDao.java`
- [x] T009 [P] 建立公司成員 DAO `backend/src/main/java/com/agentflow/base/dao/CompanyMembershipDao.java`
- [x] T010 建立 DTO `backend/src/main/java/com/agentflow/base/model/dto/CompanySupervisorManagementDtos.java`

## Phase 3 - User Story 1

- [x] T011 [US1] 先完成公司 CRUD 失敗/成功測試 `backend/src/test/java/com/agentflow/base/CompanySupervisorManagementIntegrationTest.java`
- [x] T012 [US1] 實作公司 CRUD Service `backend/src/main/java/com/agentflow/base/service/CompanySupervisorManagementService.java`
- [x] T013 [US1] 實作公司 REST API `backend/src/main/java/com/agentflow/base/controller/CompanySupervisorManagementController.java`
- [x] T014 [US1] 建立 React 公司標籤 `frontend/src/pages/CompanySupervisorManagementPage.tsx`

## Phase 4 - User Story 2

- [x] T015 [US2] 先完成主管既有使用者與角色測試 `backend/src/test/java/com/agentflow/base/CompanySupervisorManagementIntegrationTest.java`
- [x] T016 [US2] 實作主管 CRUD 與角色規則 `backend/src/main/java/com/agentflow/base/service/CompanySupervisorManagementService.java`
- [x] T017 [US2] 實作主管 REST API `backend/src/main/java/com/agentflow/base/controller/CompanySupervisorManagementController.java`
- [x] T018 [US2] 建立 React 主管標籤 `frontend/src/pages/CompanySupervisorManagementPage.tsx`

## Phase 5 - User Story 3

- [x] T019 [US3] 先完成多人同公司、一人一公司與名稱查詢測試 `backend/src/test/java/com/agentflow/base/CompanySupervisorManagementIntegrationTest.java`
- [x] T020 [US3] 實作綁定、取消與查詢 Service `backend/src/main/java/com/agentflow/base/service/CompanySupervisorManagementService.java`
- [x] T021 [US3] 實作綁定 REST API `backend/src/main/java/com/agentflow/base/controller/CompanySupervisorManagementController.java`
- [x] T022 [US3] 建立 React 綁定標籤 `frontend/src/pages/CompanySupervisorManagementPage.tsx`

## Phase 6 - User Story 4

- [x] T023 [US4] 套用 API RBAC 與指定訊息 `backend/src/main/java/com/agentflow/base/config/SecurityConfig.java`
- [x] T024 [US4] 建立管理頁路由 Guard `frontend/src/App.tsx`
- [x] T025 [US4] 建立側欄入口 `frontend/src/components/AppShell.tsx`
- [x] T026 [US4] 套用 uiux 對應樣式 `frontend/src/styles.css`

## Phase 7 - Verification

- [x] T027 跑綠後端整合與全套測試 `backend/`
- [x] T028 建立並執行 Postman collection `postman/company-supervisor-management.postman_collection.json`
- [x] T029 產生 Postman 報告 `report/test/result-20260729-company-supervisor-management-postman.md`
- [x] T030 建立並執行 Cypress `frontend/cypress/e2e/company-supervisor-management.cy.ts`
- [x] T031 產生 Cypress 報告 `report/test/result-20260729-company-supervisor-management-cypress.md`
- [x] T032 完成前端與全專案建置 `frontend/`、`backend/`
- [x] T033 建立業務 skill 並更新索引 `skills/business-logic/company-supervisor-management/SKILL.md`、`skills/SKILLS_INDEX.md`
- [x] T034 確認 checklist 18/18、task 34/34 後回寫 Sheet「任務指派」B12:B15

## Phase 8 - Unicode Bug Fix

- [x] T035 將公司名稱、說明與主管職稱 JPA 欄位標註為 `@Nationalized`
- [x] T036 建立 SQL Server `varchar` 至 `nvarchar` 冪等遷移並串接資料庫初始化流程
- [x] T037 新增 Unicode 映射防回歸測試，並以繁中資料驗證實際 SQL Server 與 Postman API
- [x] T038 完成後端 27/27 測試、全專案建置與 Unicode Postman 測試報告

## Phase 9 - Sheet 第 16–17 列 Speckit 增量

- [x] T039 更新第 16–17 列需求與自動釐清 `specs/009-company-supervisor-management/spec.md`
- [x] T040 更新需求品質檢核 `specs/009-company-supervisor-management/checklists/requirements.md`
- [x] T041 更新技術計畫、研究、資料模型、API 契約與 quickstart `specs/009-company-supervisor-management/`
- [x] T042 完成 FR-020–FR-024 與 T043–T055 一致性分析 `specs/009-company-supervisor-management/analyze.md`

## Phase 10 - User Story 4 員工綁定後端

- [x] T043 [US4] 先新增員工資格、綁定、查詢、衝突與取消測試 `backend/src/test/java/com/agentflow/base/CompanySupervisorManagementIntegrationTest.java`
- [x] T044 [P] [US4] 新增員工綁定查詢 DAO `backend/src/main/java/com/agentflow/base/dao/CompanyMembershipDao.java`
- [x] T045 [P] [US4] 新增員工綁定 request/response DTO `backend/src/main/java/com/agentflow/base/model/dto/CompanySupervisorManagementDtos.java`
- [x] T046 [US4] 實作員工資格、綁定、取消與查詢 Service `backend/src/main/java/com/agentflow/base/service/CompanySupervisorManagementService.java`
- [x] T047 [US4] 新增員工綁定 REST API `backend/src/main/java/com/agentflow/base/controller/CompanySupervisorManagementController.java`

## Phase 11 - User Story 4–5 綁定公司前端

- [x] T048 [US4] 先擴充主管與員工綁定 Cypress 情境 `frontend/cypress/e2e/company-supervisor-management.cy.ts`
- [x] T049 [US5] 將標籤改名「綁定公司」並新增主管／員工類型切換 `frontend/src/pages/CompanySupervisorManagementPage.tsx`
- [x] T050 [US4] 串接員工候選、綁定、查詢與取消 UI `frontend/src/pages/CompanySupervisorManagementPage.tsx`
- [x] T051 [US5] 沿用 uiux 頁籤、卡片、篩選與表格樣式完成響應式調整 `frontend/src/styles.css`

## Phase 12 - 增量驗收與回寫

- [x] T052 跑綠後端整合測試與建置 `backend/`
- [x] T053 更新並實際執行 Postman collection，產生 `report/test/result-20260730-company-supervisor-management-postman.md`
- [x] T054 完成前端建置與 Cypress，產生 `report/test/result-20260730-company-supervisor-management-cypress.md`
- [x] T055 確認 task 55/55、checklist 26/26，更新業務 skill 並回寫 Sheet「任務指派」B16:B17

## Dependencies

- T001–T003 完成後進入資料層；T004–T010 為所有 User Story 基礎。
- 各 User Story 依「測試 → Service → Controller → React」順序實作。
- T020 依賴 T012 與 T016；公司與主管資料完成後才建立綁定。
- T027–T034 依序進行；只有所有建置與測試綠燈後才能執行 T034。
- T035–T038 為 Unicode 問號問題修正，依「映射 → 遷移 → 實際資料庫/API 驗證」順序進行。
- T039–T042 完成後才進入本次增量實作；T043 測試先行，T044–T047 依 DAO/DTO → Service → Controller。
- T048 先建立前端失敗案例，T049–T051 完成 React 與樣式後進入 T052–T055。
- 只有後端建置、Postman、前端建置與 Cypress 全綠後，才能將 Sheet B16:B17 改為「開發完成」。
