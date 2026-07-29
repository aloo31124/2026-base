# 註冊登入管理 Cypress 測試報告

## 目錄

- [測試環境](#測試環境) — React、Cypress、mock backend
- [執行結果](#執行結果) — 2 scenarios
- [紅燈修復](#紅燈修復) — CORS 與非同步輸入
- [完成率](#完成率) — 100%

## 測試環境

- 日期：2026-07-29
- 前端：React 19.2.7、Vite 7.3.6
- 後端：Spring Boot test profile、H2、mock mail
- Cypress：15.18.1，Electron 138 headless
- Spec：`frontend/cypress/e2e/registration-management.cy.ts`

## 執行結果

| Scenario | 結果 |
|---|---|
| 管理員設定密碼政策並檢視信箱註冊成功紀錄 | PASS |
| 一般使用者導向指定無權限頁 | PASS |

- Tests：2
- Passing：2
- Failing：0
- Duration：4 秒
- Screenshots on final run：0

## 紅燈修復

1. 隔離前端使用 5174，初次後端只允許 5173；測試環境加入 5174 CORS 後重跑。
2. 政策資料非同步載入期間會覆寫 number input；測試先等待值為 8，再以全選方式輸入 10。
3. 修正後完整規格重跑，2/2 通過。

## 完成率

**100%（2/2 scenarios）**
