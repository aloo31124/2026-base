# Tasks：Gmail 信箱驗證測試

## 目錄

- [Phase 1 - Setup](#phase-1---setup) — 2 項
- [Phase 2 - Foundational Tests](#phase-2---foundational-tests) — 2 項
- [Phase 3 - User Story 1 寄送驗證碼](#phase-3---user-story-1-寄送驗證碼) — 4 項
- [Phase 4 - User Story 2 顯示寄送結果](#phase-4---user-story-2-顯示寄送結果) — 2 項
- [Phase 5 - User Story 3 管理員權限](#phase-5---user-story-3-管理員權限) — 3 項
- [Phase 6 - Polish and Validation](#phase-6---polish-and-validation) — 3 項
- [Dependencies](#dependencies) — 執行順序

## Phase 1 - Setup

- [x] T001 在 `backend/build.gradle` 加入 Spring Mail 相依套件
- [x] T002 在 `backend/src/main/resources/application.yml` 與 `.env.example` 建立非敏感 Gmail SMTP 設定介面

## Phase 2 - Foundational Tests

- [x] T003 [P] 建立 `backend/src/test/java/com/agentflow/base/service/EmailVerificationServiceTest.java` 服務成功、遮罩與 SMTP 失敗測試
- [x] T004 [P] 建立 `backend/src/test/java/com/agentflow/base/EmailVerificationIntegrationTest.java` 管理員、一般使用者與無效 email API 測試

## Phase 3 - User Story 1 寄送驗證碼

- [x] T005 [US1] 建立 `backend/src/main/java/com/agentflow/base/config/MailProperties.java` 與設定註冊
- [x] T006 [US1] 建立 `backend/src/main/java/com/agentflow/base/service/MailGateway.java` 及 `SmtpMailGateway.java`
- [x] T007 [US1] 建立 `backend/src/main/java/com/agentflow/base/service/EmailVerificationService.java` 安全亂數與信件內容
- [x] T008 [US1] 建立 `backend/src/main/java/com/agentflow/base/model/dto/EmailVerificationDtos.java` 與 `EmailVerificationController.java`

## Phase 4 - User Story 2 顯示寄送結果

- [x] T009 [US2] 建立 `frontend/src/pages/EmailVerificationPage.tsx` 的輸入、載入、成功與失敗狀態
- [x] T010 [US2] 在 `frontend/src/styles.css` 加入信箱驗證頁響應式樣式

## Phase 5 - User Story 3 管理員權限

- [x] T011 [US3] 在 `backend/src/main/java/com/agentflow/base/controller/EmailVerificationController.java` 套用 SYSTEM_ADMIN 權限
- [x] T012 [US3] 在 `frontend/src/App.tsx` 新增管理員 Guard 路由
- [x] T013 [US3] 在 `frontend/src/components/AppShell.tsx` 僅對系統管理員顯示信箱驗證導覽

## Phase 6 - Polish and Validation

- [x] T014 更新 `skills/business-logic/email-verification/SKILL.md` 與 `skills/SKILLS_INDEX.md`
- [x] T015 執行 `./backend/gradlew -p backend test` 與 `./backend/gradlew -p backend build`
- [x] T016 執行 `cd frontend && npm run build` 並逐條核對 FR-001 至 FR-010

## Dependencies

- T001–T002 完成後執行測試與核心實作。
- T003–T004 先建立並確認在核心實作前無法通過。
- T005–T008 完成後，T003–T004 應轉為綠燈。
- T009–T013 可在後端契約固定後進行。
- T014–T016 於全部功能完成後執行。
