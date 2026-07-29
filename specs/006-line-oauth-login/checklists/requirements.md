# Requirements Checklist：LINE OAuth 註冊登入

## Sheet MUST Traceability

- [x] CHK001 第 25 列「OAuth 驗證後即時登入且免密碼」是否由 API、React 與 E2E 驗證？[FR-001–006, FR-009, FR-014]
- [x] CHK002 第 26 列「首次綁定與 LINE 官方登入頁」是否由 redirect 流程與首次/再次登入測試驗證？[FR-001–008]
- [x] CHK003 第 27 列「首次註冊寫入資料庫」是否包含 app_user、line_oauth_account、user_role？[FR-007–008]
- [x] CHK004 第 28 列「成功與失敗都存入資料庫」是否涵蓋 SUCCESS、DENIED、FAILED？[FR-010]

## Security

- [x] CHK005 state 是否不可預測、僅保存 hash、單次使用並有逾時？[FR-002, FR-004]
- [x] CHK006 PKCE S256 與 nonce 是否產生、使用並在終態清除？[FR-003, FR-006]
- [x] CHK007 ID token 是否經 LINE endpoint 驗證 channel ID 與 nonce？[FR-006]
- [x] CHK008 secret/token/code/raw state 是否不落庫、不進 Log、不進報告？[FR-011–012]
- [x] CHK009 mock provider 是否預設關閉且只能由明確測試環境開關啟用？[FR-013]

## Layering and UX

- [x] CHK010 是否完整具備 DB/BO/JPA DAO/Service/Controller/React page？[Constitution]
- [x] CHK011 登入與 callback 是否遵循 `uiux/0.共用樣式` 並支援手機版？[FR-014]
- [x] CHK012 失敗與取消是否顯示可操作訊息並允許回到登入？[FR-014]

## Quality Gates

- [x] CHK013 Gradle build（含 React production build）是否通過？
- [x] CHK014 Postman/Newman 對應 API assertions 是否 100% 通過且有指定報告？
- [x] CHK015 Cypress 是否依 task/checklist 100% 通過且有指定報告？
- [x] CHK016 Sheet 是否只更新 B25:B28，且在全部閘門通過後才改為「開發完成」？
