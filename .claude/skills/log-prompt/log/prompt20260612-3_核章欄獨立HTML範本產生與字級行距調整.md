2026-06-12 16:30:00

## git commit
[feature] #755 [公文製作].[核章欄] 依舊版範本圖產生獨立核章欄 HTML 並調整字級行距。
- 新增 Frontend/src/formatExamples/核章欄.html：依核章欄.png 重現核章欄版面。
- 沿用簽.html 之 CSS 變數、標楷體字型、A4 寬度與邊界規範。
- 結構：框外「會辦單位：」標籤＋虛線外框；框內含「第一層決行」(紅色必填星號)、
  三欄標頭(承辦單位/會辦單位/決行)、簽核空白區。
- 外框虛線、內部列間實線，對應原圖線條呈現。
- 後續調整：所有文字統一縮小至 16px、行距由固定 38.6px 改為倍數 1.3（收窄）；
  移除未使用的 --fs-small / --lh-small 變數。

## prompt 1
請參考 舊[公文製作]之 核章欄，
如下圖，
Frontend\src\formatExamples\核章欄.png 
並產生獨立之 html：
Frontend\src\formatExamples\核章欄.html

## prompt 1 response
- 讀取 核章欄.png 解析版面，並參考既有 簽.html 取得格式規範。
- 確認 核章欄.html 原為空檔，重新產出完整獨立 HTML。
- 結構重現：框外「會辦單位：」標籤、虛線外框；框內第一列「*第一層決行」(紅色必填星號)、
  第二列三欄標頭（承辦單位／會辦單位／決行）、下方大片簽核空白區。
- 沿用簽.html 之 CSS 變數、標楷體字型、A4 寬度與左右邊界，外框虛線、內部列間實線。

## prompt 2
請將 核章欄.html  行距調窄，所有文字縮小到 16px

## prompt 2 response
- 所有文字統一為 --fs-main 16px（原主文字 21.3px）。
- 行距由固定 --lh-main 38.6px 改為倍數行高 1.3，整體收窄。
- 移除已不再使用的 --fs-small / --lh-small 變數。
