param([switch]$AcceptEula)
$ErrorActionPreference = 'Stop'
if (-not $AcceptEula) { throw 'Read https://aka.ms/MinecraftEULA and pass -AcceptEula only after agreeing.' }
if (-not $env:JAVA_HOME) { throw 'Set JAVA_HOME to JDK 25 first.' }
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$testId = 'equipment-test-' + [DateTime]::Now.ToString('yyyyMMdd-HHmmss') + '-' + [Guid]::NewGuid().ToString('N').Substring(0,6)
$testRoot = Join-Path $projectRoot "run/$testId"
$evidence = Join-Path $projectRoot ".gradle/$testId"
$utf8 = [Text.UTF8Encoding]::new($false)
New-Item -ItemType Directory -Force "$testRoot/config/buildupmobtweaks", $evidence | Out-Null
[IO.File]::WriteAllText("$testRoot/eula.txt", "eula=true`n", $utf8)
[IO.File]::WriteAllText("$testRoot/server.properties", @"
server-ip=127.0.0.1
server-port=25587
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
[IO.File]::WriteAllText($configPath, "version = 2`n[general]`nenabled = true`ndiagnosticProbe = false`n[performance]`ndiagnosticLines = 4`n[traits]`ncommonMarker = false`n", $utf8)
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
            if ($step -is [scriptblock]) { & $step; continue }
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
if ($config -notmatch 'version = 8' -or $config -notmatch 'diagnosticProbe = false' -or
    $config -notmatch 'diagnosticLines = 4' -or $config -notmatch 'commonMarker = false') {
    throw 'Config version 2 -> 8 did not preserve old values'
}
$config = $config.Replace('assignmentChance = 100', 'assignmentChance = 1000')
[IO.File]::WriteAllText($configPath, $config, $utf8)
$packRoot = "$testRoot/world/datapacks/bmt_test"
Copy-Item -LiteralPath "$projectRoot/examples/equipment-demo" -Destination $packRoot -Recurse
$poolPath = "$packRoot/data/buildupmobtweaks/buildupmobtweaks/equipment_pool/zombie_demo.json"
$itemTagPath = "$packRoot/data/buildupmobtweaks/tags/item/demo_equipment.json"
$validPool = [IO.File]::ReadAllText($poolPath)
$otherPoolPath = "$packRoot/data/buildupmobtweaks/buildupmobtweaks/equipment_pool/zz_valid_husk.json"
$otherPool = '{"version":1,"entities":["minecraft:husk"],"slot":"mainhand","entries":[{"item":"minecraft:wooden_sword","weight":1}]}'
function Write-ItemTag([string]$item) {
    [IO.File]::WriteAllText($itemTagPath, ('{"replace":true,"values":["' + $item + '"]}'), $utf8)
}
Invoke-TestServer 'live-reloads' @(
    'forceload add 16000 16000', 3,
    'fill 16004 80 16004 16012 80 16012 minecraft:stone',
    'buildupmobtweaks equipment_pools',
    'say S1C_CASE baseline',
    'summon minecraft:zombie 16008 81 16008 {Tags:["s1c_old"],NoAI:1b,Invulnerable:1b,PersistenceRequired:1b}',
    'buildupmobtweaks equipment @e[tag=s1c_old,limit=1]',
    { Write-ItemTag 'minecraft:bow' }, 'reload', 8,
    'say S1C_CASE tag_changed',
    'buildupmobtweaks equipment_pools',
    'summon minecraft:zombie 16008 81 16008 {Tags:["s1c_new"],NoAI:1b,Invulnerable:1b,PersistenceRequired:1b}',
    'buildupmobtweaks equipment @e[tag=s1c_new,limit=1]',
    'buildupmobtweaks equipment @e[tag=s1c_old,limit=1]',
    { [IO.File]::WriteAllText($poolPath, $validPool.Replace('#buildupmobtweaks:demo_equipment','missing:weapon'), $utf8); [IO.File]::WriteAllText($otherPoolPath, $otherPool, $utf8) },
    'reload', 8, 'say S1C_CASE bad_id', 'buildupmobtweaks equipment_pools',
    'summon minecraft:zombie 16008 81 16008 {Tags:["s1c_bad"],NoAI:1b,Invulnerable:1b,PersistenceRequired:1b}',
    'buildupmobtweaks equipment @e[tag=s1c_bad,limit=1]',
    'summon minecraft:husk 16008 81 16008 {Tags:["s1c_valid_other"],NoAI:1b,Invulnerable:1b,PersistenceRequired:1b}',
    'buildupmobtweaks equipment @e[tag=s1c_valid_other,limit=1]',
    { [IO.File]::WriteAllText($poolPath, '{', $utf8) },
    'reload', 8, 'say S1C_CASE malformed', 'buildupmobtweaks equipment_pools',
    { [IO.File]::WriteAllText($poolPath, $validPool, $utf8); [IO.File]::WriteAllText($itemTagPath, '{"replace":true,"values":[]}', $utf8) },
    'reload', 8, 'say S1C_CASE empty_tag', 'buildupmobtweaks equipment_pools',
    { Write-ItemTag 'minecraft:trident' },
    'reload', 8, 'say S1C_CASE restored', 'buildupmobtweaks equipment_pools',
    'summon minecraft:zombie 16008 81 16008 {Tags:["s1c_restored"],NoAI:1b,Invulnerable:1b,PersistenceRequired:1b}',
    'buildupmobtweaks equipment @e[tag=s1c_restored,limit=1]',
    'buildupmobtweaks equipment @e[tag=s1c_bad,limit=1]',
    'datapack disable "file/bmt_test"', 8,
    'say S1C_CASE disabled_pack', 'buildupmobtweaks equipment_pools',
    'summon minecraft:zombie 16008 81 16008 {Tags:["s1c_default"],NoAI:1b,Invulnerable:1b,PersistenceRequired:1b}',
    'buildupmobtweaks equipment @e[tag=s1c_default,limit=1]',
    'buildupmobtweaks equipment @e[tag=s1c_old,limit=1]',
    'save-all flush', 'forceload remove 16000 16000', 40,
    'buildupmobtweaks equipment @e[tag=s1c_old,limit=1]',
    'forceload add 16000 16000', 3,
    'buildupmobtweaks equipment @e[tag=s1c_old,limit=1]', 'save-all flush'
)
$config = [IO.File]::ReadAllText($configPath).Replace('assignmentChance = 1000', 'assignmentChance = 0')
[IO.File]::WriteAllText($configPath, $config, $utf8)
Invoke-TestServer 'restart-zero-chance' @('forceload add 16000 16000', 3,
    'buildupmobtweaks equipment @e[tag=s1c_old,limit=1]', 'save-all flush')
$live = [IO.File]::ReadAllText("$evidence/live-reloads-latest.log")
$restart = [IO.File]::ReadAllText("$evidence/restart-zero-chance-latest.log")
function Case-Text([string]$name) {
    $match = [regex]::Match($live, ('(?s)S1C_CASE ' + $name + '\r?\n(.*?)(?=S1C_CASE |\z)'))
    if (-not $match.Success) { throw "Missing case $name" }
    return $match.Groups[1].Value
}
foreach ($case in @('bad_id','malformed','empty_tag')) {
    if ((Case-Text $case) -notmatch 'Equipment pools: active=\[buildupmobtweaks:zz_valid_husk\]; rejected=1') { throw "Invalid rule not safely disabled: $case" }
}
if ((Case-Text 'baseline') -notmatch 'off=\[SHIELD\]' -or (Case-Text 'tag_changed') -notmatch 'off=\[BOW\]' -or
    (Case-Text 'restored') -notmatch 'off=\[TRIDENT\]' -or (Case-Text 'disabled_pack') -notmatch 'main=\[MELEE\]') {
    throw "Pack replacement/tag reload/restore/disable did not change new equipment; see $evidence"
}
if ((Case-Text 'bad_id') -notmatch 'pool:"buildupmobtweaks:zz_valid_husk"' -or
    (Case-Text 'disabled_pack') -notmatch 'item:"minecraft:wooden_sword"') {
    throw 'Unrelated valid rule or restored built-in rule did not equip correctly'
}
if ((Case-Text 'restored') -notmatch 'outcome:"no_rule"' -or $live -notmatch 'No entity was found') {
    throw 'Missing no-backfill or real-unload observation'
}
$pattern = 'Equipment for ([0-9a-f-]+): saved=([^\r\n]+); abilities=([^\r\n]+)'
$baseline = [regex]::Match((Case-Text 'baseline'), $pattern)
$all = [regex]::Matches($live + "`n" + $restart, $pattern) | Where-Object { $_.Groups[1].Value -eq $baseline.Groups[1].Value }
if (-not $baseline.Success -or $all.Count -ne 5) { throw "Expected five baseline observations; got $($all.Count)" }
foreach ($match in $all) { if ($match.Value -ne $baseline.Value) { throw 'Old equipment changed or rerolled' } }
foreach ($errorKind in @('Unknown ID: missing:weapon','Empty tag: #buildupmobtweaks:demo_equipment','Equipment pool rejected:')) {
    if (-not $live.Contains($errorKind)) { throw "Missing precise validation error: $errorKind" }
}
Write-Host 'PASS: config migration, pack/tag replacement, invalid fallback, pack disable, no backfill, unload and restart.'
Write-Host $baseline.Value
Write-Host "Evidence: $evidence"
Write-Host "Isolated test world retained: $testRoot"