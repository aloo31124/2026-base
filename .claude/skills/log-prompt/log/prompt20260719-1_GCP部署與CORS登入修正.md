2026-07-19 10:41:10

## git commit
[feature] # [GCP部署] 新增 cloudbuild.yaml 一鍵部署前後端至 Cloud Run，MSSQL 改以 Cloud SQL 連接器承載。

- 新增根目錄 `cloudbuild.yaml`：依序建置部署 `base-server`（Spring Boot）與 `base-client`（Vite+Nginx），自動注入後端 API URL 並校正 CORS；頂部含全部一次性前置作業指令。
- 新增 `backend/Dockerfile`（Gradle bootJar 多階段建置 + JRE 21 執行）與 `frontend/Dockerfile`、`frontend/nginx.conf`（Vite build + Nginx SPA 路由）。
- `backend/build.gradle` 加入 `cloud-sql-connector-jdbc-sqlserver:1.28.4`，透過 IAM 加密通道連 Cloud SQL for SQL Server，免公網白名單，本機連線不受影響。
- 新增 `.gcloudignore` 與前後端 `.dockerignore`，上傳建置僅 274 KiB。
- `README.md` 補「部署至 GCP Cloud Run」章節。
- 原因：測試機需一鍵部署至 GCP（專案 base-502702），且 MSSQL 儲存機制須可於 GCP 環境運作；已實測部署成功、登入 API 與資料庫讀寫正常。

[fix] # [GCP部署][CORS] 放行 Cloud Run 雜湊型與確定型兩種前端網址，修正登入 Failed to fetch。

- `backend/.../config/SecurityConfig.java`：`app.cors.allowed-origin` 改支援逗號分隔多來源，本機開發預設值不變。
- `cloudbuild.yaml` Step「update-server-cors」：部署後自動將前端雜湊型與確定型網址一併寫入後端 `CORS_ALLOWED_ORIGIN`。
- 原因：Cloud Run 同一服務有兩種網址，先前 CORS 僅放行雜湊型；使用者由確定型網址開站時 preflight 回 403，瀏覽器顯示 Failed to fetch 且請求不會進到後端 log。
- 已重新部署並以 curl 驗證：兩種 origin preflight 皆 200、登入正常、錯誤帳密可收到後端 JSON 訊息。

## prompt 1
將前後端部屬至測試機 GCP Cloud Run 環境，
您目前的專案是： 2026-base

專案編號： 316015752010 專案 ID： base-502702 。

並於 根目錄創建 cloudbuild.yaml 一鍵部屬。
參考 C:\Users\ASUS\Desktop\2026-spacein\cloudbuild.yaml 專案設定方式。

並確保 MSSQL 連線機制也可被部屬至 GCP 環境上存儲。

## prompt 1 response
- 參考 `2026-spacein/cloudbuild.yaml` 與現有 backend/frontend 結構後，新增 `backend/Dockerfile`（Gradle bootJar 多階段）、`frontend/Dockerfile`+`nginx.conf`（Vite+Nginx SPA），以及對應 `.dockerignore`。
- `backend/build.gradle` 加入 Cloud SQL SQL Server 連接器依賴，並以 `gradlew bootJar` 驗證建置成功。
- 建立根目錄 `cloudbuild.yaml`（9 個 step：AR repo 確保、後端建置/推送/部署、取後端URL、前端建置/推送/部署、CORS 校正、顯示結果）與 `.gcloudignore`。
- GCP 環境準備：啟用 cloudbuild/run/artifactregistry/sqladmin/compute API，建立 Artifact Registry repo `base-repo`，授予預設服務帳戶 run.admin/iam.serviceAccountUser/cloudsql.client 等角色，建立 Cloud SQL 執行個體 `base-mssql`（SQLSERVER_2022_EXPRESS, asia-east1）。
- 建立資料庫 `base_20260716_01` 與同名使用者；首次部署因 SQL Server login 未對應至資料庫而登入失敗，臨時開放本機 IP 以 sqlcmd 執行 `CREATE USER`/`ALTER ROLE db_owner` 後移除白名單。
- 執行 `gcloud builds submit` 完成部署：後端 `base-server`、前端 `base-client` 皆成功，健康檢查 UP，登入 API 回傳 JWT，前端頁面 200 且 bundle 已注入正確後端 URL，四組初始帳號無重複。
- `README.md` 新增「部署至 GCP Cloud Run」章節；提供可複製 git commit。

