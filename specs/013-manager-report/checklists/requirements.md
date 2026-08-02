# Specification Quality Checklist：主管報表

**Purpose**：驗證主管報表的範圍、統計口徑、分層、介面與交付條件均完整且可測試。  
**Created**：2026-08-02  
**Feature**：[spec.md](../spec.md)

## 目錄

- [內容品質](#內容品質) — 4 項
- [範圍與統計規則](#範圍與統計規則) — 8 項
- [分層與介面需求](#分層與介面需求) — 7 項
- [測試與完成條件](#測試與完成條件) — 7 項

## 內容品質

- [x] CHK001 規格是否聚焦主管查看公司總覽、自己指派趨勢與狀態比例的價值？ [Completeness, Spec §User Scenarios]
- [x] CHK002 所有 FR 是否具明確對象、MUST 規則與可量測結果？ [Clarity, Spec §Functional Requirements]
- [x] CHK003 是否無 `NEEDS CLARIFICATION`、TODO、矛盾或未定義名詞？ [Ambiguity]
- [x] CHK004 成功標準是否具體、技術中立且可驗證？ [Measurability, Spec §Success Criteria]

## 範圍與統計規則

- [x] CHK005 是否明確只包含「任務報表」第 18–20 列的「主管報表」？ [Scope, Spec §Assumptions]
- [x] CHK006 是否區分公司總覽的同公司任務與圖表的目前主管建立任務？ [Consistency, Spec §Clarifications, FR-003–FR-005]
- [x] CHK007 是否定義公司歸屬依受派人的唯一公司綁定？ [Clarity, Spec §FR-003]
- [x] CHK008 是否定義執行者選項只來自目前主管實際指派對象？ [Privacy, Spec §FR-007]
- [x] CHK009 是否把執行狀態明確限制為待處理、進行中與已完成？ [Clarity, Spec §FR-008]
- [x] CHK010 是否定義預設一年、含首尾、最多 366 天與台北時區？ [Boundary, Spec §FR-009–FR-010, FR-016]
- [x] CHK011 是否定義日期補零、狀態全零與無資料提示？ [Edge Case, Spec §FR-006, FR-014]
- [x] CHK012 是否定義退回／撤回任務與目前工作狀態的統計方式？ [Assumption, Spec §Assumptions]

## 分層與介面需求

- [x] CHK013 是否盤點 `company`、`company_membership`、`app_user`、`assigned_task` DB 表與關聯？ [Completeness, Spec §Key Entities]
- [x] CHK014 是否要求 JPA DAO 只處理唯讀資料存取？ [Architecture, Spec §FR-018]
- [x] CHK015 是否要求 Service 承擔身分、公司、日期、篩選、彙總與補零規則？ [Architecture, Spec §FR-018]
- [x] CHK016 是否定義 BO/DTO 的公司摘要、執行者、趨勢點與比例欄位？ [Data Contract, Spec §Key Entities]
- [x] CHK017 是否要求 Controller 使用標準 response 與主管角色授權？ [API, Spec §FR-001, FR-017–FR-018]
- [x] CHK018 是否要求 React page、共用篩選、兩標籤、折線圖與圓餅圖沿用 UIUX？ [UX, Spec §FR-006, FR-014–FR-015, FR-019]
- [x] CHK019 是否定義鍵盤、文字圖例與可存取圖表名稱？ [Accessibility, Spec §FR-006, FR-014]

## 測試與完成條件

- [x] CHK020 是否要求 JUnit 涵蓋公司隔離、主管隔離、正常、篩選、邊界與權限錯誤？ [Coverage, Spec §FR-020]
- [x] CHK021 是否要求 Postman/Newman 實際 HTTP response 驗證？ [Coverage, Spec §FR-020]
- [x] CHK022 是否要求 Postman 報告列出 request、assertion、通過數與完成率？ [Deliverable, Spec §FR-020]
- [x] CHK023 是否要求 Cypress 逐條對照工作表 MUST、task 與 checklist？ [Traceability, Spec §FR-020]
- [x] CHK024 是否要求 Cypress 報告列出情境、通過數與完成率？ [Deliverable, Spec §FR-020]
- [x] CHK025 是否要求後端、前端 production build 與全部回歸測試成功？ [Definition of Done, Spec §SC-006]
- [x] CHK026 是否要求全部證據完成後才把 Sheet `B18:B20` 更新為「開發完成」？ [Traceability, Spec §來源]
