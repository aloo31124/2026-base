# Requirements Checklist：公司主管管理

**Purpose**：驗證 Sheet 第 12–15 列需求的完整性、清晰度、一致性與可測性  
**Created**：2026-07-29  
**Feature**：[spec.md](../spec.md)

## 目錄

- [Requirement Completeness](#requirement-completeness) — 6 項
- [Requirement Clarity](#requirement-clarity) — 4 項
- [Scenario & Edge Coverage](#scenario--edge-coverage) — 5 項
- [Traceability & Acceptance](#traceability--acceptance) — 4 項
- [Notes](#notes) — 通過依據

## Requirement Completeness

- [x] CHK001 公司標籤的新增、查詢、修改與刪除需求是否完整定義？ [Completeness, Spec §FR-001–FR-004]
- [x] CHK002 主管必須來自既有註冊使用者及角色異動是否明確定義？ [Completeness, Spec §FR-005–FR-009]
- [x] CHK003 綁定、取消及雙條件查詢需求是否完整定義？ [Completeness, Spec §FR-010–FR-013]
- [x] CHK004 DB、API、React 頁面與三個標籤需求是否均有規格依據？ [Completeness, Spec §FR-014–FR-018]
- [x] CHK005 Sheet 第 12–15 列每一項 MUST task 是否都有對應 FR？ [Traceability, Spec §FR-001–FR-014]
- [x] CHK019 公司名稱、公司說明與主管職稱是否明確要求以 Unicode 保存並可驗證繁中原字讀回？ [Completeness, Spec §FR-019, SC-007]

## Requirement Clarity

- [x] CHK006 公司唯一性的正規化方式是否具體且可測？ [Clarity, Spec §FR-003]
- [x] CHK007 「已經註冊之使用者」是否限定為存在且啟用的帳號？ [Clarity, Spec §FR-005]
- [x] CHK008 一人一公司的唯一性是否明確套用主管與員工？ [Clarity, Spec §FR-012]
- [x] CHK009 主管與公司名稱查詢欄位及空白查詢行為是否清楚？ [Clarity, Spec §FR-013, Edge Cases]

## Scenario & Edge Coverage

- [x] CHK010 是否定義公司仍有綁定時的刪除行為？ [Edge Case, Spec §FR-004]
- [x] CHK011 是否定義主管仍有公司時的刪除行為？ [Edge Case, User Story 2]
- [x] CHK012 是否定義重複主管、重複綁定與第二家公司衝突？ [Exception Flow, Edge Cases]
- [x] CHK013 是否定義停用使用者不得建立主管？ [Edge Case, Spec §FR-005]
- [x] CHK014 是否定義空查詢結果與無權限流程？ [Coverage, User Story 3–4]

## Traceability & Acceptance

- [x] CHK015 每條 FR 是否可對應到至少一個 User Story 驗收情境？ [Traceability]
- [x] CHK016 成功標準是否具有量測方式且不依賴特定實作？ [Measurability, Spec §Success Criteria]
- [x] CHK017 Postman 與 Cypress 的完成率報告是否列入 Definition of Done？ [Acceptance, Spec §SC-006]
- [x] CHK018 UI 參考來源與本次不處理的後續子模組是否清楚界定？ [Scope, Spec §Assumptions]

## Notes

- 19/19 項通過；公司、主管、綁定、唯一性、Unicode、權限、測試與報告範圍均已具體化。
