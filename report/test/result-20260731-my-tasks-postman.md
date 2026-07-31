# 我的任務 Postman 測試報告

## 目錄

- [執行摘要](#執行摘要) — 16 個 request 與 assertion
- [功能覆蓋](#功能覆蓋) — API response 驗證
- [結論](#結論) — 完成率 100%

## 執行摘要

- 日期：2026-07-31
- Collection：`postman/task-assignment.postman_collection.json`
- 指令：`npx newman run task-assignment.postman_collection.json`
- Requests：16 executed、0 failed
- Assertions：16 passed、0 failed
- 平均 response time：70 ms

## 功能覆蓋

| 功能 | 驗證 response | 結果 |
|---|---|---|
| 我的任務查詢與排序 | `workStatus=PENDING`、`progressPercent=10` | 通過 |
| 工作進度更新 | `workStatus=COMPLETED`、`progressPercent=80` | 通過 |
| 附件上傳 | `fileName=postman.txt`、`fileSize=2` | 通過 |
| 提交審核 | `submittedAt` 有值且 80% 可提交 | 通過 |
| 既有建檔與權限前置 | 公司、主管、員工、任務 response 正確 | 通過 |

## 結論

Postman/Newman 完成率 **100%（16/16 assertions）**，所有 response 值符合契約。
