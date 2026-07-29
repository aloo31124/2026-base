# Tasks：LINE OAuth 註冊登入

## Phase 1 - Data and Persistence

- [x] T001 [P] [US2] 建立 `LineOAuthAccount` BO 與 `LineOAuthAccountDao`
- [x] T002 [P] [US3] 建立 `LineOAuthAttempt` BO、狀態轉換與 `LineOAuthAttemptDao`
- [x] T003 [US2] 擴充 `UserAccountDao` 與首次 LINE 註冊的 EMPLOYEE 關聯

## Phase 2 - OAuth Provider and Service

- [x] T004 [P] [US1] 建立 LINE OAuth 環境設定與官方 v2.1 HTTP client
- [x] T005 [P] [US3] 建立 state、nonce、PKCE S256 產生與雜湊工具
- [x] T006 [P] [US1] 建立僅測試開關啟用的 mock LINE provider
- [x] T007 [US1] 建立授權開始 Service 與 PENDING attempt
- [x] T008 [US1] 建立 callback state/逾時/重播驗證與 token/ID token 驗證
- [x] T009 [US3] 建立成功、拒絕、provider error 稽核終態且清除暫存安全值
- [x] T010 [US2] 建立首次註冊、既有綁定登入、EMPLOYEE 角色與 JWT session

## Phase 3 - REST API

- [x] T011 [US1] 建立 `GET /api/auth/line/authorize`
- [x] T012 [US1] 建立 `POST /api/auth/line/callback`
- [x] T013 [US4] 建立一致 ApiResponse、輸入驗證與安全錯誤訊息

## Phase 4 - React Pages

- [x] T014 [US1] 在 LoginPage 建立符合 uiux 的 LINE 登入按鈕與分隔視覺
- [x] T015 [US1] 建立 `LineOAuthCallbackPage`、Redux callback thunk、session 儲存與導向
- [x] T016 [US4] 建立 callback 載入、取消與失敗畫面

## Phase 5 - Validation

- [x] T017 [P] 建立 LINE OAuth JUnit/MockMvc 首次、再次、失敗與重播測試
- [x] T018 [P] 建立 LINE OAuth Postman collection 與 response/database assertions
- [x] T019 [P] 建立 LINE OAuth Cypress 官方頁導向、callback session 與錯誤 UI 測試
- [x] T020 執行 Gradle build、Postman/Newman、Cypress 並達 100%
- [x] T021 產生 `report/test/result-20260728-line-oauth-postman.md`
- [x] T022 產生 `report/test/result-20260728-line-oauth-cypress.md`
- [x] T023 逐項完成 requirements checklist 並回寫 Sheet B25:B28 為「開發完成」
