---
name: manager-report
description: 維護 AgentFlow 主管報表、公司任務總覽、主管自己指派的任務趨勢與狀態比例、執行者／狀態／日期篩選、Postman 與 Cypress 驗收時使用。
---

# 主管報表

## 目錄

- [統計口徑](#統計口徑) — 公司總覽與主管圖表的不同範圍
- [日期與篩選契約](#日期與篩選契約) — 台北時區、執行者、狀態與預設一年
- [分層與 API](#分層與-api) — DB、DAO、Service、DTO、Controller
- [前端契約](#前端契約) — 共用條件、標籤與可存取圖表
- [權限與測試](#權限與測試) — 主管邊界與交付門檻

## 統計口徑

公司任務總覽依任務受派人在 `company_membership` 的唯一公司歸屬統計，涵蓋同公司所有主管建立的任務；「指派任務趨勢」與「指派任務狀態比」只統計 `creator` 為目前登入主管的任務。不要把兩種範圍合併，否則會少算公司總量或暴露其他主管個人指派明細。

## 日期與篩選契約

使用 `Asia/Taipei` 日界線與含首尾日期，DAO 查詢採起日零時至迄日次日零時的半開 Instant 區間；未指定日期時預設今日往前一年，最多 366 天。執行者只列目前主管實際指派過的對象；執行狀態固定為 `PENDING`、`IN_PROGRESS`、`COMPLETED`，對應待處理、進行中、已完成。兩個圖表共用條件，指定單一狀態時其他狀態桶為 0。

## 分層與 API

沿用 `company`、`company_membership`、`app_user`、`assigned_task`，不建立 MVP 快照表。DAO 只回傳任務時間、受派人與工作狀態的最小 projection；Service 驗證主管身分、公司、日期與篩選，並負責補零及比例；Controller 只解析 Principal／query 並使用標準 `{ success, message, data, timestamp }` response。API 位於 `/api/manager/reports/filters` 與 `/api/manager/reports/report`。

## 前端契約

「主管報表」頁顯示公司任務總數與目前主管指派任務數，提供執行者、執行狀態與起迄日期共用篩選。「指派任務趨勢」使用可存取 SVG 折線圖並補零；「指派任務狀態比」使用圓餅圖加精確文字圖例。切換標籤不得重設條件；載入、成功、空資料與錯誤狀態必須可區分。樣式沿用 `uiux/0.共用樣式`。

## 權限與測試

頁面及 `/api/manager/reports/**` 僅允許 `MANAGER`；其他角色回傳「[主管報表] [api] 無主管權限。」。修改本領域時同步更新 `specs/013-manager-report/` 的 FR、tasks、checklist 與 analyze，並通過 JUnit／MockMvc、真實 HTTP Postman／Newman、React production build 及 Cypress；產出完成率報告後才可更新來源 Sheet 狀態。
