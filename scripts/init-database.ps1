param(
    [string]$Server = 'localhost',
    [string]$DatabaseName = 'base_20260716_01',
    [string]$LoginName = 'base_20260716_01',
    [string]$Password = 'base_20260716_01'
)

$ErrorActionPreference = 'Stop'
if ($DatabaseName -notmatch '^[A-Za-z0-9_]+$' -or $LoginName -notmatch '^[A-Za-z0-9_]+$') {
    throw '資料庫名稱與 Login 名稱只允許英文字、數字與底線。'
}

$db = $DatabaseName.Replace(']', ']]')
$login = $LoginName.Replace(']', ']]')
$escapedPassword = $Password.Replace("'", "''")
sqlcmd -S $Server -E -b -d master -Q "IF DB_ID(N'$db') IS NULL EXEC(N'CREATE DATABASE [$db]'); IF SUSER_ID(N'$login') IS NULL CREATE LOGIN [$login] WITH PASSWORD = N'$escapedPassword', CHECK_POLICY = OFF;"
if ($LASTEXITCODE -ne 0) { throw '建立 Database 或 Login 失敗。' }

sqlcmd -S $Server -E -b -d $DatabaseName -Q "IF USER_ID(N'$login') IS NULL CREATE USER [$login] FOR LOGIN [$login]; IF IS_ROLEMEMBER(N'db_owner', N'$login') <> 1 ALTER ROLE [db_owner] ADD MEMBER [$login]; SELECT DB_NAME() AS database_name, N'$login' AS login_name, N'初始化完成' AS result;"
if ($LASTEXITCODE -ne 0) { throw '建立 Database User 或 db_owner 關聯失敗。' }
Write-Host "資料庫 [$DatabaseName] 與專用 Login [$LoginName] 初始化完成。"
