# Cypress 測試報告：信箱註冊登入

## 目錄

- [執行摘要](#執行摘要) — 2 tests、100%
- [環境與命令](#環境與命令) — React、H2 與 mock SMTP
- [MUST 與 Checklist 結果](#must-與-checklist-結果) — UI 操作逐項驗收
- [首次紅燈與修正](#首次紅燈與修正) — 非同步登入等待
- [完成率](#完成率) — Cypress 驗收 100%

## 執行摘要

| 項目 | 結果 |
|---|---|
| 執行日期 | 2026-07-29 |
| Spec | `frontend/cypress/e2e/email-registration-login.cy.ts` |
| Browser | Electron 138 headless |
| Tests | 2 / 2 通過 |
| Failures | 0 |
| Duration | 10 秒 |
| Screenshots on final run | 0 |
| 完成率 | **100%** |

## 環境與命令

- 前端：Vite `http://localhost:5173`
- API：Spring Boot `http://localhost:18080/api`
- 資料庫：H2 MSSQLServer mode
- 郵件：mock SMTP，固定 E2E 驗證碼 `123456`

```bash
cd frontend
npm run test:e2e:email
```

## MUST 與 Checklist 結果

| Sheet MUST / Checklist | Cypress 操作 | 結果 |
|---|---|---|
| 信箱註冊頁寄碼（CK-002） | 登入頁→使用信箱註冊→輸入 email→發送 | ✅ |
| 重複信箱提醒（CK-002） | 以已註冊 email 再次發送 | ✅ 顯示「此信箱已註冊」 |
| 驗證後設定密碼並進首頁（CK-003） | 輸入 123456→兩次密碼→完成註冊 | ✅ 導向 `/test/testTemp/` |
| 信箱密碼登入（CK-004） | 登出→email/password 登入 | ✅ |
| 忘記密碼（CK-005） | 登入頁→忘記密碼→寄碼→核銷→更新 | ✅ |
| 新密碼登入（CK-005） | 返回登入→新密碼登入 | ✅ |
| 管理員測試寄信與紀錄（CK-001） | admin 登入→信箱驗證→寄送→查表 | ✅ 最新列為管理員測試／成功 |

## 首次紅燈與修正

首次執行第 2 個測試時，Cypress 在登入 API 完成前先導向管理員頁，Guard 將畫面送回 `/login`。測試 helper 加入 URL 離開 `/login` 的等待條件後重跑，最終 2 / 2 綠燈。此修正只同步真實非同步行為，未降低斷言。

## 完成率

- Cypress tests：2 / 2 = **100%**
- Sheet 六條 MUST UI／整合流程覆蓋：6 / 6 = **100%**
- 對應 checklist CK-001～CK-006、CK-016：**全部通過**
