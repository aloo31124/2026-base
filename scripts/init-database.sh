#!/usr/bin/env bash
set -euo pipefail

SERVER="${DB_SERVER:-localhost}"
DATABASE_NAME="${DB_NAME:-base_20260716_01}"
LOGIN_NAME="${DB_USERNAME:-base_20260716_01}"
PASSWORD="${DB_PASSWORD:-base_20260716_01}"

[[ "$DATABASE_NAME" =~ ^[A-Za-z0-9_]+$ ]] || { echo "Invalid DB_NAME" >&2; exit 1; }
[[ "$LOGIN_NAME" =~ ^[A-Za-z0-9_]+$ ]] || { echo "Invalid DB_USERNAME" >&2; exit 1; }
ESCAPED_PASSWORD=${PASSWORD//\'/\'\'}

sqlcmd -S "$SERVER" -E -b -d master -Q "IF DB_ID(N'$DATABASE_NAME') IS NULL EXEC(N'CREATE DATABASE [$DATABASE_NAME]'); IF SUSER_ID(N'$LOGIN_NAME') IS NULL CREATE LOGIN [$LOGIN_NAME] WITH PASSWORD = N'$ESCAPED_PASSWORD', CHECK_POLICY = OFF;"
sqlcmd -S "$SERVER" -E -b -d "$DATABASE_NAME" -Q "IF USER_ID(N'$LOGIN_NAME') IS NULL CREATE USER [$LOGIN_NAME] FOR LOGIN [$LOGIN_NAME]; IF IS_ROLEMEMBER(N'db_owner', N'$LOGIN_NAME') <> 1 ALTER ROLE [db_owner] ADD MEMBER [$LOGIN_NAME]; SELECT DB_NAME() AS database_name, N'$LOGIN_NAME' AS login_name, N'initialized' AS result;"
