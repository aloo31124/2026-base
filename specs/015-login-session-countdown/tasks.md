# Tasks：登入工作階段倒數

## 目錄

- [Phase 1: Setup & Specification](#phase-1-setup--specification) — 4 項
- [Phase 2: Foundational Tests](#phase-2-foundational-tests) — 2 項
- [Phase 3: User Story 1 - 顯示登入倒數](#phase-3-user-story-1---顯示登入倒數) — 3 項
- [Phase 4: User Story 2 - 到期自動登出](#phase-4-user-story-2---到期自動登出) — 3 項
- [Phase 5: User Story 3 & 4 - 校時與跨登入一致性](#phase-5-user-story-3--4---校時與跨登入一致性) — 3 項
- [Phase 6: Polish & Acceptance](#phase-6-polish--acceptance) — 5 項
- [Dependencies](#dependencies) — 執行順序
- [Parallel Opportunities](#parallel-opportunities) — 可平行工作
- [Implementation Strategy](#implementation-strategy) — 短效 JWT TDD

## Phase 1: Setup & Specification

- [x] T001 建立 `specs/015-login-session-countdown/spec.md` 並定義 4 條 User Story 與 11 條 FR
- [x] T002 [P] 建立 `specs/015-login-session-countdown/checklists/requirements.md` 並完成 24 項需求品質檢核
- [x] T003 [P] 建立 `specs/015-login-session-countdown/plan.md`、`research.md`、`data-model.md` 與 `quickstart.md`
- [x] T004 [P] 建立 `specs/015-login-session-countdown/contracts/session-countdown.md` 與 `analyze.md`

## Phase 2: Foundational Tests

- [x] T005 新增短效 JWT、倒數遞減、到期登出與窄螢幕情境於 `frontend/cypress/e2e/login-session-countdown.cy.ts`
- [x] T006 確認 T005 在未實作 `SessionCountdown` 前紅燈（2026-08-02：0 passing / 3 failing，皆因倒數 test id 尚不存在）並保留失敗證據於本任務紀錄

## Phase 3: User Story 1 - 顯示登入倒數

- [x] T007 [US1] 在 `frontend/src/features/auth/sessionExpiry.ts` 與 `frontend/src/components/SessionCountdown.tsx` 實作 JWT exp 解析與 `HH:MM:SS` 格式化
- [x] T008 [US1] 在 `frontend/src/components/SessionCountdown.tsx` 實作每秒絕對時間重算與 timer 語意
- [x] T009 [US1] 在 `frontend/src/components/AppShell.tsx` 將倒數接入右上角且保留既有 mobile sidebar 修改

## Phase 4: User Story 2 - 到期自動登出

- [x] T010 [US2] 在 `frontend/src/components/AppShell.tsx` 實作一次性逾時 logout 與 replace navigation
- [x] T011 [US2] 在 `frontend/src/pages/LoginPage.tsx` 顯示逾時重新登入訊息
- [x] T012 [US2] 在 `frontend/src/features/auth/authSlice.ts` 強化已到期 session 初始載入與安全 JSON 解析

## Phase 5: User Story 3 & 4 - 校時與跨登入一致性

- [x] T013 [US3] 在 `frontend/src/components/SessionCountdown.tsx` 實作 visibilitychange 與 focus 立即校時
- [x] T014 [US4] 驗證 `SessionCountdown` 僅依共用 `Session.token`，不耦合帳密、信箱或 LINE 流程
- [x] T015 [US3] 在 `frontend/src/styles.css` 實作五分鐘警示、桌面與 720px 以下 RWD 樣式

## Phase 6: Polish & Acceptance

- [x] T016 在 `frontend/package.json` 新增專屬 Cypress script
- [x] T017 執行 `npm run build`（成功；80 modules transformed）並修正所有 TypeScript/Vite 問題
- [x] T018 實際啟動前端並執行 `npm run test:e2e:login-session-countdown` 至 5/5 綠燈
- [x] T019 建立 `skills/business-logic/login-session-countdown/SKILL.md` 並更新 `skills/SKILLS_INDEX.md`
- [x] T020 重核 FR-001–011、24 項 checklist 與 `TASKS_PENDING=0`

## Dependencies

`T001–T004 → T005 → T006 → T007–T009 → T010–T012 → T013–T016 → T017 → T018 → T019–T020`

- T005 先建立使用者可見行為的紅燈；T007 起才新增正式元件。
- AppShell 的 T009/T010 必須在使用者既有 staged mobile sidebar 內容上增量修改。
- build 與 Cypress 都綠燈後才完成 business skill 與 closeout。

## Parallel Opportunities

- T002、T003、T004 在 spec 定稿後可針對不同文件平行完成。
- T011 與 T015 修改不同檔案，但仍在核心 expiry callback 確定後執行。
- 本功能核心元件與 AppShell 有介面依賴，實作階段採依序完成以降低同檔衝突。

## Implementation Strategy

先以 Cypress 固定現在時間並注入短效 JWT 建立紅燈，再實作純前端 `SessionCountdown`。倒數只讀 token、不延長 session；AppShell 負責既有 logout 與 Router 導頁。最後以 production build 與專屬 E2E 雙重驗收。
