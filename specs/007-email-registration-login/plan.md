# Implementation Plan：信箱註冊登入

## Architecture

React 註冊／忘記密碼頁 → EmailAuthController → EmailAuthService → EmailVerificationDao、UserAccountDao、UserRoleDao；寄信透過 VerificationEmailSender，密碼統一由 PasswordPolicyService 驗證。

## Implementation

1. 建立 email_verification、password_policy BO 與 JPA DAO。
2. 建立驗證碼生命週期、SMTP 寄信、註冊與重設密碼 Service。
3. 建立四個公開 REST API 與一致 ApiResponse。
4. 建立 React／Redux 註冊、忘記密碼、重設密碼流程。
5. 完成 JUnit、Postman/Newman、Cypress、報告後回寫 Sheet。

## Security

驗證碼以 BCrypt 保存；信箱正規化；重寄使舊碼失效；忘記密碼寄送回應不洩漏帳號存在性；SMTP 憑證僅接受環境變數。
