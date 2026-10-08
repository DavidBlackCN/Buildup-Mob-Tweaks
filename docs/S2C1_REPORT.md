# S2-C1 — 袭击核心批次报告

> 历史批次报告，保留当时结果。当前 S2 实现与验证以 [S2_REPORT](S2_REPORT.md) 为准。

> 后续状态：用户已授权 S2-C2，见 [恼鬼冲刺批次报告](S2C2_REPORT.md)。下文保留 C1 当时范围与结果。

日期：2026-10-08（Asia/Shanghai）。基于 a3cc34b，开工时工作区干净。用户授权下一阶段 S2-C；按任务书「大规模功能必须分批」先完成袭击核心批次，停止等待本批验收。不是跨版本迁移，无上游实现复制。

## 本阶段完成情况

- 阶段/子批次：S2-C1 袭击核心；**本批 PASS（实现与自动验证），整个 S2-C 为 PARTIAL**。未开始 S2-D。
- TESTED（限定范围）：PILL-02 安全退让、PILL-03 已有弩斧切换、ILL-04 卫道士支援角色、EVOK-06 持久召唤冷却、WITCH-02 投药频率。
- IN_PROGRESS：EVOK-05 只完成 32 格内已加载自有恼鬼限制，不是跨区块全局硬上限；WITCH-01 仅实际药水粒子前摇，未实现手持模型；CORE-13 仅局部扫描节流/只读诊断，完整性能预算未完成。
- TODO 保留：PILL-01/04/05、ILL-01/02/03、EVOK-01/02/03/04、WITCH-03/04，以及 VEX、幻术师、末影人等剩余 S2-C 条目。图腾/火球等高影响招式未引入；后续实现仍需概率、次数、前摇与冷却验收。
- 七个独立功能默认开启，四个有范围限制的数值；配置 v6。这里没有新增概率强化，基础装备与角色策略不依赖 Traits。保持原版属性、装备池、Raid 生命周期及地形行为。

| 改动文件 | 原因 |
|---|---|
| combat/RaidCombat.java | 袭击四物种服务端策略、物品引用切换、持久冷却、有限查询与女巫前摇 |
| combat/SkeletonCombat.java | 仅将现有 safeStep 参数推广为 Mob，复用同一脚下安全检查；旧骷髅测试全部回归 |
| mixin/RaidMobMixin.java、PillagerCrossbowMixin.java、EvokerSummonMixin.java、WitchPotionMixin.java | 最小接入原版 AI tick、弩 Goal、恼鬼施法和药水工厂；目标 26.3 描述符已核对 |
| command/RaidCommands.java、BuildupMobTweaks.java | 注册策略及等级 4 的 raider 只读诊断 |
| config/BuildupConfig.java、feature/FeatureId.java、FeatureRegistry.java | v6、七门控、四个数值配置 |
| fabric.mod.json、主 Mixin JSON、中英文 JSON | 同步入口与实际功能范围，148 对语言键 |
| src/gametest/RaidTests.java、测试入口 JSON | 16 项新增战斗/生命周期/胜利结算验证；不进入正式 JAR |
| scripts/Test-RaidPersistence.ps1 | 独立服务端 v5→v6、非零冷却真实卸载重载/重启、七项禁用验证 |
| 原四个 Test-*.ps1 | 更新现版本断言为 6；本轮没有重跑这四个完整脚本 |
| AGENTS.md、README.md、docs/CONFIGURATION.md、S2B_REPORT.md、RAID_COMBAT.md、本报告、UPDATE_NOTES.md | 明确本批与 S2-C 剩余边界、行为契约、验证和发布说明 |
| 父目录 README.md、docs/FEATURE_MATRIX.md、docs/TEST_REPORT.md | 同步共享台账；位于 26.3 Git 根之外，不随本次 commit 保存 |

主 Java 前缀为 src/main/java/com/davidblackcn/buildupmobtweaks/，测试为 src/gametest/java/com/davidblackcn/buildupmobtweaks/。未改依赖、Wrapper、构建脚本、Access Widener、其他版本、LICENSE/NOTICE 或默认装备池。

## 验证记录

