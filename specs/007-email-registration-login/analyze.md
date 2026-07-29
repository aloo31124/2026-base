# Analyze：信箱註冊登入

- FR-001–009 均有對應 task 與 checklist。
- 分層涵蓋 DB／BO／JPA DAO／Service／Controller／Redux／React page。
- 公開 API 與 OpenAPI 合約一致。
- 唯一外部完成條件為輪替後 Gmail 憑證的真實寄信 smoke test；自動測試不使用真實 secret。
