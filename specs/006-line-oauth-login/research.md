# Research：LINE OAuth 註冊登入

## Official Protocol Findings

- LINE Login Web 授權端點使用 `https://access.line.me/oauth2/v2.1/authorize` 與 Authorization Code flow。
- `state` 必須在 callback 比對以避免 CSRF。
- token endpoint 為 `POST https://api.line.me/oauth2/v2.1/token`，`redirect_uri` 必須與授權請求一致。
- LINE 建議 Web app 使用 PKCE；LINE 僅支援 `S256`。
- 使用 ID token 內資料前必須驗證；Verify ID token endpoint 可同時驗證 channel ID 與 nonce。

## References

- [Integrating LINE Login with your web app](https://developers.line.biz/en/docs/line-login/integrate-line-login/)
- [PKCE support for LINE Login](https://developers.line.biz/en/docs/line-login/integrate-pkce/)
- [LINE Login v2.1 API reference](https://developers.line.biz/en/reference/line-login/)
- [Get profile information from ID tokens](https://developers.line.biz/en/docs/line-login/verify-id-token/)

## Decisions

- 採 PKCE S256、state、nonce 與 server-side attempt persistence。
- callback 由前端 SPA 收取，再以 POST 傳後端，避免 JWT 出現在 URL。
- 本地測試使用 property-gated mock provider，沿用完整 redirect/callback 流程。
- 不保存 LINE token；本地 JWT 僅於 callback API response 回傳。
