# 註冊登入管理 Postman 測試報告

## 目錄

- [測試環境](#測試環境) — H2、mock mail、Postman
- [執行結果](#執行結果) — 10 requests、15 assertions
- [案例對照](#案例對照) — API 與權限覆蓋
- [完成率](#完成率) — 100%

## 測試環境

- 日期：2026-07-29
- 後端：Spring Boot test profile，H2，mock mail
- 測試工具：Postman 12.21.2 已實際開啟 collection；Newman 6.2.2 執行並輸出 JSON
- Collection：`postman/registration-management.postman_collection.json`
- 原始結果：`backend/build/newman-registration-management-result.json`

## 執行結果

| 指標 | 結果 |
|---|---:|
| Requests | 10 / 10 |
| Test scripts | 10 / 10 |
| Assertions | 15 / 15 |
| Failed | 0 |
| 平均 response time | 96 ms |
| 最慢 response time | 376 ms |

## 案例對照

- 管理員登入、政策讀取、政策更新：通過。
- 一般使用者呼叫管理 API：403 與指定模組訊息通過。
- 信箱寄碼、核銷：通過。
- 弱密碼同時回覆長度與數字缺口：通過。
- 符合政策完成註冊：通過。
- 管理員查詢信箱成功註冊紀錄：通過。

## 完成率

**100%（10/10 requests，15/15 assertions）**
