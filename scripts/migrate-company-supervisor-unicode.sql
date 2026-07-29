SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRANSACTION;

IF OBJECT_ID(N'dbo.company', N'U') IS NOT NULL
BEGIN
    IF EXISTS (
        SELECT 1
        FROM sys.columns
        WHERE object_id = OBJECT_ID(N'dbo.company')
          AND name = N'name'
          AND system_type_id <> TYPE_ID(N'nvarchar')
    )
    BEGIN
        DECLARE @companyNameConstraint sysname;
        DECLARE @companyNameSql nvarchar(max);

        SELECT TOP (1) @companyNameConstraint = key_constraint.name
        FROM sys.key_constraints AS key_constraint
        INNER JOIN sys.index_columns AS index_column
            ON index_column.object_id = key_constraint.parent_object_id
           AND index_column.index_id = key_constraint.unique_index_id
        INNER JOIN sys.columns AS column_definition
            ON column_definition.object_id = index_column.object_id
           AND column_definition.column_id = index_column.column_id
        WHERE key_constraint.parent_object_id = OBJECT_ID(N'dbo.company')
          AND key_constraint.type = N'UQ'
          AND column_definition.name = N'name';

        IF @companyNameConstraint IS NOT NULL
        BEGIN
            SET @companyNameSql =
                N'ALTER TABLE dbo.company DROP CONSTRAINT ' + QUOTENAME(@companyNameConstraint);
            EXEC sys.sp_executesql @companyNameSql;
        END;

        ALTER TABLE dbo.company ALTER COLUMN name nvarchar(120) NOT NULL;

        IF @companyNameConstraint IS NOT NULL
        BEGIN
            SET @companyNameSql =
                N'ALTER TABLE dbo.company ADD CONSTRAINT '
                + QUOTENAME(@companyNameConstraint)
                + N' UNIQUE (name)';
            EXEC sys.sp_executesql @companyNameSql;
        END;
    END;

    IF EXISTS (
        SELECT 1
        FROM sys.columns
        WHERE object_id = OBJECT_ID(N'dbo.company')
          AND name = N'description'
          AND system_type_id <> TYPE_ID(N'nvarchar')
    )
        ALTER TABLE dbo.company ALTER COLUMN description nvarchar(500) NULL;
END;

IF OBJECT_ID(N'dbo.supervisor_profile', N'U') IS NOT NULL
   AND EXISTS (
       SELECT 1
       FROM sys.columns
       WHERE object_id = OBJECT_ID(N'dbo.supervisor_profile')
         AND name = N'title'
         AND system_type_id <> TYPE_ID(N'nvarchar')
   )
    ALTER TABLE dbo.supervisor_profile ALTER COLUMN title nvarchar(80) NOT NULL;

COMMIT TRANSACTION;

SELECT N'公司主管中文欄位已確認為 Unicode 格式。' AS result;
