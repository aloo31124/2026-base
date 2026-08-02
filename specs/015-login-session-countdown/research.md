# Research：登入工作階段倒數

## 目錄

- [決策 1：工作階段到期來源](#決策-1工作階段到期來源) — 使用 JWT exp
- [決策 2：瀏覽器計時校正](#決策-2瀏覽器計時校正) — 絕對時間重算
- [決策 3：逾時登出整合](#決策-3逾時登出整合) — 沿用 Redux logout 與 replace
- [決策 4：無 exp 相容策略](#決策-4無-exp-相容策略) — 不顯示虛假倒數
- [決策 5：測試策略](#決策-5測試策略) — Cypress 控制時間

## 決策 1：工作階段到期來源

**Decision**：只讀解析現有 JWT payload 的 `exp`（Unix seconds）。  
**Rationale**：`JwtService` 已由 `JWT_EXPIRATION_MINUTES` 建立到期時間，帳密、信箱與 LINE 登入共用相同 token 格式。  
**Alternatives considered**：前端固定 120 分鐘會忽略部署設定；修改 LoginResponse 會擴大後端與所有登入流程的契約變更。

## 決策 2：瀏覽器計時校正

**Decision**：每次以到期毫秒減 `Date.now()` 並向上取整為秒，另在 visibilitychange 與 focus 立即同步。  
**Rationale**：背景分頁 interval 會被節流，tick 累減可能錯誤延長登入時間。  
**Alternatives considered**：Web Worker 增加不必要複雜度；只依 interval 無法處理休眠。

## 決策 3：逾時登出整合

**Decision**：AppShell 以 callback dispatch 帶逾時原因的既有 `logout()`，再 `navigate('/login', {replace: true})`。  
**Rationale**：既有 reducer 已集中清除 Redux 與 localStorage；將原因保留在 auth state 可避免 Guard 導頁覆蓋 Router state，replace 仍符合安全導頁。  
**Alternatives considered**：直接操作 storage 會複製登出邏輯；整頁 reload 會犧牲 SPA 狀態與測試可控性。

## 決策 4：無 exp 相容策略

**Decision**：解析失敗或沒有 exp 時不渲染 timer，不自行建立期限。  
**Rationale**：正式後端 token 一定含 exp；專案既有 E2E 以 opaque token 攔截 API，強制登出會造成非功能性回歸。API 401 仍能拒絕無效正式 token。  
**Alternatives considered**：立即登出較嚴格但會破壞既有測試相容；假定 120 分鐘會對使用者顯示不可信時間。

## 決策 5：測試策略

**Decision**：建立只含測試 payload 的短效 JWT，Cypress `cy.clock`/`cy.tick` 驗證畫面、儲存與路由。  
**Rationale**：本功能完全位於瀏覽器，控制時間可在毫秒內重現分鐘級逾時。  
**Alternatives considered**：真實等待緩慢且不穩定；只測純函式無法覆蓋 Redux 與 Router 整合。
