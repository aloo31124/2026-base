# Quickstart & Acceptance: 我的報表

## 目錄

- [Prerequisites](#prerequisites) — 本機依賴與測試帳號
- [Backend Verification](#backend-verification) — Gradle 與 API
- [Frontend Verification](#frontend-verification) — build 與頁面
- [MUST Acceptance Matrix](#must-acceptance-matrix) — Sheet 第 23–25 列
- [Report Outputs](#report-outputs) — 測試報告位置

## Prerequisites

- JDK 21、Node/npm（Gradle 可下載專案指定版本）。
- 後端測試使用 `application-test.yml` 的獨立 H2。
- 真實 API/E2E 使用本機後端 `http://localhost:8080` 與前端 `http://localhost:5173`。
- 基礎測試帳號：`user / admin123`。

## Backend Verification

```bash
cd backend
./gradlew test --tests com.agentflow.base.MyReportIntegrationTest
./gradlew test build
```

啟動後端後執行：

```bash
cd postman
npx newman run my-report.postman_collection.json
```

## Frontend Verification

```bash
cd frontend
npm run build
npm run test:e2e:my-report
```

## MUST Acceptance Matrix

| Sheet 列 | MUST 功能 | Backend | Postman | Cypress | 預期 |
|---|---|---|---|---|---|
| 23 | 我的報表個人任務總覽 | 本人隔離與 totalTasks | filters/report response | 頁面摘要 | 只含本人任務 |
| 24 | 任務趨勢折線圖與篩選 | 連續日期與條件聚合 | workStatus/date query | 折線圖與套用篩選 | 預設最近一年 |
| 25 | 任務狀態比圓餅圖與篩選 | 三狀態數量與比例 | statusBuckets | tab、圓餅圖、圖例 | 有資料合計 100% |

## Report Outputs

- `report/test/result-20260802-my-report-postman.md`
- `report/test/result-20260802-my-report-cypress.md0`
