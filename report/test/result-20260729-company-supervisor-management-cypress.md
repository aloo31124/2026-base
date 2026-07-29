# Cypress 測試報告：公司主管管理

## 目錄

- [測試範圍](#測試範圍) — React 三標籤與權限
- [執行環境](#執行環境) — 真實前後端
- [結果摘要](#結果摘要) — 2 scenarios
- [驗收明細](#驗收明細) — task/checklist 對應
- [完成率](#完成率) — 100%

## 測試範圍

- 管理員登入後進入「公司主管管理」頁。
- 公司新增、修改、名稱查詢與刪除。
- 既有註冊使用者建立主管、修改職稱、名稱/職稱查詢與刪除。
- 公司主管綁定、雙條件查詢與取消綁定。
- 一般使用者直接進入頁面時導向無權限頁。

## 執行環境

- 日期：2026-07-29
- 前端：React 19.2.7、Vite 7.3.6，`http://localhost:5174`
- 後端：Spring Boot 4.1.0、H2，`http://localhost:8081`
- Cypress：15.18.1
- Browser：Electron 138 headless
- Spec：`frontend/cypress/e2e/company-supervisor-management.cy.ts`
- 指令：`npx cypress run --spec cypress/e2e/company-supervisor-management.cy.ts --config baseUrl=http://localhost:5174`

## 結果摘要

| 指標 | 總數 | 通過 | 失敗 | Pending | Skipped |
|---|---:|---:|---:|---:|---:|
| Specs | 1 | 1 | 0 | 0 | 0 |
| Scenarios | 2 | 2 | 0 | 0 | 0 |
| Screenshots | 0 | 0 | 0 | 0 | 0 |

執行時間約 8 秒；Cypress exit code 為 0。

## 驗收明細

| Scenario | 對應 task/checklist | 結果 |
|---|---|---|
| 管理員完成公司、主管與綁定 CRUD 及名稱查詢 | T014、T018、T022、T026；CHK001–CHK014 | 通過 |
| 一般使用者導向指定無權限頁 | T024–T026；CHK014、CHK017 | 通過 |

## 完成率

- 前端 task 覆蓋率：100%
- Cypress scenario 通過率：100%
- Checklist 對應項通過率：100%
- **總完成率：100%**
