# Feature Specification：後台登出時間設定

**來源**：使用者需求「後台可設定登出時間」

## 目錄

- [User Scenarios & Testing](#user-scenarios--testing) — 4 條管理與生效流程
  - [User Story 1 - 管理登出時間 (Priority: P1)](#user-story-1---管理登出時間-priority-p1) — 後台讀寫分鐘數
  - [User Story 2 - 新登入套用設定 (Priority: P1)](#user-story-2---新登入套用設定-priority-p1) — JWT exp 動態生效
  - [User Story 3 - 既有登入保持原效期 (Priority: P2)](#user-story-3---既有登入保持原效期-priority-p2) — 不改寫已簽發 token
  - [User Story 4 - 權限與邊界保護 (Priority: P2)](#user-story-4---權限與邊界保護-priority-p2) — 管理員限定與 5–1440 分鐘
  - [Edge Cases](#edge-cases) — 初始值、並行更新與既有 token
- [Requirements](#requirements-mandatory) — 12 條功能需求
  - [Functional Requirements](#functional-requirements) — 儲存、API、JWT 與 UI
  - [Key Entities](#key-entities) — 登出時間政策
- [Clarifications](#clarifications) — 4 項自動決策
  - [自動決策紀錄](#自動決策紀錄) — 位置、範圍、生效與單位
- [Success Criteria](#success-criteria-mandatory) — 5 項量化成果
  - [Measurable Outcomes](#measurable-outcomes) — 正確率與操作時間
- [Assumptions](#assumptions) — JWT 與現有管理模組
- [Out of Scope](#out-of-scope) — 不含撤銷與閒置續期

## User Scenarios & Testing

### User Story 1 - 管理登出時間 (Priority: P1)

系統管理員可在「註冊登入管理」後台查看目前登入效期，輸入分鐘數並儲存。

**Why this priority**：這是本需求的主要管理操作。

**Independent Test**：管理員將效期由預設 120 分鐘改為 30 分鐘，重新讀取仍為 30，且頁面顯示儲存成功。

**Acceptance Scenarios**：

1. **Given** 管理員進入註冊登入管理頁，**When** 頁面載入，**Then** 顯示目前登出時間與生效說明。
2. **Given** 管理員輸入合法分鐘數，**When** 儲存，**Then** 後台保存並回傳更新值。

---

### User Story 2 - 新登入套用設定 (Priority: P1)

政策更新後，任何帳密、信箱或 LINE 新建立的 JWT 都使用目前設定計算 `exp`，前端登入倒數自然顯示相同期限。

**Why this priority**：管理設定必須真正影響伺服器簽發的 token 才有業務價值。

**Independent Test**：設定 30 分鐘後重新登入，JWT `exp - iat` 為 30 分鐘。

**Acceptance Scenarios**：

1. **Given** 登出時間為 30 分鐘，**When** 任一登入方式簽發新 JWT，**Then** JWT 到期時間為簽發後 30 分鐘。

---

### User Story 3 - 既有登入保持原效期 (Priority: P2)

管理員修改政策時，已簽發 token 保留其原有 `exp`；使用者下次登入才套用新值。

**Why this priority**：JWT 是無狀態且已攜帶絕對到期時間，前端不可顯示與伺服器不同的期限。

**Independent Test**：先取得 120 分鐘 token，再改為 30 分鐘；舊 token 的 `exp` 不變，新 token 為 30 分鐘。

**Acceptance Scenarios**：

1. **Given** 使用者持有已簽發 JWT，**When** 管理員更新登出時間，**Then** 舊 JWT 不被改寫或提前失效。

---

### User Story 4 - 權限與邊界保護 (Priority: P2)

只有系統管理員可讀寫政策；過短或過長的時間不得保存。

**Why this priority**：避免一般使用者弱化登入安全或錯誤值造成服務不可用。

**Independent Test**：一般使用者呼叫 API 得到模組指定 403；4 或 1441 分鐘得到 400。

**Acceptance Scenarios**：

1. **Given** 一般使用者，**When** 讀取或更新政策，**Then** 回覆「[註冊登入管理] [api] 無系統管理員權限。」。
2. **Given** 管理員輸入範圍外數值，**When** 儲存，**Then** 系統拒絕且原政策不變。

### Edge Cases

- 資料庫尚無政策：以 `JWT_EXPIRATION_MINUTES` 建立首筆設定，現有預設為 120 分鐘。
- 更新後管理員目前 token 不變；若要看到新倒數，需重新登入。
- 兩個管理員先後更新：最後成功提交的合法值成為目前政策。
- DB 暫時不可用：登入失敗，不可靜默改用不同效期而造成安全設定漂移。
- 前端送出非整數、空值或 5–1440 以外數值：不得寫入。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**：系統 MUST 在資料庫保存唯一有效的登出時間政策，單位為整數分鐘。
- **FR-002**：政策 MUST 限制為 5–1440 分鐘（含端點）。
- **FR-003**：資料庫尚無政策時 MUST 以 `JWT_EXPIRATION_MINUTES` 建立預設政策，目前預設 120 分鐘。
- **FR-004**：管理 API MUST 提供目前政策 GET 與更新政策 PUT，並沿用統一 `ApiResponse`。
- **FR-005**：管理 API MUST 僅允許 `SYSTEM_ADMIN`，其他角色回覆既有註冊登入管理 403 訊息。
- **FR-006**：`JwtService` 簽發每一個新 JWT 時 MUST 讀取目前政策並以該分鐘數產生 `exp`。
- **FR-007**：帳密、信箱建帳登入與 LINE 登入 MUST 共用相同動態效期。
- **FR-008**：政策更新 MUST NOT 修改或撤銷既有 JWT；新值只套用更新後簽發的 JWT。
- **FR-009**：「註冊登入管理」頁 MUST 顯示目前分鐘數、範圍、生效時機與更新時間。
- **FR-010**：管理員 MUST 能由頁面更新政策並看到成功或錯誤訊息。
- **FR-011**：後端整合測試 MUST 驗證政策 CRUD、邊界、權限及新 JWT 的 `exp - iat`。
- **FR-012**：前端 E2E 與 production build MUST 驗證管理表單與既有登入倒數相容。

### Key Entities

- **SessionTimeoutPolicy**：全系統目前 JWT 登入效期；包含 timeoutMinutes、createdAt、updatedAt。

## Clarifications

### 自動決策紀錄

- **議題**：後台位置
  - **候選方案**：新頁面／使用者管理／註冊登入管理
  - **採用方案**：既有「註冊登入管理」頁新增「登入工作階段」卡片
  - **採用理由**：該頁已有動態安全政策、SYSTEM_ADMIN Guard 與 API namespace。
  - **影響章節**：US1、FR-004–005、FR-009–010
- **議題**：生效方式
  - **候選方案**：立即撤銷全部 token／改寫前端倒數／只套用新 JWT
  - **採用方案**：只套用更新後簽發的新 JWT
  - **採用理由**：現有 JWT 無狀態且無撤銷清單；保留舊 exp 才能讓前後端一致。
  - **影響章節**：US2–US3、FR-006–008
- **議題**：可設定範圍
  - **候選方案**：1–60／5–1440／5–10080 分鐘
  - **採用方案**：5–1440 分鐘
  - **採用理由**：避免極短值導致無法操作，同時將單次登入限制在 24 小時內。
  - **影響章節**：US4、FR-002
- **議題**：預設值來源
  - **候選方案**：硬編碼 120／沿用環境設定／要求管理員首次設定
  - **採用方案**：沿用 `JWT_EXPIRATION_MINUTES`
  - **採用理由**：保持現有部署行為與向後相容，初次讀取再落庫。
  - **影響章節**：FR-003、Assumptions

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**：系統管理員可在 1 分鐘內完成讀取、更新與成功確認。
- **SC-002**：合法設定保存與重新讀取一致率 100%，非法設定拒絕率 100%。
- **SC-003**：更新後新 JWT 的 `exp - iat` 與設定分鐘數一致率 100%。
- **SC-004**：非管理員讀寫 API 與頁面阻擋率 100%。
- **SC-005**：後端整合測試、前端專屬 Cypress 與 production build 通過率 100%。

## Assumptions

- 現有所有登入方式最終都透過同一 `JwtService.create()` 簽發 token。
- Hibernate `ddl-auto=update` 會在既有環境建立新政策表；測試使用 H2。
- 管理頁沿用現有註冊登入管理的卡片、表單、提示與 RWD 樣式。

## Out of Scope

- 立即撤銷既有 JWT、token blacklist、refresh token 或多裝置同步登出。
- 依角色、使用者或公司設定不同效期。
- 閒置逾時、操作續期與滑動 session。
