# Research：後台登出時間設定

## 目錄

- [決策 1：政策儲存](#決策-1政策儲存) — 專用 singleton 表
- [決策 2：JWT 整合](#決策-2jwt-整合) — 每次簽發讀取政策
- [決策 3：既有 token](#決策-3既有-token) — 保留原 exp
- [決策 4：後台與範圍](#決策-4後台與範圍) — 註冊登入管理、5–1440 分鐘
- [決策 5：測試](#決策-5測試) — claims 與 UI 雙層驗證

## 決策 1：政策儲存

**Decision**：新增 `session_timeout_policy` singleton 表與專用 Service。  
**Rationale**：與 PasswordPolicy 一致，可保存 updatedAt 並由 Hibernate 管理 schema。  
**Alternatives considered**：環境變數無法由後台即時修改；通用 key-value 設定表缺少型別與範圍契約。

## 決策 2：JWT 整合

**Decision**：`JwtService.create()` 每次向 `SessionTimeoutPolicyService` 取得分鐘數。  
**Rationale**：所有登入方式共用 JwtService，單點修改即可立即套用新設定且避免 cache 延遲。  
**Alternatives considered**：各 Auth Service 傳入效期會重複邏輯；長效 cache 可能違反「設定後新登入生效」。

## 決策 3：既有 token

**Decision**：政策更新不撤銷或改寫舊 token。  
**Rationale**：JWT 已簽章且含 exp，現有系統沒有 blacklist；前端倒數必須忠實呈現該 exp。  
**Alternatives considered**：全域 not-before 會需要額外驗證狀態並強制所有人登出，超出本需求。

## 決策 4：後台與範圍

**Decision**：在註冊登入管理新增 5–1440 分鐘數字欄位，單位固定為分鐘。  
**Rationale**：安全政策集中且容易理解；5 分鐘避免管理員把系統設成幾乎不可操作，1440 分鐘控制最長 24 小時。  
**Alternatives considered**：時/分雙單位增加轉換歧義；獨立頁面增加 route 與導覽成本。

## 決策 5：測試

**Decision**：後端解析實際新 JWT claims 驗證 duration，前端以 API intercept 驗證表單契約，再跑既有倒數 E2E。  
**Rationale**：claims 測試驗證真正安全行為；mock UI 測試快速穩定；既有 E2E 防止 exp 顯示回歸。  
**Alternatives considered**：只比對 DB 無法證明 JWT 套用；只測 UI 無法驗證安全核心。
