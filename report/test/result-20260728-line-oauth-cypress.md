# LINE OAuth Cypress 測試報告

## 結論

- 測試日期：2026-07-28（Asia/Taipei）
- 子模組：註冊登入驗證 / LINE OAuth 註冊登入
- Spec：`frontend/cypress/e2e/line-oauth.cy.ts`
- Cypress：15.18.1
- Browser：Electron 138（headless）
- Tests：3
- Passing：3
- Failing / Pending / Skipped：0 / 0 / 0
- 完成率：**100%**

## E2E 情境

| # | 對應 task/checklist | 操作與驗證 | 結果 |
| --- | --- | --- | --- |
| 1 | T014–T015、CHK001–003、CHK011 | 登入頁點 LINE 按鈕 → mock LINE 驗證頁 → SPA callback → JWT session → EMPLOYEE 頁 | 通過 |
| 2 | T016、CHK004、CHK012 | 建立授權 state → 模擬使用者取消 → callback 顯示可操作錯誤 → 稽核為 DENIED | 通過 |
| 3 | T008、CHK005 | 完成一次 callback → 使用相同 state 重播 → callback 顯示 state 無效/已使用 | 通過 |

## UI/UX 視覺驗證

使用本機瀏覽器額外驗證：

| Viewport | 驗證內容 | 結果 |
| --- | --- | --- |
| 1280 × 720 | `uiux/0.共用樣式` 雙欄登入、LINE 綠色按鈕、帳密表單、無水平溢位 | 通過 |
| 1280 × 720 | callback 錯誤卡片、明確訊息、返回登入操作 | 通過 |
| 390 × 844 | 手機版隱藏左側視覺區、LINE/帳密操作完整可見、頁寬 390 無溢位 | 通過 |

## Sheet MUST Traceability

| Sheet 列 | MUST 功能 | Cypress 證據 | 完成率 |
| --- | --- | --- | --- |
| 25 | OAuth 驗證後即時登入 | 情境 #1 | 100% |
| 26 | 首次綁定與瀏覽器 LINE 驗證流程 | 情境 #1 | 100% |
| 27 | 首次註冊後可建立 session | 情境 #1 | 100% |
| 28 | 失敗過程入庫與 UI 回饋 | 情境 #2–3 | 100% |

## 已知非阻斷項目

- 本機 Node.js 20.18.0 低於 Vite 建議的 20.19+；production build 與 Cypress 本次均成功。建議 CI/開發機升至 Node 22 LTS。
- `npm audit` 顯示既有相依性樹 2 個 high severity 項目；本次未執行可能造成破壞性升版的 `npm audit fix --force`。
