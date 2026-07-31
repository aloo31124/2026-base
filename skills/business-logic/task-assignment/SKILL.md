---
name: task-assignment
description: 維護 AgentFlow 註冊後公司綁定、主管員工綁定、合法受派人、任務 CRUD、查詢排序、撤回與退回狀態時使用。
---

# 任務指派

## 目錄

- [公司與成員契約](#公司與成員契約) — 同公司與唯一主管
- [合法受派人](#合法受派人) — 三類目標
- [任務與狀態](#任務與狀態) — CRUD、撤回與退回
- [權限與測試](#權限與測試) — 可見範圍及交付門檻

## 公司與成員契約

使用者只能依 `company.name` 精確比對既有公司並建立自己的 `company_membership`，不可由此流程建立公司。同一使用者最多一家公司。`supervisor_employee_binding.employee_user_id` 唯一，因此一位員工最多綁定一位主管；建立或取消綁定者只能是該主管，且雙方必須同公司。

## 合法受派人

任務建立者必須具有 `MANAGER` 角色、`SupervisorProfile` 與公司綁定。可指派對象只有：

- 建立者本人。
- 同公司的其他主管。
- 同公司且已由 `SupervisorEmployeeBinding` 綁在建立者旗下的員工。

任何跨公司、其他主管旗下員工或未綁公司對象均拒絕。

## 任務與狀態

每筆 `AssignedTask` 只有一位受派人，保存名稱、內容、期限、建立者、受派人、指派時間與狀態。狀態只允許：

- `ASSIGNED → RETURNED`：僅受派人，必須提供非空原因。
- `ASSIGNED → WITHDRAWN`：僅建立者。
- `RETURNED → ASSIGNED`：建立者修改並重新指派。

建立者只可查閱、修改、刪除或撤回自己的任務；受派人只可在收件匣看見自己的任務。退回任務保留原因，不可刪除。

## 權限與測試

主管管理端點使用 `MANAGER` 方法權限；公司綁定、收件匣與退回只要求登入，再由 Service 驗證本人。修改本領域時必須同步 JUnit/MockMvc、Postman/Newman、Cypress 與 `specs/010-task-assignment/` checklist/tasks。
