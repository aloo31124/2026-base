# Data Model：資料庫基礎

## 目錄

- [共用欄位](#共用欄位) — UUID 與審計
- [實體](#實體) — 四張資料表
- [關聯](#關聯) — FK 與唯一鍵

## 共用欄位

所有表：`id uniqueidentifier PK`、`created_at datetimeoffset`、`updated_at datetimeoffset`。

## 實體

- `app_user`：full_name、username、email、password_hash、registration_method、is_active。
- `role`：role_code、display_name。
- `user_role`：user_id、role_id。
- `test`：name、description、test_status。

## 關聯

`user_role.user_id → app_user.id`、`user_role.role_id → role.id`；`(user_id, role_id)` 唯一。

