# Research：整合測試

## 目錄

- [Postman 執行](#postman-執行) — Newman
- [E2E 範圍](#e2e-範圍) — 單一 Cypress spec

## Postman 執行

**Decision**：以 Newman 執行 Postman Collection v2.1。

**Rationale**：本機無桌面版，Newman 可執行相同 request/test script 並提供可自動化證據。

## E2E 範圍

**Decision**：一支 `base-system.cy.ts` 涵蓋 Sheet 本頁籤所有使用者流程。

**Rationale**：符合使用者「撰寫一隻 Cypress」要求且避免情境分散。

