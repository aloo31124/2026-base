# Tasks：使用者角色權限

## 目錄

- [Phase 1 - Data and Seed](#phase-1---data-and-seed) — 4 項
- [Phase 2 - Authentication](#phase-2---authentication) — 4 項
- [Phase 3 - User Administration](#phase-3---user-administration) — 5 項
- [Phase 4 - React Pages](#phase-4---react-pages) — 4 項
- [Phase 5 - Validation](#phase-5---validation) — 3 項

## Phase 1 - Data and Seed

- [x] T001 [P] [US2] 建立 UserAccount、Role、UserRole BO 於 backend/src/main/java/com/agentflow/base/model/bo
- [x] T002 [P] [US2] 建立三個 JPA DAO 於 backend/src/main/java/com/agentflow/base/dao
- [x] T003 [US2] 建立四組指定帳號與三角色 seed 於 backend/src/main/java/com/agentflow/base/config/SeedDataConfig.java
- [x] T004 [US2] 以 BCrypt 雜湊所有初始密碼且僅在 user 表為空時建立

## Phase 2 - Authentication

- [x] T005 [US1] 建立 UserDetails AccountService 於 backend/src/main/java/com/agentflow/base/service/AccountService.java
- [x] T006 [P] [US1] 建立 JWT 簽發與解析於 backend/src/main/java/com/agentflow/base/security/JwtService.java
- [x] T007 [US1] 建立 JwtAuthenticationFilter 與 SecurityFilterChain 於 backend/src/main/java/com/agentflow/base/security、config
- [x] T008 [US1] 建立 AuthService/AuthController 登入 API 於 backend/src/main/java/com/agentflow/base/service、controller

## Phase 3 - User Administration

- [x] T009 [US3] 建立使用者查詢、新增、編輯、停用 Service 於 backend/src/main/java/com/agentflow/base/service/UserService.java
- [x] T010 [US3] 建立 SYSTEM_ADMIN method security REST API 於 backend/src/main/java/com/agentflow/base/controller/UserController.java
- [x] T011 [US3] 管理員新增使用者標記管理員新增並預設 EMPLOYEE
- [x] T012 [US4] 建立 MANAGER 附加角色與冪等檢查於 backend/src/main/java/com/agentflow/base/service/UserService.java
- [x] T013 [US5] 建立管理 API 403 與安全 Response

## Phase 4 - React Pages

- [x] T014 [US1] 建立 JWT 登入頁與 Redux auth slice 於 frontend/src/pages/LoginPage.tsx、features/auth
- [x] T015 [US3] 建立使用者列表、新增、停用頁於 frontend/src/pages/UsersPage.tsx
- [x] T016 [US4] 建立角色 tab 與授予主管操作於 frontend/src/pages/UsersPage.tsx
- [x] T017 [US5] 建立管理員 Guard 與無權限說明頁於 frontend/src/App.tsx、pages/UnauthorizedPage.tsx

## Phase 5 - Validation

- [x] T018 [US1] 執行登入與四組 seed 後端整合測試
- [x] T019 [US3] 執行 Postman 使用者新增預設員工與 Response assertions
- [x] T020 [US3] 執行 Cypress 管理員流程、一般使用者擋權與 UI 樣式流程

