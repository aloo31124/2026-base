# 主管報表 Postman 測試報告

## 目錄

- [測試摘要](#測試摘要) — 真實 HTTP request 與 assertion 完成率
- [執行環境](#執行環境) — Postman、Newman、Spring Boot 與 H2
- [測試範圍](#測試範圍) — 15 個 request 對照 MUST 功能
- [Response 驗證結果](#response-驗證結果) — filters、report、狀態與權限
- [完成率](#完成率) — 32/32 assertions
- [結論](#結論) — 後端 API 驗收通過

## 測試摘要

- 執行日期：2026-08-02（Asia/Taipei）
- Collection：`postman/manager-report.postman_collection.json`
- 執行指令：`npm --prefix postman run test:manager-report`
- 實際操作：已開啟本機 Postman desktop，並以同一份 collection 透過 Newman 對 `http://localhost:8080` 執行。
- 輸出證據：`backend/build/newman-manager-report-result.json`

## 執行環境

- Postman desktop：已實際開啟
- Newman：6.2.2
- Spring Boot：4.1.0，`h2` profile
- Java：21
- API base URL：`http://localhost:8080/api`

## 測試範圍

| Request | 驗證重點 | 結果 |
|---|---|---|
| 01–02 登入 | 系統管理員與一般使用者 token、角色 | 通過 |
| 03–07 管理資料 | 建立公司、主管、員工、授予主管與公司綁定 | 通過 |
| 08–11 主管情境 | 主管／員工登入、員工公司與主管員工綁定 | 通過 |
| 12 建立任務 | 受派人與預設 `PENDING` response | 通過 |
| 13 filters | 公司名稱、實際執行者、三種工作狀態、預設日期 | 通過 |
| 14 report | 公司總數、主管總數、執行者、單日趨勢與狀態比例 | 通過 |
| 15 權限 | 一般使用者 403 與主管報表專屬訊息 | 通過 |

## Response 驗證結果

- `GET /api/manager/reports/filters` 回傳所屬公司、實際受派人、`PENDING／IN_PROGRESS／COMPLETED` 與今日預設迄日。
- `GET /api/manager/reports/report` 回傳 `companyTotalTasks=1`、`managerTotalTasks=1`、單日 `taskCount=1`。
- 狀態桶 `PENDING` 回傳 `taskCount=1`、`percentage=100.0`，與任務總數一致。
- 一般使用者回傳 HTTP 403，訊息為「[主管報表] [api] 無主管權限。」。
- 第一輪偵測到 collection 重複 `const` 宣告後已修正為 Postman 相容變數，第二輪全部通過。

## 完成率

| 指標 | 通過 | 總數 | 完成率 |
|---|---:|---:|---:|
| Request 執行 | 15 | 15 | 100% |
| Test script | 15 | 15 | 100% |
| Assertion | 32 | 32 | 100% |
| 失敗 | 0 | 32 | 0% |

## 結論

主管報表後端從登入、資料準備、JPA／Service／Controller response 到權限拒絕皆以真實 HTTP 驗收通過，Postman 完成率 100%。
