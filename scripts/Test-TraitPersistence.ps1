param([switch]$AcceptEula)
$ErrorActionPreference = 'Stop'
if (-not $AcceptEula) { throw 'Read https://aka.ms/MinecraftEULA and pass -AcceptEula only after agreeing.' }
if (-not $env:JAVA_HOME) { throw 'Set JAVA_HOME to JDK 25 first.' }
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$testId = 'trait-test-' + [DateTime]::Now.ToString('yyyyMMdd-HHmmss') + '-' + [Guid]::NewGuid().ToString('N').Substring(0,6)
$testRoot = Join-Path $projectRoot "run/$testId"
$evidence = Join-Path $projectRoot ".gradle/$testId"
$utf8 = [Text.UTF8Encoding]::new($false)
New-Item -ItemType Directory -Force "$testRoot/config/buildupmobtweaks", $evidence | Out-Null
[IO.File]::WriteAllText("$testRoot/eula.txt", "eula=true`n", $utf8)
[IO.File]::WriteAllText("$testRoot/server.properties", @"
server-ip=127.0.0.1
server-port=25586
level-name=world
level-type=minecraft:flat
generate-structures=false
gamemode=creative
view-distance=2
simulation-distance=2
pause-when-empty-seconds=0
max-players=2
"@, $utf8)
$configPath = "$testRoot/config/buildupmobtweaks/main.toml"
[IO.File]::WriteAllText($configPath, "version = 1`n[general]`nenabled = true`ndiagnosticProbe = false`n[performance]`ndiagnosticLines = 3`n", $utf8)
$groovyRoot = $testRoot.Replace('\','/').Replace("'", "\'")
$initFile = "$evidence/server-input.gradle"
[IO.File]::WriteAllText($initFile, "gradle.projectsEvaluated { rootProject.loom.runs.named('server') { runDir('$groovyRoot') }; rootProject.tasks.named('runServer') { standardInput = System.in } }", $utf8)

function Invoke-TestServer([string]$case, [object[]]$steps) {
    Write-Host "Starting $case in $testRoot"
    $info = [Diagnostics.ProcessStartInfo]::new()
    $info.FileName = $env:ComSpec
    $info.Arguments = '/d /c .\gradlew.bat --console=plain --no-configuration-cache -I "' + $initFile + '" runServer --args=nogui'
    $info.WorkingDirectory = $projectRoot
    $info.UseShellExecute = $false
    $info.CreateNoWindow = $true
    $info.RedirectStandardInput = $true
    $info.RedirectStandardOutput = $true
    $info.RedirectStandardError = $true
    $info.StandardInputEncoding = $utf8
    $started = [DateTime]::UtcNow
    $process = [Diagnostics.Process]::Start($info)
    $outTask = $process.StandardOutput.ReadToEndAsync()
    $errTask = $process.StandardError.ReadToEndAsync()
    $log = "$testRoot/logs/latest.log"
    $ready = $false
    try {
        for ($i = 0; $i -lt 180; $i++) {
            if ($process.HasExited) { break }
            if ((Test-Path $log) -and (Get-Item $log).LastWriteTimeUtc -gt $started -and
                (Get-Content $log -Raw -ErrorAction SilentlyContinue) -match 'Done \(') { $ready = $true; break }
            Start-Sleep -Seconds 1
        }
        if (-not $ready) { throw "$case never reached Done" }
        Start-Sleep -Seconds 2
        foreach ($step in $steps) {
            if ($step -is [int]) { Start-Sleep -Seconds $step; continue }
            $process.StandardInput.WriteLine([string]$step)
            $process.StandardInput.Flush()
            Start-Sleep -Seconds 1
        }
    } finally {
        if (-not $process.HasExited) {
            $process.StandardInput.WriteLine('stop')
            $process.StandardInput.Flush()
            if (-not $process.WaitForExit(30000)) { $process.Kill($true); $process.WaitForExit() }
        }
        [IO.File]::WriteAllText("$evidence/$case-console.log", $outTask.GetAwaiter().GetResult() + "`n" + $errTask.GetAwaiter().GetResult(), $utf8)
        if (Test-Path $log) { Copy-Item -LiteralPath $log -Destination "$evidence/$case-latest.log" }
    }
    if ($process.ExitCode -ne 0) { throw "$case failed: exit $($process.ExitCode); see $evidence" }
    Write-Host "${case}: server exited normally"
}

Invoke-TestServer 'migration' @('buildupmobtweaks status', 'save-all flush')
$config = Get-Content $configPath -Raw
if ($config -notmatch 'version = 5' -or $config -notmatch 'diagnosticProbe = false' -or $config -notmatch 'diagnosticLines = 3') {
    throw 'Config version 1 -> 5 did not preserve old values'
}
$config = [regex]::Replace($config, '(?ms)^\[traits\.cow\].*?(?=^\[|\z)', "[traits.cow]`ncommon = 0`nadvanced = 0`nrare = 1000`n`n")
[IO.File]::WriteAllText($configPath, $config, $utf8)
Invoke-TestServer 'unload-reload' @(
    'forceload add 16000 16000', 3,
    'fill 16004 80 16004 16012 80 16012 minecraft:stone',
    'summon minecraft:cow 16008 81 16008 {Tags:["bmt_s1b"],NoAI:1b,Invulnerable:1b,PersistenceRequired:1b}',
    'buildupmobtweaks traits @e[tag=bmt_s1b,limit=1]',
    'save-all flush', 'forceload remove 16000 16000', 40,
    'buildupmobtweaks traits @e[tag=bmt_s1b,limit=1]',
    'forceload add 16000 16000', 3,
    'buildupmobtweaks traits @e[tag=bmt_s1b,limit=1]', 'save-all flush'
)
$config = Get-Content $configPath -Raw
$config = [regex]::Replace($config, '(?ms)^\[traits\.cow\].*?(?=^\[|\z)', "[traits.cow]`ncommon = 0`nadvanced = 0`nrare = 0`n`n")
[IO.File]::WriteAllText($configPath, $config, $utf8)
Invoke-TestServer 'restart-zero-chance' @('forceload add 16000 16000', 3, 'buildupmobtweaks traits @e[tag=bmt_s1b,limit=1]', 'save-all flush')
$first = Get-Content "$evidence/unload-reload-latest.log" -Raw
$second = Get-Content "$evidence/restart-zero-chance-latest.log" -Raw
$pattern = 'Traits for ([0-9a-f-]+): saved=(.*?); active=([^\r\n]+)'
$observed = [regex]::Matches($first, $pattern)
$restarted = [regex]::Matches($second, $pattern)
if ($observed.Count -ne 2 -or $restarted.Count -ne 1 -or $first -notmatch 'No entity was found') {
    throw "Missing actual unload/reload/restart observations; inspect $evidence"
}
if ($observed[0].Value -ne $observed[1].Value -or $observed[0].Value -ne $restarted[0].Value -or
    $observed[0].Value -notmatch 'demo_rare') { throw "Saved trait changed; inspect $evidence" }
Write-Host 'PASS: version migration, confirmed entity unload, reload and restart with changed probability.'
Write-Host $observed[0].Value
Write-Host "Evidence: $evidence"
Write-Host "Isolated test world retained: $testRoot"
