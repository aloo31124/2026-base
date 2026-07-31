# Quickstart：任務指派驗收

## 目錄

- [建置](#建置) — 全專案
- [API](#api) — Newman
- [E2E](#e2e) — Cypress

## 建置

`backend/gradlew -p backend clean build`

## API

啟動 `SPRING_PROFILES_ACTIVE=h2 ./gradlew -p backend bootRun` 後執行 `npm --prefix postman run test:task-assignment`。

## E2E

同時啟動後端與 `npm --prefix frontend run dev`，執行 `npm --prefix frontend run test:e2e:task-assignment`。
