# Quickstart：我的任務

## 目錄

- [後端](#後端) — 建置與整合測試
- [Postman](#postman) — 真實 HTTP 驗證
- [前端](#前端) — 建置與 Cypress

## 後端

`./backend/gradlew -p backend clean test --tests '*MyTasksIntegrationTest'`

## Postman

啟動後端後執行 `cd postman && npx newman run task-assignment.postman_collection.json`。

## 前端

`npm --prefix frontend run build`，再執行 `npm --prefix frontend run test:e2e:my-tasks`。
