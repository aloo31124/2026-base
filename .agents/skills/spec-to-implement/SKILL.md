---
name: "spec-to-implement"
description: >
  spec-to-implement：把單一需求從 spec.md 一路做到「程式碼實作完成、測試綠燈」的全自動協調器，
  等於 /spec-to-task 六階段接上 implement，中途不詢問、不中斷、不停在 tasks.md。
  若當前 feature 已有完整 spec/plan/tasks（例如剛跑完 /spec-to-task），則跳過前段直接開始實作。
  觸發語：「spec-to-implement / spec-to-task 完直接 implement / 從需求一條龍做到實作 /
  規格到程式碼一次完成 / tasks 產完直接開做 / 不要停直接實作到綠燈 / 一路做到 implement /
  幫我把這個需求做出來不要問我」。
  使用者丟「單一」需求且要求連實作一起完成、或 spec-to-task 剛完成要接著實作時使用；
  只要規格文件（停在 tasks.md）請用 /spec-to-task，多條需求批次請改用 /speckit-update。
argument-hint: "單一功能需求描述；或留空表示接續當前 feature 直接實作"
compatibility: "需要 spec-kit 專案結構（.specify/ 與 specs/）"
user-invocable: true
disable-model-invocation: false
---

## 目錄

- [目的（為什麼這樣設計）](#目的為什麼這樣設計) — 拿掉 tasks.md 與 implement 之間的人工停點
- [鐵則（先讀，貫穿全程）](#鐵則先讀貫穿全程) — 不提問、單一 feature、紅燈不收工
- [執行流程](#執行流程) — 階段 0 定位入口 → A 規格 → B 實作 → C 驗收
  - [階段 0：定位入口](#階段-0定位入口) — helper 盤點文件與任務進度，決定從哪起跑
  - [階段 A：spec-to-task 六階段](#階段-aspec-to-task-六階段) — 沿用 /spec-to-task，跑完不停下
  - [階段 B：implement（覆寫規則）](#階段-bimplement覆寫規則) — 沿用 /speckit-implement，移除 STOP 詢問
  - [階段 C：驗收](#階段-c驗收) — 測試綠燈＋任務歸零＋FR 對照
- [收尾報告](#收尾報告) — 全程唯一一次對話

---

## 目的（為什麼這樣設計）

本專案的 speckit 管線預設停在「產完 tasks.md、詢問是否 implement」；而 `/speckit-implement`
本身遇到 checklist 未完成也會 STOP 等使用者回覆。對「我已經想清楚、只想看到能動的成果」的情境，
這兩個停點都是多餘的中斷。本 skill 把 **需求 → spec/plan/tasks → 實作 → 測試綠燈** 串成一條龍：
規格段沿用 `/spec-to-task` 的全部方法論與覆寫規則，實作段沿用 `/speckit-implement` 的執行順序，
但把所有「停下來問」改為自行決策並留下決策紀錄，讓使用者一次發話就拿到可驗收的成果。

方法論留在各來源 skill（漸進式揭露，避免重複數百行）；本檔只負責**入口判斷、覆寫規則與完工定義**。
不修改 speckit 本身的任何文檔——所有覆寫只存在於本流程的執行約定中。

---

## 鐵則（先讀，貫穿全程）

- **全程不提問、不中斷**：任何階段冒出「該問使用者」的點，一律自己選最佳解，把決定與理由寫進
  對應文件或收尾報告。唯一可停下的情況：需要只有使用者能提供的外部資源（帳密、實機、未知檔號），
  這是「無法起跑」而不是「提問」。
- **單一需求、單一 feature**：只在一個 `specs/NNN-*` 目錄內運作。多條需求改用 `/speckit-update`。
- **紅燈不收工**：實作完成的定義是「對應測試通過」，不是「程式碼寫完」。測試失敗先修再前進；
  若紅燈揭露的是本功能範圍外的既有 bug，記入收尾報告而非默默繞過。
- **沿用方法論、不複製內文**：規格段的「怎麼做」以 `/spec-to-task` 為準，實作段以
  `/speckit-implement` 為準；本檔只下達覆寫指令。

---

## 執行流程

### 階段 0：定位入口

先跑確定性盤點，避免憑印象判斷文件齊不齊、任務做完沒：

```powershell
pwsh .claude/skills/spec-to-implement/scripts/spec-implement-helper.ps1 -Mode status
```

依輸出決定起點（feature 以 `.specify/feature.json` 為準，使用者指明其他 feature 時用 `-FeatureDir` 覆寫）：

| 盤點結果 | 起點 |
|---|---|
| spec/plan/tasks 有缺，且使用者給了需求敘述 | 階段 A 全跑 |
| spec/plan/tasks 有缺，且使用者沒給需求敘述 | 回報缺哪些文件、無法起跑，結束 |
| 文件齊全、`TASKS_PENDING > 0` | 直接階段 B |
| 文件齊全、`TASKS_PENDING = 0` | 只跑階段 C 驗收後收尾 |

### 階段 A：spec-to-task 六階段

依 `/spec-to-task` 完整執行 specify → clarify → checklist → plan → tasks → analyze，
沿用其全部鐵則與文件格式規範（繁中、目錄、自動決策寫回文件）。
唯一覆寫：**跑完六階段不輸出它的收尾報告、不停下**，決策清單留到本 skill 的收尾報告一次講，
直接進入階段 B。

### 階段 B：implement（覆寫規則）

依 `/speckit-implement` 的方法論執行（讀 tasks/plan 與相關文件、phase-by-phase、
依賴順序與 [P] 平行、TDD 測試先行、完成任務即時在 tasks.md 打 `[X]`），但覆寫：

- **checklist 未完成不 STOP**：原流程會停下問「要不要繼續」。本流程改為逐項評估未勾項——
  反映真實規格缺口就當場回補文件再勾；已被現有內容滿足就勾掉並註記依據。補齊後繼續，不問。
- **非平行任務失敗不 halt**：先診斷、修復、重跑；連續無法突破時才記錄阻礙並在收尾報告說明，
  不中途等使用者指示。
- **每完成一個 phase 重跑 helper status**：用確定性統計核對 `TASKS_PENDING` 遞減，
  避免漏做或做完忘記打勾。
- 寫碼風格依 `skills/SKILLS_INDEX.md` 對應的 design-style / business-logic skill，
  不要自創慣例；實際採用的技術棧一律以本次 plan.md 記載為準，不預設固定框架。

### 階段 C：驗收

- 跑本 feature 對應的測試到綠燈：單元測試與 E2E 用哪套框架、指令為何，一律以 plan.md
  記載的技術棧與既有專案慣例為準，不預設固定工具。
- 重跑 helper status，確認 `TASKS_PENDING=0` 且 checklist 無未勾項。
- 逐條對照 spec.md 的 FR：每條都能指出「由哪個檔案的哪段實作滿足」。

---

## 收尾報告

三階段跑完後，一次性向使用者回報（全程唯一一次對話）：

1. feature 目錄路徑與 spec.md / plan.md / tasks.md（及 checklist、analyze 報告）位置。
2. 階段 A 自動做的關鍵決策與理由（若從階段 B 起跑則略）。
3. 實作摘要：動了哪些檔案、任務完成統計（helper 輸出）、測試結果證據。
4. 依 skill-create 規範評估是否需為新功能建立 `skills/business-logic` 說明文件並更新
   `skills/SKILLS_INDEX.md`。
5. 依 git-commit skill 附上可直接複製的 commit 訊息區塊。
