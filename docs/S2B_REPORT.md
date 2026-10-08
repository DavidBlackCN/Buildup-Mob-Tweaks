# S2-B — 僵尸系与溺尸阶段报告

> 历史阶段报告：用户已授权继续 S2-C，当前批次见 [S2-C1 报告](S2C1_REPORT.md)。以下保留原始 S2-B 记录。

日期：2026-10-08（Asia/Shanghai）。基于 db1f2ed，开工时 26.3 Git 工作区干净。用户已授权继续下一阶段，持续授权收尾本地 commit；不推送、不发布。保持 26.3 模板与固定依赖，独立重设计，无跨版本源码移植。完成后停在 S2-B。

## 本阶段完成情况

- 阶段/子批次：S2-B；结果：**PASS（本阶段受限实现及自动验证），等待人工验收**。
- ZOM-01、ZOM-04、ZOM-05、DROWN-01/02/03：按功能矩阵所列范围 TESTED。基础盾牌、木门防御、概率主动格挡、有限幼年骑乘、三叉戟转移/步行或游泳回收/超时拾取/死亡结算，共八个默认开启的独立开关。
- HUSK-01：双方沙地、可预判换位和持久冷却已测；真正地下钻沙未做，整项保持 IN_PROGRESS。不会挖掘、穿墙或无敌。
- ZOM-02/03/06、HUSK-02/03、DROWN-04 保持 TODO：领袖僵尸马、史莱姆、鸡骑士冲撞、骆驼/Parched 协作、持矛和钓鱼竿不在本批闭环。CORE-10/12 仍 IN_PROGRESS，不声称完成全部装备协议。
- 配置 v5；高级能力一次互斥抽取，门/主动格挡/沙地/幼年骑乘权重默认 30/70/30/10 千分，冷却各自配置。保存结果、冷却与骑乘尝试持久化，未知版本不重抽。装备池独立且不补发。
- S2-C、Boss、村民、未来装备/附魔项目未开始；所有新增附魔注册、战利品和玩法继续排除。

| 改动文件 | 原因 |
|---|---|
| combat/ZombieCombat.java | 僵尸/尸壳互斥特性、有限防御与沙地 Goal、持久冷却/骑乘尝试 |
| combat/DrownedTridents.java、OwnedTrident.java | 主手和实际投射物的唯一所有权、回收导航、超时一次掉落判定、死亡归还；无备用物品 |
| mixin/DrownedMixin.java、TridentMixin.java、ZombieLifecycleMixin.java、主 Mixin JSON | 核对 26.3 实际描述符，在原版投掷/游泳判断/投射物 tick/死亡遍历前接入 |
| command/ZombieCommands.java、BuildupMobTweaks.java | 注册服务端模块及等级 4 只读状态命令 |
| config/BuildupConfig.java、feature/FeatureId.java、FeatureRegistry.java | 配置 v5、八项独立门控、四组概率/冷却及回收超时 |
| fabric.mod.json、中英文语言 JSON | 描述同步实际范围；123 对语言键，解释生效时机与关闭后事务收尾 |
| src/gametest/CombatTestWorld.java、ZombieTests.java、TridentTests.java、测试入口 JSON | 26 项新增真实服务端测试，复用既有 GameTest；不进入正式 JAR |
| scripts/Test-ZombiePersistence.ps1 | 独立本地服务端配置升级、实际卸载/重载/重启、八门控关闭测试 |
| 原三个 Test-*.ps1 | 仅将当前配置版本断言更新到 5；本轮未重跑这三个完整脚本 |
| AGENTS.md、README.md、docs/CONFIGURATION.md、TRAITS.md、EQUIPMENT_POOLS.md、ZOMBIE_COMBAT.md、S2A_REPORT.md、本报告、装备示例 README、UPDATE_NOTES.md | 阶段边界、行为契约、历史记录与人工步骤；没有变更默认装备池 |
| 父目录 README.md、docs/FEATURE_MATRIX.md、docs/TEST_REPORT.md | 同步共享台账和当前报告；位于 26.3 Git 根之外，不进入本次 commit |

Java 路径前缀：src/main/java/com/davidblackcn/buildupmobtweaks/；测试：src/gametest/java/com/davidblackcn/buildupmobtweaks/。未改其他版本、依赖、构建脚本、Wrapper、Access Widener、LICENSE/NOTICE 或第三方来源。行为契约和配置表见 [ZOMBIE_COMBAT](ZOMBIE_COMBAT.md)。

## 验证记录

