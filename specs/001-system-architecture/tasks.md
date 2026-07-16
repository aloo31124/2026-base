# Tasks：系統架構設計

## 目錄

- [Phase 1 - Setup](#phase-1---setup) — 3 項
- [Phase 2 - Backend Layers](#phase-2---backend-layers) — 3 項
- [Phase 3 - React MVVM](#phase-3---react-mvvm) — 3 項
- [Phase 4 - Validation](#phase-4---validation) — 2 項

## Phase 1 - Setup

- [x] T001 建立 Spring Boot 4.1 與 Gradle 9.6.1 設定於 backend/build.gradle
- [x] T002 將 Gradle Wrapper 與 settings 全部放置於 backend/gradle、backend/gradlew、backend/settings.gradle
- [x] T003 設定 Gradle Node plugin 串接 frontend/package.json production build

## Phase 2 - Backend Layers

- [x] T004 [US2] 建立 BO 與共用 DTO 於 backend/src/main/java/com/agentflow/base/model
- [x] T005 [US2] 建立 JPA DAO 與 Service 於 backend/src/main/java/com/agentflow/base/dao、service
- [x] T006 [US2] 建立 REST Controller 於 backend/src/main/java/com/agentflow/base/controller

## Phase 3 - React MVVM

- [x] T007 [US3] 建立 Redux store、typed hooks 與 slices 於 frontend/src/app、features
- [x] T008 [US3] 建立 React Router 與 pages 於 frontend/src/App.tsx、frontend/src/pages
- [x] T009 [US3] 套用 uiux 色彩、表格、tab、登入與 responsive 樣式於 frontend/src/styles.css

## Phase 4 - Validation

- [x] T010 [US1] 執行 backend/gradlew.bat build 並確認後端 JAR 與 frontend/dist
- [x] T011 [US1] 執行 Cypress 架構與頁面情境於 frontend/cypress/e2e/base-system.cy.ts

