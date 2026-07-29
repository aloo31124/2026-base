2026-06-12 11:43:20

## git commit
[update] #755 [文稿頁面].[分頁] 主旨／說明第一欄位共用中段斷頁路徑與 ENABLE_CHAR_SPLIT 總開關開啟。
- buildFlowableSpecs：subject／desc 由 atomic 改為 lines（→ splittable），
  使斷點可落在 .ql-editor 行框之間，與 descArea／proposal 一致。
- buildCharSplitTargets：補登 subject→subjectQuill、desc→descQuill 兩條
  prefix→編輯器把手，讓 applyCharSplit 對其逐字量測行框、插入頁界 embed。
- 四欄位（主旨／說明第一欄位／說明區塊／擬辦）共用同一 planLineSplit 斷頁
  函式與同一 ENABLE_CHAR_SPLIT 總開關（預設 true）；分頁引擎零改動。
- 同步更新 buildFlowableSpecs／buildCharSplitTargets 註解。
- pagination 單元測試全綠；vue-tsc 無新增錯誤（EditorToolbar 4 測為既有失敗）。

## prompt 1
主旨、說明、擬辦 [多行]時[分頁]中間斷頁。

多行中段斷頁 ENABLE_CHAR_SPLIT 預設為 true 開啟。
請參考原本寫法讓
主旨, 說明第一欄位, 擬辦 欄位也共用一樣的 斷頁函式與
ENABLE_CHAR_SPLIT ，
一樣可斷頁。

## prompt 1 response
- 探查分頁引擎 `Frontend/src/docRules/pagination/`，釐清字元級中段斷頁的兩個必要條件：
  (1) 欄位需登記為 `lines` 模式（`MODE_KIND` → `splittable`），斷點才能落在 `.ql-editor` 行框之間；
  (2) 需在 charSplit targets 提供 prefix→Quill 把手，`applyCharSplit` 才會逐字量測行框、插入頁界 embed。
- 確認原本只有 `descArea`（說明區塊）與 `proposal`（擬辦）走此路徑；`subject`／`desc` 為 `atomic` 整塊推頁。
- 決定「零改動引擎」，只改應用層 [DocumentBody.vue](../../../../Frontend/src/view/DocumentBody.vue)：
  - `buildFlowableSpecs()`：`subject`、`desc` 由 `'atomic'` 改 `'lines'`。
  - `buildCharSplitTargets()`：補上 `add('subject', subjectQuill)`、`add('desc', descQuill)`。
  - 同步更新兩處註解，標明四欄位共用同一斷頁函式與 `ENABLE_CHAR_SPLIT`（預設 true）。
- 驗證：`npx vitest run` → pagination 全 22 檔通過；EditorToolbar 4 測失敗為既有問題（Quill mock proxy 拋錯，與本次無關）。
  `vue-tsc` 僅剩 tsconfig `baseUrl` 既有 deprecation 警告，無新增型別錯誤。
- 補充說明：`lines` 模式整欄推續頁時 spacer 施於內層 `.ql-editor > p`（label 留前頁）為既有設計特性，
  與擬辦／說明區塊路徑一致，符合「參考原本寫法」的一致性要求。
- 產出符合規範的 git commit 區塊，並詢問是否實機驗收或直接 commit。

## prompt 2
/log-prompt

## prompt 2 response
- 依 log-prompt skill 將本次對話歸檔：Glob 查得當日（20260612）尚無紀錄 → 流水號 1。
- 寫出 `.claude/skills/log-prompt/log/prompt20260612-1_主旨說明擬辦共用中段斷頁與CharSplit開關.md`，
  含時間戳、git commit 區塊、prompt 1 逐字與回復精要。
