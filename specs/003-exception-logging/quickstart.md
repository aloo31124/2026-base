# Quickstart：例外處理與 Log

## 目錄

- [驗證](#驗證) — 401/403 與檔案 Log

## 驗證

未帶 token 呼叫保護 API 應回 401；一般使用者 token 呼叫 `/api/admin/users` 應回 403。啟動後檢查 `backend/logs/all.*.log`。

