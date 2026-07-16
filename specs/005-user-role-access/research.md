# Research：使用者角色權限

## 目錄

- [JWT](#jwt) — 無狀態認證
- [RBAC](#rbac) — 多角色關聯
- [密碼](#密碼) — BCrypt

## JWT

**Decision**：JJWT HMAC token，120 分鐘到期，filter 每次請求載入目前角色。

**Rationale**：角色停用與變更可立即反映，token 僅保存身份與角色提示。

## RBAC

**Decision**：具自身 UUID 的 `user_role` 關聯實體。

**Rationale**：符合所有表均有 UUID/審計欄位且便於擴充授權時間等欄位。

## 密碼

**Decision**：BCryptPasswordEncoder。

**Rationale**：Spring Security 原生支援且具 salt，不儲存明文。

