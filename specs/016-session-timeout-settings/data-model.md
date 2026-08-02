# Data Model：後台登出時間設定

## 目錄

- [SessionTimeoutPolicy](#sessiontimeoutpolicy) — singleton 政策實體
- [Validation Rules](#validation-rules) — 5–1440 分鐘
- [Relationships](#relationships) — 無外鍵、由 JwtService 讀取
- [State Transitions](#state-transitions) — 初始、更新與簽發

## SessionTimeoutPolicy

| 欄位 | 型別 | 規則 |
|---|---|---|
| id | UUID | BaseEntity 自動產生 |
| timeoutMinutes | int | 必填，5–1440 |
| createdAt | Instant | 首次建立時間 |
| updatedAt | Instant | 最近更新時間 |

表名：`session_timeout_policy`。服務使用最早建立的一筆作為唯一政策，與既有 PasswordPolicy 模式一致。

## Validation Rules

- API DTO 使用 `@Min(5)`、`@Max(1440)`；Service 再做相同業務驗證。
- 初始環境值也必須落在相同範圍，避免後台顯示不可保存的值。
- timeoutMinutes 只影響未來的 `JwtService.create()`。

## Relationships

無外鍵。JwtService 透過 SessionTimeoutPolicyService 讀取目前值；JWT 本身只保存標準 `iat`、`exp`。

## State Transitions

```text
ABSENT --首次後台讀取--> ACTIVE(default environment minutes)
ACTIVE --合法 PUT--> ACTIVE(new minutes, updatedAt changed)
ACTIVE --新登入--> JWT(iat, exp = iat + active minutes)
ACTIVE --非法 PUT--> ACTIVE(original minutes unchanged)
```
