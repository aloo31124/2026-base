# Requirements Quality Checklist：後台登出時間設定

**Purpose**：驗證管理範圍、動態 JWT 效期、權限、生效時機與邊界需求是否完整可測。
**Created**：2026-08-02
**Feature**：[spec.md](../spec.md)

## 目錄

- [Requirement Completeness](#requirement-completeness) — 5 項
- [Requirement Clarity](#requirement-clarity) — 4 項
- [Requirement Consistency](#requirement-consistency) — 4 項
- [Acceptance Criteria Quality](#acceptance-criteria-quality) — 4 項
- [Scenario & Edge Case Coverage](#scenario--edge-case-coverage) — 5 項
- [Notes](#notes) — 22/22 通過

## Requirement Completeness

- [x] CHK001 是否定義政策欄位、單位、範圍與預設值？ [Completeness, Spec §FR-001–003]
- [x] CHK002 是否定義 GET/PUT API、回應格式與權限？ [Completeness, Spec §FR-004–005]
- [x] CHK003 是否定義 JWT 簽發如何套用目前政策？ [Completeness, Spec §FR-006–007]
- [x] CHK004 是否定義後台表單、說明、成功與錯誤狀態？ [Completeness, Spec §FR-009–010]
- [x] CHK005 是否定義後端、E2E 與 build 完成訊號？ [Completeness, Spec §FR-011–012]

## Requirement Clarity

- [x] CHK006 「登出時間」是否明確定義為 JWT 絕對效期分鐘數？ [Clarity, Spec §US2, §FR-006]
- [x] CHK007 範圍是否明確為含 5 與 1440 的整數？ [Clarity, Spec §FR-002]
- [x] CHK008 更新後生效時點是否明確限定為新簽發 JWT？ [Clarity, Spec §FR-008]
- [x] CHK009 預設值是否明確沿用部署環境設定？ [Clarity, Spec §FR-003]

## Requirement Consistency

- [x] CHK010 後台設定與前端 JWT exp 倒數是否維持單一時間來源？ [Consistency, Spec §FR-006, Feature 015]
- [x] CHK011 帳密、信箱與 LINE 是否共用相同效期規則？ [Consistency, Spec §FR-007]
- [x] CHK012 API 與頁面是否沿用註冊登入管理的雙層 SYSTEM_ADMIN 權限？ [Consistency, Spec §FR-005, §US4]
- [x] CHK013 既有 token 不變與無 refresh/blacklist 範圍是否一致？ [Consistency, Spec §FR-008, §Out of Scope]

## Acceptance Criteria Quality

- [x] CHK014 是否可客觀驗證更新、重新讀取值一致？ [Measurability, Spec §US1, §SC-002]
- [x] CHK015 是否可由 JWT `exp - iat` 客觀驗證動態效期？ [Measurability, Spec §US2, §SC-003]
- [x] CHK016 是否可驗證舊 JWT exp 不被政策更新改寫？ [Measurability, Spec §US3]
- [x] CHK017 是否有整體測試通過率完成標準？ [Measurability, Spec §SC-005]

## Scenario & Edge Case Coverage

- [x] CHK018 是否涵蓋資料庫無政策時的初始建立？ [Coverage, Spec §Edge Cases]
- [x] CHK019 是否涵蓋 4、5、1440、1441 分鐘邊界？ [Coverage, Spec §FR-002, §US4]
- [x] CHK020 是否涵蓋非管理員 API 與頁面拒絕？ [Coverage, Spec §US4]
- [x] CHK021 是否涵蓋並行更新採最後成功值？ [Coverage, Spec §Edge Cases]
- [x] CHK022 是否涵蓋舊 token 與新 token 的不同生效語意？ [Coverage, Spec §US2–US3]

## Notes

- 22/22 項通過；所有需求均有 Spec、Decision、Coverage 或 Measurability 追溯。
