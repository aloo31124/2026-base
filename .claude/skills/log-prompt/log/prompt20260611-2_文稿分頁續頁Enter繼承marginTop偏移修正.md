2026-06-11 15:43:27

## git commit
[fix] #755 [文稿分頁] 修正續頁 Enter 分行繼承 margin-top 造成偏移

- 症狀：第 2 頁按 Enter 產生的第二行(li)多出 margin-top（如 291px）造成高度偏移，僅續頁發生。
- 真因：Quill 於已被分頁推頁(帶 margin-top)的 li 中 Enter 分行時，新生 li 繼承原 li 的 inline margin-top；該新元素從未被 SpacerTracker touch，舊清除條件 this.touched.has(el) 為 false → 繼承來的 spacer 永不被清。
- 修正：SpacerTracker.apply 清除條件改為「規劃不位移且元素殘留 inline margin-top 即清」(else if el.style.marginTop)，涵蓋繼承來的 margin；v1/v2 共用路徑同受惠。
- 新增 spacerTracker.spec（4 測，含 Enter 分行繼承 291px 應清除案例）；docRules 121 測全綠、vue-tsc 淨。
- 移除先前暫加的 __A4_DEBUG 診斷碼。

## prompt 1
沒有成功修正，請將上述修改退版。

## prompt 1 response
- 退回前一輪「line-height:0;font-size:0」嘗試（pageBreakBlot.ts 內聯樣式、DocumentBody.vue `.a4-page-break` CSS）。
- 更新 memory：標記該盲猜已退版、需重查問題本質。
- 請使用者提供瀏覽器實測數據（embed height / 間距）以利定位。

## prompt 2
修正失敗，已經退版，請重新修正。
（按：此時 model 切回 opus；使用者補充「新分頁打字 Enter 後新行落在頁面接近中央」）

## prompt 2 response
- 反覆推導 breakPlanner/lineSplitPlanner/補償邏輯，判定數學自洽 → bug 在「餵給數學的量測」。
- 查 quill/blots/embed.js 證實 Embed 內含 leftGuard/rightGuard 兩個 ﻿ 護衛文字。
- 假設 lineBoxMeasure 的 TreeWalker 鑽進 embed 把護衛文字逐字計入 → charOffset 每 embed 超計 2 → startIndex 膨脹 → reconcileBreaks 每輪刪重插、embed 漂入中段。
- 在文字分支加守衛略過 `.a4-page-break` 內護衛文字；117 測全綠（此修正後續經實測仍未解、再退版）。

## prompt 3
我找到原因，因為額外增加了 margin-top: 246px; 造成，請修正。

## prompt 3 response
- 判定為「v1 margin-top 與 v2 embed 雙重位移」：applyCharSplit 內 planPagination+spacers.apply 對量不到多行行框的 splittable（Enter 短新行 li）走整塊推頁配 margin-top，與上游 embed 疊加多推約半頁。
- 修正：applyCharSplit 於 spacers.apply 前把所有 kind==='splittable' 的 plan.spacer 歸零（splittable 僅由 embed 分頁）。
- 117 測全綠（後證實此分支因 ENABLE_CHAR_SPLIT=false 未被執行，屬死碼、無效）。

## prompt 4
並未成功修正，上述所有修正皆退版，
新分頁中，按下 enter 產生的第二行會額外產生 margin-top: 造成偏移，
請釐清原因並修正。

## prompt 4 response
- 全域 grep 確認唯一動態寫 marginTop 者為 SpacerTracker.apply。
- 讀 DocumentBody 接線，發現 `ENABLE_CHAR_SPLIT = false` → 走 v1 路徑、applyCharSplit 從不執行 → 先前兩次改在 applyCharSplit 內皆為死碼，故無效。
- 確認 descArea/proposal 為 lines 模式（splittable），bug 落在 v1 的 planPagination→spacers.apply。

## prompt 5
重新描述問題，
在第一頁[文稿分頁]頁底判斷間距按下 enter 後，
確實可產生新的 第二頁[文稿分頁]，該新的分頁第一行在頁首，
此部分位置都是正確的。
但在 第二頁[文稿分頁] 按下 enter後，產生第二行，
便會產生 margin-top 造成高度偏移。
請找出原因並修正。

## prompt 5 response
- 確立症狀：第 2 頁第一行（推頁 margin 正確）OK；第二行（Enter）多 margin → 偏移。
- v1 路徑數學再推導，理論上第二行 spacer 應為 0，實機卻有 margin → 判定有讀不出的真實版面因素。
- 加入 `__A4_DEBUG` 診斷（gated、不改行為）：印各 block 的 dirtyTop/cleanTop/height/ownMargin/planSpacer 表，請使用者實機擷取。

## prompt 6
console 沒印出任何錯誤資訊。

## prompt 6 response
- 研判旗標於 reload 後被清（window 變數不跨重新整理）。
- 將診斷開關改讀 localStorage（`localStorage.__A4_DEBUG='1'`，跨 reload 保留），相容 window 臨時旗標。
- 給出「先設 localStorage→強制重整→重現→看 Info/Verbose 層級」步驟，並說明若仍無輸出代表 run() 未觸發（反推 margin 來源非分頁引擎）。

## prompt 7
（貼上 desc-editor-area 實際 DOM：li 六、li 七 兩者皆 `style="margin-top: 291px;"`）
請修正

## prompt 7 response
- DOM 鐵證：li 六（正確推頁 291px）與 Enter 新生的 li 七同帶 291px。
- 真因確認：Quill Enter 分裂 li 時新 li 繼承原 li 的 inline margin-top；新元素未被 SpacerTracker touch → 舊清除條件 this.touched.has(el)=false → 繼承的 291px 永不清。
- 修正 paginationDecorator.ts：清除條件改 `else if (el.style.marginTop)`，涵蓋繼承來的 margin（v1/v2 共用同受惠）。
- 移除診斷碼；新增 spacerTracker.spec（4 測）；docRules 121 測全綠、vue-tsc 淨。
- 提供 git commit 區塊。

## prompt 8
/log-prompt

## prompt 8 response
- 首次歸檔：產生 prompt20260611-1_文稿分頁續頁Enter繼承marginTop偏移修正.md。
- 記錄 7 則 prompt（逐字）與各自回復精要、最終 git commit。

## prompt 9
/log-prompt（第二次呼叫，要求補內容）

## prompt 9 response
- 使用者要求補進本次互動內容。
- 查詢發現已有 prompt20260611-1，流水號應改為 -2。
- 重新生成完整版本，涵蓋 9 則 prompt 與 final commit。
