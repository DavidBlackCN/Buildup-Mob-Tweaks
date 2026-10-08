param([switch]$AcceptEula)
$ErrorActionPreference = 'Stop'
if (-not $AcceptEula) { throw 'Read https://aka.ms/MinecraftEULA and pass -AcceptEula only after agreeing.' }
if (-not $env:JAVA_HOME) { throw 'Set JAVA_HOME to JDK 25 first.' }
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$testId = 's2-test-' + [DateTime]::Now.ToString('yyyyMMdd-HHmmss') + '-' + [Guid]::NewGuid().ToString('N').Substring(0,6)
$testRoot = Join-Path $projectRoot "run/$testId"
$evidence = Join-Path $projectRoot ".gradle/$testId"
$utf8 = [Text.UTF8Encoding]::new($false)
New-Item -ItemType Directory -Force "$testRoot/config/buildupmobtweaks", $evidence | Out-Null
[IO.File]::WriteAllText("$testRoot/eula.txt", "eula=true`n", $utf8)
[IO.File]::WriteAllText("$testRoot/server.properties", @"
server-ip=127.0.0.1
server-port=25592
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
[IO.File]::WriteAllText($configPath, "version = 7`n[general]`nenabled = true`ndiagnosticProbe = false`n[performance]`ndiagnosticLines = 3`n", $utf8)
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
$config=Get-Content $configPath -Raw
if($config -notmatch 'version = 8' -or $config -notmatch 'diagnosticProbe = false' -or $config -notmatch 'diagnosticLines = 3' -or $config -notmatch 'evoker_totem = true' -or $config -notmatch 'evoker_totem_chance = 10' -or $config -notmatch 'skeleton_aim_fix = true'){
    throw 'Config v7 -> v8 failed to retain old values and add S2 defaults'
}
Invoke-TestServer 'unload-reload' @(
    'gamerule minecraft:mob_griefing false',
    'forceload add 16000 16000', 'forceload add 16512 16000', 3,
    'fill 16004 80 16004 16012 80 16012 minecraft:stone',
    'summon minecraft:evoker 16008 81 16008 {UUID:[I;1,2,3,4],Tags:["bmt_s2_owner"],NoAI:1b,Invulnerable:1b,PersistenceRequired:1b,"fabric:attachments":{"buildupmobtweaks:major_trait":{version:1,trait:"buildupmobtweaks:evoker_fireball",origin:"rolled",exclusive_group:"major_attack"},"buildupmobtweaks:hostile_combat":{version:1,major_next:987654321L}}}',
    'summon minecraft:vex 16520 81 16008 {Tags:["bmt_s2_vex"],NoAI:1b,Invulnerable:1b,PersistenceRequired:1b,owner:[I;1,2,3,4]}',
    'summon minecraft:vex 16521 81 16008 {Tags:["bmt_s2_vex"],NoAI:1b,Invulnerable:1b,PersistenceRequired:1b,owner:[I;1,2,3,4]}',
    'summon minecraft:vex 16522 81 16008 {Tags:["bmt_s2_vex"],NoAI:1b,Invulnerable:1b,PersistenceRequired:1b,owner:[I;1,2,3,4]}',
    'buildupmobtweaks s2 @e[tag=bmt_s2_owner,limit=1]',
    'save-all flush', 'forceload remove 16512 16000', 40,
    'data get entity @e[tag=bmt_s2_vex,limit=1] UUID',
    'buildupmobtweaks s2 @e[tag=bmt_s2_owner,limit=1]',
    'forceload add 16512 16000', 3,
    'buildupmobtweaks s2 @e[tag=bmt_s2_owner,limit=1]',
    'buildupmobtweaks feature evoker_fireball', 'save-all flush'
)
$config=Get-Content $configPath -Raw
$config=$config.Replace('evoker_fireball = true','evoker_fireball = false').Replace('skeleton_aim_fix = true','skeleton_aim_fix = false')
[IO.File]::WriteAllText($configPath,$config,$utf8)
Invoke-TestServer 'restart-disabled' @(
    'forceload add 16000 16000', 'forceload add 16512 16000', 3,
    'buildupmobtweaks s2 @e[tag=bmt_s2_owner,limit=1]',
    'buildupmobtweaks feature evoker_fireball','buildupmobtweaks feature skeleton_aim_fix',
    'kill @e[tag=bmt_s2_vex]', 'buildupmobtweaks s2 @e[tag=bmt_s2_owner,limit=1]', 'save-all flush'
)
$first=Get-Content "$evidence/unload-reload-latest.log" -Raw
$second=Get-Content "$evidence/restart-disabled-latest.log" -Raw
$pattern='S2 for ([0-9a-f-]+): cooldowns=(.*?); major=(.*?); vex_owned=(\d+)'
$observed=[regex]::Matches($first,$pattern);$restarted=[regex]::Matches($second,$pattern)
if($observed.Count-ne3 -or $restarted.Count-ne2 -or $first-notmatch'No entity was found'){throw "Missing real unload/reload/restart observations: $evidence"}
foreach($row in @($observed[0],$observed[1],$observed[2],$restarted[0])){
    if($row.Value-ne$observed[0].Value -or $row.Groups[2].Value-notmatch'987654321L' -or $row.Groups[3].Value-notmatch'evoker_fireball' -or $row.Groups[4].Value-ne'3'){
        throw "Saved traits, cooldowns or three ownership slots changed: $evidence"
    }
}
if($restarted[1].Groups[4].Value-ne'0' -or $first-notmatch'buildupmobtweaks:evoker_fireball=true' -or $second-notmatch'buildupmobtweaks:evoker_fireball=false' -or $second-notmatch'buildupmobtweaks:skeleton_aim_fix=false'){
    throw "Destroyed minions did not release slots or independent disabled gates failed: $evidence"
}
Write-Host 'PASS: v7 -> v8 preserves old values; actual unload/reload/restart retains major trait, nonzero cooldown and three globally registered vex slots; destruction releases slots; two gates disabled.'
Write-Host $observed[0].Value;Write-Host $restarted[1].Value
Write-Host "Evidence: $evidence"
Write-Host "Isolated test world retained: $testRoot"
