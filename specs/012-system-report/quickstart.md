# Quickstart：系統報表

## 目錄

- [後端整合測試](#後端整合測試) — 系統報表 MockMvc 驗收
- [完整建置](#完整建置) — 後端與 React build
- [Postman](#postman) — 啟動 H2 並執行 Newman
- [Cypress](#cypress) — 啟動前後端並執行 E2E

## 後端整合測試

`./Backend/gradlew -p Backend test --tests '*SystemReportIntegrationTest'`

## 完整建置

`./Backend/gradlew -p Backend clean test` 與 `npm --prefix Frontend run build`。

## Postman

以 `SPRING_PROFILES_ACTIVE=h2 ./Backend/gradlew -p Backend bootRun` 啟動後端，再執行 `npm --prefix postman run test:system-report`。

## Cypress

後端啟動後執行 `npm --prefix Frontend run dev -- --host 127.0.0.1`，再執行 `npm --prefix Frontend run test:e2e:system-report`。
