# Research：主管報表

## 目錄

- [統計範圍](#統計範圍) — 公司總覽與主管個人圖表分流
- [狀態與篩選](#狀態與篩選) — 工作狀態、執行者及共用條件
- [日期與時區](#日期與時區) — 台北日界線與 366 天上限
- [圖表與可存取性](#圖表與可存取性) — 原生 SVG 與文字替代
- [測試工具](#測試工具) — JUnit、Newman、Cypress

## 統計範圍

- **Decision**：公司摘要依受派人公司統計；趨勢與比例依 `creator` 等於目前主管統計。
- **Rationale**：工作表以不同語句明確定義兩層範圍，分開才不會少算公司工作量或洩漏其他主管個人指派細節。
- **Alternatives considered**：全部依建立者公司會無法涵蓋公司內其他主管；全部依公司會違反「自己指派出」。

## 狀態與篩選

- **Decision**：使用既有 `AssignedTask.WorkStatus` 三態；執行者只列目前主管曾指派的對象，兩標籤共用全部條件。
- **Rationale**：工作狀態直接對應執行進度；實際受派人清單兼顧隱私與有效選項，共用條件使兩圖可比較。
- **Alternatives considered**：指派生命週期無法表示進度；公司全員可能包含無關人員；各標籤獨立條件易造成誤讀。

## 日期與時區

- **Decision**：以 `Asia/Taipei` 起日零時至迄日次日零時的半開 Instant 區間查詢，預設今日往前一年，最多 366 天。
- **Rationale**：完整含首尾並避開時間精度錯誤，亦與既有系統報表一致。
- **Alternatives considered**：UTC 會造成台灣凌晨跨日；本曆年不等於「一年內」；無上限會放大查詢與 SVG 負載。

## 圖表與可存取性

- **Decision**：沿用原生 SVG 折線圖，新增以 conic-gradient 顯示、文字圖例與隱藏資料表支援的圓餅圖元件。
- **Rationale**：不增加依賴，圖例可提供精確數量與比例；CSS 圓餅搭配語意文字比大量 SVG path 計算更簡潔可維護。
- **Alternatives considered**：Canvas 缺少 DOM 可測性；第三方套件超出 MVP；只顯示顏色不具可存取性。

## 測試工具

- **Decision**：JUnit／MockMvc 驗證分層規則，Newman 執行 Postman collection 驗證真實 HTTP response，Cypress 驗證完整 React 操作。
- **Rationale**：符合工作表指定的建置與證據鏈，並沿用專案既有版本及報告格式。
- **Alternatives considered**：只用單元測試或 curl 無法提供 Postman 與瀏覽器流程證據。
