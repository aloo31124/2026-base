param([ValidateSet('status')][string]$Mode = 'status', [string]$FeatureDir)
$root = (Resolve-Path (Join-Path $PSScriptRoot '..\..\..\..')).Path
if (-not $FeatureDir) { $FeatureDir = (Get-Content -Raw (Join-Path $root '.specify\feature.json') | ConvertFrom-Json).feature_directory }
$dir = Join-Path $root $FeatureDir
$pending = if (Test-Path (Join-Path $dir 'tasks.md')) { (Select-String -Path (Join-Path $dir 'tasks.md') -Pattern '^- \[ \] T\d+' | Measure-Object).Count } else { -1 }
$total = if (Test-Path (Join-Path $dir 'tasks.md')) { (Select-String -Path (Join-Path $dir 'tasks.md') -Pattern '^- \[[ xX]\] T\d+' | Measure-Object).Count } else { 0 }
$checkPending = if (Test-Path (Join-Path $dir 'checklists')) { (Get-ChildItem (Join-Path $dir 'checklists') -File | Select-String -Pattern '^- \[ \] CHK\d+' | Measure-Object).Count } else { -1 }
[ordered]@{ FEATURE_DIR=$FeatureDir; SPEC=(Test-Path (Join-Path $dir 'spec.md')); PLAN=(Test-Path (Join-Path $dir 'plan.md')); TASKS=(Test-Path (Join-Path $dir 'tasks.md')); TASKS_TOTAL=$total; TASKS_PENDING=$pending; CHECKLIST_PENDING=$checkPending } | ConvertTo-Json -Compress

