# Postman 測試報告：公司主管管理「綁定公司」增量

## 目錄

- [測試範圍](#測試範圍) — Sheet「任務指派」第 16–17 列
- [執行環境](#執行環境) — Postman 與真實 Spring Boot/H2 API
- [結果摘要](#結果摘要) — 22 requests、41 assertions
- [增量驗收明細](#增量驗收明細) — 主管向後相容與員工綁定
- [Task 與 Checklist 對應](#task-與-checklist-對應) — Speckit 逐項追溯
- [完成率](#完成率) — 100%

## 測試範圍

- 保留既有公司、主管與主管綁定 API 行為。
- 公司可綁定多名主管，同一主管只能綁定一家公司。
- 系統管理員可將符合資格的已註冊員工綁定公司。
- 員工綁定可依公司名稱、員工姓名或帳號查詢。
- 同一員工第二家公司回覆 409，取消後可改綁。
- 主管取消端點不得刪除員工綁定，確認成員類型保護。
- 一般使用者呼叫管理 API 回覆 403 與模組指定訊息。

## 執行環境

- 日期：2026-07-30
- Postman Desktop：已實際開啟並載入 `postman/company-supervisor-management.postman_collection.json`
- Collection Runner：Newman 6.2.2
- 後端：Spring Boot 4.1.0、Java 21、`test` profile、H2
- 實際 API：`http://localhost:8081`
- 執行指令：`npx newman run company-supervisor-management.postman_collection.json --env-var baseUrl=http://localhost:8081 --reporters cli,json --reporter-json-export ../backend/build/newman-company-supervisor-management-20260730-result.json`
- 原始結果：`backend/build/newman-company-supervisor-management-20260730-result.json`

## 結果摘要

| 指標 | 總數 | 通過 | 失敗 |
|---|---:|---:|---:|
| Requests | 22 | 22 | 0 |
| Test scripts | 22 | 22 | 0 |
| Assertions | 41 | 41 | 0 |
| Iterations | 1 | 1 | 0 |

總執行時間 1,399 ms；平均 response time 50.55 ms，最短 8 ms，最長 371 ms；Newman exit code 為 0。

## 增量驗收明細

| 驗收項目 | 關鍵 response 驗證 | 結果 |
|---|---|---|
| 主管 API 向後相容 | `/bindings` 建立、查詢、取消與改綁皆維持既有 payload | 通過 |
| 員工資格來源 | 新建使用者 response 包含 `EMPLOYEE` 角色 | 通過 |
| 員工綁定 | HTTP 200，`companyId`、`userId`、`employeeName` 等於 request 與來源資料 | 通過 |
| 公司／員工雙條件查詢 | HTTP 200，只回傳一筆同時符合公司與員工名稱的資料 | 通過 |
| 一人一家公司 | 同一員工綁定第二家公司回覆 HTTP 409，訊息包含「已綁定公司」 | 通過 |
| 類型保護 | 以主管端點取消員工綁定回覆 HTTP 409，訊息包含「不是主管綁定」 | 通過 |
| 取消員工綁定 | HTTP 200，`data` 為 `null` | 通過 |
| 取消後改綁 | 第二家公司綁定回覆 HTTP 200，`companyId` 與 `userId` 正確 | 通過 |

## Task 與 Checklist 對應

| Speckit 項目 | Postman request | 結果 |
|---|---|---|
| T043–T047、CHK021–CHK024 | 16–22 | 7/7 requests 通過 |
| FR-021 員工綁定與取消 | 17、21、22 | 通過 |
| FR-022 員工資格 | 16、17 | 通過 |
| FR-023 公司／員工查詢 | 18 | 通過 |
| FR-024 一人一公司與類型保護 | 19、20 | 通過 |

## 完成率

- 後端增量 task 覆蓋率：100%
- Postman request 通過率：100%（22/22）
- Postman assertion 通過率：100%（41/41）
- Sheet 第 16–17 列後端 MUST task 完成率：100%
- **總完成率：100%**
