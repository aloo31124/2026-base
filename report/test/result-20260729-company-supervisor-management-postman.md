# Postman 測試報告：公司主管管理

## 目錄

- [測試範圍](#測試範圍) — Sheet 第 12–15 列與 API
- [執行環境](#執行環境) — 真實啟動的 Spring Boot/H2
- [結果摘要](#結果摘要) — 15 requests、27 assertions
- [驗收明細](#驗收明細) — response 值與業務規則
- [完成率](#完成率) — 100%

## 測試範圍

- 公司新增、查詢、修改與刪除 API。
- 主管只能綁定既有註冊使用者及主管角色。
- 一家公司多名主管、同一主管一家公司、取消後改綁。
- 依公司名稱與主管姓名查詢綁定。
- 非系統管理員的 API 403 與指定訊息。

## 執行環境

- 日期：2026-07-29
- 後端：Spring Boot 4.1.0、Java 21、`test` profile、H2。
- 實際 API：`http://localhost:8081`
- Collection：`postman/company-supervisor-management.postman_collection.json`
- 指令：`npx newman run company-supervisor-management.postman_collection.json --env-var baseUrl=http://localhost:8081 --reporters cli,json --reporter-json-export ../backend/build/newman-company-supervisor-management-result.json`
- 原始結果：`backend/build/newman-company-supervisor-management-result.json`

## 結果摘要

| 指標 | 總數 | 通過 | 失敗 |
|---|---:|---:|---:|
| Requests | 15 | 15 | 0 |
| Test scripts | 15 | 15 | 0 |
| Assertions | 27 | 27 | 0 |
| Iterations | 1 | 1 | 0 |

平均 response time 為 35 ms，最短 7 ms，最長 113 ms；Newman exit code 為 0。

## 驗收明細

| 驗收項目 | 關鍵 response 驗證 | 結果 |
|---|---|---|
| 管理員與一般使用者登入 | HTTP 200、JWT 與角色 | 通過 |
| API 權限 | HTTP 403、`[公司主管管理] [api] 無系統管理員權限。` | 通過 |
| 公司建立 | HTTP 200、名稱與說明等於 request | 通過 |
| 主管建立 | HTTP 200、`userId` 等於既有使用者、職稱正確 | 通過 |
| 同公司多主管 | 兩筆綁定皆 HTTP 200 且 `companyId` 相同 | 通過 |
| 雙條件查詢 | 僅回傳指定公司及主管的一筆資料 | 通過 |
| 一人第二家公司 | HTTP 409、訊息包含「已綁定公司」 | 通過 |
| 取消與改綁 | DELETE data 為 `null`，取消後第二家公司綁定 HTTP 200 | 通過 |

## 完成率

- API task 覆蓋率：100%
- Postman request 通過率：100%
- Postman assertion 通過率：100%
- **總完成率：100%**
