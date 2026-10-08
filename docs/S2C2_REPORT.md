# S2-C2 — 恼鬼冲刺批次报告

日期：2026-10-08（Asia/Shanghai）。基于 337185d，开工 Git 工作区干净。用户授权继续下一批次，本轮只完成 S2-C2，不进入 S2-D 或 Boss。不是跨版本迁移，未复制上游代码或素材。

## 本阶段完成情况

- 阶段/子批次：S2-C2 恼鬼冲刺；**本批 PASS（实现与自动验证），整个 S2-C 仍 PARTIAL**。
- VEX-01 TESTED：起手采样一次落点，屏蔽接近时重新追踪，保留原版音效/红色状态与碰撞攻击。固定落点或停顿任一开启时，冲刺最多 40 tick，目标变更/失效会中断。
- VEX-02 TESTED：默认 20 tick 恢复，10–60 可配，停止自身飞行动量后暂停 AI 加速；外部击退不被逐 tick 清零。持久截止时间防重载跳过恢复；运行态攻击不恢复。
- VEX-03 TESTED：将 26.3 原版两格起手边界扩展为默认三格，2–6 可配；保留随机选择与原版两格限制。这是重设计，不冒称原版完全没有近距离限制。
- 三项独立默认开启，经 FeatureRegistry 和总开关控制，不依赖 Traits；配置 v7 保留旧值。未改变伤害属性、装备、掉率、召唤所有权或地形。
- VEX-04、GHAST 全部条目及其他剩余 S2-C 条目仍 TODO；C1 的全局恼鬼数量限制和女巫手持药水模型仍未完成。不将本批自动测试代表全部敌对生物已验收。

| 改动文件 | 原因 |
|---|---|
| combat/VexCombat.java | 单实体冲刺运行态、版本化恢复附件、边界与回退 |
| mixin/VexChargeMixin.java、VexMoveMixin.java | 接入已核实 26.3 原版冲刺 Goal 和飞行控制器 |
| command/VexCommands.java、BuildupMobTweaks.java | 初始化及等级 4 只读诊断 |
| config/BuildupConfig.java、feature/FeatureId.java、FeatureRegistry.java | 配置 v7、三个独立开关、两个校验数值 |
| 主 Mixin JSON、fabric.mod.json、中英文 JSON | 注册 Mixin、同步功能描述及 160 对语言键 |
| src/gametest/VexTests.java、测试入口 JSON | 新增 11 项实际 Goal/控制器/存档与门控测试 |
| scripts/Test-VexPersistence.ps1 | 隔离独立服配置升级、卸载重载、重启、开关验证；检查服务端 ERROR |
| 既有五个 Test-*.ps1 | 仅将当前配置版本断言改为 7，未重跑其完整流程 |
| AGENTS.md、README.md、docs/CONFIGURATION.md、S2C1_REPORT.md、VEX_COMBAT.md、本报告、UPDATE_NOTES.md | 精确行为契约、阶段入口、验证与发布说明 |
| 父目录 README.md、docs/FEATURE_MATRIX.md、docs/TEST_REPORT.md | 同步三条功能状态与报告入口；不在 26.3 Git 根内，不随本次提交保存 |

Java 路径前缀为 src/main/java/com/davidblackcn/buildupmobtweaks/，测试为 src/gametest/java/com/davidblackcn/buildupmobtweaks/。未改 Wrapper、依赖、构建文件、Access Widener、LICENSE/NOTICE、其他版本或装备池。

## 验证记录

固定环境：Minecraft 26.3 / Oracle JDK 25.0.3（C:\Program Files\Java\jdk-25.0.3）/ Gradle 9.7.1 / Loom 1.18.3 / Loader 0.19.5 / Fabric API 0.162.0+26.3 / Fzzy 0.7.7+fix3+26.3 / Kotlin 1.14.1+kotlin.2.4.20。官方未混淆命名，无 Yarn。所有命令在 26.3 目录执行，JAVA_HOME 使用用户指定路径。

