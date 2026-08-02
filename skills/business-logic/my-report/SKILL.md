---
name: my-report
description: 維護 AgentFlow 我的報表、登入員工自己的任務總覽、任務趨勢與狀態比例、執行者／狀態／日期篩選、本人資料隔離、Postman 與 Cypress 驗收時使用。
---

# 我的報表

## 目錄

- [統計口徑](#統計口徑) — 僅登入員工自己的受派任務
- [日期與篩選契約](#日期與篩選契約) — 台北時區、本人、狀態與預設一年
- [分層與 API](#分層與-api) — DB、DAO、Service、DTO、Controller
- [前端契約](#前端契約) — 共用條件、標籤與可存取圖表
- [權限與測試](#權限與測試) — 本人資料邊界與交付門檻

## 統計口徑

「我的報表」只統計 `assigned_task.assignee_user_id` 為目前登入員工的任務，不依建立者、公司或外部 `assigneeId` 擴張範圍。跨員工任務不得計入總數、趨勢或狀態比例；指定的 `assigneeId` 不是本人時必須回 403。

## 日期與篩選契約

使用 `Asia/Taipei` 日界線與含首尾日期，DAO 查詢採起日零時至迄日次日零時的半開 Instant 區間；未指定日期時預設今日往前一年，最多 366 天。執行者選項只有登入員工本人；執行狀態固定為 `PENDING`、`IN_PROGRESS`、`COMPLETED`，對應待處理、進行中、已完成。兩個圖表共用條件，指定單一狀態時其他狀態桶為 0。

## 分層與 API

沿用 `app_user` 與 `assigned_task`，不建立 MVP 報表快照表。DAO 固定以 `task.assignee = :assignee` 查詢最小 projection；Service 驗證本人、日期及狀態，負責台北日期補零與百分比；Controller 只解析 Principal／query 並使用標準 `{ success, message, data, timestamp }` response。API 位於 `/api/my/reports/filters` 與 `/api/my/reports/report`。

## 前端契約

「我的報表」頁顯示自己任務總數、本人、工作狀態與日期範圍，提供執行者、執行狀態與起迄日期共用篩選。「任務趨勢」沿用可存取 SVG 折線圖並補零；「任務狀態比」沿用圓餅圖與精確文字圖例。切換標籤不得重設條件；載入、成功、空資料、錯誤與未登入導頁必須可區分。樣式沿用 `uiux/0.共用樣式`。

## 權限與測試

頁面及 `/api/my/reports/**` 允許所有已登入員工，但資料範圍永遠只限本人。修改本領域時同步更新 `specs/014-my-report/` 的 FR、tasks、checklist 與 analyze，並通過 JUnit／MockMvc、真實 HTTP Postman／Newman、React production build 及 Cypress；產出完成率報告後才可更新來源 Sheet 狀態。
