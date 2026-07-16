# Tasks：例外處理與 Log

## 目錄

- [Phase 1 - Error Contract](#phase-1---error-contract) — 3 項
- [Phase 2 - Logging](#phase-2---logging) — 3 項
- [Phase 3 - Authorization Errors](#phase-3---authorization-errors) — 3 項
- [Phase 4 - Validation](#phase-4---validation) — 2 項

## Phase 1 - Error Contract

- [x] T001 [US1] 建立 ApiResponse envelope 於 backend/src/main/java/com/agentflow/base/model/dto/ApiResponse.java
- [x] T002 [P] [US1] 建立 BusinessException 於 backend/src/main/java/com/agentflow/base/exception/BusinessException.java
- [x] T003 [US1] 建立 @RestControllerAdvice 驗證、業務、權限與未知例外處理於 backend/src/main/java/com/agentflow/base/exception/GlobalExceptionHandler.java

## Phase 2 - Logging

- [x] T004 [US2] 設定 Console 與 30 日 rolling file 於 backend/src/main/resources/logback-spring.xml
- [x] T005 [P] [US2] 在 Controllers 與 JWT filter 加入安全 Log 於 backend/src/main/java/com/agentflow/base/controller、security
- [x] T006 [P] [US2] 在 Services 與 JPA SQL 加入追蹤 Log 於 backend/src/main/java/com/agentflow/base/service、application.yml

## Phase 3 - Authorization Errors

- [x] T007 [US3] 建立 Security 401/403 JSON handler 於 backend/src/main/java/com/agentflow/base/config/SecurityConfig.java
- [x] T008 [US3] 建立前端 UnauthorizedPage 於 frontend/src/pages/UnauthorizedPage.tsx
- [x] T009 [US3] 在 React Guard 區分未登入與無權限於 frontend/src/App.tsx

## Phase 4 - Validation

- [x] T010 [US1] 以 AgentFlowIntegrationTest 驗證一般使用者 API 403 明確訊息
- [x] T011 [US3] 以 Cypress 驗證一般使用者導向無權限頁與指定文字

