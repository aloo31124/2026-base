-- 將 password_policy 由早期草稿欄位（policy_key / minimum_length / require_english / require_digit）
-- 遷移為 PasswordPolicy 實體目前使用的 min_length / require_letter / require_number。
--
-- 背景：SQL Server 不允許對「已有資料列」的表直接新增 NOT NULL 欄位，
-- 因此 ddl-auto=update 的 `alter table password_policy add min_length int not null` 會失敗，
-- 舊欄位仍留在表上，導致 /api/auth/email/register 與 /api/auth/email/password-reset
-- 讀取密碼政策時拋出 SQL Server 207（Invalid column name 'min_length'），新密碼無法存入。
--
-- 本腳本可重複執行：先以可為 NULL 的欄位補齊、回填既有政策值、再收斂為 NOT NULL，最後移除死欄位。

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRANSACTION;

IF OBJECT_ID(N'dbo.password_policy', N'U') IS NOT NULL
BEGIN
    -- 1. 先以可為 NULL 新增，避開非空表無法新增 NOT NULL 欄位的限制。
    IF COL_LENGTH(N'dbo.password_policy', N'min_length') IS NULL
        ALTER TABLE dbo.password_policy ADD min_length int NULL;

    IF COL_LENGTH(N'dbo.password_policy', N'require_letter') IS NULL
        ALTER TABLE dbo.password_policy ADD require_letter bit NULL;

    IF COL_LENGTH(N'dbo.password_policy', N'require_number') IS NULL
        ALTER TABLE dbo.password_policy ADD require_number bit NULL;

    -- 2. 回填既有政策值；舊欄位不存在時採用 PasswordPolicy 的安全預設（8 碼、需英文與數字）。
    --    新欄位在同一批次中才建立，必須以動態 SQL 引用，否則會被編譯期的欄位檢查擋下。
    DECLARE @minLengthSource nvarchar(64) = CASE
        WHEN COL_LENGTH(N'dbo.password_policy', N'minimum_length') IS NULL THEN N'8'
        ELSE N'minimum_length'
    END;
    DECLARE @requireLetterSource nvarchar(64) = CASE
        WHEN COL_LENGTH(N'dbo.password_policy', N'require_english') IS NULL THEN N'1'
        ELSE N'require_english'
    END;
    DECLARE @requireNumberSource nvarchar(64) = CASE
        WHEN COL_LENGTH(N'dbo.password_policy', N'require_digit') IS NULL THEN N'1'
        ELSE N'require_digit'
    END;

    DECLARE @backfillSql nvarchar(max) = N'
        UPDATE dbo.password_policy
        SET min_length = COALESCE(min_length, ' + @minLengthSource + N'),
            require_letter = COALESCE(require_letter, ' + @requireLetterSource + N'),
            require_number = COALESCE(require_number, ' + @requireNumberSource + N');';
    EXEC sys.sp_executesql @backfillSql;

    -- 3. 收斂為 NOT NULL，與 PasswordPolicy 的 nullable = false 對齊。
    EXEC sys.sp_executesql N'ALTER TABLE dbo.password_policy ALTER COLUMN min_length int NOT NULL;';
    EXEC sys.sp_executesql N'ALTER TABLE dbo.password_policy ALTER COLUMN require_letter bit NOT NULL;';
    EXEC sys.sp_executesql N'ALTER TABLE dbo.password_policy ALTER COLUMN require_number bit NOT NULL;';

    -- 4. 移除已不再對應任何實體欄位的死欄位。
    --    這些欄位皆為 NOT NULL 且無 DEFAULT，若留著，Hibernate 建立預設政策時的 INSERT 會失敗。
    DECLARE @obsoleteColumns TABLE (column_name sysname);
    INSERT INTO @obsoleteColumns (column_name)
    VALUES (N'policy_key'), (N'minimum_length'), (N'require_english'), (N'require_digit');

    DECLARE @columnName sysname;
    DECLARE @dropSql nvarchar(max);
    DECLARE @constraintName sysname;

    WHILE EXISTS (SELECT 1 FROM @obsoleteColumns)
    BEGIN
        SELECT TOP (1) @columnName = column_name FROM @obsoleteColumns;
        DELETE FROM @obsoleteColumns WHERE column_name = @columnName;

        IF COL_LENGTH(N'dbo.password_policy', @columnName) IS NOT NULL
        BEGIN
            -- 4a. 先卸除相依的唯一約束（例如舊 policy_key 的 UQ）與 DEFAULT 約束。
            SELECT TOP (1) @constraintName = key_constraint.name
            FROM sys.key_constraints AS key_constraint
            INNER JOIN sys.index_columns AS index_column
                ON index_column.object_id = key_constraint.parent_object_id
               AND index_column.index_id = key_constraint.unique_index_id
            INNER JOIN sys.columns AS column_definition
                ON column_definition.object_id = index_column.object_id
               AND column_definition.column_id = index_column.column_id
            WHERE key_constraint.parent_object_id = OBJECT_ID(N'dbo.password_policy')
              AND key_constraint.type = N'UQ'
              AND column_definition.name = @columnName;

            IF @constraintName IS NOT NULL
            BEGIN
                SET @dropSql =
                    N'ALTER TABLE dbo.password_policy DROP CONSTRAINT ' + QUOTENAME(@constraintName);
                EXEC sys.sp_executesql @dropSql;
                SET @constraintName = NULL;
            END;

            SELECT TOP (1) @constraintName = default_constraint.name
            FROM sys.default_constraints AS default_constraint
            INNER JOIN sys.columns AS column_definition
                ON column_definition.object_id = default_constraint.parent_object_id
               AND column_definition.column_id = default_constraint.parent_column_id
            WHERE default_constraint.parent_object_id = OBJECT_ID(N'dbo.password_policy')
              AND column_definition.name = @columnName;

            IF @constraintName IS NOT NULL
            BEGIN
                SET @dropSql =
                    N'ALTER TABLE dbo.password_policy DROP CONSTRAINT ' + QUOTENAME(@constraintName);
                EXEC sys.sp_executesql @dropSql;
                SET @constraintName = NULL;
            END;

            SET @dropSql =
                N'ALTER TABLE dbo.password_policy DROP COLUMN ' + QUOTENAME(@columnName);
            EXEC sys.sp_executesql @dropSql;
        END;
    END;
END;

COMMIT TRANSACTION;

SELECT N'密碼政策欄位已對齊 min_length / require_letter / require_number。' AS result;
