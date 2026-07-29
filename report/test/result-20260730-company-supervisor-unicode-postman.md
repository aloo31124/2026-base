# 公司主管管理 Unicode 修正測試報告

## 目錄

- [問題與修正](#問題與修正)
- [測試結果](#測試結果)
- [完成率](#完成率)
- [既有資料注意事項](#既有資料注意事項)

## 問題與修正

- 根因：SQL Server 的 `company.name`、`company.description`、`supervisor_profile.title` 原為 `varchar`，繁體中文寫入後被轉換成 `?`。
- JPA 修正：三個欄位加入 Hibernate `@Nationalized`，新建結構會採用 Unicode 字串型別。
- 既有資料庫修正：執行 `scripts/migrate-company-supervisor-unicode.sql`，三欄已轉為 `nvarchar`，公司名稱唯一限制在遷移後保留。
- 維運修正：Bash 與 PowerShell 資料庫初始化流程會自動且冪等地套用遷移。

## 測試結果

| 測試項目 | 結果 |
|---|---:|
| SQL Server 欄位型別 | `company.name = nvarchar(120)`、`company.description = nvarchar(500)`、`supervisor_profile.title = nvarchar(80)` |
| SQL Server 交易內繁中寫入／讀回 | 3/3 通過，測試交易已回滾 |
| 實際 API Postman requests | 15/15 通過 |
| 實際 API Postman assertions | 27/27 通過 |
| Postman 建立後資料庫原字讀回 | `Postman甲公司`、`Postman乙公司`、`營運主管`、`專案主管` 全部正確 |
| 後端自動測試 | 27/27 通過，0 failures、0 errors、0 skipped |
| Gradle 全專案建置（含 React production build） | `BUILD SUCCESSFUL` |
| 遷移重複執行 | 通過，未改壞既有 schema 或 constraint |

## 完成率

- Unicode 修正 task：4/4，完成率 **100%**
- Unicode requirement checklist：1/1，完成率 **100%**
- Postman requests：15/15，完成率 **100%**
- Postman assertions：27/27，完成率 **100%**
- 後端測試：27/27，完成率 **100%**
- 整體完成率：**100%**

## 既有資料注意事項

已經保存成 `????` 的內容在寫入當下已失去原始字元，無法由資料庫自動還原。欄位遷移後，請在公司主管管理頁重新輸入這些既有公司的名稱、說明與主管職稱；後續繁體中文會正常保存。