固定环境：MC 26.3 / Oracle JDK 25.0.3（C:\Program Files\Java\jdk-25.0.3）/ Gradle 9.7.1 / Loom 1.18.3 / Loader 0.19.5 / Fabric API 0.162.0+26.3 / Fzzy 0.7.7+fix3+26.3 / Kotlin 1.14.1+kotlin.2.4.20。官方未混淆命名，无 Yarn。

| 命令/测试 | 环境 | 结果 | 证据 |
|---|---|---|---|
| `.\gradlew.bat compileJava --console=plain` | 指定 JDK25 | PASS | .gradle/s2b/compile.log |
| `.\gradlew.bat build -PtestEula=true --console=plain` | 最终代码/资源/测试 | PASS，23s，exit 0；71 项必需 GameTest | .gradle/s2b/build-reviewed.log，15:12:15 All 71 required tests passed |
| `.\scripts\Test-ZombiePersistence.ps1 -AcceptEula` | 独立 127.0.0.1:25589；已获用户 EULA 授权 | PASS，三次启动和 stop 均正常 | .gradle/s2b/server.log；.gradle/zombie-test-20261008-150346-cdb2bc/ |
| `.\gradlew.bat runClient --console=plain` | 开发客户端，无 Mod Menu | PASS（仅启动烟测），50s，exit 0 | .gradle/s2b/client.log；15:10:24 音频/图集完成，15:10:53 正常 Stopping |
| `javap -c -p`：Drowned、Mob、ThrownTrident、AbstractArrow、BaseSpawner 等 | 已解析目标 26.3 JAR | PASS，实际方法/描述符/生命周期核对 | .gradle/s2b/*.txt |
| JSON、双语键、PowerShell AST、`git diff --check`、JAR/文档路径检查 | 完整改动 | PASS | 123 对语言键，四个脚本语法通过，测试源码与场地不在正式 JAR |
| GUI、单机战斗、真实多人同步、Mod Menu 新字段 | 人工环境 | NOT RUN | 启动不等于界面/进世界验收 |
| 自然群体刷怪算法、跨维度、主人/投射物分别跨区块卸载和重启、高密度 TPS、第三方 AI/优化/保护项目 | 补充场景 | NOT RUN | 目前不宣称普遍兼容或性能达标 |
| 原 Traits/装备/骷髅三个完整持久化脚本、干净 clone 无缓存、远端 CI | 补充场景 | NOT RUN（本轮） | 旧 45 项 GameTest 已回归，不把历史脚本结果计作本轮执行 |

71 项 = **12 项僵尸测试 + 14 项三叉戟测试 + 45 项既有回归**（20 骷髅、13 Traits、11 装备、1 Fabric）。Gradle test 为 NO-SOURCE，不冒称 JUnit。

新增覆盖：自然/刷怪笼/命令来源、实际自然 finalizeSpawn + ENTITY_LOAD、玩家附近真实刷怪笼方块、真实转换与序列化、旧来源和未知版本保留、持久冷却、八个独立开关/Traits/总门控、门正背面/冷却/三次耗用/实际伤害事件、真实原版盾牌阻伤并结束、基础和高级盾牌解耦、沙地前摇/实际位移/障碍/缺沙、仅骑现有成年僵尸并关闭解除。

三叉戟测试覆盖实际注入投掷、空手不合成、三个独立投射物 UUID 的连续回收、原命名/耐久/组件/掉率保持、陆地和水中实际导航保留战斗目标、主人及投射物真实序列化、零/保证掉率与仅判定一次、独立禁用、实际死亡仅掉一把、手槽已有其他物品、缺失投射物不补发、忠诚原物品保留、实际地面物品拾取后投掷、真实超时及生存玩家拾取、已释放投射物关闭门控、消失诅咒不可掉落。

刷怪笼测试使用真实方块、玩家和 server tick，显式放宽测试光照规则以隔离出生生命周期；不是自然黑暗条件刷怪场的概率测试。自然场景调用真实 finalizeSpawn 和加载链，不覆盖整套世界自然刷怪算法。测试确定性高级概率只用于局部测试配置；生产不强制中奖。

### 持久化与配置证据

- 独立世界 run/zombie-test-20261008-150346-cdb2bc/，实体 UUID ba773561-b31f-4e4a-afad-66ef13cd1980，zombie_active_guard、level=2、exclusive_group=zombie_special。
- 15:04:28 初次查询；15:05:11 卸载后 No entity was found；15:05:16 再加载 UUID 与完整附件一致。停服置概率为 0、八开关为 false，新 JVM 查询仍保留原记录，八门控全 false。
- v4→v5 保留 diagnosticProbe=false、diagnosticLines=3，补入新默认值。真实服务端脚本比较输出内容，不仅检查文件存在。
- 三叉戟与非零能力冷却保存由 GameTest 实体真实序列化覆盖；不要将上述僵尸区块测试冒称已完成所有三叉戟跨区块/重启场景。
- 客户端开发账号 Realms 授权失败为既有 FabricMC token 提示；未验收 Realms。未见 Mixin 注入失败或 common 加载客户端类异常。

### 失败与审查修正

- 第一轮 64 项中沙地位移失败：测试沙层下方悬空，重力使地形塌落。补实体测试场地石支撑，不放宽生产地形安全判断；随后 64、69、71 项依次通过。
- 审查补充已释放三叉戟的实时拾取门控：关闭选项后不会继续可拾取，重新启用也不重新抽掉率；新增真实玩家拾取测试通过。
- 后续补齐水中、实际拾取、满手死亡、消失诅咒、自然初始化和真实刷怪笼覆盖。无通过删测试、放宽兼容范围或移除依赖掩盖失败。

最终 binary SHA256：82D47A5BD6A78449844585A5B4A7F0D2B4E5062A2D34552494EC6E2C35C07821；sources：3CBF38D4D38EE4D16A788F85895D9C677FE7D096E10B7679BF7CD26E296D0DED。产物仅保留本地，不提交或发布。日志、字节码证据、测试世界均被 Git 忽略。

## 风险与遗留

- 超时拾取不是必掉：沿用原基础掉率一次判定，遵守 mobDrops 和原版消失诅咒；没有释放时的抢夺加成。主人正常死亡且投射物已加载、原手槽为空时归回原版死亡流程，才沿用原版完整结算。
- 不强载投射物；若主人死亡时投射物未加载，或物品进入不同维度/被外部删除，不能恢复原击杀上下文。加载后超时收尾、不补发，优先避免资源复制。第三方装备/投射物改写未验收。
- 沙地是最多 3 格换位，木门无专用盾牌视觉，幼年骑乘只使用现有成年普通僵尸；不等同上游全套行为。
- 现有默认池仍是 10% 空主手木剑；盾牌/门需原有数据包示例或主动配装，高级能力独立中奖。没有为了展示功能暗改默认战利品或附魔资源。
- 本项目 MIT 不改变第三方许可。上游 L1 直接复用边界仍待澄清，本轮未复制上游代码或素材，没有把发行 JAR 当完整源码。LICENSE/NOTICE 内容无需变化。
- 父目录共享台账不在 26.3 Git 中；本地 commit 只包含工程及其 docs/，需另行保存共享文件。

## 自检

Code Review: **PASS WITH RISKS**（未运行人工/第三方环境及已披露卸载边界）。

- Scope：26.3 本次全部代码、Mixin、配置、资源、测试、脚本、文档及共享台账；不覆盖用户修改。
- BLOCKER：无。MAJOR：无已知阻断缺陷。MINOR：无待处理项。
- 决策：目标 26.3 实际字节码优先；common 服务端执行；附件只存引用、先移除再归还；基础能力和高级 Traits 分离；扫描/导航有界，无每 tick 全世界遍历。
- 验证：Wrapper build、71 GameTest、独立服务端、客户端启动、JSON/双语/脚本/JAR/差异检查 PASS。人工与兼容项明确 NOT RUN。
- Update Notes：PASS，追加同提交的 Release 用户说明，保留既有记录。

## 人工验收清单

- [ ] Fzzy 中英文新增八项开关/概率/冷却/超时显示正确；普通玩家不能修改服务端规则，OP 同步生效。
- [ ] 按 ZOMBIE_COMBAT 步骤对照普通盾牌、木门三次耗用、主动格挡与关闭回退；能恢复原版近战。
- [ ] 尸壳沙地前摇可见，墙/缺沙/换目标不穿越；幼年骑乘不生成额外坐骑，关闭解除。
- [ ] 生存单机和独立多人环境：陆地/水中连续三轮三叉戟投掷回收，保持原物品和目标；超时/死亡/关闭不复制。
- [ ] 主人和投射物分别跨区块卸载/重启、复杂深水及真实自然刷怪场验证，确认可接受已披露丢失/掉落结算边界。
- [ ] 装备数据包、新旧存档及拟安装第三方 Mod 回归；高密度性能另测。

## 下一阶段建议

- 下一任务块：S2-C，袭击和剩余主要敌对生物。
- 前置条件：人工验收本批；明确接受当前受限重设计及三叉戟卸载/掉落边界。剩余矩阵条目按后续授权逐项收敛。

**当前停止，等待人工验收与下一条指令。**
