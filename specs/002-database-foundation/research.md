# Research：資料庫基礎

## 目錄

- [初始化安全](#初始化安全) — 專用 Login
- [JPA 策略](#jpa-策略) — 自動建表

## 初始化安全

**Decision**：使用 Windows 整合驗證建立專案專用 Login，不改 `sa`。

**Rationale**：修改全域 sa 密碼會影響同 SQL Server 的其他系統；專用 Login 仍可滿足應用程式帳密連線。

**Alternatives considered**：自動 `ALTER LOGIN sa`，因破壞性風險否決。

## JPA 策略

**Decision**：開發環境 `ddl-auto=update`，測試環境 `create-drop`。

**Rationale**：符合需求的 JPA 自動建表，測試可隔離重跑。

