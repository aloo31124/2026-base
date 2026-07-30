# Data Model：公司主管管理

## 目錄

- [Company](#company) — 公司主檔
- [SupervisorProfile](#supervisorprofile) — 已註冊使用者主管資料
- [CompanyMembership](#companymembership) — 主管／員工共用的一人一公司綁定
- [Relationships](#relationships) — 實體關聯與狀態

## Company

| 欄位 | 型別 | 規則 |
|---|---|---|
| id | UUID | 主鍵 |
| name | nvarchar(120) | 非空、忽略大小寫唯一 |
| description | nvarchar(500) | 可空 |
| created_at / updated_at | timestamp | 稽核欄位 |

## SupervisorProfile

| 欄位 | 型別 | 規則 |
|---|---|---|
| id | UUID | 主鍵 |
| user_id | UUID | 關聯 `app_user`、非空、唯一 |
| title | nvarchar(80) | 非空 |
| created_at / updated_at | timestamp | 稽核欄位 |

## CompanyMembership

| 欄位 | 型別 | 規則 |
|---|---|---|
| id | UUID | 主鍵 |
| company_id | UUID | 關聯 `company`、非空 |
| user_id | UUID | 關聯 `app_user`、非空、唯一 |
| member_type | varchar(20) | `SUPERVISOR` 或 `EMPLOYEE` |
| created_at / updated_at | timestamp | 稽核欄位 |

## Relationships

- `Company 1 — 0..N CompanyMembership`：一家公司可有多名主管或員工。
- `UserAccount 1 — 0..1 CompanyMembership`：資料庫唯一鍵保證一人一家公司。
- `UserAccount 1 — 0..1 SupervisorProfile`：主管資料只能建立於已註冊使用者。
- 建立 `SupervisorProfile` 時附加 `MANAGER`；刪除前必須無 `CompanyMembership`。
- `EMPLOYEE` 綁定僅允許啟用、具 `EMPLOYEE` 角色且沒有 `SupervisorProfile` 的使用者；身分類型由 `member_type` 明確保存。
