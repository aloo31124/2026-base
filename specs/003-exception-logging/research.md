# Research：例外處理與 Log

## 目錄

- [錯誤責任](#錯誤責任) — Advice 與 Security handler
- [日誌策略](#日誌策略) — 30 日滾動

## 錯誤責任

**Decision**：MVC 例外交給 Advice；filter chain 的 401/403 交給 Security handlers。

**Rationale**：Security 例外發生在 Controller 前，無法可靠由 Advice 處理。

## 日誌策略

**Decision**：Console + SizeAndTimeBasedRollingPolicy，保留 30 日。

**Rationale**：兼顧本機除錯與檔案追蹤，對齊參考專案。

