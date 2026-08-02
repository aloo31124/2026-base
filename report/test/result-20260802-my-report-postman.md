# 我的報表 Postman 測試報告

## 目錄

- [測試摘要](#測試摘要) — 真實 HTTP request 與 assertion 完成率
- [執行環境](#執行環境) — Postman、Newman、Spring Boot 與 H2
- [測試範圍](#測試範圍) — 17 個 request 對照 MUST 功能
- [Response 驗證結果](#response-驗證結果) — filters、report、資料隔離與錯誤
- [修復紀錄](#修復紀錄) — collection 腳本紅轉綠
- [完成率](#完成率) — 38/38 assertions
- [結論](#結論) — 後端 API 驗收通過

## 測試摘要

- 執行日期：2026-08-02（Asia/Taipei）
- Collection：`postman/my-report.postman_collection.json`
- 執行指令：`npm --prefix postman run test:my-report`
- 實際操作：已開啟本機 Postman desktop，並以同一份 collection 透過 Newman 對 `http://localhost:8080` 執行。
- 輸出證據：`backend/build/newman-my-report-result.json`

## 執行環境

- Postman desktop：已實際開啟
- Newman：6.2.2
- Spring Boot：4.1.0，`h2` profile
- Java：21
- API base URL：`http://localhost:8080/api`

## 測試範圍

| Request | 驗證重點 | 對應需求 | 結果 |
|---|---|---|---|
| 01–08 fixture 與登入 | 管理員、主管、員工、公司與 token | FR-001、FR-011 | 通過 |
| 09–12 任務資料 | 員工公司綁定、主管員工綁定、任務建立與完成狀態 | FR-002、FR-008 | 通過 |
| 13 filters | 本人唯一選項、三種工作狀態、今日預設迄日 | Sheet 23–24、FR-003、FR-006 | 通過 |
| 14 report | 本人、完成狀態、總數、單日趨勢與狀態 100% | Sheet 23–25、FR-004–008 | 通過 |
| 15 資料隔離 | 非本人 `assigneeId` 回 403 與指定訊息 | FR-002、FR-005 | 通過 |
| 16 非法日期 | 起日晚於迄日回 400 | FR-006 | 通過 |
| 17 未登入 | 無 token 回 401 標準 response | FR-001 | 通過 |

## Response 驗證結果

- `GET /api/my/reports/filters` 回傳一個且僅一個本人執行者、3 個工作狀態與 `defaultTo=2026-08-02`。
- `GET /api/my/reports/report` 在本人、`COMPLETED`、單日條件下回傳 `totalTasks=1`、單一 `trendPoints.taskCount=1`。
- `COMPLETED` 狀態桶回傳 `taskCount=1`、`percentage=100.0`，其餘狀態為 0。
- 指定主管 UUID 作為 `assigneeId` 回 HTTP 403，訊息為「只能查詢自己的任務報表。」。
- 起日晚於迄日回 HTTP 400，訊息為「開始日期不得晚於結束日期。」。
- 未登入 filters 回 HTTP 401，`success=false`。

## 修復紀錄

1. 第一輪抓到 Postman sandbox 中重複 `const data` 宣告，造成 test script 語法錯誤與 token 未保存。
2. 第二輪抓到機械替換誤將部分 `json().data` 改為不存在的 property，fixture 後續驗證失敗。
3. 修正為可重複宣告的 `var responseData` 並保留正確 `json().data` 後，第三輪 17 requests、38 assertions 全部通過。

## 完成率

| 指標 | 通過 | 總數 | 完成率 |
|---|---:|---:|---:|
| Request 執行 | 17 | 17 | 100% |
| Test script | 17 | 17 | 100% |
| Assertion | 38 | 38 | 100% |
| 失敗 | 0 | 38 | 0% |

## 結論

我的報表後端從登入、fixture、JPA／Service／Controller response、本人資料隔離到錯誤處理皆以真實 HTTP 驗收通過，Postman 完成率 100%。
