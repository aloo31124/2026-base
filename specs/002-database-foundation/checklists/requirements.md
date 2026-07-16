# Requirements Checklist：資料庫基礎

## 目錄

- [完整性](#完整性) — 5 項
- [安全與可測量性](#安全與可測量性) — 4 項

## 完整性

- [x] CHK001 是否定義 Windows 與 macOS/Linux 初始化需求？[Spec §FR-001]
- [x] CHK002 是否涵蓋 DB/Login/User/db_owner 全流程與冪等？[Spec §FR-003–005]
- [x] CHK003 是否定義 UUID、審計、PK、FK、狀態與布林命名？[Spec §FR-007–010]
- [x] CHK004 是否定義多對多關聯表命名與自身主鍵？[Spec §FR-008]
- [x] CHK005 是否要求 JPA 自動建表與實際 MSSQL CRUD？[Spec §FR-006, FR-011]

## 安全與可測量性

- [x] CHK006 動態 SQL 的名稱驗證與引號規則是否明確？[Spec §FR-004]
- [x] CHK007 是否明確記錄不重設共用 sa 密碼的安全決策？[Assumption]
- [x] CHK008 初始化重跑與既有資源情境是否已涵蓋？[Edge Case]
- [x] CHK009 每項資料規範是否有自動測試或 Postman task？[Traceability]

