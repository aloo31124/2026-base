# AgentFlow Base

Spring Boot 4.1 + React 19 的前後端分離基礎專案，提供 MSSQL/JPA、JWT 登入、RBAC 使用者分權、統一例外與 Log，以及 test CRUD 驗證頁。

## 目錄

- [需求](#需求) — 本機工具
- [執行環境對照](#執行環境對照) — Windows / macOS / GCP
- [初始化資料庫](#初始化資料庫) — MSSQL bootstrap
- [啟動](#啟動) — 後端與前端
- [建置與測試](#建置與測試) — Gradle、Postman、Cypress
- [初始帳號](#初始帳號) — 開發環境登入
- [部署至 GCP Cloud Run](#部署至-gcp-cloud-run) — 一鍵部署測試機

## 需求

- Java 21+
- Node 20+（Gradle 完整 build 會自行下載 Node 24 LTS）
- 資料庫二選一：
  - **Docker Desktop**（macOS 必需、Windows 建議）— 以 `docker-compose.yml` 跑 SQL Server 2022 容器
  - **本機安裝的 SQL Server 2019+**（僅 Windows）

> Apple Silicon 請先在 Docker Desktop → Settings → General 開啟
> 「Use Rosetta for x86_64/amd64 emulation」，SQL Server 官方 image 僅發佈 amd64。

## 執行環境對照

三個環境統一使用 **SQL Server 2022**，只是託管方式不同，方言一致，
JPA `ddl-auto: update` 產生的 DDL 不會在環境間漂移。

| 環境 | 資料庫託管方式 | DB_URL 來源 | 建 DB / Login |
|---|---|---|---|
| Windows | 本機 SQL Server **或** Docker 容器 | `application.yml` 預設值 | `scripts/init-database.ps1` |
| macOS | Docker 容器 | 同上，無需改動 | `scripts/init-database.sh` |
| GCP | Cloud SQL for SQL Server（`base-mssql`） | `cloudbuild.yaml` 動態組出並注入 | 一次性手動，見 cloudbuild.yaml 檔頭 |
| 備援（無 Docker） | H2 檔案模式 | `application-h2.yml` | 不需要，H2 自動建檔 |
| 自動測試 | H2 記憶體模式 | `application-test.yml` | 不需要 |

容器對外就是 `localhost:1433`，資料庫名／帳號／密碼沿用 `application.yml` 的預設值，
因此 Windows 既有的開發流程完全不受影響。

## 初始化資料庫

先啟動資料庫容器（使用本機 SQL Server 者可略過）：

```bash
docker compose up -d
```

再建立資料庫與專用 Login（冪等，可重複執行）：

```bash
bash ./scripts/init-database.sh
```

Windows：

```powershell
pwsh ./scripts/init-database.ps1
```

腳本建立 `base_20260716_01` 資料庫與同名專用 Login，可透過參數／環境變數覆寫。
腳本會自動判斷連線方式：容器在跑就借用容器內建的 `sqlcmd` 與 sa 帳號走 SQL 驗證
（本機毋須安裝 `sqlcmd`）；沒有容器則退回本機 `sqlcmd` 與 Windows 整合驗證。

停止容器（加 `-v` 會一併刪除資料）：

```bash
docker compose down
```

## 啟動

macOS / Linux：

```bash
./backend/gradlew -p backend bootRun
```

Windows：

```powershell
./backend/gradlew.bat -p backend bootRun
```

前端（兩者相同）：

```bash
cd frontend && npm install && npm run dev
```

後端：`http://localhost:8080`；前端：`http://localhost:5173`。

### 無 Docker 時的備援啟動

沒有 Docker 也不想裝 SQL Server 時，可用 H2 檔案模式先把服務跑起來：

```bash
SPRING_PROFILES_ACTIVE=h2 ./backend/gradlew -p backend bootRun
```

資料存於 `backend/data`（bootRun 的工作目錄為 `backend/`）。
H2 與真實 SQL Server 在型別對應與 `ddl-auto` 行為上仍有差異，
此路徑僅供快速預覽，**要合併進 main 的變更必須在 Docker MSSQL 上覆驗**。

## 建置與測試

```bash
./backend/gradlew -p backend build   # Windows: ./backend/gradlew.bat -p backend build
cd postman && npm install && npm test
cd ../frontend && npm run test:e2e
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
