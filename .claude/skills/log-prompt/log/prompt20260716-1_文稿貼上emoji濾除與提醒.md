2026-07-16 05:26:38

## git commit
[feature] #755 [公文製作] 文稿編輯區貼上時濾除所有 emoji 表情符號（spec 017）。
- 新增 `Frontend/src/docRules/emojiFilter.ts`：emoji 範圍唯一事實來源——`EMOJI_SEQUENCE_PATTERN`（Unicode property escapes；鍵帽序列排交替式最前確保整組含基底數字移除）、`stripEmoji`／`containsEmoji` 純函式、`registerEmojiPasteMatcher`（TEXT_NODE matcher 對 Delta string insert 逐 op 濾除、空 op 剔除、attributes 保留）。
- `Frontend/src/docRules/mouseRules.js`：強制純文字貼上路徑取出 text/plain 後先 `stripEmoji` 再套 singleLine 去換行；全 emoji 濾除後沿用既有空內容不動作慣例。
- `Frontend/src/view/DocumentBody.vue`：initQuill 緊鄰中文編號清單 matcher 註冊 `registerEmojiPasteMatcher`，四欄位一體適用；清單放行路徑貼上保留結構但不帶 emoji。
- 新增 `__tests__/emojiFilter.spec.ts` 23 條契約測試（ZWJ 家族／膚色／國旗／tag 旗／鍵帽／VS16 整組移除零碎片；合法繁中全形標點與單獨 #、*、數字零誤殺）；新增 `cypress/e2e/emoji-paste-filter.cy.ts` 10 案例全綠（四欄位濾除／全 emoji 不動作／游標位置／清單路徑／回歸）。
- 全量回歸：Vitest 274/278（4 紅為既有 EditorToolbar DI 無關）、Cypress 85/88（3 紅為既有 mixed selection 與主旨 Enter 瑕疵無關）、vite build 成功。
- 新增 `specs/017-emoji-paste-filter/`（spec／plan／tasks／analyze／checklists，/spec-to-implement 一條龍產出）、`skills/business-logic/emoji-paste-filter/SKILL.md`，更新 `skills/SKILLS_INDEX.md` 與 `.specify/feature.json`。

[feature] #755 [公文製作] 貼上 emoji 時追加視覺提醒 toast（spec 017 Revision）。
- `Frontend/src/docRules/emojiFilter.ts`：`registerEmojiPasteMatcher` 新增選填 `onEmojiDetected` 回呼，清單放行路徑偵測到 emoji 時觸發一次；濾除邏輯本身不變。
- `Frontend/src/docRules/mouseRules.js`：`attachForcePlainTextPaste` 新增 `onEmojiDetected` 選項，濾除前以 `containsEmoji` 偵測。
- `Frontend/src/view/DocumentBody.vue`：兩條貼上路徑接上既有 spec 012 `showReminder`（`.keyboard-reminder-toast`：fixed 置中、2 秒自動消失），提醒文字「公文製作不可貼上 emoji 表情符號」；未新建提醒元件。
- 新增單元測試 3 條（matcher callback 正反例、未提供回呼不拋例外，共 26/26 綠）；`cypress/e2e/emoji-paste-filter.cy.ts` 新增 3 條 E2E（toast 出現／文字／不阻斷插入、`cy.clock()`+`cy.tick(2000)` 驗證自動消失、不含 emoji 不觸發，共 13/13 綠）。
- 修正 E2E 陷阱：`.keyboard-reminder-toast` 之 `pointer-events:none` 會使 Cypress `be.visible` 誤判遮蔽，改用 `should('exist')`。
- 全量回歸：Vitest 277/281、Cypress 88/91（既有失敗筆數不變）、vite build 成功。
- 更新 `specs/017-emoji-paste-filter/spec.md`（FR-007 修訂、議題 2 保留原決策＋新增 Revision 章節、US1 AC5、SC-010）、`tasks.md`（Phase 9）、`analyze.md`、`skills/business-logic/emoji-paste-filter/SKILL.md` 與 `skills/SKILLS_INDEX.md`。

## prompt 1
/spec-to-implement 文稿編輯區，不可貼上所有 emoji 的表情符號。

