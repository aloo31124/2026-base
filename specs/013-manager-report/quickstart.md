# Quickstart：主管報表

## 目錄

- [後端整合測試](#後端整合測試) — 主管報表 MockMvc 驗收
- [完整建置](#完整建置) — 後端與 React production build
- [Postman](#postman) — 啟動 H2 並執行 Newman
- [Cypress](#cypress) — 啟動前端並執行 E2E
- [完成檢核](#完成檢核) — task、checklist、報告與 Sheet

## 後端整合測試

`./backend/gradlew -p backend test --tests '*ManagerReportIntegrationTest'`

## 完整建置

`./backend/gradlew -p backend clean test` 與 `npm --prefix frontend run build`。

## Postman

以 `SPRING_PROFILES_ACTIVE=h2 ./backend/gradlew -p backend bootRun` 啟動後端，再執行 `npm --prefix postman run test:manager-report`。

## Cypress

執行 `npm --prefix frontend run dev -- --host 127.0.0.1`，再執行 `npm --prefix frontend run test:e2e:manager-report`。

## 完成檢核

確認 `tasks.md` 無未完成項、`checklists/requirements.md` 為 26/26、Postman 與 Cypress 報告完成率 100%，最後才更新 Google Sheet「任務報表」`B18:B20`。
