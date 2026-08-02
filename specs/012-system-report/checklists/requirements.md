# Specification Quality Checklist：系統報表

**Purpose**：逐項驗證「系統報表」需求完整、可測試且涵蓋使用者指定分層與驗收證據。  
**Created**：2026-08-02  
**Feature**：[spec.md](../spec.md)

## 目錄

- [內容品質](#內容品質) — 4 項
- [範圍與業務規則](#範圍與業務規則) — 7 項
- [分層需求](#分層需求) — 6 項
- [測試與完成條件](#測試與完成條件) — 7 項

## 內容品質

- [x] CHK001 規格是否聚焦管理員查看任務趨勢的使用者價值？ [Completeness, Spec §User Scenarios]
- [x] CHK002 所有 FR 是否具 MUST、明確對象與可驗證結果？ [Clarity, Spec §Functional Requirements]
- [x] CHK003 是否無 `NEEDS CLARIFICATION`、TODO 或未定義名詞？ [Ambiguity]
- [x] CHK004 成功標準是否具體、可量測且不依賴實作細節？ [Measurability, Spec §Success Criteria]

## 範圍與業務規則

- [x] CHK005 是否明確只包含「任務報表」第 13–14 列的「系統報表」？ [Scope, Spec §Assumptions]
- [x] CHK006 是否定義全部公司與單一公司的統計範圍？ [Completeness, Spec §FR-003–FR-006]
- [x] CHK007 是否定義任務以受派人公司歸屬且不重複計數？ [Clarity, Spec §Clarifications]
- [x] CHK008 是否定義預設一年、含首尾與最多 366 天？ [Boundary, Spec §FR-007–FR-008]
- [x] CHK009 是否定義無任務日補零、完全空資料與不存在公司行為？ [Edge Case, Spec §FR-009, Edge Cases]
- [x] CHK010 是否定義 Asia/Taipei 日期切分與任務日期來源？ [Assumption, Spec §Assumptions]
- [x] CHK011 是否定義僅 SYSTEM_ADMIN 可存取頁面與 API？ [Security, Spec §FR-001]

## 分層需求

- [x] CHK012 是否盤點 `company`、`company_membership`、`assigned_task` DB 表與關聯？ [Completeness, Spec §Key Entities]
- [x] CHK013 是否要求 JPA DAO 僅執行資料查詢？ [Architecture, Spec §FR-013]
- [x] CHK014 是否要求 Service 承擔日期驗證、公司規則、彙總與補零？ [Architecture, Spec §FR-013]
- [x] CHK015 是否定義 BO/DTO 的公司選項、趨勢點與摘要欄位？ [Data Contract, Spec §Key Entities]
- [x] CHK016 是否要求 Controller 使用標準 REST response 與角色授權？ [API, Spec §FR-001, FR-010, FR-013]
- [x] CHK017 是否要求 React page、任務趨勢標籤、篩選與可存取折線圖沿用 UIUX？ [UX, Spec §FR-002, FR-011, FR-014]

## 測試與完成條件

- [x] CHK018 是否要求 JUnit 整合測試涵蓋正常、邊界與權限錯誤？ [Coverage, Spec §FR-015]
- [x] CHK019 是否要求 Postman/Newman 實際 HTTP response 驗證？ [Coverage, Spec §FR-015]
- [x] CHK020 是否要求 Postman 報告包含 request、assertion 與完成率？ [Deliverable, Spec §FR-015]
- [x] CHK021 是否要求 Cypress 對應 task 與 checklist 的 UI 核心流程？ [Coverage, Spec §FR-015]
- [x] CHK022 是否要求 Cypress 報告包含通過數與完成率？ [Deliverable, Spec §FR-015]
- [x] CHK023 是否要求後端與前端完整建置成功？ [Definition of Done, Spec §SC-005]
- [x] CHK024 是否要求所有證據完成後才將 Sheet 狀態更新為「開發完成」？ [Traceability, Spec §來源]
