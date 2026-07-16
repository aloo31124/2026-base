# AgentFlow 專案憲章

## 目錄

- [核心原則](#核心原則) — 五項不可妥協的工程原則
- [品質閘門](#品質閘門) — 完工前必須通過的驗證

## 核心原則

1. 所有 Sheet 標示 MUST 的功能都必須具備可追溯的 FR、task 與 checklist。
2. 後端功能必須依序具備 DB 表、JPA DAO、Service、BO 與 REST Controller。
3. 前端使用 React、Hook 與 Redux，樣式及流程遵循 `uiux/`。
4. 所有資料表使用 UUID 主鍵、審計時間、單數 snake_case 表名。
5. 權限、例外與資料操作不得靜默失敗，API 必須回傳明確訊息並留下 Log。

## 品質閘門

- Gradle `build` 必須成功，且同時建置 React。
- 後端 API 必須由 Postman/Newman 實際執行並產生報告。
- 前端流程必須由 Cypress 實際執行並產生報告。
- 所有任務與 checklist 必須歸零後才能回寫 Sheet 為「開發完成」。

