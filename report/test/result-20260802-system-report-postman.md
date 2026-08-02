# 系統報表 Postman 測試報告

## 目錄

- [執行摘要](#執行摘要) — 真實 HTTP request 與 assertion 統計
- [API response 驗證](#api-response-驗證) — 正常、邊界與權限結果
- [Task 與 Checklist 對照](#task-與-checklist-對照) — 後端分層及測試證據
- [結論](#結論) — 完成率 100%

## 執行摘要

- 日期：2026-08-02
- 執行環境：Spring Boot 4.1.0 + H2 profile，`http://localhost:8080`
- Collection：`postman/system-report.postman_collection.json`
- 指令：`npm --prefix postman run test:system-report`
- Requests：17 executed、0 failed
- Test scripts：17 executed、0 failed
- Assertions：30 passed、0 failed
- 平均 response time：43 ms
- 總執行時間：956 ms
- 原始結果：`Backend/build/newman-system-report-result.json`

## API response 驗證

| 功能 | 實際 response 驗證 | 結果 |
|---|---|---|
| 系統管理員登入 | HTTP 200、roles 包含 `SYSTEM_ADMIN`、取得 JWT | 通過 |
| 測試資料建置 | 公司、主管、員工、公司綁定、員工綁定、任務皆 HTTP 200 且識別值正確 | 通過 |
| 公司篩選選項 | HTTP 200，回應包含本次建立的 `companyId` | 通過 |
| 預設一年趨勢 | HTTP 200、`companyName=全部公司`、365–366 個連續資料點 | 通過 |
| 單一公司趨勢 | `companyId/companyName` 正確、`totalTasks=1`、`companyCount=1`、3 個日期點 | 通過 |
| 每日點與摘要一致 | `sum(points.taskCount) = totalTasks` | 通過 |
| 日期反向 | HTTP 400、訊息為「開始日期不得晚於結束日期。」 | 通過 |
| 一般使用者權限 | HTTP 403、訊息為「[系統報表] [api] 無系統管理員權限。」 | 通過 |

## Task 與 Checklist 對照

| 驗收項目 | Task / Checklist | 證據 | 結果 |
|---|---|---|---|
| DB 表與 JPA DAO | T005–T006、CHK012–CHK013 | 公司歸屬後任務可由報表 API 正確統計 | 通過 |
| Service 業務邏輯 | T008、CHK014 | 預設一年、公司篩選、日期錯誤與連續資料點 assertions | 通過 |
| BO/DTO 與 Controller | T007、T009、CHK015–CHK016 | 標準 envelope 與 response 欄位 assertions | 通過 |
| 權限與錯誤處理 | T010、CHK011、CHK018 | 管理員成功、一般使用者 403、日期 400 | 通過 |
| Postman 完成條件 | T015、T018、CHK019–CHK020 | 17 requests、30 assertions 全數通過 | 通過 |

## 結論

Postman/Newman 完成率 **100%（30/30 assertions）**；所有系統報表 API response 值、日期邊界與角色權限均符合契約。
