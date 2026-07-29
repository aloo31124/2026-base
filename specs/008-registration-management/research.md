# Research：註冊登入管理

## 目錄

- [密碼政策](#密碼政策) — 動態政策的最小模型
- [註冊稽核](#註冊稽核) — 首次成功建帳事件
- [權限錯誤](#權限錯誤) — 模組化拒絕訊息

## 密碼政策

- **Decision**：使用單筆 JPA entity，預設最小長度 8、英文字母與數字皆必須。
- **Rationale**：符合 Sheet 欄位且不引入規則 DSL。
- **Alternatives considered**：環境變數不利後台即時更新；多列版本化超出 MVP。

## 註冊稽核

- **Decision**：僅在 Email/LINE 首次帳號建立成功後寫入 `registration_record`。
- **Rationale**：精確對應「註冊紀錄與是否成功」，且不與登入嘗試紀錄重疊。
- **Alternatives considered**：由 `app_user` 即時計算會缺乏明確成功欄位與事件時間。

## 權限錯誤

- **Decision**：後端依 API 路徑產生模組化 403 訊息；前端 Guard 以 route state 傳遞頁面訊息。
- **Rationale**：保留其他模組既有訊息並精確滿足 Sheet 要求。
- **Alternatives considered**：全域改寫會破壞既有測試與向後相容。
