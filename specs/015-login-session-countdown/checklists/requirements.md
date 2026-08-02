# Requirements Quality Checklist：登入工作階段倒數

**Purpose**：驗證登入倒數、JWT 到期、自動登出、RWD 與例外需求的完整性、清楚度及可測量性。
**Created**：2026-08-02
**Feature**：[spec.md](../spec.md)

## 目錄

- [Requirement Completeness](#requirement-completeness) — 5 項
- [Requirement Clarity](#requirement-clarity) — 4 項
- [Requirement Consistency](#requirement-consistency) — 3 項
- [Acceptance Criteria Quality](#acceptance-criteria-quality) — 4 項
- [Scenario & Edge Case Coverage](#scenario--edge-case-coverage) — 5 項
- [Dependencies & Assumptions](#dependencies--assumptions) — 3 項
- [Notes](#notes) — 24/24 通過

## Requirement Completeness

- [x] CHK001 是否定義倒數的位置、標籤與時間格式？ [Completeness, Spec §FR-001]
- [x] CHK002 是否定義時間來源、更新頻率與歸零行為？ [Completeness, Spec §FR-002–004]
- [x] CHK003 是否列出到期時需清除的所有前端登入狀態？ [Completeness, Spec §FR-005]
- [x] CHK004 是否定義到期後的導頁方式與使用者訊息？ [Completeness, Spec §FR-006–007]
- [x] CHK005 是否定義桌面、窄螢幕與輔助技術需求？ [Completeness, Spec §FR-010]

## Requirement Clarity

- [x] CHK006 「時間限制」是否明確對應目前 JWT 的 `exp` 而非另一個固定值？ [Clarity, Spec §FR-002]
- [x] CHK007 「自動登出」是否明確量化清除 Redux 與兩個 localStorage 鍵值？ [Clarity, Spec §FR-005]
- [x] CHK008 倒數誤差與到期導頁反應時間是否有數值標準？ [Clarity, Spec §SC-002–003]
- [x] CHK009 使用者操作是否延長期限已有明確答案？ [Clarity, Spec §FR-009]

## Requirement Consistency

- [x] CHK010 帳密、信箱與 LINE 登入是否一致沿用相同 JWT 到期規則？ [Consistency, Spec §US4]
- [x] CHK011 前端倒數與後端 JWT 到期語意是否維持單一來源？ [Consistency, Spec §FR-002, §Assumptions]
- [x] CHK012 到期清除、Guard 導頁與登入頁提示是否無互相矛盾？ [Consistency, Spec §FR-005–007]

## Acceptance Criteria Quality

- [x] CHK013 每條 User Story 是否有可獨立驗證的 test 與 acceptance scenario？ [Measurability, Spec §US1–US4]
- [x] CHK014 是否能客觀驗證經過一秒後倒數減少一秒？ [Measurability, Spec §US1, §SC-002]
- [x] CHK015 是否能客觀驗證到期後清除儲存與導頁？ [Measurability, Spec §US2, §SC-003]
- [x] CHK016 是否將 build 與專屬 E2E 綠燈列為完成訊號？ [Measurability, Spec §SC-005]

## Scenario & Edge Case Coverage

- [x] CHK017 是否涵蓋正常倒數、到期與頁面初載即過期？ [Coverage, Spec §US1–US2, §Edge Cases]
- [x] CHK018 是否涵蓋背景分頁節流、休眠及恢復焦點？ [Coverage, Spec §US3, §FR-008]
- [x] CHK019 是否定義無效或缺少 `exp` 時不得顯示虛假時間？ [Coverage, Spec §Edge Cases]
- [x] CHK020 是否定義剩餘五分鐘內的警示狀態？ [Coverage, Spec §Edge Cases]
- [x] CHK021 是否定義 720px 以下仍保留時間數值？ [Coverage, Spec §Edge Cases, §FR-010]

## Dependencies & Assumptions

- [x] CHK022 是否記載 JWT 目前預設效期與環境設定依賴？ [Assumption, Spec §Assumptions]
- [x] CHK023 是否明確排除 refresh token、滑動續期與伺服器撤銷清單？ [Boundary, Spec §Out of Scope]
- [x] CHK024 是否說明不修改後端登入契約的理由？ [Decision, Spec §Clarifications]

## Notes

- 24/24 項需求品質檢核通過，追溯率 100%。
- 清單檢查需求文字是否完整可測，不取代 Cypress 實作驗收。
