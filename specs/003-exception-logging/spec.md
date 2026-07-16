# Feature Specification：例外處理與 Log

**來源**：Google Sheet「基礎架構使用者分權」第 29–34 列

## 目錄

- [User Stories](#user-stories) — 3 條錯誤與權限情境
- [Functional Requirements](#functional-requirements) — 8 條處理需求
- [Edge Cases](#edge-cases) — 安全與未知例外
- [Success Criteria](#success-criteria) — 4 項量化成果
- [Assumptions](#assumptions) — 訊息中的 xxx 代入規則

## User Stories

### User Story 1 - 明確取得 API 錯誤 (Priority: P1)

前端遇到驗證、業務或未知例外時，收到一致且可閱讀的錯誤訊息。

### User Story 2 - 追蹤跨層操作 (Priority: P1)

維護者能從 API、Service、JPA 與 SQL Log 追蹤請求處理。

### User Story 3 - 阻擋非管理員 (Priority: P1)

一般使用者呼叫管理 API 或進入管理頁時，分別收到明確 API 訊息與說明頁。

## Functional Requirements

- **FR-001**：後端 MUST 以 `@RestControllerAdvice` 集中處理例外。
- **FR-002**：驗證、業務、權限與未知例外 MUST 回傳一致 JSON envelope。
- **FR-003**：API、Service 與 DAO/JPA 操作 MUST 留下可追蹤 Log。
- **FR-004**：Log MUST 同時輸出 Console 與 30 日滾動檔案。
- **FR-005**：錯誤 Log MUST 保留堆疊，Response 不得洩漏堆疊或密碼。
- **FR-006**：管理 API 無權限 MUST 回傳 `[使用者角色] [api] 無系統管理員權限。`。
- **FR-007**：管理頁無權限 MUST 導向顯示 `[使用者角色] [頁面] 無系統管理員權限。`。
- **FR-008**：HTTP 401 與 403 MUST 可由前端區分處理。

## Edge Cases

- JWT 過期或格式錯誤回 401；已登入但角色不足回 403。
- 未知例外只回通用訊息，詳細資訊留在 Log。

## Success Criteria

- **SC-001**：四類錯誤格式一致率 100%。
- **SC-002**：一般使用者管理 API/頁面阻擋率 100%。
- **SC-003**：所有必要層級均可找到操作 Log。
- **SC-004**：安全訊息測試 100% 通過。

## Assumptions

- 原文 `[xxx]` 以當前功能名稱「使用者角色」代入，避免回應含未解析 placeholder。

