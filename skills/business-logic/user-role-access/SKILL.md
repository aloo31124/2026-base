---
name: user-role-access
description: 維護 AgentFlow 使用者登入、JWT、角色關聯、系統管理員 API 與 React 路由權限時使用。
---

# 使用者角色權限

## 目錄

- [資料契約](#資料契約) — UUID 與角色表
- [安全契約](#安全契約) — JWT、BCrypt 與雙層授權
- [流程契約](#流程契約) — 新增與授予角色

## 資料契約

使用者、角色與 user_role 都繼承 BaseEntity；所有表使用 UUID、created_at、updated_at。新使用者一定附加 EMPLOYEE。

## 安全契約

密碼只存 BCrypt；JWT 經 Security Filter Chain 驗證。`/api/admin/**` 只允許 SYSTEM_ADMIN，React `/users` 亦須做角色 Guard。

## 流程契約

管理員新增帳號標記「管理員新增」；MANAGER 為附加角色，不取代 EMPLOYEE；停用帳號不得再登入。

