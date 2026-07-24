[CmdletBinding()]
param(
    [string]$Branch,
    [string]$PrimaryRemote = 'origin',
    [string]$MirrorRemote = 'gitlab',
    [switch]$PushTags
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

function Get-RemoteBranchCommit {
    param(
        [Parameter(Mandatory)]
        [string]$Remote,
        [Parameter(Mandatory)]
        [string]$BranchName
    )

    $result = & git ls-remote --exit-code $Remote "refs/heads/$BranchName"
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to read branch '$BranchName' from remote '$Remote'."
    }

    return ($result -split '\s+')[0]
}

Invoke-Git -Arguments @('rev-parse', '--show-toplevel')

if (-not $Branch) {
    $Branch = (& git branch --show-current).Trim()
    if ($LASTEXITCODE -ne 0 -or -not $Branch) {
        throw 'Unable to determine the current branch. Specify it with -Branch.'
    }
}

$localCommit = (& git rev-parse $Branch).Trim()
if ($LASTEXITCODE -ne 0) {
    throw "Local branch '$Branch' does not exist."
}

Write-Host "1/4 Push primary: $PrimaryRemote/$Branch"
Invoke-Git -Arguments @('push', '--set-upstream', $PrimaryRemote, $Branch)

Write-Host '2/4 Verify primary commit SHA'
$primaryCommit = Get-RemoteBranchCommit -Remote $PrimaryRemote -BranchName $Branch
if ($primaryCommit -ne $localCommit) {
    throw "Primary verification failed: local $localCommit, remote $primaryCommit."
}

Write-Host "3/4 Push mirror: $MirrorRemote/$Branch"
Invoke-Git -Arguments @('push', $MirrorRemote, $Branch)

Write-Host '4/4 Verify mirror commit SHA'
$mirrorCommit = Get-RemoteBranchCommit -Remote $MirrorRemote -BranchName $Branch
if ($mirrorCommit -ne $localCommit) {
    throw "Mirror verification failed: local $localCommit, remote $mirrorCommit."
}

if ($PushTags) {
    Write-Host 'Push tags to both remotes'
    Invoke-Git -Arguments @('push', $PrimaryRemote, '--tags')
    Invoke-Git -Arguments @('push', $MirrorRemote, '--tags')
}

Write-Host ''
Write-Host 'Dual-remote synchronization completed:'
Write-Host "  Local:  $Branch @ $localCommit"
Write-Host "  GitHub: $PrimaryRemote/$Branch @ $primaryCommit"
Write-Host "  GitLab: $MirrorRemote/$Branch @ $mirrorCommit"
