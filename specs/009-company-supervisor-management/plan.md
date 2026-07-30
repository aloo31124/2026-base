# Implementation Plan：公司主管管理

**Date**：2026-07-29  
**Spec**：[spec.md](spec.md)

## 目錄

- [技術樹（心智圖）](#技術樹心智圖) — 本功能完整技術鏈
- [Summary](#summary) — DB 到 React 的增量實作
- [Technical Decision Log](#technical-decision-log) — 實體、綁定與測試決策
- [Technical Context](#technical-context) — 既有技術棧與限制
- [Constitution Check](#constitution-check) — 分層、測試與相容性
- [Architecture](#architecture) — 資料流與分層
- [Project Structure](#project-structure) — 文件與程式碼路徑
- [Test Strategy](#test-strategy) — JUnit、Postman、Cypress
- [Sheet 第 16–17 列增量](#sheet-第-1617-列增量) — 綁定公司與員工綁定

## 技術樹（心智圖）

```mermaid
mindmap
  root((公司主管管理))
    Backend
      Java 21
      Spring Boot 4.1.0
      Spring Data JPA 4.1.0
      Spring Security 7
    Database
      MSSQL
      H2 test
    Frontend
      React 19.2.7
      TypeScript 5.9.3
      Vite 7.3.6
    Verification
      JUnit 5
      MockMvc
      Newman 6.2.2
      Cypress 15.18.1
```

- Backend：Java 21、Spring Boot 4.1.0、Spring Data JPA 4.1.0、Spring Security 7
- Database：MSSQL、H2 test
- Frontend：React 19.2.7、TypeScript 5.9.3、Vite 7.3.6
- Verification：JUnit 5、MockMvc、Newman 6.2.2、Cypress 15.18.1

## Summary

在既有單體專案沿用 `Company`、`SupervisorProfile`、`CompanyMembership` 三個 BO 與 JPA DAO，由單一 `CompanySupervisorManagementService` 維護公司、主管／員工身分及一人一公司的原子性規則；REST Controller 保留既有主管綁定 API 並新增員工綁定 API，React 頁面依 `uiux/` 樣式提供公司、主管、綁定公司三個標籤。

## Technical Decision Log

| 決策面向 | 評估方案 | 採用方案 | 採用理由 |
|---|---|---|---|
| 主管模型 | 獨立主管帳號／既有使用者附加資料 | `SupervisorProfile` 一對一既有使用者 | 完整符合「只能以已註冊使用者綁定」，不複製登入資料 |
| 公司綁定 | 主管專用 join table／通用成員綁定 | `CompanyMembership` 對 `user_id` 唯一 | 同時落實主管與員工一人一公司規則 |
| 刪除策略 | 級聯刪除／有關聯時拒絕 | 回覆 409 並要求先取消綁定 | 避免隱性刪除且符合業務可理解性 |
| UI 資料流 | 新增 Redux slice／頁面局部 state | 頁面局部 state + 既有 `api()` | 單頁管理資料，不增加不必要的全域狀態 |
| 測試策略 | 僅單元／整合加真實 HTTP E2E | MockMvc + Newman + Cypress | 覆蓋分層業務、API response 與真實頁面流程 |

## Technical Context

- **Language/Version**：Java 21、TypeScript 5.9.3。
- **Primary Dependencies**：Spring Boot 4.1.0、JPA、Security、React 19.2.7、Vite 7.3.6。
- **Storage**：MSSQL 正式環境；H2 測試與本機驗收。
- **Testing**：JUnit 5、MockMvc、Newman 6.2.2、Cypress 15.18.1。
- **Target Platform**：Docker/Linux Web 應用。
- **Performance Goals**：一般列表及名稱篩選在測試資料規模下 2 秒內完成。
- **Constraints**：本次只修改第 16–17 列「預計開發」；不實作第 18 列後空白狀態功能；沿用 JWT 與 `ApiResponse<T>`。
- **Scale/Scope**：單一公司主管管理頁、既有主管 API 加員工綁定 API、三張既有資料表。

## Constitution Check

- **分層架構 PASS**：Controller 僅處理 HTTP；Service 持有規則；DAO 僅存取。
- **測試必要性 PASS**：先建立整合測試，再實作 BO/DAO/Service/Controller/React；追加 Newman 與 Cypress。
- **MVP PASS**：既有第 13–15 列保持不變，本次只新增 Sheet 第 16–17 列需要的欄位與操作。
- **業務正確性 PASS**：以資料庫唯一限制及交易 Service 雙重確保一人一公司。
- **向後相容 PASS**：新增路徑，不改既有 API payload。
- **繁中註解 PASS**：所有新增方法與主要段落使用繁中註解。

## Architecture

1. `company` 保存唯一公司名稱與說明。
2. `supervisor_profile` 一對一連到 `app_user`，保存職稱。
3. `company_membership` 以 `user_id` 唯一連接公司，支援一家公司多名成員。
4. JPA DAO 提供忽略大小寫的名稱查詢、關聯存在判斷與綁定搜尋。
5. `CompanySupervisorManagementService` 在交易中執行 CRUD、角色授予/移除、關聯檢查與 DTO 轉換。
6. `CompanySupervisorManagementController` 在 `/api/admin/company-supervisor-management` 暴露 REST API。
7. React `/company-supervisor-management` 以三個標籤呈現管理流程；「綁定公司」內以局部 state 切換主管／員工，Route Guard 與側欄僅供系統管理員。

## Project Structure

```text
specs/009-company-supervisor-management/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── tasks.md
├── analyze.md
├── checklists/requirements.md
└── contracts/openapi.yaml

backend/src/main/java/com/agentflow/base/
├── model/bo/{Company,SupervisorProfile,CompanyMembership}.java
├── model/dto/CompanySupervisorManagementDtos.java
├── dao/{CompanyDao,SupervisorProfileDao,CompanyMembershipDao}.java
├── service/CompanySupervisorManagementService.java
└── controller/CompanySupervisorManagementController.java

frontend/src/pages/CompanySupervisorManagementPage.tsx
frontend/cypress/e2e/company-supervisor-management.cy.ts
postman/company-supervisor-management.postman_collection.json
report/test/result-20260729-company-supervisor-management-{postman,cypress}.md
report/test/result-20260730-company-supervisor-management-{postman,cypress}.md
```

## Test Strategy

- MockMvc：既有公司／主管流程，加上員工資格、一人第二家公司衝突、公司／員工查詢、類型保護與取消後改綁。
- Postman/Newman：以啟動中的 H2 API 逐項確認 status、`ApiResponse`、主管向後相容及員工綁定 response 值。
- Cypress：管理員使用「綁定公司」標籤切換主管／員工並完成兩種綁定主流程，另驗證一般使用者無權限頁。
- 建置：`backend/gradlew test build` 與 `frontend npm run build`。

## Sheet 第 16–17 列增量

1. 保留既有 `/bindings` 主管綁定 API，避免破壞現有 Postman、Cypress 與呼叫端。
2. 新增 `/employee-bindings` 的查詢、建立與取消端點，沿用 `CompanyMembership` 與 `EMPLOYEE` 類型。
3. DAO 新增只查 `EMPLOYEE` 的公司／員工姓名或帳號篩選；Service 驗證啟用、`EMPLOYEE` 角色、非主管及尚未綁定。
4. React 將第三標籤文字改為「綁定公司」，在同一張表單與列表以主管／員工子類型切換。
5. 先新增 MockMvc 與 Cypress 失敗案例，再實作後端與前端，最後用 Newman 與 Cypress 實際驗證並產生 2026-07-30 報告。
