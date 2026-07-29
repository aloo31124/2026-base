# Requirements Checklist：信箱註冊登入

## 目錄

- [規格品質](#規格品質) — 4 項
- [Sheet MUST 覆蓋](#sheet-must-覆蓋) — 6 項
- [分層與安全](#分層與安全) — 5 項
- [建置與測試驗收](#建置與測試驗收) — 7 項

## 規格品質

- [x] CQ-001 規格聚焦使用者價值，實作細節集中於 plan 與 data model。
- [x] CQ-002 無 `[NEEDS CLARIFICATION]`、TODO 或未定義名詞。
- [x] CQ-003 每條 FR 可測試且 Success Criteria 可量測。
- [x] CQ-004 範圍明確排除非「預計開發」項目與 LINE OAuth。

## Sheet MUST 覆蓋

- [x] CK-001 管理員 SMTP 測試寄信成功／失敗均保存資料庫，頁面可檢視紀錄。
- [x] CK-002 首次信箱可寄送並核銷驗證碼，重複信箱顯示提醒。
- [x] CK-003 驗證通過後可設定密碼與確認密碼，建帳後立即進入首頁。
- [x] CK-004 新帳號可使用信箱作為帳號及密碼登入。
- [x] CK-005 忘記密碼可透過信箱驗證後更新密碼。
- [x] CK-006 六條 MUST 均有 task、JUnit/API 與 Cypress 驗收證據。

## 分層與安全

- [x] CK-007 DB 表、BO、JPA DAO、Service、Controller、React Page 皆已實作。
- [x] CK-008 Controller 無直接存取 DAO，業務規則集中 Service。
- [x] CK-009 驗證碼僅保存雜湊，有 10 分鐘效期、5 次限制與一次性使用。
- [x] CK-010 新註冊帳號只取得 `EMPLOYEE`，管理員寄信與紀錄 API 維持 `SYSTEM_ADMIN`。
- [x] CK-011 API、應用程式日誌及前端不洩漏驗證碼、明文密碼、雜湊或 SMTP 秘密。

## 建置與測試驗收

- [x] CK-012 後端 `./gradlew test` 全數通過。
- [x] CK-013 後端 `./gradlew build` 建置成功。
- [x] CK-014 前端 `npm run build` 建置成功。
- [x] CK-015 Postman/Newman collection 全數通過並產生完成率報告。
- [x] CK-016 Cypress `email-registration-login.cy.ts` 全數通過並產生完成率報告。
- [x] CK-017 tasks.md 無未完成項目，FR↔task 覆蓋率 100%。
- [x] CK-018 Google Sheet 六列狀態於全部驗收成功後更新為「開發完成」。
