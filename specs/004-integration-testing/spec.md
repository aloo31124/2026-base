# Feature Specification：整合測試

**來源**：Google Sheet「基礎架構使用者分權」第 35–37 列

## 目錄

- [User Stories](#user-stories) — 3 條驗證流程
- [Functional Requirements](#functional-requirements) — 8 條測試需求
- [Edge Cases](#edge-cases) — 測試資料隔離
- [Success Criteria](#success-criteria) — 4 項完成率
- [Assumptions](#assumptions) — Postman CLI 執行方式

## User Stories

### User Story 1 - 透過頁面驗證 CRUD (Priority: P1)

測試者登入後在 `test/testTemp/` 頁面新增、查詢、編輯與刪除 test 資料。

### User Story 2 - 以 Postman 驗證 API (Priority: P1)

測試者執行 Postman collection，驗證登入、權限、使用者及 test API Response。

### User Story 3 - 以 Cypress 驗證前端 (Priority: P1)

測試者執行 Cypress，逐項覆蓋 tasks 與 checklist 的關鍵情境。

## Functional Requirements

- **FR-001**：資料庫 MUST 建立 `test` 表與完整 BO/DAO/Service/Controller。
- **FR-002**：前端 MUST 提供 `/test/testTemp/` CRUD 頁面。
- **FR-003**：後端 MUST 有 H2 自動化整合測試與 MSSQL 實際 API 測試。
- **FR-004**：Postman collection MUST 驗證 Response 狀態與重要值。
- **FR-005**：Postman 報告 MUST 位於 `report/test/20260716-01-postman.md`。
- **FR-006**：Cypress MUST 覆蓋管理員登入、新增使用者、一般使用者擋權與 test CRUD。
- **FR-007**：Cypress 報告 MUST 位於 `report/test/20260716-02-cypress.md`。
- **FR-008**：兩份報告 MUST 明確列出通過數、總數與完成率。

## Edge Cases

- 測試使用唯一帳號，避免重跑與唯一鍵衝突。
- CRUD 測試結束後刪除測試記錄。

## Success Criteria

- **SC-001**：Gradle tests 完成率 100%。
- **SC-002**：Postman assertions 完成率 100%。
- **SC-003**：Cypress scenarios 完成率 100%。
- **SC-004**：所有報告均含可重現命令與時間。

## Assumptions

- 本機未安裝 Postman 桌面版，採 Postman 官方 collection runner Newman 實際呼叫 API；collection 可直接匯入桌面版重跑。