环境固定：Minecraft 26.3 / Oracle JDK 25.0.3（C:\Program Files\Java\jdk-25.0.3）/ Gradle 9.7.1 / Loom 1.18.3 / Loader 0.19.5 / Fabric API 0.162.0+26.3 / Fzzy 0.7.7+fix3+26.3 / Kotlin 1.14.1+kotlin.2.4.20。官方未混淆命名，无 Yarn。

| 命令/测试 | 环境 | 结果 | 证据 |
|---|---|---|---|
| `.\gradlew.bat build -PtestEula=true --console=plain` | 最终代码、资源与测试，指定 JDK25 | PASS，23s，exit 0，87 项必需 GameTest | .gradle/s2c/build-reviewed.log，15:45:45 All 87 required tests passed |
| `.\scripts\Test-RaidPersistence.ps1 -AcceptEula` | 唯一世界，127.0.0.1:25590；用户已接受 EULA | PASS，三次启动和正常 stop | .gradle/s2c/server.log；.gradle/raid-test-20261008-154343-871c5f/ |
| `.\gradlew.bat runClient --console=plain` | 开发客户端，无 Mod Menu | PASS（仅启动检查），正常关闭 exit 0 | .gradle/s2c/client.log，15:46:42 音频/图集完成，15:49:17 正常关闭；总运行 2m54s |
| `javap -c -p`：Pillager、RangedCrossbowAttackGoal、Evoker、召唤 Goal、Witch、Raid 等 | 实际解析 26.3 JAR | PASS，Mixin 签名/调用链核对；运行无注入异常 | .gradle/s2c/*.txt |
| JSON、双语键/占位符、PowerShell AST、`git diff --check`、JAR 许可/测试隔离、文档路径 | 完整改动 | PASS | 148 对语言键；生产 JAR 不包含测试类/场地 |
| 完整自然触发多波袭击、真实多人同步/战斗、GUI/药水粒子视觉、Mod Menu 新字段 | 人工环境 | NOT RUN | 受控最后一波和客户端启动不替代完整游戏验收 |
| 高密度 MSPT/TPS、领地/优化/AI Mod、复杂地形、全局恼鬼所有权、跨维度 | 补充场景 | NOT RUN | 不宣称普遍兼容或全局数量保证 |
| 原四个持久化完整脚本、干净 clone 无缓存、远端 CI | 补充场景 | NOT RUN（本轮） | 71 项旧 GameTest 已回归，历史脚本结果不计入本轮 |

87 项 = **16 项新增袭击测试 + 71 项既有测试**；Gradle test 为 NO-SOURCE，不冒称 JUnit。

新增用例包括七独立开关/Traits 解耦/总开关、同两把武器引用/装填组件/耐久/掉率/存档、真实弩 Goal 停止时保留目标、实际斧伤害、平地退让、缺口/未知武器/关闭边界、卫道士支援/不抢目标/只清除自身目标、恼鬼区分主人并预留整批、实际原版召唤三只并保存冷却、未知数据保留、女巫原版选药→前摇→同组件投射物、向低血量 Raider 投治疗药水、换目标/关闭取消、两开关解耦及加载取消前摇、实际近战后恢复弩射击。

### Raid 结束证据的准确范围

测试通过原版 Raid Codec 创建「最后一波已开始」夹具，加入四种真实 Raider，创建并占用 HOME POI。四实体通过实际 kill/死亡回调退出 Raid；getTotalRaidersAlive 从 4 变 0，随后原版 Raid.tick 达到 isVictory。没有改生产 Raid 类或通过测试专用 Mixin 强制胜利。此测试验证本批策略不阻断成员死亡和最后一波结算，**不覆盖不祥之兆、完整波次自然生成、多人奖励或所有村庄地形**。

### 服务端存档证据

- 世界 run/raid-test-20261008-154343-871c5f/；唤魔者 UUID 724e38d6-310b-4ba1-b795-06f9abb76e04。
- 用测试命令设置 next_summon_at=987654321L；15:44:26 记录，15:45:09 确认卸载后 No entity was found，15:45:14 重载完整数据保持；停服将七项开关 false，新 JVM 仍保持同 UUID 和非零截止时间，门控全部 false。
- 上述数值是持久化夹具，不是生产冷却。实际施法生成冷却另由 GameTest 验证。
- v5→v6 保留 diagnosticProbe=false、diagnosticLines=3，新增 vexLimit=6、summonCooldown=680 等默认值。
- 日志确认 mob_griefing=false；本批无方块写入或新增爆炸/火焰路径。该检查不代表已经测试全部领地或自动化场景。
- 开发客户端仅有既有 FabricMC token 的 Realms 授权提示，未测试 Realms。旧 ZombieTests 的 mock-player API 有上游弃用警告，未修改无关历史用例，不影响构建与运行。

### 失败与修正

首次编译的 ItemParticleOption 和测试 ChargedProjectiles 使用 ItemStack 参数，26.3 实际 API 要求 ItemStackTemplate；根据目标类签名修正。随后原 71 项、新增后 86 项、最终 87 项全部通过。没有移除依赖、关闭注入校验或删测试掩盖问题。审查将诊断读取待投药水改为不创建运行态附件。

最终 binary SHA256：20D1FAFD9E3364158445FA5871751FE38ADED0AC87FC243B40F4A513C1E6CC7E；sources：3A79201DAFB189A8332577F55FB6863196FD4A715357BE94B6AD5200398F3948。仅本地构建，不发布；日志、缓存和世界不提交。

## 风险与遗留

- 整个 S2-C 尚未完成；剩余条目保持 TODO/IN_PROGRESS，详见矩阵，不直接进入 S2-D。
- 恼鬼是局部加载范围限制；跨区块硬上限尚缺可靠所有权生命周期方案。药水为粒子预告，手持模型尚未实现，视觉可读性需人工判断。
- 不赠斧，不支持未经核对的 Mod 弩斧；不修改原版门破坏/掉落/袭击结构，不声称第三方 AI/领地兼容。现有武器掉率和组件保持，未知武器交回原处理。
- 新增扫描有返回数量上限与 20 tick 节流，没有全世界 tick 扫描；实际多人密度性能仍需量测。
- 本项目 MIT 不改变外部许可；上游 L1 直接复用边界仍待澄清，本轮独立实现、无代码或素材复制，未把发行 JAR 视作完整源码，LICENSE/NOTICE 无需变化。
- 父目录共享台账不在 26.3 Git 根内，须另行保存。

## 自检

Code Review: **PASS WITH RISKS**。

- Scope：本批全部代码、Mixin、配置/语言资源、测试、五个脚本、文档与共享台账。
- BLOCKER：无。MAJOR：无已知阻断缺陷。MINOR：无待修项。
- 原版 Goal/工厂接入点已核对并启动验证；common 不引用客户端类。未改永久属性、未注册额外物品/附魔、未复制装备，两个持久冷却与瞬态前摇分别管理。
- Wrapper build、87 GameTest、独立服务端、客户端启动、资源/差异/脚本/JAR 检查通过；未运行环境已列出。
- Update Notes：PASS，同提交追加实际已验证用户变化，不把剩余计划写成完成。

## 人工验收清单

- [ ] Fzzy 新分组中英文、七项开关/四个数值及 OP 权限、多人同步正常。
- [ ] 掠夺者近战/远程往返不丢目标和装备；平地后退、缺口/障碍边缘停止。
- [ ] 卫道士支援符合预期，关闭不干扰原版角色；唤魔者保留可读前摇，局部数量与冷却符合说明。
- [ ] 女巫粒子可辨识，敌方攻击和友方治疗均正常；换目标/关闭取消，无迟到药水或预览掉落。
- [ ] 单机及至少两名玩家专用服完整袭击从触发到胜利/奖励可结束；原版难度、村庄地形和 mobGriefing 两种值回归。
- [ ] 拟安装领地、优化与 AI Mod 各自启动/战斗验证；记录高密度性能。

## 下一阶段建议

- 下一任务块：S2-C 后续批次，继续剩余主要敌对生物与高影响技能；先确定图腾/火球等的独立概率、次数、前摇、冷却与保护边界。
- 前置条件：人工验收 S2-C1，接受当前局部计数和粒子预告边界；若要求本批补齐全局计数/手持视觉，应先补齐再扩展。

**当前停止，等待人工验收与下一条指令。**
