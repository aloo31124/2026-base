# Data Model：我的任務

## 目錄

- [AssignedTask 延伸](#assignedtask-延伸) — 工作狀態與流程欄位
- [TaskAttachment](#taskattachment) — 附件資料表

## AssignedTask 延伸

- `work_status`：`PENDING | IN_PROGRESS | COMPLETED`，預設 `PENDING`。
- `progress_content`：NVARCHAR(4000)，可空。
- `progress_percent`：10–100 且為 10 倍數，預設 10。
- `submitted_at`：提交審核時間，可空。
- `extension_reason`：NVARCHAR(500)，可空。
- `extension_requested_at`：申請延期時間，可空。

既有生命週期 `status` 不變；提交與延期狀態由對應時間及原因欄位判定。

## TaskAttachment

- `id`、`created_at`、`updated_at`：沿用 `BaseEntity`。
- `task_id`：必填 FK 至 `assigned_task`。
- `uploader_user_id`：必填 FK 至 `user_account`。
- `file_name`：必填，最長 255。
- `content_type`：必填，最長 120。
- `file_size`：必填，最大 10 MB。
- `content`：必填 BLOB。
