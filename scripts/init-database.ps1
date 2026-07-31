# 建立本機開發用的資料庫與專用登入帳號（冪等，可重複執行）。
#
# 支援三種連線情境：
#   1. docker compose 起的 mssql 容器（容器在跑就優先採用，本機毋須安裝 sqlcmd）
#   2. 本機安裝的 SQL Server + SQL 驗證（指定 -SaPassword）
#   3. 本機安裝的 SQL Server + Windows 整合驗證（原有行為，無容器且未指定 -SaPassword 時走這條）
param(
    [string]$Server = 'localhost',
    [string]$DatabaseName = 'base_20260716_01',
    [string]$LoginName = 'base_20260716_01',
    [string]$Password = 'base_20260716_01',
    [string]$SaUser = 'sa',
    [string]$SaPassword = $env:MSSQL_SA_PASSWORD,
    [string]$ComposeService = 'mssql',
    [int]$ReadyTimeoutSeconds = 180
)

$ErrorActionPreference = 'Stop'
if ($DatabaseName -notmatch '^[A-Za-z0-9_]+$' -or $LoginName -notmatch '^[A-Za-z0-9_]+$') {
    throw '資料庫名稱與 Login 名稱只允許英文字、數字與底線。'
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$composeFile = Join-Path $repoRoot 'docker-compose.yml'

function Test-ComposeUp {
    if (-not (Test-Path $composeFile)) { return $false }
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { return $false }
    $id = docker compose -f $composeFile ps -q $ComposeService 2>$null
    return -not [string]::IsNullOrWhiteSpace($id)
}

$composeUp = Test-ComposeUp

# sqlcmd 解析順序：容器內建的 sqlcmd → PATH 上的 sqlcmd。
# 容器優先是刻意的：它自帶的 sqlcmd 版本相容且必定可用。
if ($Server -eq 'localhost' -and $composeUp) {
    $sqlcmdPrefix = @('docker', 'compose', '-f', $composeFile, 'exec', '-T', $ComposeService, '/opt/mssql-tools18/bin/sqlcmd')
}
elseif (Get-Command sqlcmd -ErrorAction SilentlyContinue) {
    $sqlcmdPrefix = @('sqlcmd')
}
else {
    throw "找不到可用的 sqlcmd。請擇一：啟動容器（docker compose up -d）、或安裝 sqlcmd 並確保它在 PATH 上。"
}

# 容器在跑就用它的 SA 密碼走 SQL 驗證，避免密碼在兩處重複維護；
# 沒有容器（例：本機實體 SQL Server）才退回整合驗證。
if ([string]::IsNullOrWhiteSpace($SaPassword) -and $composeUp) {
    $SaPassword = (docker compose -f $composeFile exec -T $ComposeService printenv MSSQL_SA_PASSWORD).Trim()
}

if ([string]::IsNullOrWhiteSpace($SaPassword)) {
    $authArgs = @('-E')
}
else {
    $authArgs = @('-U', $SaUser, '-P', $SaPassword, '-C')
}

function Invoke-Sql {
    param([string]$Database, [string]$Query)
    $exe = $sqlcmdPrefix[0]
    $rest = @($sqlcmdPrefix[1..($sqlcmdPrefix.Count - 1)])
    & $exe @rest -S $Server @authArgs -b -d $Database -Q $Query
}

function Invoke-SqlFile {
    param([string]$Database, [string]$File)
    $exe = $sqlcmdPrefix[0]
    $rest = @($sqlcmdPrefix[1..($sqlcmdPrefix.Count - 1)])
    Get-Content -Raw $File | & $exe @rest -S $Server @authArgs -b -d $Database
}

# SQL Server 冷啟動需數十秒才接受連線，先等到就緒再下 DDL
Write-Host "等待 SQL Server ($Server) 就緒..."
$deadline = (Get-Date).AddSeconds($ReadyTimeoutSeconds)
while ($true) {
    Invoke-Sql -Database 'master' -Query 'SELECT 1' *> $null
    if ($LASTEXITCODE -eq 0) { break }
    if ((Get-Date) -ge $deadline) {
        throw "等待逾時（${ReadyTimeoutSeconds}s），SQL Server 仍無法連線。"
    }
    Start-Sleep -Seconds 3
}

$db = $DatabaseName.Replace(']', ']]')
$login = $LoginName.Replace(']', ']]')
$escapedPassword = $Password.Replace("'", "''")

Invoke-Sql -Database 'master' -Query "IF DB_ID(N'$db') IS NULL EXEC(N'CREATE DATABASE [$db]'); IF SUSER_ID(N'$login') IS NULL CREATE LOGIN [$login] WITH PASSWORD = N'$escapedPassword', CHECK_POLICY = OFF;"
if ($LASTEXITCODE -ne 0) { throw '建立 Database 或 Login 失敗。' }

Invoke-Sql -Database $DatabaseName -Query "IF USER_ID(N'$login') IS NULL CREATE USER [$login] FOR LOGIN [$login]; IF IS_ROLEMEMBER(N'db_owner', N'$login') <> 1 ALTER ROLE [db_owner] ADD MEMBER [$login]; SELECT DB_NAME() AS database_name, N'$login' AS login_name, N'初始化完成' AS result;"
if ($LASTEXITCODE -ne 0) { throw '建立 Database User 或 db_owner 關聯失敗。' }

Invoke-SqlFile -Database $DatabaseName -File (Join-Path $PSScriptRoot 'migrate-company-supervisor-unicode.sql')
if ($LASTEXITCODE -ne 0) { throw '公司主管 Unicode 欄位遷移失敗。' }

Invoke-SqlFile -Database $DatabaseName -File (Join-Path $PSScriptRoot 'migrate-password-policy-columns.sql')
if ($LASTEXITCODE -ne 0) { throw '密碼政策欄位遷移失敗。' }

Write-Host "資料庫 [$DatabaseName] 與專用 Login [$LoginName] 初始化完成。"
