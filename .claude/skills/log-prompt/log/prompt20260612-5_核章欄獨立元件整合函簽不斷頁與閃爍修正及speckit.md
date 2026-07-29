2026-06-12 16:19:08

## git commit
[feature] #755 [公文製作].[核章欄] 函／簽新增核章欄獨立元件並納入 atomic 不斷頁分頁。
- 新增純展示元件 Frontend/src/components/ChopSection.vue（抽離自 formatExamples/核章欄.html）；caption/levelLabel/columns/bodyHeight（預設 4cm）以 props 開放、自含 --chop-fs:16px 等 CSS 變數與父層主字級脫鉤，降低耦合。
- documentFormats.ts 之 sections schema 新增 chopSection flag，函／簽設 true、簡易樣式設 false。
- DocumentBody.vue 以外層 <div class="chop-flow-block"> 包裹 <ChopSection>，buildFlowableSpecs() 新增 add('chop', …, 'atomic')，越過頁底時整塊推至新分頁、不斷頁。
- 文件：document-format-component-architecture SKILL 補核章欄落地實例與 atomic-margin 陷阱。

[fix] #755 [文稿分頁].[核章欄] 修正函說明換行時核章欄於新分頁不斷閃爍。
- 起因：atomic spacer 以 inline margin-top 套用會覆寫 .a4-doc-page > * + * 的 0.25cm section-gap，而 compensateAppliedSpacers 只讀 inline marginTop 還原乾淨幾何，致「套用/清除 spacer」兩狀態幾何相差一個 gap，核章欄底端落於頁界帶時溢出判定每輪翻覆 → 不斷閃爍。
- 修法：新增 .a4-flow > .chop-flow-block { margin-top:0; padding-top:var(--section-gap) }，令 atomic margin-top 專供分頁 spacer，視覺間距改用 padding-top（計入量測高度、不受 inline 覆寫），兩狀態幾何一致、穩態收斂。
- ChopSection.vue 簡化為純展示元件（移除 defineExpose/rootEl）。

[docs] #755 [文稿分頁].[核章欄] 補逆向 speckit：核章欄不斷頁區段與頁界閃爍修正。

- specs/001-doc-format-selection：spec.md FR-005 區段清單加「核章欄」、新增 FR-005a；data-model.md sections 加 chopSection，sign/letter 設 true。
- specs/015-a4-pagination：spec.md 新增 FR-021/FR-022、Edge Case、US1 Acceptance Scenario 4、SC-010、Clarifications Session 2026-06-12（核章欄 follow-up）；plan.md 補 chop=atomic 與檔案樹/Scope；tasks.md 新增 Phase 9（T041–T047）；analyze.md 補 FR-021/022、SC-010。


## prompt 1
[文稿選擇]呈現之 [函] [簽]格式增加 [核章欄]樣式。

### 概述
如題，請參考如下範例，
在 [新增文稿] 視窗選擇格式後，
[函] [簽]格式增加 [核章欄]樣式。
並且 [核章欄] 不斷頁分頁 。

### 撰寫風格
[核章欄]樣式 請抽離成獨立之 component 程式檔，
方便後續調整該[核章欄] 高度與增加其他功能。
並且降低耦合。

### [核章欄] 不斷頁分頁
當 [核章欄] 超出 [文稿頁面] 之 [頁面間距] 時，
確保 必須 MUST 讓該  [核章欄] 完整替換到新的 [文稿頁面]分頁。 
不會讓 [核章欄] 斷頁。

### 請參考 
Frontend\src\formatExamples\核章欄.html 
Frontend\src\formatExamples\函.html
Frontend\src\formatExamples\簽.html

