# Data Model：系統報表

## 目錄

- [既有 DB 表](#既有-db-表) — 報表資料來源與關聯
- [DAO 查詢模型](#dao-查詢模型) — 半開期間與公司條件
- [BO/DTO 回應模型](#bodto-回應模型) — 公司、趨勢點與摘要
- [Validation Rules](#validation-rules) — 日期、公司與零資料規則

## 既有 DB 表

- `company`：`id`、`name`，提供公司篩選主檔。
- `company_membership`：`company_id`、`user_id`，每位使用者唯一公司歸屬。
- `assigned_task`：`assignee_user_id`、`assigned_at`，作為任務趨勢統計來源。

關聯：`assigned_task.assignee_user_id → company_membership.user_id → company.id`。本 feature 不新增資料表或欄位。

## DAO 查詢模型

- 輸入：`fromInclusive: Instant`、`toExclusive: Instant`、可選 `companyId: UUID`。
- 回傳：期間內且受派人具有公司歸屬的 `AssignedTask`，載入受派人資料供 Service 映射公司。
- 公司篩選：有 `companyId` 時僅保留該公司；沒有時涵蓋全部公司。

## BO/DTO 回應模型

- `CompanyOption(id, name)`：公司下拉選項。
- `TaskTrendPoint(date, taskCount)`：一個本地日期及非負任務量。
- `TaskTrendReport(companyId, companyName, from, to, totalTasks, companyCount, points)`：篩選與趨勢摘要。

## Validation Rules

- `from` 與 `to` 皆為 ISO `yyyy-MM-dd`。
- `from <= to`，含首尾共 1–366 天。
- 指定 `companyId` 必須存在；不存在回傳 404。
- 無任務日補 `taskCount=0`；完全無資料仍回傳完整日期點。
- 任務受派人無 `company_membership` 時排除，不猜測歸屬。