## prompt 1 response
- 階段 0 定位入口：`.specify/feature.json` 指向既有 015，本需求為全新單一功能，手動建立 `specs/017-emoji-paste-filter/` 並改指 feature.json。
- 階段 A 六階段一次跑完：spec.md（5 個 User Story、8 條 FR、5 條 SC、5 項自動釐清決策，含「濾除而非整段拒絕」「Extended_Pictographic 全集寧嚴勿漏」「鍵帽整組含基底移除」「僅管 paste 不管 drop/IME」等拍板）、checklist（21 項全通過）、plan.md（技術樹＋正則設計依據＋接線契約）、tasks.md（15 任務）、analyze.md（FR↔task 全覆蓋、無孤兒）。
- 階段 B TDD 實作：新增 `docRules/emojiFilter.ts`（`EMOJI_SEQUENCE_PATTERN`／`stripEmoji`／`containsEmoji`／`registerEmojiPasteMatcher`），先寫 23 條單元測試紅燈再實作至綠燈；接線 `mouseRules.js` 純文字路徑與 `DocumentBody.vue` 清單放行路徑（`registerChineseListClipboardMatcher` 旁）；新增 `emoji-paste-filter.cy.ts` 10 案例 E2E。
- 排除環境障礙：`ELECTRON_RUN_AS_NODE=1`（VSCode 擴充注入）導致 Cypress 報 `bad option: --smoke-test`，需在同一指令內 `Remove-Item Env:\ELECTRON_RUN_AS_NODE`；dev server 4200 埠被佔用自動退到 4201。
- 階段 C 驗收：Vitest 274/278（4 紅為既有 EditorToolbar DI，與本次無關）、Cypress 全量 85/88（3 紅為既有瑕疵無關）、本次變更檔案 tsc 0 型別錯誤、vite build 成功；tasks.md 15 項全打勾。
- 新增 `skills/business-logic/emoji-paste-filter/SKILL.md`、更新 `SKILLS_INDEX.md`；記憶新增 spec017 進度與 Cypress ELECTRON_RUN_AS_NODE 陷阱兩則。
- 產出收尾報告（含 5 項關鍵自動決策說明）與可複製 git commit 訊息。

## prompt 2
貼上 emoji 表情符號，會跳出提醒，公文製作不可貼上 emoji 表情符號。
格式樣式與 "請先輸入內容再換行" 一樣，fixed 畫面中，閃爍後消失。

## prompt 2 response
- 找出既有 spec 012 `showReminder`／`.keyboard-reminder-toast` 機制（`DocumentBody.vue`：fixed 置中、2 秒後自動消失），決定重用而非新建元件；此為 spec 017 議題 2 當初已預留之擴充點。
- 更新 `spec.md`：FR-007 由「MUST 靜默」改為「MUST 顯示提醒 toast」，保留原決策文字並標記「已於 Revision 修訂」，新增 Revision 章節說明理由，US1 補 AC5，新增 SC-010。
- `emojiFilter.ts` `registerEmojiPasteMatcher` 新增選填 `onEmojiDetected` 回呼（偵測到 emoji 呼叫一次）；`mouseRules.js` `attachForcePlainTextPaste` 新增同名選項，濾除前以 `containsEmoji` 偵測；`DocumentBody.vue` 兩條貼上路徑接上 `showReminder('公文製作不可貼上 emoji 表情符號')`，濾除行為本身不變。
- 新增 3 條單元測試（callback 正例／反例／未提供回呼不拋例外，共 26/26 綠）；新增 3 條 E2E（toast 出現＋文字＋不阻斷插入、`cy.clock()`+`cy.tick(2000)` 驗證 2 秒自動消失、不含 emoji 不觸發，共 13/13 綠）。
- 踩坑並修正：`.keyboard-reminder-toast` 設有 `pointer-events:none`，導致 Cypress `.should('be.visible')` 之 `elementFromPoint` 遮蔽判定誤判逾時失敗；改用 `.should('exist')`（對應 v-if 掛載狀態）解決，記入 SKILL.md 陷阱清單與新增記憶。
- 全量回歸：Vitest 277/281、Cypress 88/91（既有失敗筆數與內容完全一致，零退化）、vite build 成功。
- 更新 `tasks.md`（新增 Phase 9 Revision）、`analyze.md`（Revision 覆蓋核對）、`skills/business-logic/emoji-paste-filter/SKILL.md`、`SKILLS_INDEX.md`；記憶更新 `project_spec017_emoji_paste_filter` 並新增 `reference_cypress_pointer_events_none_visibility`。
