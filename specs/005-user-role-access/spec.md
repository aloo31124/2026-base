# Feature Specification：使用者角色權限

**來源**：Google Sheet「基礎架構使用者分權」第 38–46 列

## 目錄

- [User Stories](#user-stories) — 5 條使用者與角色流程
- [Functional Requirements](#functional-requirements) — 13 條安全需求
- [Key Entities](#key-entities) — 使用者、角色與關聯
- [Edge Cases](#edge-cases) — 帳號與角色邊界
- [Success Criteria](#success-criteria) — 5 項量化成果
- [Assumptions](#assumptions) — 初始帳號限制

## User Stories

### User Story 1 - JWT 登入 (Priority: P1)

使用者以帳密登入，通過 Spring Security 認證後取得 JWT。

### User Story 2 - 初始化四組帳號 (Priority: P1)

空資料庫啟動時自動建立 2 組系統管理員與 2 組一般使用者。

### User Story 3 - 管理使用者 (Priority: P1)

系統管理員在使用者頁檢視、新增、編輯與停用帳號，新帳號標記管理員新增並設定密碼。

### User Story 4 - 授予主管角色 (Priority: P2)

系統管理員在角色 tab 將主管角色附加給保有員工角色的使用者。

### User Story 5 - 阻擋未授權存取 (Priority: P1)

一般使用者無法呼叫管理 API 或進入使用者管理頁。

## Functional Requirements

- **FR-001**：空使用者表 MUST 建立 `admin/admin123`、`admin2/admin234`。
- **FR-002**：空使用者表 MUST 建立 `user/admin123`、`user2/admin234`。
- **FR-003**：密碼 MUST 以 BCrypt 儲存，不得明文落庫或 Log。
- **FR-004**：登入 MUST 使用 Spring Security Authentication、Authorization 與 Security Filter Chain。
- **FR-005**：成功登入 MUST 簽發具到期時間的 JWT。
- **FR-006**：使用者總表 MUST 透過 `user_role` 支援一人多角色。
- **FR-007**：每位新建使用者 MUST 預設擁有 EMPLOYEE。
- **FR-008**：系統 MUST 提供 SYSTEM_ADMIN、MANAGER、EMPLOYEE 角色。
- **FR-009**：使用者 tab MUST 顯示姓名、帳號、信箱、註冊方式、角色、停用與編輯操作。
- **FR-010**：管理員建立帳號 MUST 標記「管理員新增」並允許設定至少 8 位密碼。
- **FR-011**：角色 tab MUST 顯示使用者角色列表並可附加 MANAGER。
- **FR-012**：管理 API MUST 僅允許 SYSTEM_ADMIN。
- **FR-013**：前端路由 MUST 在角色不足時導向無權限說明頁。

## Key Entities

- **UserAccount**：姓名、帳號、信箱、密碼雜湊、註冊方式、啟用狀態。
- **Role**：角色代碼與顯示名稱。
- **UserRole**：以 UUID id 關聯一位使用者與一個角色。

## Edge Cases

- 重複帳號或信箱回 409；停用帳號無法登入。
- 重複授予同一角色保持冪等，不建立重複關聯。

## Success Criteria

- **SC-001**：四組初始帳號首次啟動建立率 100%，重啟不重複。
- **SC-002**：管理員流程 E2E 通過率 100%。
- **SC-003**：一般使用者管理資源阻擋率 100%。
- **SC-004**：新使用者預設員工角色正確率 100%。
- **SC-005**：所有密碼欄位均不以明文回傳。

## Assumptions

- 指定初始帳密僅供本開發環境；正式環境應由部署密鑰或首次登入改密流程取代。

