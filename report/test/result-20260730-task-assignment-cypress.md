# 任務指派 Cypress 測試報告

## 目錄

- [結果](#結果) — 1 個完整 E2E
- [覆蓋範圍](#覆蓋範圍) — React 與真實 API

## 結果

- 執行日期：2026-07-30
- 工具：Cypress 15.18.1 / Electron 138
- Tests：1
- Passing：1
- Failing：0
- 完成率：100%
- 執行時間：5 秒

## 覆蓋範圍

1. 以 API 建立公司、主管、員工與公司綁定前置資料。
2. React 頁面依員工信箱搜尋並綁定員工。
3. 由 UI 填寫任務名稱、內容、期限及受派人並建立。
4. 由 UI 依任務名稱、員工帳號與排序查詢。
5. 員工以真實 API 退回並保存原因。
6. 主管由 UI 修改後重新指派，再撤回任務並顯示 `WITHDRAWN`。

指令：`npm --prefix frontend run test:e2e:task-assignment`
