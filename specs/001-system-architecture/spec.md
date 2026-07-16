# Feature Specification：系統架構設計

**來源**：Google Sheet「基礎架構使用者分權」第 13–17 列

## 目錄

- [User Stories](#user-stories) — 3 條可獨立驗收情境
- [Functional Requirements](#functional-requirements) — 7 條架構需求
- [Edge Cases](#edge-cases) — 建置與服務異常
- [Success Criteria](#success-criteria) — 4 項量化成果
- [Assumptions](#assumptions) — 自動決策與理由

## User Stories

### User Story 1 - 一鍵建置前後端 (Priority: P1)

開發者從 `backend/` 執行單一 Gradle build，即可同時完成 Spring Boot 與 React production build。

**Acceptance Scenarios**：建置命令回傳 0；產生後端 JAR 與前端 `dist/`。

### User Story 2 - 分層開發功能 (Priority: P1)

開發者依 BO、DAO、Service、Controller 的 MVC 分層新增 REST 功能，避免跨層耦合。

**Acceptance Scenarios**：測試 CRUD 同時經過 JPA、Service 與 REST Controller。

### User Story 3 - React MVVM 操作 (Priority: P2)

使用者在 React 頁面透過 Hook 與 Redux 管理狀態，畫面遵循 `uiux/` 操作流程。

**Acceptance Scenarios**：登入與使用者頁均由集中狀態驅動，重新整理後保留登入狀態。

## Functional Requirements

- **FR-001**：系統 MUST 採前後端分離，後端只提供 Server REST API。
- **FR-002**：後端 MUST 位於 `backend/`，前端 MUST 位於 `frontend/`。
- **FR-003**：後端 MUST 使用 Spring Boot 4.1.0 與 Java 21 可重現建置。
- **FR-004**：前端 MUST 使用 React 19.2、Hook、Redux Toolkit 與 MVVM 狀態分離。
- **FR-005**：所有 Gradle 設定檔與 Wrapper MUST 位於 `backend/`。
- **FR-006**：Gradle build MUST 自動執行前端 production build。
- **FR-007**：後端功能 MUST 依 BO、JPA DAO、Service、REST Controller 分層。

## Edge Cases

- npm 或依賴下載失敗時，Gradle 必須回傳非零狀態並保留明確錯誤。
- API 與前端不同源時，只允許設定的前端 Origin。

## Success Criteria

- **SC-001**：單一建置命令 100% 完成後端測試與前端編譯。
- **SC-002**：所有 API 均有一致 JSON envelope。
- **SC-003**：UI 在 320px 與桌面寬度均可操作。
- **SC-004**：架構 FR 的 task 覆蓋率為 100%。

## Assumptions

- 「最新版」採 2026-07-16 官方穩定版；Java 以本機可驗證且 Spring Boot 4.1 支援的 Java 21，優先確保可重現建置。
- 原需求 `fronend` 視為拼字誤植，依既有目錄使用 `frontend/`。

