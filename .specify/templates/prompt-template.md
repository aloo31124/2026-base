# Specify Prompt — [FEATURE SHORT NAME]

- **Feature Directory**: [SPECIFY_FEATURE_DIRECTORY]
- **Branch**: [BRANCH_NAME 或「(無 git 分支)」]
- **Created**: [YYYY-MM-DD HH:mm]
- **Invoked Command**: `/speckit.specify`

> 本檔案由 `/speckit.specify` 在建立 feature 目錄時自動產生，目的是把實際丟給 `/speckit.specify` 的原始 prompt、以及當時為什麼這樣描述的脈絡，跟產出的 `spec.md` 物理上綁在一起，方便日後回看「當初為什麼這樣寫」。
>
> - 與 `spec.md` 並列於同一 feature 目錄，請一併納入版本控管。
> - 若同一 feature 第二次跑 `/speckit.specify`，**不要覆寫**舊內容；改在檔尾以 `## Re-run @ <YYYY-MM-DD HH:mm>` 標題追加新一輪的 Original Prompt / Context & Rationale。
> - 撰寫時除 Original Prompt 段落必須逐字保留使用者輸入外，其餘段落一律使用**繁體中文**。

## Original Prompt

> 以下為使用者實際丟給 `/speckit.specify` 的原始文字，請逐字保留，不做潤飾或翻譯。

```text
[ORIGINAL_PROMPT_VERBATIM]
```

## Context & Rationale

- [用 2–6 條列點，說明這段 prompt 為什麼這樣寫：包含對話中提到的上游需求、規劃書章節、先前 spec 的延伸、限制條件、使用者偏好等。]
- [若本輪對話沒有額外脈絡，整段以一行「（無額外脈絡，使用者直接提出本需求）」代替。]

## Related References

- [列出對話中明確提到的相關文件、issue、PR、既有 spec 編號或檔案路徑。]
- [若無，整段省略（連同標題一起刪除）。]
