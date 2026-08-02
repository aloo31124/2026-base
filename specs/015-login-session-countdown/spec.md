# Feature Specification：登入工作階段倒數

**來源**：使用者需求「右上角顯示登入倒數，若超過時間限制，則自動登出並回到登入頁」

## 目錄

- [User Scenarios & Testing](#user-scenarios--testing) — 4 條登入倒數與逾時流程
  - [User Story 1 - 檢視剩餘登入時間 (Priority: P1)](#user-story-1---檢視剩餘登入時間-priority-p1) — 右上角即時倒數
  - [User Story 2 - 到期自動登出 (Priority: P1)](#user-story-2---到期自動登出-priority-p1) — 清除憑證並返回登入頁
  - [User Story 3 - 背景分頁恢復校時 (Priority: P2)](#user-story-3---背景分頁恢復校時-priority-p2) — 不依賴累加計時
  - [User Story 4 - 跨登入方式一致套用 (Priority: P2)](#user-story-4---跨登入方式一致套用-priority-p2) — 帳密、信箱與 LINE 共用規則
  - [Edge Cases](#edge-cases) — 到期、無效 token 與 RWD 邊界
- [Requirements](#requirements-mandatory) — 11 條可驗證功能需求
  - [Functional Requirements](#functional-requirements) — 顯示、計時與登出契約
- [Clarifications](#clarifications) — 4 項自動決策
  - [自動決策紀錄](#自動決策紀錄) — 時間來源、續期、導頁與相容性
- [Success Criteria](#success-criteria-mandatory) — 5 項量化成果
  - [Measurable Outcomes](#measurable-outcomes) — 正確率與反應時間
- [Assumptions](#assumptions) — 現有 JWT 與瀏覽器能力
- [Out of Scope](#out-of-scope) — 不含 refresh token 與閒置續期

## User Scenarios & Testing

### User Story 1 - 檢視剩餘登入時間 (Priority: P1)

已登入使用者可在應用程式右上角持續看到登入剩餘時間，以便在工作階段結束前保存工作或重新登入。

**Why this priority**：這是使用者明示的主要可見功能，也是理解逾時行為的必要提示。

**Independent Test**：以具有未來到期時間的登入憑證進入任一受保護頁面，右上角須以時、分、秒顯示倒數，經過一秒後數值減少一秒。

**Acceptance Scenarios**：

1. **Given** 使用者已登入且憑證剩餘 2 小時，**When** 進入受保護頁面，**Then** 右上角顯示「登入倒數 02:00:00」。
2. **Given** 倒數正在顯示，**When** 經過一秒，**Then** 顯示值依憑證到期時間更新且不增加。

---

### User Story 2 - 到期自動登出 (Priority: P1)

登入期限到達後，使用者不需進行任何操作，系統會自動清除本機登入資料並返回登入頁，避免繼續停留在受保護畫面。

**Why this priority**：逾時後終止前端工作階段是本需求的安全核心。

**Independent Test**：建立兩秒後到期的登入憑證；倒數歸零後 1.5 秒內須返回登入頁，且本機 session 與 token 都不存在。

**Acceptance Scenarios**：

1. **Given** 登入憑證即將到期，**When** 到達到期時間，**Then** 系統自動登出並以不可回退至受保護頁面的方式導向登入頁。
2. **Given** 使用者因逾時回到登入頁，**When** 頁面完成導向，**Then** 顯示「登入時間已到，請重新登入。」。

---

### User Story 3 - 背景分頁恢復校時 (Priority: P2)

使用者切換到其他分頁或讓裝置休眠後再回來時，倒數必須依實際到期時間立即校正；若已到期，立即登出。

**Why this priority**：瀏覽器可能節流背景計時器，若只逐秒遞減會錯誤延長登入狀態。

**Independent Test**：模擬時間直接跨越到期點後觸發分頁恢復，畫面不得繼續顯示未過期狀態。

**Acceptance Scenarios**：

1. **Given** 分頁在背景期間跨越到期時間，**When** 分頁重新可見或取得焦點，**Then** 系統立即依目前時間判定到期並登出。

---

### User Story 4 - 跨登入方式一致套用 (Priority: P2)

不論使用帳密、信箱註冊後登入或 LINE 登入，只要取得現有格式的 JWT，都套用相同倒數與逾時登出規則。

**Why this priority**：所有登入方式共用相同 LoginResponse 與 JWT 到期規則，使用者不應看到不同安全行為。

**Independent Test**：以任一具有標準 `exp` 欄位的登入憑證建立 session，倒數與到期處理結果一致。

**Acceptance Scenarios**：

1. **Given** 任一既有登入流程成功建立 JWT session，**When** 進入受保護頁面，**Then** 右上角顯示該 JWT 的剩餘時間。

### Edge Cases

- 憑證在頁面載入時已到期：不得進入受保護內容，清除登入資料並返回登入頁。
- 瀏覽器計時器延遲或系統休眠：每次更新以絕對到期時間減目前時間重算，不以已執行的 tick 次數推估。
- token 無法解析或沒有 `exp`：不顯示虛假的倒數；既有 API 驗證仍是授權最終依據，避免破壞歷史測試使用的替身 token。
- 剩餘時間小於五分鐘：以警示樣式呈現，但仍維持可讀文字與 `role="timer"`。
- 小螢幕：至少保留 `HH:MM:SS` 數值，不得被既有「隱藏使用者資料」規則一併隱藏。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**：已登入且 JWT 含有效 `exp` 的頁面 MUST 在應用程式右上角顯示「登入倒數」與 `HH:MM:SS`。
- **FR-002**：倒數的唯一時間來源 MUST 為目前登入 JWT 的 `exp`，不得另設與伺服器不同步的固定期限。
- **FR-003**：倒數 MUST 至少每秒更新一次，且每次以 `exp - 現在時間` 重算剩餘秒數。
- **FR-004**：剩餘時間 MUST 不得顯示負值；到期點顯示 `00:00:00`。
- **FR-005**：到達或超過 `exp` 時 MUST 自動清除 Redux session、`localStorage.session` 與 `localStorage.token`。
- **FR-006**：自動登出後 MUST 使用 replace navigation 返回 `/login`，避免瀏覽器返回鍵重新顯示受保護頁面。
- **FR-007**：逾時導頁 MUST 在登入頁顯示「登入時間已到，請重新登入。」；手動登出不顯示此訊息。
- **FR-008**：分頁 `visibilitychange` 或視窗 `focus` 時 MUST 立即重新校時並在必要時登出。
- **FR-009**：使用者操作 MUST NOT 延長到期時間；本功能不新增 refresh token 或閒置續期。
- **FR-010**：倒數 MUST 支援桌面與窄螢幕 header，並提供可由輔助技術辨識的 timer 語意。
- **FR-011**：自動化測試 MUST 覆蓋倒數遞減、到期清除與導頁，且 production build MUST 通過。

## Clarifications

### 自動決策紀錄

- **議題**：登入期限來源
  - **候選方案**：前端固定分鐘數／伺服器另回傳 expiresAt／解析現有 JWT `exp`
  - **採用方案**：解析現有 JWT `exp`
  - **採用理由**：後端所有登入方式已簽發含到期時間的 JWT，直接沿用可避免 API 契約變更與雙重時間來源。
  - **影響章節**：FR-001–004、US1
- **議題**：操作是否延長登入
  - **候選方案**：固定絕對到期／閒置逾時／滑動續期
  - **採用方案**：固定絕對到期
  - **採用理由**：目前後端沒有 refresh token；前端自行延長會與 JWT 真實效期衝突。
  - **影響章節**：FR-002、FR-009、US3
- **議題**：到期導頁方式
  - **候選方案**：push／replace／留在原頁顯示 modal
  - **採用方案**：replace 至登入頁並顯示原因
  - **採用理由**：符合使用者要求，並避免返回鍵重現已失效的受保護畫面。
  - **影響章節**：FR-005–007、US2
- **議題**：不含 exp 的歷史替身 token
  - **候選方案**：立即登出／假設固定期限／不顯示倒數並交由 API 驗證
  - **採用方案**：不顯示虛假倒數並交由既有 API 驗證
  - **採用理由**：正式 JWT 一定含 exp，同時維持既有 mock E2E 與向後相容；不把無依據的期限呈現給使用者。
  - **影響章節**：Edge Cases、FR-001–002

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**：含有效 `exp` 的登入工作階段，右上角倒數顯示率 100%。
- **SC-002**：模擬經過 N 秒後，倒數與憑證實際剩餘秒數誤差不超過 1 秒。
- **SC-003**：到期後 1.5 秒內清除兩個本機登入鍵值並返回登入頁的成功率 100%。
- **SC-004**：桌面與 720px 以下窄螢幕均可讀取倒數數值，且 timer 可由自動化選取。
- **SC-005**：專屬 Cypress 測試與前端 production build 通過率 100%。

## Assumptions

- 現有 Spring Boot JWT 以 `exp` 表示絕對到期時間，預設效期由 `JWT_EXPIRATION_MINUTES` 控制，目前預設 120 分鐘。
- 使用者裝置提供可用的系統時間；伺服器仍是授權最終裁決者。
- 所有現有登入方式回傳相同 JWT 格式，無需新增後端資料表或 API 欄位。

## Out of Scope

- refresh token、靜默續期、滑動 session 或依使用者活動重設倒數。
- 後端 JWT 效期設定調整、管理員動態設定效期。
- 多裝置同步登出與伺服器端 token 撤銷清單。
