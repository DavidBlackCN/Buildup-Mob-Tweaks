# S1-B — Traits 与生命周期阶段报告

> 后续记录（2026-10-08）：用户明确确认本阶段验收通过，授权开始 S1-C。以下保留 S1-B 当时的测试和未运行项，不将用户验收改写为 Agent 自动测试。

日期：2026-10-08（Asia/Shanghai）。基于本地提交 2517f3f；开始时工作区干净。用户明确确认 S1-A 验收通过并授权下一阶段，本轮只执行 S1-B。

## 本阶段完成情况

- 阶段/子批次：S1-B；结果：**PASS（本阶段自动验收）**，停下等待人工复查，不进入 S1-C。
- CORE-05：诊断特性定义、按实体池配置的互斥抽取；600,000 次固定种子采样及边界断言通过。
- CORE-06：持久化数据版本、空结果标记、未知数据保留；真实区块卸载/重载及跨 JVM 重启通过，改概率不重掷。
- CORE-07：原版转换继承、真实繁殖、命令/自然/两种刷怪笼生成原因入口、未知来源跳过。完整刷怪笼方块场景仍列人工项。
- CORE-01–04：S1-A 用户验收单独记入历史报告；本轮增量验证配置 v1→v2 保留旧值。没有冒充 Agent 已执行用户的手测。
- 无新增 Mixin、AI Goal、属性强化、装备池、Boss 接管、村民机制或新增附魔。上游实现与资源没有复制；MIT/第三方许可路线保持不变。

| 变更文件 | 原因 |
|---|---|
| traits/TraitKind.java、TraitSampler.java、TraitState.java、TraitService.java | 定义、互斥算法、版本/校验、附件持久化及事件接入 |
| command/TraitCommands.java | 等级 4 的只读实体查询，显示 UUID、saved 与 active |
| BuildupMobTweaks.java、feature/FeatureId.java、FeatureRegistry.java | 注册生命周期与统一功能门控，不分散读配置 |
| config/BuildupConfig.java、lang/en_us.json、lang/zh_cn.json | 配置版本 2、Traits 总/子开关和两套有界权重；54 对语言键 |
| build.gradle、src/gametest/ | 使用已有 Fabric GameTest，加入 13 项项目测试，测试代码不打入发布包 |
| scripts/Test-TraitPersistence.ps1 | Windows 可复现的独立服务端升级/卸载/重启测试 |
| README.md、AGENTS.md、docs/CONFIGURATION.md、TRAITS.md、S1A_REPORT.md、本报告、UPDATE_NOTES.md | 当前范围、算法/存档约定、验收历史及发布说明 |
| 父目录 README.md、docs/FEATURE_MATRIX.md、docs/TEST_REPORT.md | 同步共享状态；这些文件不在 26.3 Git 根内，不随本地提交保存 |

Java 主源码路径为 src/main/java/com/davidblackcn/buildupmobtweaks/；语言文件在 assets/buildupmobtweaks/lang/。未改 Wrapper、依赖版本、用户已有 Mixin 或其他版本目录。

## 验证记录

环境固定为 MC 26.3 / JDK 25.0.3 / Gradle 9.7.1 / Loom 1.18.3 / Loader 0.19.5 / Fabric API 0.162.0+26.3；Fzzy 0.7.7+fix3+26.3，Kotlin 1.14.1+kotlin.2.4.20。官方未混淆命名，无 Yarn。使用 C:\Program Files\Java\jdk-25.0.3。

| 命令/测试 | 环境 | 结果 | 证据 |
|---|---|---|---|
| `.\gradlew.bat build --console=plain` | 新增核心代码初编译 | PASS；当时只有 Fabric 内置测试，不计本项目验收 |
| `.\gradlew.bat build -PtestEula=true --console=plain` | 最终代码及 src/gametest | PASS，exit 0，15s；13 项项目测试 + 1 项 Fabric 内置测试 | `.gradle/s1b/build-final.log`，12:34:50 All 14 required tests passed |
| `.\scripts\Test-TraitPersistence.ps1 -AcceptEula` | 独立回环服务端 127.0.0.1:25586 | PASS，3 轮正常 stop/exit 0 | `.gradle/trait-test-20261008-123245-655a3f/` |
| v1→v2 配置升级 | 同脚本第一轮 | PASS | diagnosticProbe=false、diagnosticLines=3 保留，新增 Traits 默认字段 |
| JSON/两侧语言键/产物检查 | 最终本地 JAR | PASS | 54 个语言键一致；无测试类、无内嵌依赖，MIT/NOTICE 均在 |
| `git diff --check` | 全部代码及说明 | PASS | 无空白错误，既有 LF/CRLF 转换提醒不影响内容 |
| 新增 Traits GUI 字段、客户端/单机/多人增量测试 | S1-B | NOT RUN | 本轮验证集中在服务端；S1-A 验收不能代替 S1-B 新字段验收 |
| 自然群体生成、完整刷怪笼方块、跨维度旅行、外部 Mod、性能/旧真实世界 | 完整场景 | NOT RUN | 不宣称这些额外兼容已通过 |

13 项项目测试覆盖：概率/互斥统计，数值边界与非法输入，五种生成原因的一次初始化，实体真实序列化，尸壳→僵尸→溺尸转换，无源记录转换，原版 spawnChildFromBreeding，总/子开关和配置变化，旧实体与五种 Boss 排除，未知来源，空抽取持久化，非法互斥/等级组合，未来版本/损坏记录保留。

### 统计证据

每组 n=200,000；分类依次 common / advanced / rare / none。以各类别二项分布 6σ+1 为误差限，固定种子可重现；不将此测试当作性能基准。

