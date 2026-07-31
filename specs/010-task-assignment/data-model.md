# Data Model：任務指派

## 目錄

- [主管員工綁定](#主管員工綁定) — 一位員工最多一位主管
- [任務](#任務) — 單一受派人與狀態
- [狀態轉換](#狀態轉換) — 指派、退回、撤回

## 主管員工綁定

`supervisor_employee_binding`

- `id` UUID PK
- `supervisor_user_id` UUID FK → `app_user`
- `employee_user_id` UUID FK → `app_user`，UNIQUE
- `created_at`、`updated_at`

## 任務

`assigned_task`

- `id` UUID PK
- `name` NVARCHAR(160) NOT NULL
- `content` NVARCHAR(4000)
- `deadline` TIMESTAMP NOT NULL
- `creator_user_id` UUID FK → `app_user`
- `assignee_user_id` UUID FK → `app_user`
- `assigned_at` TIMESTAMP NOT NULL
- `status` VARCHAR(20) NOT NULL
- `return_reason` NVARCHAR(500) NULL
- `returned_at` TIMESTAMP NULL
- `created_at`、`updated_at`

## 狀態轉換

- `ASSIGNED → RETURNED`：僅受派人，必填原因。
- `ASSIGNED → WITHDRAWN`：僅建立者。
- `RETURNED → ASSIGNED`：建立者修改任務即重新指派。
