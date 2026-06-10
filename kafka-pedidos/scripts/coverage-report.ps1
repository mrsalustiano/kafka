param(
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
)

$services = @(
    "clientes-service",
    "produtos-service",
    "pedidos-service",
    "broker-service"
)

Write-Host "Executando testes e gerando relatorios JaCoCo..." -ForegroundColor Cyan

$results = @()

foreach ($service in $services) {
    $servicePath = Join-Path $ProjectRoot "servicos\$service"
    Write-Host "`n==> $service" -ForegroundColor Yellow

    Push-Location $servicePath
    try {
        mvn test verify -q
        if ($LASTEXITCODE -ne 0) {
            throw "Falha em mvn test verify (exit $LASTEXITCODE)"
        }
    } finally {
        Pop-Location
    }

    $csvPath = Join-Path $servicePath "target\site\jacoco\jacoco.csv"
    if (-not (Test-Path $csvPath)) {
        throw "Relatorio JaCoCo nao encontrado: $csvPath"
    }

    $rows = Import-Csv $csvPath
    $lineMissed = ($rows | Measure-Object -Property LINE_MISSED -Sum).Sum
    $lineCovered = ($rows | Measure-Object -Property LINE_COVERED -Sum).Sum
    $branchMissed = ($rows | Measure-Object -Property BRANCH_MISSED -Sum).Sum
    $branchCovered = ($rows | Measure-Object -Property BRANCH_COVERED -Sum).Sum

    $appMissed = ($rows | Where-Object { $_.CLASS -like "*Application" } | Measure-Object -Property LINE_MISSED -Sum).Sum
    if ($null -eq $appMissed) { $appMissed = 0 }
    $appCovered = ($rows | Where-Object { $_.CLASS -like "*Application" } | Measure-Object -Property LINE_COVERED -Sum).Sum
    if ($null -eq $appCovered) { $appCovered = 0 }

    $lineTotal = $lineMissed + $lineCovered - $appMissed - $appCovered
    $lineHit = $lineCovered - $appCovered
    $linePct = if ($lineTotal -gt 0) { [math]::Round(($lineHit / $lineTotal) * 100, 2) } else { 0 }

    $branchTotal = $branchMissed + $branchCovered
    $branchPct = if ($branchTotal -gt 0) { [math]::Round(($branchCovered / $branchTotal) * 100, 2) } else { 100 }

    $reportHtml = Join-Path $servicePath "target\site\jacoco\index.html"

    $results += [PSCustomObject]@{
        Service = $service
        LineCoverage = "$linePct%"
        BranchCoverage = "$branchPct%"
        LinesCovered = $lineHit
        LinesTotal = $lineTotal
        BranchesCovered = $branchCovered
        BranchesTotal = $branchTotal
        Report = $reportHtml
        Status = if ($linePct -ge 95) { "OK" } else { "FAIL" }
    }
}

Write-Host "`n================ RELATORIO JACOCO ================" -ForegroundColor Green
$results | Format-Table Service, LineCoverage, BranchCoverage, LinesCovered, LinesTotal, Status -AutoSize

foreach ($result in $results) {
    Write-Host "Relatorio HTML: $($result.Report)"
}

$failed = $results | Where-Object { $_.Status -eq "FAIL" }
if ($failed.Count -gt 0) {
    Write-Host "`nCobertura abaixo de 95% em: $($failed.Service -join ', ')" -ForegroundColor Red
    exit 1
}

Write-Host "`nTodos os servicos atingiram cobertura minima de 95% (linhas)." -ForegroundColor Green
exit 0
