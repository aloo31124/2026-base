# Data Model: 我的報表

## 目錄

- [持久化資料](#持久化資料) — 沿用 2 個既有實體
  - [UserAccount](#useraccount) — 登入員工
  - [AssignedTask](#assignedtask) — 受派任務
- [DAO Projection](#dao-projection) — 最小報表來源欄位
- [BO／DTO](#bodto) — filters、report、trend、status
- [Validation Rules](#validation-rules) — 本人、日期與狀態約束
- [State Transitions](#state-transitions) — 唯讀功能

## 持久化資料

### UserAccount

| 欄位 | 型別 | 約束 | 報表用途 |
|---|---|---|---|
| id | UUID | 主鍵 | 本人識別與 assigneeId 驗證 |
| username | String | unique, non-null | principal 定位 |
| fullName | String | non-null | 執行者顯示名稱 |

### AssignedTask

| 欄位 | 型別 | 約束 | 報表用途 |
|---|---|---|---|
| id | UUID | 主鍵 | 任務唯一性 |
| assignee | UserAccount | non-null FK | 強制本人資料邊界 |
| assignedAt | Instant | non-null | 台北日期趨勢 |
| workStatus | enum | nullable legacy | 狀態篩選與比例；空值視為 PENDING |

本功能不新增 DB 表；正式 MSSQL 與測試 H2 皆沿用既有 JPA schema。

## DAO Projection

`MyTaskSource(assignedAt, assigneeId, assigneeName, workStatus)` 僅讀圖表聚合必要欄位，查詢條件固定包含 `task.assignee = :assignee`，另含期間與可選工作狀態。

## BO／DTO

- `AssigneeOption(id, name)`：登入員工本人唯一選項。
- `WorkStatusOption(value, label)`：三種狀態選項。
- `MyReportFilters(assignees, workStatuses, defaultFrom, defaultTo)`：頁面初始化。
- `TaskTrendPoint(date, taskCount)`：連續每日資料點。
- `StatusBucket(status, label, taskCount, percentage)`：狀態數量與比例。
- `MyReport(from, to, assigneeId, assigneeName, workStatus, totalTasks, trendPoints, statusBuckets)`：綜合結果。

## Validation Rules

- principal 對應使用者不存在：401。
- `assigneeId` 未指定代表本人；指定時必須等於本人，否則 403。
- 空白 `workStatus` 代表全部；未知值回 400。
- `from <= to` 且含起迄日最多 366 天；違反回 400。
- 所有日期以 `Asia/Taipei` 轉換為 Instant 查詢邊界。

## State Transitions

本功能唯讀，不改變 `AssignedTask` 狀態。狀態僅由既有任務流程在 `PENDING → IN_PROGRESS → COMPLETED` 間更新。
