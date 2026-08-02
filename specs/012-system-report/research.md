# Research：系統報表

## 目錄

- [統計歸屬](#統計歸屬) — 以受派人公司唯一歸屬
- [日期與時區](#日期與時區) — 台北日界線與 366 天上限
- [折線圖](#折線圖) — 原生 SVG 與可存取摘要
- [測試工具](#測試工具) — 沿用 JUnit、Newman、Cypress

## 統計歸屬

- **Decision**：任務依受派人在 `company_membership` 的公司歸屬計入一次。
- **Rationale**：符合工作量歸屬，且現有唯一公司綁定可防止重複計數。
- **Alternatives considered**：依建立者公司可能誤表達承接負載；雙方公司都計入會重複。

## 日期與時區

- **Decision**：查詢 Instant 區間使用 `Asia/Taipei` 起日零時至迄日次日零時的半開區間，畫面回應以 LocalDate 表示並補零，期間最多 366 天。
- **Rationale**：包含完整首尾日、避免 23:59:59 精度問題，並與工作表所在時區一致。
- **Alternatives considered**：UTC 日期會造成台灣凌晨任務落在前一日；無上限查詢會放大 API 與 SVG 負載。

## 折線圖

- **Decision**：使用 React 原生 SVG `polyline`、座標標籤、`role=img` 與文字摘要。
- **Rationale**：不增加套件，Cypress 可直接驗證，並保留螢幕閱讀器語意。
- **Alternatives considered**：Canvas DOM 可測性較差；第三方圖表套件超出 MVP。

## 測試工具

- **Decision**：JUnit/MockMvc 驗證規則，Newman 執行 Postman collection 驗證真實 HTTP response，Cypress 驗證使用者流程。
- **Rationale**：符合使用者指定驗收鏈與專案既有工具版本。
- **Alternatives considered**：只用 curl 或單元測試不足以提供 Postman 與瀏覽器證據。
