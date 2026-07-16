# Data Model：使用者角色權限

## 目錄

- [UserAccount](#useraccount) — 登入主檔
- [Role](#role) — 角色字典
- [UserRole](#userrole) — 多對多關聯
- [State Transitions](#state-transitions) — 帳號狀態

## UserAccount

username/email 唯一；password_hash 不回傳；registration_method 為系統初始化或管理員新增；is_active 控制登入。

## Role

role_code 唯一：SYSTEM_ADMIN、MANAGER、EMPLOYEE。

## UserRole

user_id + role_id 唯一；每筆有 UUID 與審計欄位。

## State Transitions

建立 → 啟用；啟用 → 停用。角色可附加，EMPLOYEE 為新使用者基礎角色。

