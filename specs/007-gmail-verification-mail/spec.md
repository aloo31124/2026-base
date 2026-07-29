# Feature Specification：信箱註冊登入

**Feature Branch**：`信箱註冊-帳密登入`  
**Created**：2026-07-29  
**Status**：Approved  
**Input**：只實作 Google Sheet「註冊登入驗證」分頁中狀態為「預計開發」的「信箱註冊登入」子模組。

## 目錄

- [User Scenarios & Testing](#user-scenarios--testing) — 六條 MUST 的可獨立驗收流程
  - [User Story 1 - 管理員測試寄信與保存紀錄 (Priority: P1)](#user-story-1---管理員測試寄信與保存紀錄-priority-p1) — SMTP 與資料庫紀錄
  - [User Story 2 - 首次信箱註冊驗證 (Priority: P1)](#user-story-2---首次信箱註冊驗證-priority-p1) — 重複檢查、寄碼與核銷
  - [User Story 3 - 設定密碼並完成註冊 (Priority: P1)](#user-story-3---設定密碼並完成註冊-priority-p1) — 建帳與自動登入
  - [User Story 4 - 使用信箱密碼登入 (Priority: P1)](#user-story-4---使用信箱密碼登入-priority-p1) — 向後相容帳號登入
  - [User Story 5 - 忘記密碼並重設 (Priority: P2)](#user-story-5---忘記密碼並重設-priority-p2) — 驗證信箱後更新密碼
  - [Edge Cases](#edge-cases) — 安全、重試與失效邊界
- [Requirements](#requirements) — 共 18 條可測試需求
  - [Functional Requirements](#functional-requirements) — Sheet MUST 與分層、測試要求
  - [Key Entities](#key-entities) — 使用者、驗證流程與寄送紀錄
- [Clarifications](#clarifications) — 自動決策與採用理由
  - [自動決策紀錄](#自動決策紀錄) — 效期、嘗試限制與登入相容性
- [Success Criteria](#success-criteria) — 共 5 項量測成果
  - [Measurable Outcomes](#measurable-outcomes) — 完成率、建置與安全
- [Assumptions](#assumptions) — 外部 SMTP 與既有帳號邊界

## User Scenarios & Testing

### User Story 1 - 管理員測試寄信與保存紀錄 (Priority: P1)

系統管理員可在既有信箱驗證頁輸入收件信箱、寄送測試驗證碼，並檢視最近寄送成功或失敗的紀錄。

**Why this priority**：可先確認 Gmail SMTP 與寄送稽核基礎，亦是後續註冊及重設密碼共用能力。

**Independent Test**：管理員寄送一封測試信後，API 回應遮罩信箱，資料庫新增一筆寄送紀錄，頁面顯示該筆結果。

**Acceptance Scenarios**：

1. **Given** 管理員已登入且 SMTP 可用，**When** 寄送測試信，**Then** 系統寄出 6 位數驗證碼、保存成功紀錄且不在 API 回傳驗證碼。
2. **Given** SMTP 寄送失敗，**When** 管理員送出，**Then** 系統保存失敗紀錄並顯示安全錯誤訊息。
3. **Given** 一般使用者已登入，**When** 呼叫管理員寄信或紀錄 API，**Then** 系統回覆無系統管理員權限。

### User Story 2 - 首次信箱註冊驗證 (Priority: P1)

訪客輸入信箱並要求驗證碼；系統先檢查是否已註冊，只有首次信箱才寄送驗證碼，輸入正確驗證碼後取得一次性驗證票券。

**Why this priority**：避免重複帳號並證明使用者可控制該信箱。

**Independent Test**：以未註冊信箱完成寄碼與核銷；以既有信箱要求寄碼時收到重複提醒且不寄信。

**Acceptance Scenarios**：

1. **Given** 信箱尚未註冊，**When** 使用者按下「發送驗證碼」，**Then** 系統寄送 6 位數驗證碼並保存具效期的雜湊資料。
2. **Given** 信箱已存在，**When** 使用者要求註冊驗證碼，**Then** 頁面顯示「此信箱已註冊」且不建立驗證資料或寄送信件。
3. **Given** 驗證碼正確且未失效，**When** 使用者驗證，**Then** 系統回傳一次性票券且不回傳驗證碼。

### User Story 3 - 設定密碼並完成註冊 (Priority: P1)

信箱驗證通過後，使用者輸入密碼與確認密碼；系統建立信箱帳號、賦予一般使用者角色並立即登入首頁。

**Why this priority**：完成工作表定義的首次註冊核心成果。

**Independent Test**：以有效票券及相同且合規的密碼註冊，確認使用者、角色、票券核銷與 JWT 回應皆正確。

**Acceptance Scenarios**：

1. **Given** 驗證票券有效，**When** 兩次密碼一致且符合規則，**Then** 系統建立帳號、雜湊密碼、賦予 `EMPLOYEE` 並回傳登入工作階段。
2. **Given** 密碼不一致或不足 8 碼，**When** 使用者送出，**Then** 系統不建立帳號並顯示明確訊息。
3. **Given** 驗證票券已使用、失效或不屬於該信箱，**When** 使用者送出，**Then** 系統拒絕註冊。

### User Story 4 - 使用信箱密碼登入 (Priority: P1)

已完成信箱註冊的使用者以信箱作為帳號及其密碼登入，成功後進入系統首頁。

**Why this priority**：註冊完成後必須能回到日常登入流程。

**Independent Test**：新註冊帳號可使用信箱與密碼登入，錯誤密碼收到一致錯誤回應。

**Acceptance Scenarios**：

1. **Given** 使用者已以信箱註冊，**When** 輸入正確信箱與密碼，**Then** 系統回傳 JWT 與角色並進入首頁。
2. **Given** 既有非信箱帳號仍存在，**When** 以原帳號登入，**Then** 既有登入行為保持可用。

### User Story 5 - 忘記密碼並重設 (Priority: P2)

使用者可從登入頁進入忘記密碼流程，透過信箱驗證後設定新密碼，再以新密碼登入。

**Why this priority**：提供帳密登入必要的帳號復原能力。

**Independent Test**：要求重設碼、核銷、更新密碼後，舊密碼失敗而新密碼成功。

**Acceptance Scenarios**：

1. **Given** 信箱帳號存在，**When** 使用者要求重設，**Then** 系統寄送具 10 分鐘效期的驗證碼並保存寄送紀錄。
2. **Given** 驗證碼正確，**When** 使用者設定合規新密碼，**Then** 系統更新密碼雜湊並使票券失效。
3. **Given** 驗證碼錯誤、逾期或超過嘗試上限，**When** 使用者驗證，**Then** 系統拒絕且不變更密碼。

### Edge Cases

- 信箱比較忽略前後空白與大小寫，資料庫保存正規化小寫值。
- 同一信箱同一用途只接受最新且未使用的驗證流程。
- 驗證碼有效 10 分鐘、最多錯誤 5 次，成功票券只能使用一次。
- Gmail SMTP 失敗不得留下可用驗證碼，但必須留下失敗寄送紀錄。
- API、應用程式日誌與前端不得顯示驗證碼、密碼雜湊或 SMTP 密碼。
- 重複提交註冊由資料庫唯一約束及 Service 交易共同防護。

## Requirements

### Functional Requirements

- **FR-001**：系統 MUST 保留管理員測試寄信頁與 API，並將每次成功或失敗結果保存至資料庫。
- **FR-002**：系統 MUST 提供管理員最近寄送紀錄 API 與頁面清單，僅顯示遮罩收件者、用途、狀態與時間。
- **FR-003**：系統 MUST 在註冊寄碼前以正規化信箱檢查是否首次註冊，重複時回覆「此信箱已註冊」。
- **FR-004**：系統 MUST 使用 Gmail SMTP 寄送 6 位數驗證碼，驗證碼有效 10 分鐘。
- **FR-005**：系統 MUST 僅保存驗證碼雜湊，不得保存或回傳明碼。
- **FR-006**：系統 MUST 限制每筆驗證流程最多 5 次錯誤嘗試，並只接受最新有效驗證碼。
- **FR-007**：系統 MUST 在正確核銷驗證碼後回傳一次性驗證票券。
- **FR-008**：系統 MUST 驗證密碼與確認密碼一致，且密碼長度至少 8 碼。
- **FR-009**：系統 MUST 以有效註冊票券建立信箱帳號，將信箱同時作為登入帳號並以 BCrypt 保存密碼。
- **FR-010**：系統 MUST 將新信箱帳號賦予 `EMPLOYEE` 角色，註冊成功後回傳可立即使用的登入工作階段。
- **FR-011**：既有 `/api/auth/login` MUST 同時支援原帳號與新註冊信箱，不破壞既有登入行為。
- **FR-012**：系統 MUST 提供忘記密碼寄碼、驗證及重設 API 與對應 React 頁面。
- **FR-013**：密碼重設 MUST 僅接受有效且未使用的重設票券，成功後立即使票券失效。
- **FR-014**：後端 MUST 依 DB 表／DAO(JPA)／Service／BO(model)／Controller(REST API) 分層實作。
- **FR-015**：前端 MUST 依 `uiux/0.共用樣式/login.html`、`styles.css` 的雙欄登入視覺與操作流程實作註冊、登入及忘記密碼頁。
- **FR-016**：後端 MUST 有涵蓋六條 Sheet MUST、權限、重複信箱、錯碼、失效票券與 SMTP 失敗的自動測試。
- **FR-017**：前端 MUST 有一支 Cypress 規格逐項驗證註冊、重複信箱、登入與忘記密碼流程。
- **FR-018**：專案 MUST 產出 Postman 與 Cypress 測試報告，明確列出 task、checklist 與完成率，且所有建置成功。

### Key Entities

- **UserAccount**：既有使用者帳號；信箱註冊時 `username` 與 `email` 均為正規化信箱。
- **EmailVerification**：保存信箱、用途、驗證碼雜湊、效期、錯誤次數、驗證時間與使用時間。
- **EmailDeliveryLog**：保存寄送用途、遮罩收件者、成功／失敗狀態、失敗摘要與完成時間。
- **UserRole**：新信箱帳號與既有 `EMPLOYEE` 角色的關聯。

## Clarifications

### 自動決策紀錄

- **驗證流程狀態**：採用資料庫雜湊保存、10 分鐘效期、5 次嘗試與一次性票券；理由是同時滿足安全、跨程序部署與可測試性。
- **密碼規則**：本子模組採最少 8 碼且兩次輸入一致；工作表未指定複雜度細節，而管理員密碼政策屬其他非「預計開發」項目，不擴張範圍。
- **登入相容性**：新帳號以信箱同時作為 `username`，保留既有帳號登入；理由是符合「帳號即信箱」且不造成 breaking change。
- **新帳號角色**：預設 `EMPLOYEE`；理由是最小權限原則，註冊者不得自動取得管理權限。
- **Cypress 信件取得**：H2 本機測試 profile 使用明確啟用的 mock SMTP 與固定測試碼，正式環境預設關閉；理由是 E2E 不應依賴真實 Gmail 秘密或配額。
- **子模組目錄**：沿用既有 `specs/007-gmail-verification-mail`，擴充為完整信箱註冊登入；理由是現有內容已屬同一子模組，另建目錄會造成重複規格。

## Success Criteria

### Measurable Outcomes

- **SC-001**：六條 Sheet MUST 對應的後端整合測試與 Cypress 情境通過率為 100%。
- **SC-002**：所有 FR 均至少有一個 task 與一個可重現驗收證據，覆蓋率為 100%。
- **SC-003**：後端 test/build、前端 production build、Postman/Newman 與 Cypress 均以 exit code 0 完成。
- **SC-004**：驗證碼、明文密碼、密碼雜湊與 SMTP 密碼在 API 回應及應用程式日誌的洩漏數為 0。
- **SC-005**：有效使用者可在 3 分鐘內完成註冊，並可立即以信箱密碼進入首頁。

## Assumptions

- 正式環境由 `EMAIL_USER`、`EMAIL_PASSWORD` 或 Secret Manager 注入 Gmail SMTP 憑證。
- 本功能不實作管理員密碼複雜度設定，因該列狀態不是「預計開發」。
- 本功能不修改已完成的 LINE OAuth 註冊登入流程。
- Postman 自動驗收使用同一 Postman collection 的 Newman runner，以便產生可重現 CLI 與 JSON 證據。
