# Data Model：登入工作階段倒數

## 目錄

- [概述](#概述) — 無新增持久化實體
- [既有 Session](#既有-session) — Redux 與 localStorage 資料
- [衍生 Countdown State](#衍生-countdown-state) — exp 與剩餘秒數
- [State Transitions](#state-transitions) — 登入、倒數、警示與到期
- [Validation Rules](#validation-rules) — token payload 邊界

## 概述

本功能不新增資料表或 API 欄位，只從既有 `Session.token` 衍生暫態倒數狀態。

## 既有 Session

| 欄位 | 型別 | 規則 |
|---|---|---|
| token | string | 現有 JWT；正式 token payload 含數值 exp |
| tokenType | string | 既有值 Bearer，本功能不修改 |
| username | string | 既有登入帳號 |
| fullName | string | 既有 header 顯示姓名 |
| roles | string[] | 既有導覽與 Guard 權限 |

Redux `auth.session` 與 `localStorage.session` 保存 Session；`localStorage.token` 保存相同 token。

## 衍生 Countdown State

| 欄位 | 型別 | 來源與規則 |
|---|---|---|
| expiresAtMs | number \| null | 有效 `exp × 1000`；無法解析則 null |
| remainingSeconds | number | `max(0, ceil((expiresAtMs - Date.now()) / 1000))` |
| display | string | `HH:MM:SS`；小時可超過 24 |
| warning | boolean | remainingSeconds ≤ 300 |

## State Transitions

```text
NO_EXP ── 不顯示 timer，授權交由既有 API
ACTIVE (>300s) ── 每秒/恢復校時 ──> WARNING (1–300s)
WARNING ── 到達 exp ──> EXPIRED (0s)
EXPIRED ── logout + replace ──> LOGIN
```

## Validation Rules

- token 必須至少含三段，第二段可依 base64url 解碼為 JSON。
- 只有有限且大於 0 的數值 `exp` 可轉為 expiresAtMs。
- payload 解析僅用於 UI 計時，不代表驗證簽章或授權；後端仍負責 token 真實性。
- 到期 callback 在單次元件生命週期只可觸發一次。