| 命令/测试 | 环境 | 结果 | 证据 |
|---|---|---|---|
| `.\gradlew.bat build -PtestEula=true --console=plain` | 最终源码，指定 JDK25 | PASS，exit 0，22s，98 项必需 GameTest | .gradle/s2c2/build-reviewed.log，16:14:53 All 98 required tests passed |
| `.\scripts\Test-VexPersistence.ps1 -AcceptEula` | 127.0.0.1:25591，独立世界；用户已同意 EULA | PASS，三次启动/正常 stop，ERROR 与注入异常检查通过 | .gradle/s2c2/server-reviewed.log |
| `.\gradlew.bat runClient --console=plain` | 开发客户端，无 Mod Menu | PASS（仅加载），exit 0；正常关闭窗口 | .gradle/s2c2/client.log，16:13:01 音频/图集完成，16:13:59 Stopping，1m16s |
| `javap -c -p` 核对 Vex 内部 Goal/控制器、MoveControl | 实际解析的 26.3 common JAR | PASS；目标字段、方法描述符与调用位置核对 | .gradle/s2c2/ 下核对记录 |
| JSON 解析、双语键及占位符、PowerShell AST、`git diff --check` | 完整改动 | PASS，160 对语言键，六份脚本语法检查 | 本地自检 |
| `jar tf`、`Get-FileHash` | 最终 binary / sources JAR | PASS；含 MIT/NOTICE/模板 CC0，生产包不含测试类或场地 | 本地构建输出，不提交 JAR |
| GUI 新字段、视觉躲避窗口、单机世界、真实双人同步战斗、完整自然袭击 | 人工环境 | NOT RUN | 加载日志与受控 GameTest 不替代这些场景 |
| 高密度 MSPT/TPS、第三方领地/AI/优化 Mod、跨维度、所有难度与复杂墙体 | 补充环境 | NOT RUN | 不宣称性能或第三方兼容已验证 |
| 原五份持久化完整脚本、无缓存 clone、远端 CI | 补充环境 | NOT RUN（本轮） | 87 项既有 GameTest 已回归，历史脚本结果不计入本轮 |

98 项 = 11 项新增恼鬼 + 87 项既有测试。Gradle test 为 NO-SOURCE，不称为 JUnit。

新增用例：独立门控/总开关/Traits 解耦/数值边界；真实 Goal 横移目标后保持初始目的地；未知 schema 保留并回退原版追踪；2.5 格起手边界与原版两格限制；碰撞仍造成伤害且结束冲刺；实际移动控制器暂停、允许外部击退并到期恢复；40 tick 超时与重复 stop 不延长冷却；换目标前阻止额外命中且保留新目标；真实序列化取消在途攻击并保留冷却/UUID；两种冲刺策略独立关闭、不改已有数据；实际 AI 移动并进入恢复而不丢目标。

### 服务端最终证据

世界 `run/vex-test-20261008-161641-cd3bd6/`，日志 `.gradle/vex-test-20261008-161641-cd3bd6/`。恼鬼 UUID `94d3c1c6-ef28-4a1f-96b6-f15ad0d6f428`。

v6→v7 保留 diagnosticProbe=false、diagnosticLines=3，并增加三开关与默认数值。控制台设置附件 recover_until=987654321L，记录后解除强加载并等待 40 秒，确认 `No entity was found`；重新加载后 UUID 与附件完全相同。正常停服后关闭三个新开关，新 JVM 中仍保持同实体和截止时间，诊断门控全部 false。该大数值是持久化夹具，不是生产冷却；生产实际起手/停止/恢复由 GameTest 另行验证。

日志确认 mob_griefing=false。最终三份 latest 日志无 ERROR 或 Mixin 注入异常。初次包含生成器回退的记录保留在 `.gradle/vex-test-20261008-160956-72f801/`，不以其作为最终无错误通过证据。

### 自检修正与日志边界

首次 build 29s、98 项通过。自检后只整理入口 imports 和修正测试注释，再运行最终 build 22s、98 项通过；没有删除失败用例或降低注入校验。