## prompt 2
部屬至 GCP 後，
輸入帳密錯誤訊息為: Failed to fetch，
後端 log 為
DEFAULT 2026-07-19T02:14:45.966151Z Pool: DataSourceConnectionProvider
DEFAULT 2026-07-19T02:14:45.966155Z Minimum pool size: undefined/unknown
DEFAULT 2026-07-19T02:14:45.966159Z Maximum pool size: undefined/unknown
DEFAULT 2026-07-19T02:14:47.896833Z 2026-07-19 10:14:47.897 [main] INFO org.hibernate.orm.core:53 - HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
DEFAULT 2026-07-19T02:14:48.718207Z 2026-07-19 10:14:48.718 [main] INFO o.s.o.jpa.LocalContainerEntityManagerFactoryBean:464 - Initialized JPA EntityManagerFactory for persistence unit 'default'
DEFAULT 2026-07-19T02:14:48.806750Z 2026-07-19 10:14:48.806 [main] INFO o.s.d.j.repository.query.QueryEnhancerFactories:48 - Hibernate is in classpath; If applicable, HQL parser will be used.
DEFAULT 2026-07-19T02:14:49.441513Z 2026-07-19 10:14:49.441 [main] INFO o.s.s.c.a.a.c.InitializeUserDetailsBeanManagerConfigurer$InitializeUserDetailsManagerConfigurer:110 - Global AuthenticationManager configured with UserDetailsService bean with name accountService
DEFAULT 2026-07-19T02:14:50.819411Z 2026-07-19 10:14:50.819 [main] INFO o.s.b.actuate.endpoint.web.EndpointLinksResolver:60 - Exposing 2 endpoints beneath base path '/actuator'
DEFAULT 2026-07-19T02:14:50.902263Z 2026-07-19 10:14:50.902 [main] INFO org.apache.coyote.http11.Http11NioProtocol:168 - Starting ProtocolHandler ["http-nio-8080"]
INFO 2026-07-19T02:14:50.906873Z Default STARTUP TCP probe succeeded after 1 attempt for container "base-server-1" on port 8080.
DEFAULT 2026-07-19T02:14:50.926921Z 2026-07-19 10:14:50.927 [main] INFO org.springframework.boot.tomcat.TomcatWebServer:240 - Tomcat started on port 8080 (http) with context path '/'
DEFAULT 2026-07-19T02:14:50.940385Z 2026-07-19 10:14:50.940 [main] INFO com.agentflow.base.AgentFlowApplication:60 - Started AgentFlowApplication in 13.91 seconds (process running for 15.938)
DEFAULT 2026-07-19T02:14:51.057383Z 2026-07-19 10:14:51.057 [http-nio-8080-exec-5] INFO o.a.c.c.ContainerBase.[Tomcat].[localhost].[/]:168 - Initializing Spring DispatcherServlet 'dispatcherServlet'
DEFAULT 2026-07-19T02:14:51.057666Z 2026-07-19 10:14:51.058 [http-nio-8080-exec-5] INFO o.springframework.web.servlet.DispatcherServlet:523 - Initializing Servlet 'dispatcherServlet'
DEFAULT 2026-07-19T02:14:51.086302Z 2026-07-19 10:14:51.086 [http-nio-8080-exec-5] INFO o.springframework.web.servlet.DispatcherServlet:545 - Completed initialization in 28 ms

請找出錯誤原因並修正

## prompt 2 response
- 判斷「Failed to fetch」屬瀏覽器端 CORS preflight 被拒（OPTIONS 在進 Controller 前即被擋，故後端 log 只有啟動訊息、無任何登入請求紀錄），非後端當機。
- 以 curl 重現：Cloud Run 同一服務有雜湊型（`base-client-rjp7kitxla-de.a.run.app`）與確定型（`base-client-316015752010.asia-east1.run.app`）兩種網址；確定型 origin 的 preflight 回 403，雜湊型回 200，證實根因為 CORS 只放行單一來源。
- 修正 `SecurityConfig.java`：`app.cors.allowed-origin` 改支援逗號分隔多來源。
- 修正 `cloudbuild.yaml` Step「update-server-cors」：部署後自動組出雜湊型 + 確定型兩個網址寫入 `CORS_ALLOWED_ORIGIN`。
- 本機執行 `gradlew test` 全數通過後，重新 `gcloud builds submit` 部署成功（4分35秒）。
- 驗證：兩種 origin 的 preflight 皆回 200、確定型 origin 登入成功取得 JWT、錯誤密碼可正常收到 JSON 錯誤（不再 Failed to fetch）。
- 附帶觀察（未修）：錯誤密碼目前回 500 而非 401，因 `BusinessException` 被 Spring Security 包裝未拆包，已提示使用者但非本次修復範圍。
