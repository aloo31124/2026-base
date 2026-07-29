# Feature Specification：Gmail 信箱驗證測試

**Feature Branch**：`信箱註冊-帳密登入`  
**Created**：2026-07-29  
**Status**：Approved  
**Input**：系統管理員可在信箱驗證頁輸入收件信箱，透過 Gmail SMTP 寄送驗證碼。

## 目錄

- [User Scenarios & Testing](#user-scenarios--testing) — 系統管理員寄信、狀態回饋與權限邊界
  - [User Story 1 - 寄送信箱驗證碼 (Priority: P1)](#user-story-1---寄送信箱驗證碼-priority-p1) — 核心寄信流程
  - [User Story 2 - 顯示寄送結果 (Priority: P2)](#user-story-2---顯示寄送結果-priority-p2) — 成功與失敗回饋
  - [User Story 3 - 限制管理員使用 (Priority: P2)](#user-story-3---限制管理員使用-priority-p2) — 前後端雙層授權
  - [Edge Cases](#edge-cases) — 無效輸入與外部服務異常
- [Requirements](#requirements) — 可測試的功能與安全需求
  - [Functional Requirements](#functional-requirements) — 共 10 項
- [Clarifications](#clarifications) — 自動決策與理由
  - [自動決策紀錄](#自動決策紀錄) — 驗證碼、保存與設定策略
- [Success Criteria](#success-criteria) — 可量測成果
  - [Measurable Outcomes](#measurable-outcomes) — 共 4 項
- [Assumptions](#assumptions) — 範圍與外部依賴

## User Scenarios & Testing

### User Story 1 - 寄送信箱驗證碼 (Priority: P1)

系統管理員進入「信箱驗證」頁，輸入有效收件信箱並送出後，收件者可收到 AgentFlow 寄出的 6 位數驗證碼。

**Why this priority**：這是確認 Gmail SMTP 設定與寄信能力的核心價值。

**Independent Test**：以系統管理員送出有效信箱，確認寄信服務收到收件者、主旨與 6 位數驗證碼內容。

**Acceptance Scenarios**：

1. **Given** 系統管理員已登入且 Gmail SMTP 設定完整，**When** 輸入有效信箱並按下寄送，**Then** 系統寄出包含 6 位數驗證碼與 10 分鐘有效提示的純文字信件。
2. **Given** 系統管理員輸入格式錯誤的信箱，**When** 送出表單，**Then** 系統拒絕請求且不呼叫寄信服務。

### User Story 2 - 顯示寄送結果 (Priority: P2)

系統管理員送出後可在同一頁看到處理中、成功或失敗訊息，且成功回應不揭露驗證碼。

**Why this priority**：明確結果讓管理員能判斷 SMTP 是否正常，又不會透過 API 洩漏驗證碼。

**Independent Test**：分別模擬寄送成功與 SMTP 失敗，確認頁面顯示正確狀態且可再次操作。

**Acceptance Scenarios**：

1. **Given** SMTP 接受信件，**When** 寄送完成，**Then** 頁面顯示遮罩後的收件信箱與成功訊息。
2. **Given** SMTP 拒絕或連線失敗，**When** 寄送結束，**Then** 頁面顯示可理解的失敗訊息且驗證碼不出現在回應或日誌。

### User Story 3 - 限制管理員使用 (Priority: P2)

只有具 `SYSTEM_ADMIN` 角色的使用者能看見信箱驗證導覽並呼叫寄信 API。

**Why this priority**：寄信會消耗外部配額，必須避免一般使用者濫用。

**Independent Test**：以管理員與一般使用者分別呼叫 API，確認只有管理員成功。

**Acceptance Scenarios**：

1. **Given** 一般使用者已登入，**When** 開啟信箱驗證頁或呼叫寄信 API，**Then** 系統拒絕存取。
2. **Given** 系統管理員已登入，**When** 開啟功能，**Then** 可看見導覽項目與寄送表單。

### Edge Cases

- 空白或不符合電子郵件格式的輸入不得進入寄信服務。
- Gmail 帳號或應用程式密碼未設定時，API 回覆服務尚未設定，不回顯設定值。
- Gmail 連線逾時、驗證失敗或拒絕收件時，統一轉為安全的寄送失敗訊息。
- 使用者連續點擊時，送出期間按鈕停用，避免同一頁面重複送出。

## Requirements

### Functional Requirements

- **FR-001**：系統 MUST 提供僅系統管理員可存取的「信箱驗證」頁。
- **FR-002**：頁面 MUST 提供必填且具 email 格式驗證的收件信箱欄位。
- **FR-003**：後端 MUST 提供僅 `SYSTEM_ADMIN` 可呼叫的寄送驗證碼 API。
- **FR-004**：系統 MUST 使用 Gmail SMTP、TLS 與環境變數提供的帳號和應用程式密碼寄信。
- **FR-005**：系統 MUST 使用安全亂數產生器建立 6 位數驗證碼。
- **FR-006**：信件 MUST 包含 AgentFlow 識別、驗證碼及 10 分鐘有效提示。
- **FR-007**：API 成功回應 MUST 僅包含遮罩收件者與寄送時間，不得包含驗證碼。
- **FR-008**：SMTP 設定缺漏或寄送失敗 MUST 透過既有統一錯誤格式回應。
- **FR-009**：驗證碼與 Gmail 密碼 MUST NOT 寫入日誌、版本庫或前端資源。
- **FR-010**：新增功能 MUST 有服務單元測試、API 權限／輸入整合測試及前端 production build 驗證。

## Clarifications

### 自動決策紀錄

- **議題**：驗證碼格式與效期
  - **候選方案**：5 位英數、6 位數字、8 位英數
  - **採用方案**：6 位數字，信件提示 10 分鐘有效
  - **採用理由**：符合常見輸入體驗，管理員測試時容易辨識，且保留後續擴充正式驗證流程的合理介面。
  - **影響章節**：FR-005、FR-006
- **議題**：是否保存與驗證驗證碼
  - **候選方案**：記憶體、資料庫、目前不保存
  - **採用方案**：目前不保存
  - **採用理由**：需求只要求測試 Gmail SMTP 寄送；提前建立驗證狀態與驗證 API會超出 MVP 範圍。
  - **影響章節**：User Story 1、Assumptions
- **議題**：敏感設定保存方式
  - **候選方案**：寫入 application.yml、寫入前端、由執行環境注入
  - **採用方案**：由 `EMAIL_USER`、`EMAIL_PASSWORD` 環境變數注入
  - **採用理由**：避免憑證進入 Git，並支援本機與部署環境分離。
  - **影響章節**：FR-004、FR-009

## Success Criteria

### Measurable Outcomes

- **SC-001**：管理員能在 30 秒內完成輸入信箱並送出測試信。
- **SC-002**：有效與無效信箱、管理員與一般使用者、SMTP 成功與失敗案例的自動測試通過率為 100%。
- **SC-003**：任何 API 回應、應用程式日誌與受版控檔案中的 Gmail 密碼及驗證碼洩漏數為 0。
- **SC-004**：前後端 production build 均成功完成。

## Assumptions

- Gmail 帳號已啟用可供 SMTP 使用的應用程式密碼。
- 本功能是 SMTP 連線測試入口，不包含使用者註冊、忘記密碼或驗證碼核銷。
- 驗證碼的 10 分鐘效期目前為信件告知；正式核銷功能導入時再建立持久化與一次性使用規則。
- 部署環境的 Gmail 憑證應由 Secret Manager 或等效秘密管理服務注入。