## prompt 1 response
- 探查程式碼後發現分支錯置：工作目錄當下在 755-公文製作-分頁測試1-codex（分頁引擎已被 revert，無 docRules/pagination），而分頁引擎＋核章欄.html 參考檔在 755-temp-文稿分頁-claude；「不斷頁」需分頁引擎，故以 AskUserQuestion 詢問目標分支，使用者選擇切到 755-temp-文稿分頁-claude。
- 研讀分頁引擎：FlowableBlockSpec 的 atomic 模式＝整塊不可切分；breakPlanner 對 atomic 區塊「越過頁底安全線即整塊推下一頁」，正是「不斷頁」的天然解，無須改演算法。
- 落地三處：(1) 新增 Frontend/src/components/ChopSection.vue 純展示元件，props 開放 caption/levelLabel/columns/bodyHeight、自含 CSS 變數脫鉤主字級；(2) documentFormats.ts sections 加 chopSection（函/簽=true）；(3) DocumentBody.vue 模板於署長簽名後加 <ChopSection>、buildFlowableSpecs 以 atomic 登記。
- 驗證：docRules 121 測全綠、單元測試 207 passed（4 既有 EditorToolbar DI 失敗無關）；build:wc 三組 web component 建置成功並同步 12 檔至 Frontend-old。
- 依 CLAUDE.md 更新 document-format-component-architecture SKILL（後該段於後續被使用者/linter 還原為原狀，屬刻意）。

## prompt 2
當 函 說明欄位 新增第二行時，
會造成 [核章欄] 不斷閃爍無法正常產生於新分頁。

請修正此問題，確保 [核章欄] 可產生於 新分頁。

## prompt 2 response
- 定位根因：分頁引擎以 inline margin-top 對 atomic 區塊施加 spacer，會覆寫 .a4-doc-page > * + * 的 0.25cm section-gap；控制器收斂保護 compensateAppliedSpacers 只讀 el.style.marginTop 還原乾淨幾何，導致「套用 spacer（inline 取代 gap）」與「清除 spacer（CSS gap 生效但 inline=0）」兩狀態乾淨幾何相差一個 gap（≈9.4px）。核章欄底端落在此 gap 寬度的頁界帶內時，溢出判定每輪翻覆 → ResizeObserver 持續被自身改動觸發 → 不斷閃爍。
- 修法：以外層 <div class="chop-flow-block"> 包裹 <ChopSection> 並改登記該 div 為 atomic；新增 .a4-flow > .chop-flow-block { margin-top:0; padding-top:var(--section-gap) }（兩 class 提高權重蓋過 * + *），使 atomic 區塊 margin-top 專供 spacer，視覺間距改由 padding-top 提供（padding 計入量測高度、不受 inline 覆寫）→ 兩狀態幾何一致、穩態收斂。
- ChopSection.vue 簡化為純展示元件（移除 defineExpose/rootEl）；chopSectionRef 改為 HTMLElement、buildFlowableSpecs 改 add('chop', chopSectionRef.value, 'atomic')。
- 驗證：docRules 121 測全綠、單元測試 207 passed（同 4 既有失敗）；build:wc 建置並同步成功。提醒分頁屬瀏覽器相依，閃爍修正需 npm run dev 實機驗收。

## prompt 3
請修改相關 speckit 文件

## prompt 3 response
- 判定相關 spec：核章欄屬「函/簽版面區段」（001-doc-format-selection 的 sections schema）＋「不斷頁/閃爍」（015-a4-pagination）；經 grep 確認 001 的區段清單與 schema、015 的 plan 列舉皆因缺 chopSection 而 stale，須一併更新。
- specs/001-doc-format-selection：spec.md FR-005 函/簽區段清單加「核章欄」、新增 FR-005a（獨立元件/props/不斷頁，指向 015 FR-021）；data-model.md sections 介面加 chopSection、sign/letter config 設 true。
- specs/015-a4-pagination：spec.md 新增 FR-021（atomic 不斷頁）、FR-022（atomic margin-top 專供 spacer 去閃爍）、Edge Case、US1 Acceptance Scenario 4、SC-010、Clarifications「Session 2026-06-12（核章欄 follow-up）」兩議題；plan.md 補 chop=atomic、Scale/Scope 與檔案樹（ChopSection.vue/documentFormats.ts/.chop-flow-block）；tasks.md 新增 Phase 9（T041–T047，標題加「（核章欄）」避免 MD024 重複標題）；analyze.md FR↔碼對照補 FR-021/022、SC 對照補 SC-010，維持「無孤兒 FR」。
- 共改 6 檔（001×2、015×4），跨檔交叉引用一致（001 FR-005a ↔ 015 FR-021；spec/tasks/analyze 的 FR/SC 編號互通）；SC-010/T046 標待瀏覽器實機驗收，符合既有慣例。
