# Research：公司主管管理

## 目錄

- [主管身分](#主管身分) — 既有使用者附加資料
- [公司成員綁定](#公司成員綁定) — 一人一公司
- [關聯刪除](#關聯刪除) — 明確取消優先
- [前端操作](#前端操作) — 三標籤與成員類型局部狀態
- [員工綁定契約](#員工綁定契約) — 增量端點與資格

## 主管身分

- **Decision**：使用 `SupervisorProfile` 一對一連結 `UserAccount`，建立時授予 `MANAGER`。
- **Rationale**：主管必須是已註冊使用者，身份資料與認證資料不重複。
- **Alternatives considered**：建立獨立主管帳號會破壞既有 RBAC 與帳號唯一性。

## 公司成員綁定

- **Decision**：使用 `CompanyMembership`，對 `user_id` 建立唯一限制。
- **Rationale**：一家公司自然支援多筆成員，同一主管或員工只能出現一次。
- **Alternatives considered**：主管專用 join table 無法共同約束後續員工綁定。

## 關聯刪除

- **Decision**：公司或主管仍有綁定時拒絕刪除。
- **Rationale**：明確取消流程可避免級聯刪除造成不可見資料遺失。
- **Alternatives considered**：級聯刪除較省步驟但不利稽核與使用者理解。

## 前端操作

- **Decision**：單一頁面三標籤，將第三標籤改為「綁定公司」，使用頁面局部 state 切換主管／員工並沿用既有 `api()`。
- **Rationale**：資料只服務此頁，沿用 `uiux` 的頁籤、卡片、篩選與表格模式即可。
- **Alternatives considered**：新增 Redux slice 增加檔案與同步成本，超出 MVP。

## 員工綁定契約

- **Decision**：保留主管 `/bindings`，新增員工 `/employee-bindings`，兩者共用 `CompanyMembership`。
- **Rationale**：新增端點可讓 request／response 與類型驗證清楚，同時不破壞既有主管呼叫端。
- **Alternatives considered**：把既有 `/bindings` 改為通用 payload 會造成破壞性 API 變更；建立員工專用資料表則重複一人一公司的唯一規則。