初次服务端脚本的功能断言通过，但首建超平坦世界出现 `No key layers in MapLike[{}]`。根因是继承的测试夹具缺少 generator-settings.layers，原版回退默认地形。核对目标 FlatLevelGeneratorSettings Codec 后，仅修正本批新脚本的生成参数，并加入遇 ERROR/Mixin 注入异常直接失败的检查，使用新世界重跑。旧五个脚本未顺带改生成参数，未来重跑应先处理同类夹具问题。

开发客户端仍有 FabricMC 开发凭证引起的 Realms 授权提示；非 Mixin/本 Mod 崩溃，未验证 Realms。已有 ZombieTests mock-player API 弃用警告保留，不影响构建。没有掩盖这些日志。

最终 binary SHA256：1C7FAE4DEB322DF8006FED23787462CD67B5FE01AB2A4AEF31A130B7AC6A679F。
Sources SHA256：CD8D8FF90A3C9DB80AD3BDE502ABF666B4A9DC6B8DA23DB39BB39ACD03875BCA。

## 风险与遗留

- 起手只采样目的地，不将飞行强制为直线；原版惯性与穿墙保持。新增的是一段可躲避窗口，实际玩家体验需要人工验收。
- 开启停顿时，进行中冲刺卸载会保留预留时限；默认最多剩余 60 tick、配置上限为 100 tick。按游戏时间计算，不按停服墙上时间计算；关闭策略不删除截止时间。
- 只近距离开关启用时保持其他原版冲刺逻辑；全关状态起手的冲刺启用后，40 tick 上限从下一次起手建立，详见行为说明。
- 两个内部类 Mixin 的装载冲突不能靠 GUI 开关消除；第三方重写相同 Goal/控制器须具体测试。
- 本批无方块写入、爆炸、物品生成或扫描；mobGriefing 关闭测试不等于证明全部领地/多人场景兼容。没有上限计数或新概率强化。
- 上游 L1 直接复制许可边界仍待澄清；本批独立实现，本项目 MIT 和外部依赖许可不变，未将发行 JAR 当作上游完整源码。
- 父目录共享文档在版本 Git 根外，需另行保存；未推送、未发布 JAR。

## Code Review

Code Review: PASS WITH RISKS

- Scope：本批所有生产 Java、配置、Mixin、测试、脚本、资源及文档；唯一工程目标 26.3。
- BLOCKER：无。MAJOR：无。MINOR：无待修项。
- 修正：测试世界 layers 参数及错误断言、注释与重载动量文档精度、入口 imports。
- Verification：最终 build/GameTest PASS；服务端完整脚本 PASS；客户端加载 PASS；资源/脚本/许可与差异检查 PASS。
- Update Notes：收尾追加已验证恼鬼功能，格式为 Gitmoji + Conventional Commits，与本地提交主要变化一致。
- Residual Risks：GUI、真实多人、视觉平衡、第三方兼容和性能测试未运行，见上文。

## 人工验收清单

- [ ] 生存模式横移躲避起手后的冲刺：应保持原目的地并在结束后停顿；不应持续贴身转弯或无限冲刺。
- [ ] 三开关分别对照：关固定落点恢复原版追踪，关停顿恢复自主飞行，关近距离限制恢复原版两格边界；其他两项保留。
- [ ] GUI 中英文字段、数值限制可读，服务端应用后两位玩家观察一致；普通玩家不能修改权威配置。
- [ ] 暂停期间恼鬼正常受伤/被击退；目标换人、死亡、退出后无额外命中或永久挂起。
- [ ] 冲刺/停顿中保存退出重进：旧攻击不重播，冷却到期后重新选敌战斗；不得清零重复冲刺或永久冻结。
- [ ] 自然召唤恼鬼的完整袭击、复杂墙体、不同难度、高密度与实际整合包兼容补测；异常保留日志与复现环境。

## 下一阶段建议

- 下一任务块：继续 S2-C 剩余独立批次，可优先恶魂投射物速度/冷却；VEX-04 仍需单独定义与验证伤害规则。
- 前置条件：用户验收本批并明确授权；整个 S2-C 未完成前不自动进入 S2-D 或 S3。

**当前停止，等待人工验收与下一条指令。**
