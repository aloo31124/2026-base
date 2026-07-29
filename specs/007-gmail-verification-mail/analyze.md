# Analyze：Gmail 信箱驗證測試

## 目錄

- [Coverage Summary](#coverage-summary) — FR 與 Task 對照
- [Constitution Alignment](#constitution-alignment) — 無衝突
- [Risk Review](#risk-review) — 敏感資料、濫用與外部服務
- [Metrics](#metrics) — 完整性統計

## Coverage Summary

| Requirement | Tasks | 覆蓋結果 |
|-------------|-------|----------|
| FR-001–003 | T004、T008、T011–T013 | 管理員頁面與 API 雙層授權 |
| FR-004 | T001–T002、T005–T006 | Gmail SMTP、TLS 與環境變數 |
| FR-005–006 | T003、T007 | 6 位數安全亂數與信件內容 |
| FR-007–008 | T003–T004、T007–T009 | 遮罩回應與統一錯誤 |
| FR-009 | T002–T003、T005–T007、T014 | 秘密與驗證碼不洩漏 |
| FR-010 | T003–T004、T015–T016 | 單元、整合與建置驗證 |

## Constitution Alignment

- 分層架構：Controller → Service → MailGateway，無跨層存取。
- 測試必要性：單元與整合測試皆列入任務。
- MVP：未建立驗證碼資料表或核銷流程。
- 向後相容：只新增 API 與頁面。
- 文件規範：全部使用繁體中文並具目錄。

## Risk Review

- 敏感資料：真實 Gmail 憑證只由環境注入，API 與日誌不回顯。
- 寄信濫用：前端 Guard、導覽隱藏及後端角色授權三層限制。
- SMTP 不穩定：外部例外轉為統一 502；設定缺漏回 503。
- 測試誤寄：測試以 mock gateway 執行。

## Metrics

- Functional Requirements：10
- Tasks：16
- FR Task Coverage：100%
- Ambiguity Count：0
- Duplication Count：0
- Critical Issues：0