| 权重 | 种子 | 实测计数 | 实测比例 |
|---|---|---|---|
| 200 / 70 / 10 | 74123 | 39586 / 14244 / 1987 / 144183 | 19.793% / 7.122% / 0.9935% / 72.0915% |
| 100 / 30 / 5 | 92731 | 20070 / 6161 / 987 / 172782 | 10.035% / 3.0805% / 0.4935% / 86.391% |
| 1000 / 1000 / 1000 | 42731 | 66614 / 66830 / 66556 / 0 | 33.307% / 33.415% / 33.278% / 0% |

### 持久化证据

隔离世界：run/trait-test-20261008-123245-655a3f/。脚本在区块 [1000,1000] 创建一头测试牛，并用稀有权重 1000 确保非空记录。

- 12:33:25：UUID `f7f992bd-6506-444c-9f87-524c29e39d9c`，version=1、outcome=rolled、origin=COMMAND、demo_rare/level=3。
- save-all flush、移除 forceload 并等待 40 秒；12:34:08 查询返回 No entity was found，确认实体已不在加载集合中。
- 再次 forceload，12:34:13 查询恢复完全相同 UUID、saved 与 active。
- 正常停服，将该池所有概率改为 0，再启动新 JVM；12:34:39 仍是完全相同的记录。
- 控制脚本断言两次重载结果与首次字符串一致，三个服务端均正常保存退出；无遗留游戏进程。测试世界和日志留在忽略目录，未提交或删除。

### 失败及修正记录

- 首次测试编译错误：26.3 常量位于 EntityTypes，不在 EntityType；核对本地 javap 后修正。
- 首次完整 GameTest 三项失败：助手 spawn 使用 STRUCTURE 创建实体、繁殖测试只调用 finalize 未把后代加入世界。按实际调用链改用 EntityTypes.spawn 和完整 spawnChildFromBreeding；未放宽断言，最终全部通过。
- 初次持久化脚本的 JavaExec workingDir 被 Loom 运行设置覆盖，启动的是既有 run/ 测试服。控制器未找到预期目录的 Done，未发任何实体测试命令，超时后正常 stop；常规启动升级了该本地配置。改为设置 Loom runs.server.runDir 后，三轮隔离验证通过。
- 工具写入脚本/文档时曾出现 PowerShell here-string/引号解析错误，未产生相应文件修改；修正后重新写入并验证。
- 空白 GameTest 环境首次缺 server.properties 的生成提示不算功能错误；不将首次只有内置测试的成功算作 Traits 测试。

## 风险与遗留

- 仅诊断骨架；对正式战斗特性、装备依赖和冷却执行没有实现或兼容声明。Boss 使用白名单排除，未预设 Stellarity ID。
- 旧实体无记录时跳过；关闭时新出生的实体以后也不补抽。该策略避免概率变化导致旧实体隐式增强，需人工确认符合整合包预期。
- 未知/损坏版本保存但停用；没有自动修复或管理员重掷。卸载 Mod 后再保存世界的附件保留行为未测，不能承诺卸载后数据不丢失；操作前保留世界备份。
- 附件源码实际版本 2.2.31+b9b88df45d；实体事件 6.0.4+3434d6d95d、生命周期 4.1.9+ffef5f675d、GameTest 4.0.33+3434d6d95d。已核对来源接口与转换发生在 addFreshEntity 前的时序。
- S1-B 没有重新执行客户端启动或真实联机；新 GUI 项目列人工回归。服务器事件已在纯服务端验证，不据此声称所有客户端组合兼容。
- 上游 L1 直接复制边界保持原审计结论；本轮独立实现，没有跨版本源码迁移，不触发 Migration Plan 实施门。

## 自检

Code Review: **PASS WITH RISKS**。

- Reviewed files：本报告变更表全部文件；目标仅 26.3 与三份明确相关共享文档。
- BLOCKER：无；构建、概率与核心生命周期验收通过。
- MAJOR：无已确认代码缺陷；未测的额外场景在上表披露。
- MINOR：无；自检合并了重复特性 ID 来源，存档校验拒绝冲突组/错误等级/不合法状态。
- common/client/server：只注册服务端实体事件，附件不向客户端同步；命令只读且等级 4，不增加 Mixin/反射或 Tick 扫描。
- 配置：版本 2 为增量字段，实际升级保留旧值；权重有界，关闭功能保留已有存档。
- Update Notes：追加已验证 Traits 抽取、只读查询与存档保持；不宣称正式 AI 已实现。
- 残余风险：见额外场景及人工清单。自动验收 PASS 不代替下一阶段之前的人工确认。

## 人工验收清单

- [ ] GUI 中 Traits 总开关、三个子开关与两套权重，中英文显示和范围正确。
- [ ] 查询新牛/僵尸：saved 与 active 可辨；关闭子开关保留 saved，重开恢复同一记录。
- [ ] 确认旧实体跳过、关闭期间出生不补抽、繁殖独立、转换继承的策略符合预期。
- [ ] 用真实刷怪笼/试炼刷怪笼、自然生成和跨维度场景补充回归；不得据生成原因测试宣称全场景兼容。
- [ ] 单机和多人复查新配置同步；Boss 不带通用 Traits，未出现新增物品/属性强化。
- [ ] 接受本轮阶段报告后再授权 S1-C。

## 下一阶段建议

下一任务块为 **S1-C：装备 + Data Pack 最小闭环**，前置条件是本轮人工验收与新的明确指令。当前不实现装备分配或 Boss 代码，不推送、不发布 JAR。

**当前停止，等待人工验收与下一条指令。**
