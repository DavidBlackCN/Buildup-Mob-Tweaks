param([switch]$AcceptEula)
$ErrorActionPreference = 'Stop'
if (-not $AcceptEula) { throw 'Read https://aka.ms/MinecraftEULA and pass -AcceptEula only after agreeing.' }
if (-not $env:JAVA_HOME) { throw 'Set JAVA_HOME to JDK 25 first.' }
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$testId = 'vex-test-' + [DateTime]::Now.ToString('yyyyMMdd-HHmmss') + '-' + [Guid]::NewGuid().ToString('N').Substring(0,6)
$testRoot = Join-Path $projectRoot "run/$testId"
$evidence = Join-Path $projectRoot ".gradle/$testId"
$utf8 = [Text.UTF8Encoding]::new($false)
New-Item -ItemType Directory -Force "$testRoot/config/buildupmobtweaks", $evidence | Out-Null
[IO.File]::WriteAllText("$testRoot/eula.txt", "eula=true`n", $utf8)
[IO.File]::WriteAllText("$testRoot/server.properties", @"
server-ip=127.0.0.1
server-port=25591
level-name=world
level-type=minecraft:flat
generator-settings={"layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains","features":false,"lakes":false}
generate-structures=false
gamemode=creative
view-distance=2
simulation-distance=2
pause-when-empty-seconds=0
max-players=2
"@, $utf8)
$configPath = "$testRoot/config/buildupmobtweaks/main.toml"
[IO.File]::WriteAllText($configPath, "version = 6`n[general]`nenabled = true`ndiagnosticProbe = false`n[performance]`ndiagnosticLines = 3`n", $utf8)
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
    if ((Get-Content "$evidence/$case-latest.log" -Raw) -match '\[.*?/ERROR\]|MixinApplyError|InvalidInjectionException') {
        throw "$case contains server errors; see $evidence"
    }
    Write-Host "${case}: server exited normally"
}

Invoke-TestServer 'migration' @('buildupmobtweaks status', 'save-all flush')
$config = Get-Content $configPath -Raw
if ($config -notmatch 'version = 8' -or $config -notmatch 'diagnosticProbe = false' -or $config -notmatch 'diagnosticLines = 3' -or
    $config -notmatch 'fixedCharge = true' -or $config -notmatch 'recoveryTicks = 20' -or $config -notmatch 'minimumChargeDistance = 3') {
    throw 'Config version 6 -> 8 did not preserve old values or add defaults'
}
Invoke-TestServer 'unload-reload' @(
    'gamerule minecraft:mob_griefing false',
    'forceload add 16000 16000', 3,
    'fill 16004 80 16004 16012 80 16012 minecraft:stone',
    'summon minecraft:vex 16008 81 16008 {Tags:["bmt_s2c2"],NoAI:1b,Invulnerable:1b,PersistenceRequired:1b}',
    'data modify entity @e[tag=bmt_s2c2,limit=1] "fabric:attachments"."buildupmobtweaks:vex_combat".recover_until set value 987654321L',
    'buildupmobtweaks vex @e[tag=bmt_s2c2,limit=1]',
    'save-all flush', 'forceload remove 16000 16000', 40,
    'buildupmobtweaks vex @e[tag=bmt_s2c2,limit=1]',
    'forceload add 16000 16000', 3,
    'buildupmobtweaks vex @e[tag=bmt_s2c2,limit=1]', 'save-all flush'
)
$config = Get-Content $configPath -Raw
foreach ($key in @('fixedCharge','recoveryPause','closeRangeGuard')) {
    $config = $config.Replace("$key = true", "$key = false")
}
[IO.File]::WriteAllText($configPath, $config, $utf8)
Invoke-TestServer 'restart-disabled' @('forceload add 16000 16000', 3, 'buildupmobtweaks vex @e[tag=bmt_s2c2,limit=1]', 'save-all flush')
$first = Get-Content "$evidence/unload-reload-latest.log" -Raw
$second = Get-Content "$evidence/restart-disabled-latest.log" -Raw
$pattern = 'Vex for ([0-9a-f-]+): saved=(.*?); gates=([^\r\n]+)'
$observed = [regex]::Matches($first, $pattern); $restarted = [regex]::Matches($second, $pattern)
if ($observed.Count -ne 2 -or $restarted.Count -ne 1 -or $first -notmatch 'No entity was found') {
    throw "Missing actual unload/reload/restart observations; inspect $evidence"
}
if ($observed[0].Value -ne $observed[1].Value -or $observed[0].Groups[1].Value -ne $restarted[0].Groups[1].Value -or
    $observed[0].Groups[2].Value -ne $restarted[0].Groups[2].Value -or $observed[0].Groups[2].Value -notmatch '987654321L' -or
    $observed[0].Groups[3].Value -match '=false' -or $restarted[0].Groups[3].Value -match '=true') {
    throw "Saved cooldown changed or disabled gates ineffective; inspect $evidence"
}
Write-Host 'PASS: v6 -> v8 preserves old values; actual unload/reload/restart preserves nonzero cooldown; three gates disabled.'
Write-Host $observed[0].Value; Write-Host $restarted[0].Value
Write-Host "Evidence: $evidence"
Write-Host "Isolated test world retained: $testRoot"
