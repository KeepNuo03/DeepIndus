[CmdletBinding()]
param(
    [string]$BaseUrl = "http://localhost:8000",
    [string]$Token = "",
    [string]$ClientType = "pc",
    [int]$SinceHours = 24,
    [int]$MaxDelete = 200,
    [string[]]$TitleKeywords = @("new", "please", "test", "regression"),
    [switch]$AllRecent,
    [switch]$Execute
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($Token) -and -not [string]::IsNullOrWhiteSpace($env:AGENT_E2E_TOKEN)) {
    $Token = $env:AGENT_E2E_TOKEN
}

if ([string]::IsNullOrWhiteSpace($Token)) {
    throw "Missing JWT token. Pass -Token or set AGENT_E2E_TOKEN."
}

$headers = @{
    "Authorization" = "Bearer $Token"
    "X-Client-Type" = $ClientType
    "Content-Type"  = "application/json; charset=utf-8"
}

function Get-AllSessions {
    $page = 1
    $pageSize = 100
    $all = New-Object System.Collections.Generic.List[object]
    while ($true) {
        $uri = "${BaseUrl}/v1/agent/sessions?page=$page&pageSize=$pageSize"
        $resp = Invoke-RestMethod -Uri $uri -Method Get -Headers $headers -TimeoutSec 30
        if ($resp.code -ne 200 -or $null -eq $resp.data) {
            throw "Failed to fetch sessions. code=$($resp.code) message=$($resp.message)"
        }
        $items = @()
        if ($resp.data.items) { $items = $resp.data.items }
        foreach ($it in $items) { $all.Add($it) | Out-Null }

        if ($items.Count -lt $pageSize) { break }
        $page++
        if ($page -gt 50) { break }
    }
    return $all
}

function Match-Title([string]$title, [string[]]$keywords) {
    if ([string]::IsNullOrWhiteSpace($title)) { return $false }
    $t = $title.ToLowerInvariant()
    foreach ($kw in $keywords) {
        if ([string]::IsNullOrWhiteSpace($kw)) { continue }
        if ($t.Contains($kw.ToLowerInvariant())) { return $true }
    }
    return $false
}

function Parse-Time([string]$timeText) {
    try {
        return [DateTimeOffset]::Parse($timeText)
    } catch {
        return $null
    }
}

$sessions = Get-AllSessions
$cutoff = [DateTimeOffset]::Now.AddHours(-1 * [Math]::Abs($SinceHours))

$candidates = @()
foreach ($s in $sessions) {
    $last = Parse-Time([string]$s.lastMessageAt)
    if ($null -eq $last) { continue }
    if ($last -lt $cutoff) { continue }

    $isMatch = $AllRecent.IsPresent -or (Match-Title -title ([string]$s.title) -keywords $TitleKeywords)
    if ($isMatch) { $candidates += $s }
}

$candidates = $candidates | Select-Object -First $MaxDelete

Write-Host "Found candidate sessions: $($candidates.Count)" -ForegroundColor Cyan
if ($candidates.Count -eq 0) { exit 0 }

$candidates |
    Select-Object sessionId, title, lastMessageAt, clientType |
    Format-Table -AutoSize

if (-not $Execute.IsPresent) {
    Write-Host ""
    Write-Host "Preview only. Re-run with -Execute to archive/delete these sessions." -ForegroundColor Yellow
    exit 0
}

$ok = 0
$fail = 0
foreach ($s in $candidates) {
    try {
        $sid = [Uri]::EscapeDataString([string]$s.sessionId)
        $uri = "${BaseUrl}/v1/agent/sessions/$sid"
        $resp = Invoke-RestMethod -Uri $uri -Method Delete -Headers $headers -TimeoutSec 30
        if ($resp.code -eq 200) {
            $ok++
            Write-Host "[OK] $($s.sessionId)" -ForegroundColor Green
        } else {
            $fail++
            Write-Host "[FAIL] $($s.sessionId) code=$($resp.code)" -ForegroundColor Red
        }
    } catch {
        $fail++
        Write-Host "[FAIL] $($s.sessionId) -> $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Cleanup finished: OK=$ok FAIL=$fail" -ForegroundColor Yellow
if ($fail -gt 0) { exit 1 }
exit 0
