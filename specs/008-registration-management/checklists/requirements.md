# Requirements Checklist：註冊登入管理

**目的**：驗證需求完整、明確、一致且可追溯  
**建立日期**：2026-07-29

## 目錄

- [Requirement Completeness](#requirement-completeness) — 5 項
- [Requirement Clarity](#requirement-clarity) — 4 項
- [Scenario Coverage](#scenario-coverage) — 4 項
- [Security & Traceability](#security--traceability) — 4 項

## Requirement Completeness

- [x] CHK001 是否定義密碼長度的合法範圍？[Spec §FR-001]
- [x] CHK002 是否分別定義英文字母與數字條件？[Spec §FR-002, FR-003]
- [x] CHK003 是否定義政策套用的流程範圍？[Spec §FR-004]
- [x] CHK004 是否定義兩種註冊方式的紀錄欄位？[Spec §FR-006, FR-007]
- [x] CHK005 是否明確排除既有帳號登入的重複紀錄？[Spec §FR-008]

## Requirement Clarity

- [x] CHK006 「密碼複雜度」是否已轉為可測量條件？[Clarity, Spec §FR-001–FR-003]
- [x] CHK007 密碼失敗回應是否要求列出全部缺少條件？[Spec §FR-005, Edge Cases]
- [x] CHK008 「是否成功」是否具體定義為首次建帳成功？[Clarification, Spec §FR-006]
- [x] CHK009 政策變更對既有密碼的影響是否清楚？[Spec §Edge Cases]

## Scenario Coverage

- [x] CHK010 是否涵蓋政策讀取、合法更新與非法更新？[Spec §US1]
- [x] CHK011 是否涵蓋註冊與忘記密碼兩條政策驗證流程？[Spec §US2]
- [x] CHK012 是否涵蓋信箱、LINE 與空資料狀態？[Spec §US3, Edge Cases]
- [x] CHK013 是否涵蓋 API 與頁面兩層拒絕流程？[Spec §US4]

## Security & Traceability

- [x] CHK014 是否指定後端管理角色？[Spec §FR-009]
- [x] CHK015 是否明確定義 API 拒絕訊息？[Spec §FR-010]
- [x] CHK016 是否明確定義頁面拒絕訊息？[Spec §FR-011, FR-012]
- [x] CHK017 每一條 Sheet MUST task 是否皆可對應 FR 與驗收情境？[Traceability]
