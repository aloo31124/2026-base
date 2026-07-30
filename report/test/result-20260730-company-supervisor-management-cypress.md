# Cypress 測試報告：公司主管管理「綁定公司」增量

## 目錄

- [測試範圍](#測試範圍) — React 綁定公司與權限
- [執行環境](#執行環境) — 真實前後端與 Electron
- [結果摘要](#結果摘要) — 1 spec、2 scenarios
- [驗收明細](#驗收明細) — task/checklist 對應
- [環境修正紀錄](#環境修正紀錄) — CORS 測試來源
- [完成率](#完成率) — 100%

## 測試範圍

- 系統管理員登入並進入「公司主管管理」頁。
- 原「綁定」標籤顯示為「綁定公司」。
- 公司新增、修改、名稱查詢與刪除。
- 既有註冊使用者建立主管、修改職稱、查詢與刪除。
- 主管綁定公司、公司／主管雙條件查詢及取消。
- 切換至員工綁定，選擇符合資格員工、建立綁定、公司／員工雙條件查詢及取消。
- 一般使用者直接進入頁面時導向指定無權限頁。

## 執行環境

- 日期：2026-07-30
- 前端：React 19.2.7、Vite 7.3.6，`http://127.0.0.1:5174`
- 後端：Spring Boot 4.1.0、H2，`http://localhost:8081`
- Cypress：15.18.1
- Browser：Electron 138 headless
- Spec：`frontend/cypress/e2e/company-supervisor-management.cy.ts`
- 指令：`npx cypress run --spec cypress/e2e/company-supervisor-management.cy.ts --config baseUrl=http://127.0.0.1:5174`
- 前端 production build：`npm run build` 成功
- 全專案 build：`backend/gradlew test build` 成功

## 結果摘要

| 指標 | 總數 | 通過 | 失敗 | Pending | Skipped |
|---|---:|---:|---:|---:|---:|
| Specs | 1 | 1 | 0 | 0 | 0 |
| Scenarios | 2 | 2 | 0 | 0 | 0 |
| Screenshots（最終成功執行） | 0 | 0 | 0 | 0 | 0 |

最終執行時間約 9 秒；Cypress exit code 為 0。

## 驗收明細

| Scenario | 對應 task/checklist | 結果 |
|---|---|---|
| 管理員完成公司、主管、主管綁定與員工綁定 CRUD 及名稱查詢 | T048–T051；CHK020–CHK026；FR-020–FR-024 | 通過 |
| 一般使用者導向指定無權限頁 | T023–T026；CHK014、CHK017 | 通過 |

主要 UI assertion：

- `binding-tab` 顯示「綁定公司」。
- `employee-binding-kind` 可切換員工流程。
- 員工候選包含 `Demo User 2（user2）`。
- 建立後顯示「公司員工綁定成功」。
- 員工列表同時顯示指定公司、姓名與帳號。
- 取消綁定後可繼續刪除公司，不殘留關聯。

## 環境修正紀錄

第一次執行因前端來源為 `http://127.0.0.1:5174`、後端仍採預設 CORS 來源而得到 `Failed to fetch`。後端以 `CORS_ALLOWED_ORIGIN=http://127.0.0.1:5174` 重新啟動後，在不修改功能程式碼的情況下重跑，2/2 scenarios 全數通過。

## 完成率

- 前端增量 task 覆蓋率：100%
- Cypress spec 通過率：100%（1/1）
- Cypress scenario 通過率：100%（2/2）
- Checklist 對應項通過率：100%（26/26）
- Sheet 第 16–17 列前端 MUST task 完成率：100%
- **總完成率：100%**
