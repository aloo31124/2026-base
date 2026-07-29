<#
.SYNOPSIS
  /spec-to-implement 的機械性輔助：盤點當前 feature 的文件齊備度與任務進度。
  判斷題（怎麼實作、紅燈怎麼修）交給 agent；本腳本只做易算錯的確定性統計。

.PARAMETER Mode
  status  輸出 FEATURE_DIR、spec/plan/tasks 是否存在、tasks.md 任務統計
          （總數／已完成／待處理）與 checklists/ 各檔完成度，供進場判斷與收尾把關。

.PARAMETER FeatureDir
  指定 feature 目錄（相對 repo 根，例：specs/015-a4-pagination）；
  未給時讀 .specify/feature.json 的 feature_directory。

.EXAMPLE
  pwsh spec-implement-helper.ps1 -Mode status
  pwsh spec-implement-helper.ps1 -Mode status -FeatureDir specs/015-a4-pagination
#>
param(
  [Parameter(Mandatory = $true)]
  [ValidateSet('status')]
  [string]$Mode,

  [string]$FeatureDir
)

$ErrorActionPreference = 'Stop'
# 確保中文輸出不亂碼
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# 定位 repo 根目錄（腳本位於 .claude/skills/spec-to-implement/scripts/）。
# 不用 `git rev-parse --show-toplevel`：當路徑含中文時 git 會輸出 octal 跳脫而失效，
# 改由腳本實體位置往上推四層取得，對 CJK 路徑穩定。
$repoRoot = (Resolve-Path "$PSScriptRoot\..\..\..\..").Path

# 統計一個 markdown 檔內 checkbox 任務的完成度
function Get-CheckboxStats {
  param([string]$FilePath)
  $done = 0; $pending = 0
  foreach ($line in (Get-Content $FilePath)) {
    if ($line -match '^\s*-\s*\[[xX]\]') { $done++ }
    elseif ($line -match '^\s*-\s*\[ \]') { $pending++ }
  }
  return [pscustomobject]@{ Total = $done + $pending; Done = $done; Pending = $pending }
}

switch ($Mode) {

  'status' {
    if (-not $FeatureDir) {
      $featureJson = Join-Path $repoRoot '.specify\feature.json'
      if (-not (Test-Path $featureJson)) {
        Write-Error "未指定 -FeatureDir 且找不到 $featureJson，無法定位 feature。"
        exit 1
      }
      $FeatureDir = (Get-Content $featureJson -Raw | ConvertFrom-Json).feature_directory
    }

    $featurePath = Join-Path $repoRoot $FeatureDir
    if (-not (Test-Path $featurePath)) {
      Write-Error "feature 目錄不存在：$featurePath"
      exit 1
    }

    Write-Host "FEATURE_DIR=$FeatureDir"

    # 文件齊備度
    foreach ($doc in 'spec.md', 'plan.md', 'tasks.md') {
      $key = ($doc -replace '\.md$', '').ToUpper()
      $state = if (Test-Path (Join-Path $featurePath $doc)) { 'OK' } else { 'MISSING' }
      Write-Host "$key=$state"
    }

    # tasks.md 任務統計
    $tasksFile = Join-Path $featurePath 'tasks.md'
    if (Test-Path $tasksFile) {
      $t = Get-CheckboxStats -FilePath $tasksFile
      Write-Host ("TASKS_TOTAL={0} TASKS_DONE={1} TASKS_PENDING={2}" -f $t.Total, $t.Done, $t.Pending)
    }

    # checklists/ 各檔完成度
    $checklistDir = Join-Path $featurePath 'checklists'
    $checklistPending = 0
    if (Test-Path $checklistDir) {
      Get-ChildItem -Path $checklistDir -Filter '*.md' | Sort-Object Name | ForEach-Object {
        $c = Get-CheckboxStats -FilePath $_.FullName
        $checklistPending += $c.Pending
        Write-Host ("CHECKLIST {0} TOTAL={1} DONE={2} PENDING={3}" -f $_.Name, $c.Total, $c.Done, $c.Pending)
      }
    }
    Write-Host "CHECKLISTS_PENDING_TOTAL=$checklistPending"
  }
}
