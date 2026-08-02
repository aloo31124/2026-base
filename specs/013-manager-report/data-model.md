# Data Model：主管報表

## 目錄

- [既有 DB 表](#既有-db-表) — 報表資料來源與關聯
- [DAO 查詢模型](#dao-查詢模型) — 公司及主管任務投影
- [BO/DTO 回應模型](#bodto-回應模型) — 篩選、摘要、趨勢與比例
- [Validation Rules](#validation-rules) — 身分、公司、日期與條件規則

## 既有 DB 表

- `company`：`id`、`name`，界定主管唯一公司範圍。
- `company_membership`：`company_id`、`user_id`、`member_type`，每位使用者唯一公司歸屬。
- `app_user`：`id`、`full_name`、`username`，提供主管與執行者識別及顯示名稱。
- `assigned_task`：`creator_user_id`、`assignee_user_id`、`assigned_at`、`status`、`work_status`，作為公司摘要、趨勢與比例來源。

公司總覽關聯：`assigned_task.assignee_user_id → company_membership.user_id → company.id`。主管圖表關聯：`assigned_task.creator_user_id → app_user.id`。本 feature 不新增資料表、欄位或 migration。

## DAO 查詢模型

- `CompanyTaskSource(assignedAt)`：指定公司及期間內的任務最小資料，用於公司總數。
- `ManagerTaskSource(assignedAt, assigneeId, assigneeName, workStatus)`：目前主管建立且符合期間的任務資料，用於執行者、趨勢及比例。
- 兩個查詢皆採 `fromInclusive`、`toExclusive` 半開 Instant 區間；執行者與工作狀態為可選條件。

## BO/DTO 回應模型

- `AssigneeOption(id, name)`：目前主管曾指派的執行者選項。
- `ManagerReportFilters(companyName, assignees, workStatuses, defaultFrom, defaultTo)`：頁面初始化資料。
- `TaskTrendPoint(date, taskCount)`：一個本地日期及非負任務量。
- `StatusBucket(status, label, taskCount, percentage)`：一個執行狀態的數量與百分比。
- `ManagerReport(companyName, from, to, assigneeId, assigneeName, workStatus, companyTotalTasks, managerTotalTasks, trendPoints, statusBuckets)`：同一次篩選的完整報表。

## Validation Rules

- Principal 必須對應既有使用者且具有主管資料及唯一公司綁定。
- `from`、`to` 為 ISO `yyyy-MM-dd`，`from <= to`，含首尾 1–366 天。
- `assigneeId` 若提供，必須存在於目前主管曾指派對象；否則回傳 404。
- `workStatus` 若提供，必須為 `PENDING`、`IN_PROGRESS`、`COMPLETED`；否則回傳 400。
- 無任務日補 `taskCount=0`；無任務時三個狀態桶均為 0%，避免除以零。
- 百分比四捨五入至一位小數，最後一桶吸收四捨五入差額，使有資料時合計為 100%。
