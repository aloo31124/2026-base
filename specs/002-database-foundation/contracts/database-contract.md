# Database Contract

## 目錄

- [環境變數](#環境變數) — 應用程式連線介面
- [命名](#命名) — Schema 規範

## 環境變數

`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`；未提供時使用本機開發資料庫。

## 命名

所有表/欄位使用小寫 snake_case，關聯 FK 使用 `{table}_id`。

