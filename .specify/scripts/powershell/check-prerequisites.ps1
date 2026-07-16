param([switch]$Json, [switch]$PathsOnly, [switch]$RequireTasks, [switch]$IncludeTasks, [string]$FeatureDir)
$root = (Resolve-Path (Join-Path $PSScriptRoot '..\..\..')).Path
if (-not $FeatureDir) {
    $featureConfig = Get-Content -Raw (Join-Path $root '.specify\feature.json') | ConvertFrom-Json
    $FeatureDir = $featureConfig.feature_directory
}
$absolute = (Resolve-Path (Join-Path $root $FeatureDir)).Path
$docs = Get-ChildItem -File $absolute | Select-Object -ExpandProperty Name
$result = [ordered]@{ FEATURE_DIR=$absolute; FEATURE_SPEC=(Join-Path $absolute 'spec.md'); IMPL_PLAN=(Join-Path $absolute 'plan.md'); TASKS=(Join-Path $absolute 'tasks.md'); AVAILABLE_DOCS=$docs }
if ($RequireTasks -and -not (Test-Path $result.TASKS)) { throw "tasks.md 不存在：$absolute" }
if ($Json -or $PathsOnly) { $result | ConvertTo-Json -Compress } else { $result }

