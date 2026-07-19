# AgentFlow Base

Spring Boot 4.1 + React 19 的前後端分離基礎專案，提供 MSSQL/JPA、JWT 登入、RBAC 使用者分權、統一例外與 Log，以及 test CRUD 驗證頁。

## 目錄

- [需求](#需求) — 本機工具
- [初始化資料庫](#初始化資料庫) — MSSQL bootstrap
- [啟動](#啟動) — 後端與前端
- [建置與測試](#建置與測試) — Gradle、Postman、Cypress
- [初始帳號](#初始帳號) — 開發環境登入
- [部署至 GCP Cloud Run](#部署至-gcp-cloud-run) — 一鍵部署測試機

## 需求

- Java 21+
- SQL Server 2019+
- Node 20+（Gradle 完整 build 會自行下載 Node 24 LTS）

## 初始化資料庫

```powershell
pwsh ./scripts/init-database.ps1
```

腳本建立 `base_20260716_01` 資料庫與同名專用 Login，可透過參數覆寫。

## 啟動

```powershell
./backend/gradlew.bat -p backend bootRun
cd frontend
npm install
npm run dev
```

後端：`http://localhost:8080`；前端：`http://localhost:5173`。

## 建置與測試

```powershell
./backend/gradlew.bat -p backend build
cd postman
npm install
npm test
cd frontend
npm run test:e2e
```

完整 Gradle build 會同時執行後端測試與前端 production build。

## 初始帳號

- 系統管理員：`admin/admin123`、`admin2/admin234`
- 一般使用者：`user/admin123`、`user2/admin234`

以上僅供開發測試，正式環境不得沿用。

## 部署至 GCP Cloud Run

```powershell
gcloud builds submit --config cloudbuild.yaml --project base-502702
```

- 專案編號 `316015752010`、專案 ID `base-502702`、區域 `asia-east1`。
- 後端服務 `base-server`（Spring Boot）、前端服務 `base-client`（Vite build + Nginx）。
- MSSQL 使用 Cloud SQL for SQL Server 2022 Express（執行個體 `base-mssql`），
  後端透過 Cloud SQL Java Connector（IAM 加密通道）連線，毋須公網白名單。
- 前置作業（API 啟用、Artifact Registry、Cloud SQL 建立、IAM 角色）詳見
  [cloudbuild.yaml](cloudbuild.yaml) 頂部註解，僅需執行一次。
