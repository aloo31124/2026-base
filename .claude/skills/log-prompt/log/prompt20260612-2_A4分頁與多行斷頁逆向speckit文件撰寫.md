2026-06-12 14:10:31

## git commit
[docs] #755 [文稿編輯區] A4 分頁與多行斷頁逆向 speckit 文件（specs/015-a4-pagination）

- 依 specs/009-punctuation-symbol-bar 範式，從 Frontend/src/docRules/pagination/* 逆向產出整套規格：
  prompt / spec（5 US・20 FR・9 SC・8 Clarifications）/ plan（含技術樹 mindmap）/ research（R1–R10）
  / data-model / tasks（T001–T040 as-built）/ analyze / checklists/requirements / contracts ×2。
- analyze.md：因本機無 /speckit.analyze 命令，改人工逐條稽核——FR↔程式碼雙向 100% 可追溯、
  7 檔 50 分頁單元測試全綠、Critical/High/Medium 不一致為 0。
- .specify/feature.json 指向 specs/015-a4-pagination。
- 未改任何引擎程式碼；僅新增規格文件與 feature.json。

## prompt 1
[文稿編輯區]A4分頁與多行斷頁撰寫speckit。

### 概述
如題，請詳細檢視整份專案，
針對 [文稿編輯區]A4分頁與多行斷頁功能
與相關資料流, 程式架構逆規格，
撰寫speckit 相關文件如
spec.md, plan.md, task.md, checklist 等。

### speckit文件
產出所有相關文件與測試文件需一至，
請參考 specs\009-punctuation-symbol-bar
產生一樣的文件並詳細說明。

### spec檢查
產出所有文件後，
請使用 /speckit-analysis
檢查一次文件與程式碼一致性。

### 參考程式位置
Frontend\src\docRules\pagination\*

## prompt 1 response
- **前置研究**：完整讀 `specs/009-punctuation-symbol-bar` 全部 9 份文件（spec/plan/tasks/research/data-model/quickstart/prompt/checklists/contracts）掌握範式；逐一讀 `Frontend/src/docRules/pagination/` 10 個模組（pageGeometry/flowableBlock/breakPlanner/lineSplitPlanner/lineBoxMeasure/pageBreakBlot/pageBreakManager/paginationDecorator/paginationController/index）逆向其架構與資料流。
- **整合層勘查**：讀 `DocumentBody.vue` 的 `buildFlowableSpecs`／`buildCharSplitTargets`／`setupPagination`／`ENABLE_CHAR_SPLIT=true`／`continuationHeaderCm:0`；確認 `deltaBridge.deltaToLines:64` 略過頁界 embed、`chineseListBlot` 的 `LIST_ITEM_CONT_ATTR` 續接不重號；參照 `project_spec015_a4_pagination` 記憶補脈絡。
- **確認現況**：`specs/015-a4-pagination/` 在本分支不存在、`feature.json` 指向 014、分頁測試實際位於 `docRules/__tests__/`（非 pagination/__tests__）、無 cypress a4 測、無 /speckit.analyze 命令（.specify/extensions 僅 git）。
- **產出 10 份文件**到 `specs/015-a4-pagination/`：prompt.md、spec.md（5 US／20 FR／9 SC／8 Clarifications／Edge Cases）、plan.md（含 mermaid 技術樹 mindmap＋條列 fallback＋技術決策表）、research.md（R1–R10）、data-model.md（12 組型別對應原始碼）、quickstart.md、tasks.md（T001–T040 as-built，已完成標 [x]／待瀏覽器標 [ ]）、analyze.md、checklists/requirements.md、contracts/{pagination-controller,break-engine}.md。
- **一致性稽核**：因無 /speckit.analyze 命令，於 analyze.md 人工逐條 FR↔程式碼對照（20 FR 全 ✅ 雙向可追溯、無孤兒）；實跑 `npx vitest run` 驗證 **7 檔 50 測全綠**（pageGeometry 11／breakPlanner 11／lineSplitPlanner 6／pageBreakManager 8／spacerTracker 4／embedCompensation 8／flowableBlockLines 2），與文件宣稱一致；Critical/High/Medium=0，4 項 Note 為應用層決策或待瀏覽器驗證。
- **收尾**：`.specify/feature.json` 由 014 改指 `specs/015-a4-pagination`；更新 `project_spec015_a4_pagination` 記憶補逆向文件補齊紀錄；產出 [docs] commit message 區塊。未改任何引擎程式碼。
