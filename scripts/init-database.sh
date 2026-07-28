#!/usr/bin/env bash
# 建立本機開發用的資料庫與專用登入帳號（冪等，可重複執行）。
#
# 支援三種連線情境：
#   1. docker compose 起的 mssql 容器（容器在跑就優先採用，本機毋須安裝 sqlcmd）
#   2. 本機安裝的 SQL Server + SQL 驗證（設定 MSSQL_SA_PASSWORD）
#   3. 本機安裝的 SQL Server + Windows 整合驗證（原有行為，無容器且未設 SA 密碼時走這條）
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="$REPO_ROOT/docker-compose.yml"

SERVER="${DB_SERVER:-localhost}"
DATABASE_NAME="${DB_NAME:-base_20260716_01}"
LOGIN_NAME="${DB_USERNAME:-base_20260716_01}"
PASSWORD="${DB_PASSWORD:-base_20260716_01}"
SA_USER="${SA_USERNAME:-sa}"
SA_PASSWORD="${MSSQL_SA_PASSWORD:-}"
COMPOSE_SERVICE="${MSSQL_SERVICE:-mssql}"
READY_TIMEOUT="${DB_READY_TIMEOUT:-180}"

# 識別字直接內插進 SQL，必須先以白名單擋掉注入
[[ "$DATABASE_NAME" =~ ^[A-Za-z0-9_]+$ ]] || { echo "Invalid DB_NAME" >&2; exit 1; }
[[ "$LOGIN_NAME" =~ ^[A-Za-z0-9_]+$ ]] || { echo "Invalid DB_USERNAME" >&2; exit 1; }
ESCAPED_PASSWORD=${PASSWORD//\'/\'\'}

compose_up() {
  [[ -f "$COMPOSE_FILE" ]] && [[ -n "$(docker compose -f "$COMPOSE_FILE" ps -q "$COMPOSE_SERVICE" 2>/dev/null)" ]]
}

# sqlcmd 解析順序：SQLCMD 覆寫 → 容器內建的 sqlcmd → PATH 上的 sqlcmd。
# 容器優先是刻意的：它自帶的 sqlcmd 版本相容且必定可用，而本機安裝的 sqlcmd
# 常有缺 unixODBC 之類的環境問題。要強制用本機的請設 SQLCMD=sqlcmd。
if [[ -n "${SQLCMD:-}" ]]; then
  read -r -a SQLCMD_CMD <<< "$SQLCMD"
elif [[ "$SERVER" == "localhost" ]] && compose_up; then
  SQLCMD_CMD=(docker compose -f "$COMPOSE_FILE" exec -T "$COMPOSE_SERVICE" /opt/mssql-tools18/bin/sqlcmd)
elif command -v sqlcmd >/dev/null 2>&1; then
  SQLCMD_CMD=(sqlcmd)
else
  cat >&2 <<'EOF'
找不到可用的 sqlcmd。請擇一：
  - 啟動容器：docker compose up -d
  - 或安裝 sqlcmd 並確保它在 PATH 上
  - 或以 SQLCMD 環境變數指定執行檔路徑
EOF
  exit 1
fi

# 容器在跑就用它的 SA 密碼走 SQL 驗證，避免密碼在兩處重複維護；
# 沒有容器（例：Windows 本機實體 SQL Server）才退回整合驗證。
if [[ -z "$SA_PASSWORD" ]] && compose_up; then
  SA_PASSWORD="$(docker compose -f "$COMPOSE_FILE" exec -T "$COMPOSE_SERVICE" printenv MSSQL_SA_PASSWORD | tr -d '\r')"
fi

if [[ -n "$SA_PASSWORD" ]]; then
  AUTH_ARGS=(-U "$SA_USER" -P "$SA_PASSWORD" -C)
else
  AUTH_ARGS=(-E)
fi

run_sql() {
  "${SQLCMD_CMD[@]}" -S "$SERVER" "${AUTH_ARGS[@]}" -b "$@"
}

# SQL Server 冷啟動需數十秒才接受連線，先等到就緒再下 DDL
echo "等待 SQL Server ($SERVER) 就緒..."
deadline=$((SECONDS + READY_TIMEOUT))
until run_sql -d master -Q "SELECT 1" >/dev/null 2>&1; do
  if (( SECONDS >= deadline )); then
    echo "等待逾時（${READY_TIMEOUT}s），SQL Server 仍無法連線。" >&2
    exit 1
  fi
  sleep 3
done

run_sql -d master -Q "IF DB_ID(N'$DATABASE_NAME') IS NULL EXEC(N'CREATE DATABASE [$DATABASE_NAME]'); IF SUSER_ID(N'$LOGIN_NAME') IS NULL CREATE LOGIN [$LOGIN_NAME] WITH PASSWORD = N'$ESCAPED_PASSWORD', CHECK_POLICY = OFF;"
run_sql -d "$DATABASE_NAME" -Q "IF USER_ID(N'$LOGIN_NAME') IS NULL CREATE USER [$LOGIN_NAME] FOR LOGIN [$LOGIN_NAME]; IF IS_ROLEMEMBER(N'db_owner', N'$LOGIN_NAME') <> 1 ALTER ROLE [db_owner] ADD MEMBER [$LOGIN_NAME]; SELECT DB_NAME() AS database_name, N'$LOGIN_NAME' AS login_name, N'initialized' AS result;"
