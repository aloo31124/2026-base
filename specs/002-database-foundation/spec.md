# Feature Specification：資料庫基礎

**來源**：Google Sheet「基礎架構使用者分權」第 18–28 列

## 目錄

- [User Stories](#user-stories) — 3 條資料生命週期情境
- [Functional Requirements](#functional-requirements) — 11 條資料規範
- [Edge Cases](#edge-cases) — 初始化安全邊界
- [Success Criteria](#success-criteria) — 4 項量化成果
- [Assumptions](#assumptions) — Login 安全決策

## User Stories

### User Story 1 - 跨平台初始化 MSSQL (Priority: P1)

開發者在 Windows 或 macOS 執行腳本，安全建立專案資料庫、Login、User 與 db_owner 關聯。

**Acceptance Scenarios**：首次與重複執行均成功；非法名稱直接拒絕。

### User Story 2 - JPA 自動建立資料表 (Priority: P1)

系統啟動時依 BO 映射建立或更新 MSSQL 資料表。

**Acceptance Scenarios**：`app_user`、`role`、`user_role`、`test` 均存在。

### User Story 3 - 一致資料命名與審計 (Priority: P2)

維護者在所有資料表看到 UUID id、created_at、updated_at 與一致 snake_case 命名。

**Acceptance Scenarios**：自動化測試掃描實體規則均通過。

## Functional Requirements

- **FR-001**：根目錄 MUST 提供 PowerShell 與 Shell 初始化腳本。
- **FR-002**：資料庫名稱 MUST 為 `base_20260716_01` 格式並驗證識別名稱。
- **FR-003**：腳本 MUST 使用整合驗證連 master，僅在不存在時建立 DB/Login/User。
- **FR-004**：動態識別名稱 MUST 以受控字元及 SQL Server 引號保護。
- **FR-005**：資料庫 User MUST 加入 db_owner，重跑腳本 MUST 保持冪等。
- **FR-006**：JPA MUST 自動建表。
- **FR-007**：每張表 MUST 使用 UUID `id` 主鍵及 `created_at`、`updated_at`。
- **FR-008**：表名 MUST 使用單數 snake_case；多對多表組合實體名。
- **FR-009**：外鍵 MUST 使用 `{table}_id`，多值狀態 MUST 以 `_status` 結尾。
- **FR-010**：布林欄位 MUST 使用 `is_` 前綴。
- **FR-011**：後端 MUST 實際連線 MSSQL 完成 CRUD 驗證。

## Edge Cases

- 不得自動修改既有全域 `sa` 密碼，以免破壞同執行個體其他資料庫。
- Login 或 DB 已存在時不得重建或覆蓋資料。

## Success Criteria

- **SC-001**：兩種初始化腳本涵蓋率 100%。
- **SC-002**：初始化腳本連續執行兩次均成功。
- **SC-003**：四張實體表命名與欄位規範測試 100% 通過。
- **SC-004**：MSSQL CRUD Postman 測試 100% 通過。

## Assumptions

- 為避免破壞共用 SQL Server，採專案專用 Login `base_20260716_01`，而非重設既有 `sa` 密碼；權限仍由 Windows 整合驗證建立。

