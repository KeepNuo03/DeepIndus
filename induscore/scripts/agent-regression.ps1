[CmdletBinding()]
param(
    [string]$BaseUrl = "http://localhost:8000",
    [string]$Token = "",
    [string]$ClientType = "pc",
    [int]$TimeoutSec = 70
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($Token)) {
    if (-not [string]::IsNullOrWhiteSpace($env:AGENT_E2E_TOKEN)) {
        $Token = $env:AGENT_E2E_TOKEN
    }
}

if ([string]::IsNullOrWhiteSpace($Token)) {
    throw "Missing JWT token. Pass -Token or set AGENT_E2E_TOKEN."
}

$headers = @{
    "Authorization" = "Bearer $Token"
    "X-Client-Type" = $ClientType
    "Content-Type"  = "application/json; charset=utf-8"
}

function Invoke-AgentChat {
    param(
        [string]$Scene,
        [string]$Question,
        [hashtable]$PageParams,
        [string[]]$ExpectedTools
    )

    $body = @{
        sessionId = $null
        messages  = @(
            @{
                role    = "user"
                content = $Question
            }
        )
        context   = @{
            scene      = $Scene
            page       = "regression/$Scene"
            pageParams = $PageParams
            taskId     = $null
        }
        options   = @{
            stream      = $false
            temperature = 0.2
            maxTokens   = 1024
        }
        stream    = $false
    } | ConvertTo-Json -Depth 8

    $uri = "$BaseUrl/v1/agent/chat"
    $resp = Invoke-RestMethod -Uri $uri -Method Post -Headers $headers -Body $body -TimeoutSec $TimeoutSec
    if ($resp.code -ne 200) {
        throw "Non-200 response. scene=$Scene code=$($resp.code) message=$($resp.message)"
    }
    if ($null -eq $resp.data -or [string]::IsNullOrWhiteSpace([string]$resp.data.answer)) {
        throw "Empty answer. scene=$Scene"
    }

    $calledTools = @()
    if ($resp.data.toolCalls) {
        foreach ($tc in $resp.data.toolCalls) {
            if ($null -ne $tc.tool) {
                $calledTools += [string]$tc.tool
            }
        }
    }

    foreach ($expected in $ExpectedTools) {
        if ($calledTools -notcontains $expected) {
            $actual = ($calledTools -join ", ")
            throw "Tool mismatch. scene=$Scene expected=$expected actual=[$actual]"
        }
    }

    return @{
        Scene = $Scene
        Trace = $resp.data.traceId
        Tools = ($calledTools -join ", ")
        Answer = [string]$resp.data.answer
    }
}

# Each case should hit key tools for one module.
$cases = @(
    @{
        Scene = "workbench"
        Question = "Please summarize current pending review, processing, and severe alerts on workbench."
        PageParams = @{}
        ExpectedTools = @("getMobileWorkbench")
    },
    @{
        Scene = "detection_records"
        Question = "Please analyze current detection records overview and review workload."
        PageParams = @{ page = 1; pageSize = 20; status = "todo" }
        ExpectedTools = @("getDetectionRecordsOverview", "getPilotMetrics")
    },
    @{
        Scene = "dashboard"
        Question = "Please summarize dashboard KPI, trend and alert conclusions."
        PageParams = @{}
        ExpectedTools = @("getDashboardKpi", "getDashboardDefectTrend", "getDashboardAlerts")
    },
    @{
        Scene = "production_line"
        Question = "Please analyze production line status and output risks."
        PageParams = @{}
        ExpectedTools = @("getProductionLinesOverview")
    },
    @{
        Scene = "model_mgmt"
        Question = "Please summarize AI model management status, main model and deployment status."
        PageParams = @{}
        ExpectedTools = @("getModelManagementOverview")
    }
)

$passed = 0
$failed = 0
$results = New-Object System.Collections.Generic.List[object]

Write-Host "Starting Agent cross-module regression. Cases=$($cases.Count)" -ForegroundColor Cyan

foreach ($c in $cases) {
    try {
        $r = Invoke-AgentChat -Scene $c.Scene -Question $c.Question -PageParams $c.PageParams -ExpectedTools $c.ExpectedTools
        $results.Add($r) | Out-Null
        $passed++
        Write-Host ("[PASS] {0} trace={1} tools={2}" -f $r.Scene, $r.Trace, $r.Tools) -ForegroundColor Green
    } catch {
        $failed++
        Write-Host ("[FAIL] {0} -> {1}" -f $c.Scene, $_.Exception.Message) -ForegroundColor Red
    }
}

Write-Host ""
Write-Host ("Regression finished: PASS={0} FAIL={1}" -f $passed, $failed) -ForegroundColor Yellow

if ($failed -gt 0) {
    exit 1
}

exit 0
