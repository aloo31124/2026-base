---
name: system-report
description: 維護 AgentFlow 系統管理員跨公司任務報表、公司歸屬統計、日期趨勢、React 折線圖、Postman 與 Cypress 驗收時使用。
---

# 系統報表

## 目錄

- [統計口徑](#統計口徑) — 任務公司歸屬與日期來源
- [日期契約](#日期契約) — 台北時區、預設一年與補零
- [分層與 API](#分層與-api) — DAO、Service、DTO、Controller
- [前端契約](#前端契約) — 篩選、摘要與 SVG 折線圖
- [權限與測試](#權限與測試) — 管理員邊界與交付門檻

## 統計口徑

將每筆 `AssignedTask` 依受派人在 `company_membership` 的唯一公司歸屬計入一次；不要依建立者公司計數，也不要同時計入雙方公司。受派人未綁公司時排除，避免猜測歸屬。以 `assignedAt` 表示任務新增量，後續狀態異動不重分日期。

## 日期契約

使用 `Asia/Taipei` 日界線，查詢區間為含起日、含迄日。DAO 以起日零時至迄日次日零時的半開 Instant 區間查詢；Service 轉回 `LocalDate` 並補齊所有無任務日為 0。未指定日期時預設今日往前一年至今日，最多允許 366 天；起日晚於迄日或超出上限時回傳 400。

## 分層與 API

沿用 `company`、`company_membership`、`assigned_task`，不要為 MVP 新增快照表。DAO 只回傳彙總所需的任務時間與公司識別；Service 驗證公司、日期、彙總與補零；Controller 只解析參數並使用標準 `{ success, message, data, timestamp }` response。指定公司不存在時回傳 404。

## 前端契約

「系統報表」頁預設顯示「任務趨勢」標籤，提供全部／單一公司與起迄日期篩選，顯示任務總數、包含公司數及日期摘要。折線圖使用可存取 SVG，具 `role=img`、標題、說明、日期座標與文字摘要；載入、成功、空資料及錯誤狀態必須可區分。樣式沿用 `uiux/0.共用樣式` 的頁首、標籤、卡片與篩選流程。

## 權限與測試

頁面及 `/api/admin/system-reports/**` 僅允許 `SYSTEM_ADMIN`；其他角色回傳「[系統報表] [api] 無系統管理員權限。」。修改本領域時同步更新 `specs/012-system-report/` 的 FR、tasks、checklist 與 analyze，並通過 JUnit/MockMvc、真實 HTTP Postman/Newman、React production build 及 Cypress，產出完成率報告後才可更新來源 Sheet 狀態。
