# Research：任務指派

## 目錄

- [決策](#決策) — 三項核心技術決策

## 決策

- **Decision**：沿用 `CompanyMembership` 判斷同公司。**Rationale**：避免重複公司關係。**Alternatives**：任務表保存公司 ID，會產生同步風險。
- **Decision**：任務採單一受派人。**Rationale**：符合最小 CRUD 與明確狀態。**Alternatives**：多對多指派會需要額外指派實體與複雜部分退回。
- **Decision**：Newman 作為 Postman CLI。**Rationale**：專案已有可重現工具與報表格式。**Alternatives**：只用 curl 缺少 Postman collection 證據。
