# Data Model：信箱註冊登入

## email_verification

UUID 主鍵、email、purpose、code_hash、status、result_code、expires_at、verified_at、consumed_at、attempt_count、resend_available_at、created_at、updated_at。

## password_policy

UUID 主鍵、唯一 policy_key、minimum_length、require_english、require_digit、created_at、updated_at。

## Existing

成功註冊建立 app_user 與 EMPLOYEE user_role；密碼只保存 BCrypt hash。
