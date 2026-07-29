# Data Model：註冊登入管理

## 目錄

- [PasswordPolicy](#passwordpolicy) — 密碼政策單筆設定
- [RegistrationRecord](#registrationrecord) — 首次註冊稽核
- [Relationships](#relationships) — 與 app_user 關聯

## PasswordPolicy

| 欄位 | 型別 | 規則 |
|---|---|---|
| id | UUID | 主鍵 |
| min_length | integer | 8–72 |
| require_letter | boolean | 非空 |
| require_number | boolean | 非空 |
| created_at / updated_at | timestamp | 稽核欄位 |

## RegistrationRecord

| 欄位 | 型別 | 規則 |
|---|---|---|
| id | UUID | 主鍵 |
| user_id | UUID | 關聯 app_user、非空 |
| method | enum string | EMAIL 或 LINE |
| identifier | varchar(160) | 信箱或安全識別資料 |
| success | boolean | 非空 |
| completed_at | timestamp | 非空 |
| created_at / updated_at | timestamp | 稽核欄位 |

## Relationships

- `app_user 1 — 0..1 registration_record`：每個首次註冊帳號最多一筆。
- `password_policy` 為系統級單筆設定，不關聯使用者。
