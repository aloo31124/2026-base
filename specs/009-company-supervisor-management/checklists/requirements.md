# Requirements Checklist：公司主管管理

**Purpose**：驗證 Sheet 第 13–17 列需求的完整性、清晰度、一致性與可測性；本次增量聚焦第 16–17 列<br>
**Created**：2026-07-29<br>
**Feature**：[spec.md](../spec.md)

## 目錄

- [Requirement Completeness](#requirement-completeness) — 8 項
- [Requirement Clarity](#requirement-clarity) — 6 項
- [Scenario & Edge Coverage](#scenario--edge-coverage) — 7 項
- [Traceability & Acceptance](#traceability--acceptance) — 5 項
- [Notes](#notes) — 通過依據

## Requirement Completeness

- [x] CHK001 公司標籤的新增、查詢、修改與刪除需求是否完整定義？ [Completeness, Spec §FR-001–FR-004]
- [x] CHK002 主管必須來自既有註冊使用者及角色異動是否明確定義？ [Completeness, Spec §FR-005–FR-009]
- [x] CHK003 綁定、取消及雙條件查詢需求是否完整定義？ [Completeness, Spec §FR-010–FR-013]
- [x] CHK004 DB、API、React 頁面與三個標籤需求是否均有規格依據？ [Completeness, Spec §FR-014–FR-018]
- [x] CHK005 Sheet 第 13–15 列每一項 MUST task 是否都有對應 FR？ [Traceability, Spec §FR-001–FR-014]
- [x] CHK019 公司名稱、公司說明與主管職稱是否明確要求以 Unicode 保存並可驗證繁中原字讀回？ [Completeness, Spec §FR-019, SC-007]
- [x] CHK020 「綁定」改名「綁定公司」及主管／員工切換需求是否完整定義？ [Completeness, Spec §FR-020, US4–US5]
- [x] CHK021 員工綁定、取消與雙條件查詢是否都有明確 FR？ [Completeness, Spec §FR-021–FR-024]

## Requirement Clarity

- [x] CHK006 公司唯一性的正規化方式是否具體且可測？ [Clarity, Spec §FR-003]
- [x] CHK007 「已經註冊之使用者」是否限定為存在且啟用的帳號？ [Clarity, Spec §FR-005]
- [x] CHK008 一人一公司的唯一性是否明確套用主管與員工？ [Clarity, Spec §FR-012]
- [x] CHK009 主管與公司名稱查詢欄位及空白查詢行為是否清楚？ [Clarity, Spec §FR-013, Edge Cases]
- [x] CHK022 員工候選的啟用、角色、未綁定與非主管條件是否無歧義？ [Clarity, Spec §FR-022]
- [x] CHK023 員工查詢的姓名／帳號及空白條件行為是否清楚？ [Clarity, Spec §FR-023]

## Scenario & Edge Coverage

- [x] CHK010 是否定義公司仍有綁定時的刪除行為？ [Edge Case, Spec §FR-004]
- [x] CHK011 是否定義主管仍有公司時的刪除行為？ [Edge Case, User Story 2]
- [x] CHK012 是否定義重複主管、重複綁定與第二家公司衝突？ [Exception Flow, Edge Cases]
- [x] CHK013 是否定義停用使用者不得建立主管？ [Edge Case, Spec §FR-005]
- [x] CHK014 是否定義空查詢結果與無權限流程？ [Coverage, User Story 3–4]
- [x] CHK024 是否定義員工第二家公司衝突、取消後改綁及類型錯誤取消流程？ [Edge Case, Spec §FR-024, US4]
- [x] CHK025 是否定義綁定類型切換時表單、查詢與列表同步行為？ [Coverage, User Story 5]

## Traceability & Acceptance

- [x] CHK015 每條 FR 是否可對應到至少一個 User Story 驗收情境？ [Traceability]
- [x] CHK016 成功標準是否具有量測方式且不依賴特定實作？ [Measurability, Spec §Success Criteria]
- [x] CHK017 Postman 與 Cypress 的完成率報告是否列入 Definition of Done？ [Acceptance, Spec §SC-006]
- [x] CHK018 UI 參考來源與本次不處理的後續子模組是否清楚界定？ [Scope, Spec §Assumptions]
- [x] CHK026 Sheet 第 16–17 列兩項「預計開發」是否各自有 FR、task、Postman 與 Cypress 驗收依據？ [Traceability, Spec §FR-020–FR-024, SC-008–SC-009]

## Notes

- 26/26 項通過；公司、主管、員工綁定、唯一性、Unicode、權限、測試與報告範圍均已具體化。
