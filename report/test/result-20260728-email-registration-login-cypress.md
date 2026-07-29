# 信箱註冊登入 Cypress 測試報告

## 結果

- 日期：2026-07-28
- Cypress spec：`email-registration-login.cy.ts`
- 案例：3
- 通過：3
- 失敗：0
- 完成率：100%

## 覆蓋

1. 寄送驗證碼、首次信箱註冊、建立 session 與信箱帳密登入。
2. 忘記密碼、取得一次性驗證碼、更新密碼與新密碼登入。
3. 錯誤驗證碼顯示明確錯誤訊息。

## 執行命令

```text
SPRING_PROFILES_ACTIVE=test ./backend/gradlew -p backend bootRun
cd frontend && npm run dev
cd frontend && npm run test:e2e:email
```

結果：3 passing，無 screenshot failure，前端 task/checklist 自動驗收完成率 100%。
