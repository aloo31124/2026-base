[CmdletBinding()]
param(
    [string]$GitHubUrl = 'https://github.com/aloo31124/2026-base.git',
    [string]$GitLabUrl = 'https://gitlab.com/aloo31124/2026-base.git',
    [string]$PrimaryRemote = 'origin',
    [string]$MirrorRemote = 'gitlab'
)

$ErrorActionPreference = 'Stop'

function Invoke-Git {
    param(
        [Parameter(Mandatory)]
        [string[]]$Arguments
    )

    & git @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "git $($Arguments -join ' ') failed with exit code $LASTEXITCODE."
    }
}

function Test-GitRemote {
    param(
        [Parameter(Mandatory)]
        [string]$Name
    )

    $remoteNames = @(& git remote)
    if ($LASTEXITCODE -ne 0) {
        throw 'Unable to list Git remotes.'
    }

    return $remoteNames -contains $Name
}

Invoke-Git -Arguments @('rev-parse', '--show-toplevel')

if (Test-GitRemote -Name $PrimaryRemote) {
    Invoke-Git -Arguments @('remote', 'set-url', $PrimaryRemote, $GitHubUrl)
}
else {
    Invoke-Git -Arguments @('remote', 'add', $PrimaryRemote, $GitHubUrl)
}

if (Test-GitRemote -Name $MirrorRemote) {
    Invoke-Git -Arguments @('remote', 'set-url', $MirrorRemote, $GitLabUrl)
}
else {
    Invoke-Git -Arguments @('remote', 'add', $MirrorRemote, $GitLabUrl)
}

Invoke-Git -Arguments @('config', 'remote.pushDefault', $PrimaryRemote)

$currentBranch = (& git branch --show-current).Trim()
if ($LASTEXITCODE -ne 0) {
    throw 'Unable to determine the current branch.'
}

if ($currentBranch) {
    Invoke-Git -Arguments @('config', "branch.$currentBranch.remote", $PrimaryRemote)
    Invoke-Git -Arguments @('config', "branch.$currentBranch.merge", "refs/heads/$currentBranch")
}

Write-Host 'Git remotes configured:'
Write-Host "  Primary: $PrimaryRemote -> $GitHubUrl"
Write-Host "  Mirror:  $MirrorRemote -> $GitLabUrl"
if ($currentBranch) {
    Write-Host "  Branch:  $currentBranch (tracks $PrimaryRemote/$currentBranch)"
}
Write-Host ''
Invoke-Git -Arguments @('remote', '-v')
