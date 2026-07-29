# Postman 測試報告：信箱註冊登入

## 目錄

- [執行摘要](#執行摘要) — 12 requests、18 assertions、100%
- [環境與命令](#環境與命令) — H2 mock SMTP 與 Newman
- [MUST 與 Task 結果](#must-與-task-結果) — 六條工作表需求
- [Response 核對](#response-核對) — 狀態碼與敏感資料
- [完成率](#完成率) — Postman 驗收 100%

## 執行摘要

| 項目 | 結果 |
|---|---|
| 執行日期 | 2026-07-29 |
| Collection | `postman/email-registration-login.postman_collection.json` |
| Requests | 12 / 12 通過 |
| Test scripts | 12 / 12 通過 |
| Assertions | 18 / 18 通過 |
| Failures | 0 |
| 平均 response | 81 ms |
| 最慢 response | 142 ms |
| 完成率 | **100%** |

## 環境與命令

- 後端：Spring Boot，`SPRING_PROFILES_ACTIVE=h2`，port `18080`
- 郵件：H2 profile 明確啟用 mock SMTP；固定測試碼 `123456`
- 執行器：Postman 官方 Newman `6.2.2`
- 桌面版：已使用本機 `Postman.app` 開啟本次 collection

```bash
cd postman
npm run test:email -- --env-var baseUrl=http://localhost:18080
```

可機讀證據：`backend/build/newman-email-registration-login-result.json`。

## MUST 與 Task 結果

| Sheet MUST / Task | Postman 請求 | 結果 |
|---|---|---|
| 首次信箱寄碼與核銷（T009、T013） | 01、02 | ✅ 2xx 且不回傳驗證碼 |
| 重複信箱提醒（T009、T013） | 05 | ✅ 409／「此信箱已註冊。」 |
| 設定密碼、建帳、自動登入（T010、T013） | 03 | ✅ username=email、EMPLOYEE、JWT |
| 信箱密碼登入（T017） | 04、09 | ✅ 舊密碼註冊登入、新密碼重設後登入 |
| 忘記密碼驗證與更新（T009–T010、T013） | 06–08 | ✅ 寄碼、核銷、更新皆成功 |
| 管理員寄信與 DB 紀錄（T008、T014） | 10–12 | ✅ 管理員授權、寄信、最新紀錄成功 |

## Response 核對

- 成功 response 皆符合 `{ success, message, data }`。
- 重複信箱回覆 HTTP `409`，message 為「此信箱已註冊。」。
- 新註冊 response 含 `EMPLOYEE`、JWT 與信箱 username。
- 管理員寄信 response 未包含固定驗證碼 `123456`。
- 寄送紀錄 response 只含遮罩收件者，不含驗證碼、密碼或 SMTP 秘密。

## 完成率

- Postman requests：12 / 12 = **100%**
- Postman assertions：18 / 18 = **100%**
- Sheet 六條 MUST API 覆蓋：6 / 6 = **100%**
- 對應 checklist CK-001～CK-006、CK-015：**全部通過**
