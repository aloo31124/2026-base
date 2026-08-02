# Research: 我的報表

## 目錄

- [決策 1：報表資料來源](#決策-1報表資料來源) — 沿用 assigned_task 即時查詢
- [決策 2：員工資料隔離](#決策-2員工資料隔離) — principal 強制本人
- [決策 3：日期與聚合](#決策-3日期與聚合) — 台北時區每日補零
- [決策 4：前端圖表](#決策-4前端圖表) — 沿用無第三方依賴元件
- [決策 5：驗收工具](#決策-5驗收工具) — Newman 與 Cypress

## 決策 1：報表資料來源

**Decision**: 沿用 `assigned_task`，由 Spring Data JPA projection 取得指派時間、工作狀態與受派人。
**Rationale**: 避免報表快照與交易資料同步問題，符合 MVP 與既有報表實作。
**Alternatives considered**: 新報表表會增加同步與 migration 成本；前端聚合會洩漏不必要明細並弱化授權。

## 決策 2：員工資料隔離

**Decision**: Service 一律由 principal 取得 `UserAccount`，DAO 以 `task.assignee = :assignee` 查詢；可選 `assigneeId` 僅允許等於本人。
**Rationale**: 以伺服器端不可覆寫條件落實最小權限。
**Alternatives considered**: 公司範圍或任意 UUID 都超出「我的」語意並帶來資料外洩風險。

## 決策 3：日期與聚合

**Decision**: 使用 `Asia/Taipei`，含起日與含迄日，迄日轉次日零時 exclusive，最大 366 天；每日零值由 Service 補齊。
**Rationale**: 與既有系統及主管報表一致，可得到穩定連續折線圖。
**Alternatives considered**: UTC 日期可能跨日；只回傳有資料日期會讓折線圖誤解中間缺口。

## 決策 4：前端圖表

**Decision**: 沿用 `TaskTrendChart` 與 `TaskStatusPieChart`，只讓圓餅圖 test id 可配置。
**Rationale**: 已具可存取文字、RWD 與 uiux 共用樣式，避免新增依賴和視覺漂移。
**Alternatives considered**: 新圖表套件會增加 bundle、API 與測試成本。

## 決策 5：驗收工具

**Decision**: 後端整合測試作為可重複資料 fixture；Newman 對啟動中的真實 API 驗證 response；Cypress 對真實頁面與 API flow 驗證。
**Rationale**: 同時覆蓋業務邏輯、HTTP 邊界與瀏覽器操作。
**Alternatives considered**: 僅 mock API 無法滿足實際 Postman 與 Cypress 要求。
