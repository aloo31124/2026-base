# 任務指派 Postman 測試報告

## 目錄

- [結果](#結果) — 13 個請求與斷言
- [覆蓋範圍](#覆蓋範圍) — 後端業務流程
- [證據](#證據) — collection 與 JSON 結果

## 結果

- 執行日期：2026-07-30
- 工具：Postman collection + Newman 6.2.2
- Requests：13/13 成功
- Assertions：13/13 成功
- 失敗：0
- 完成率：100%
- 平均 response time：43 ms（最小 7 ms、最大 111 ms）

## 覆蓋範圍

1. 管理員登入、建立公司、主管帳號、員工帳號與主管身分。
2. 主管公司綁定、主管與員工登入。
3. 員工依公司名稱完成公司綁定。
4. 主管綁定同公司員工。
5. 主管建立任務，核對 `ASSIGNED` 與受派人。
6. 依任務名稱、員工帳號與期限升冪查詢唯一結果。
7. 員工退回任務，核對 `RETURNED` 與退回原因。

## 證據

- Collection：`postman/task-assignment.postman_collection.json`
- Newman JSON：`backend/build/newman-task-assignment-result.json`
- 指令：`npm --prefix postman run test:task-assignment`
