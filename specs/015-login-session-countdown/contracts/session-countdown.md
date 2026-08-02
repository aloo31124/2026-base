# UI Contract：登入工作階段倒數

## 目錄

- [輸入契約](#輸入契約) — token 與 onExpire
- [顯示契約](#顯示契約) — 文案、格式與 test id
- [事件契約](#事件契約) — tick、恢復與到期
- [登出契約](#登出契約) — 儲存清除與路由
- [相容契約](#相容契約) — 無 exp token

## 輸入契約

`SessionCountdown` 接收 `token: string` 與 `onExpire: () => void`。元件不得修改 token 或自行寫入儲存。

## 顯示契約

- 根節點：`role="timer"`、`data-testid="session-countdown"`。
- 標籤：桌面顯示「登入倒數」；窄螢幕可隱藏標籤但數值必須保留。
- 數值：`data-testid="session-countdown-value"`，固定 `HH:MM:SS`。
- 剩餘 300 秒以下加上 `warning` class。

## 事件契約

- mount 立即同步一次。
- 每 1000ms 同步一次。
- `document.visibilitychange` 與 `window.focus` 立即同步。
- 所有同步均從 `Date.now()` 重算，不採 `remaining - 1`。

## 登出契約

`onExpire` 由 AppShell 實作：dispatch `logout('session-expired')` 清除 Redux、`session` 與 `token` 並保存逾時原因，再 replace 導向 `/login`。手動登出使用 `logout(undefined)`，不得顯示逾時訊息。

## 相容契約

若 token payload 不可解析或沒有有效 `exp`，元件回傳 null，不顯示倒數、不假造到期時間；正式 API 授權行為不變。
