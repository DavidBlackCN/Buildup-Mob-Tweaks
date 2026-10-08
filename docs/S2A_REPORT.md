# S2-A — 骷髅系与基础装备 AI 阶段报告

> 历史阶段报告：用户已授权继续 S2-B，当前实现及验证见 [S2-B 报告](S2B_REPORT.md)。以下保留 S2-A 当时记录，不补报未运行测试。

日期：2026-10-08（Asia/Shanghai）。基于 5993689；开工时 26.3 Git 工作区干净。用户确认上一阶段验收并授权本阶段。本轮保持既有 26.3 工程版本，独立重设计，不实施跨版本源码迁移。只执行 S2-A，完成后停止。

## 本阶段完成情况

- 阶段/子批次：S2-A；结果：**PASS（本阶段实现及自动验证），等待人工验收**。
- SKEL-01、SKEL-03、SKEL-06、SKEL-09、FIX-01：按矩阵所列受限范围 TESTED。三种骷髅的安全横移、内部无效目标释放、已有双手弓剑切换、标准 BowItem 射击兼容、三项持久条件攻击均有独立开关，默认开启。
- SKEL-02：遮蔽处单次精确攻击已测，自动寻找遮蔽选位未做，因此整行仍 IN_PROGRESS。
- CORE-08：共享能力识别扩展到标准 BowItem，基础装备使用与 Traits 解耦。CORE-10/12、GEN-05 保持 IN_PROGRESS，不能把本轮拾取/切换/局部横移当作完整通用装备、回收或 Adapter 框架。
- FIX-02/MC-121706 未复现，仍 TODO。其他骑乘、雪球、不祥蘑菇、凋灵骷髅、Parched、陷阱骑士等未实现。S2-B、Boss、村民和全部新增附魔相关内容未开始。
- 配置版本 4，保留旧字段；高级特性按实体一次抽取，默认骷髅/流浪者 70/1000、沼骸 10/1000；等级 2/2/3 只是分类。已有及未知数据不重抽，转换不再次中奖；特性和冷却持久化。

| 改动文件 | 原因 |
|---|---|
| combat/SkeletonCombat.java | 服务端三物种策略、特性记录、短程落脚检查、前摇/冷却、目标与物品所有权控制 |
| mixin/SkeletonMixin.java、SkeletonBowGoalMixin.java、SkeletonTickMixin.java | 在原版武器选择/持弓/使用手/横移/散布处接入策略；保留原箭矢工厂及目标 Goal |
| equipment/EquipmentCapabilities.java | 共享标准 BowItem 能力识别，与实际战斗和只读诊断一致 |
| command/SkeletonCommands.java、BuildupMobTweaks.java | 注册模块和等级 4 只读实体状态/开关查询 |
| config/BuildupConfig.java、feature/FeatureId.java、FeatureRegistry.java | 七个独立开关、三种概率、配置版本 4；保留旧功能门控 |
| 主 Mixin JSON、fabric.mod.json、中英文语言 JSON | 注册新注入、更新实际范围、84 对语言键 |
| src/gametest/SkeletonTests.java、CombatTestItems.java、测试入口/翻译/arena.snbt | 20 项新 GameTest、测试专用注册弓和 16×6×16 场地；不进入正式 JAR |
| scripts/Test-SkeletonPersistence.ps1 | 独立服务端配置升级、真实卸载/加载/重启、七项关闭验证 |
| scripts/Test-TraitPersistence.ps1、Test-EquipmentPools.ps1 | 仅把现版本断言更新为 4；本轮未再次运行这两个完整脚本 |
| AGENTS.md、README.md、docs/CONFIGURATION.md、TRAITS.md、EQUIPMENT_POOLS.md、S1C_REPORT.md、SKELETON_COMBAT.md、本报告、UPDATE_NOTES.md | 当前阶段、行为契约、验收步骤、历史状态与发布说明 |
| 父目录 README.md、docs/FEATURE_MATRIX.md、docs/TEST_REPORT.md | 同步共享台账和当前报告入口；位于 26.3 Git 根之外，不包含在本次 commit 中 |

主 Java 文件路径前缀为 src/main/java/com/davidblackcn/buildupmobtweaks/；GameTest 为 src/gametest/java/com/davidblackcn/buildupmobtweaks/。不改依赖、Gradle、Wrapper、Access Widener、其他版本或模板示例 Mixin。没有上游实现或素材复制。

## 验证记录

固定环境：Minecraft 26.3 / Oracle JDK 25.0.3（C:\Program Files\Java\jdk-25.0.3）/ Gradle 9.7.1 / Loom 1.18.3 / Loader 0.19.5 / Fabric API 0.162.0+26.3 / Fzzy 0.7.7+fix3+26.3 / Kotlin 1.14.1+kotlin.2.4.20。官方未混淆命名，无 Yarn。没有盲目升级依赖。

| 命令/测试 | 环境 | 结果 | 证据 |
|---|---|---|---|
| `.\gradlew.bat compileJava --console=plain` | 指定 JDK | PASS，首次生产代码编译 | .gradle/s2a/compile.log |
| `.\gradlew.bat build -PtestEula=true --console=plain` | 最终代码/数据/测试 | PASS，exit 0，19s；45 项必需 GameTest | .gradle/s2a/build-reviewed.log，14:16:51 All 45 required tests passed |
| `.\scripts\Test-SkeletonPersistence.ps1 -AcceptEula` | 独立 127.0.0.1:25588 | PASS，最终三次 JVM 启动均正常 stop/exit 0 | .gradle/s2a/server-reviewed.log；.gradle/skeleton-test-20261008-141724-e0a750/ |
| `.\gradlew.bat runClient --console=plain` | 客户端，未加 Mod Menu | PASS（仅启动烟测），44s，正常关闭 exit 0 | .gradle/s2a/client.log：14:18:56 SDL 窗口、14:18:58 OpenAL/资源图集、14:19:19 Stopping |
| `javap -c -p`：AbstractSkeleton、RangedBowAttackGoal、Mob 等 | 实际 26.3 解析 JAR | PASS，签名/描述符/调用点核对，服务器与客户端运行注入无报错 | .gradle/s2a/*.txt；运行日志 |
| `git diff --check`、PowerShell AST、JSON/双语键、JAR 内容检查、本地文档路径检查 | 完整改动 | PASS | 84 对语言键，脚本可解析，binary/sources 均不含测试代码/测试弓/测试场地且包含许可 |
| GUI、新功能单机/实际多人入服、Mod Menu 增量检查 | 人工场景 | NOT RUN | 客户端启动不替代世界/配置界面验收 |
| 原 S1-B/S1-C 两个持久化脚本、真实第三方弓/AI/优化/保护 Mod、高密度 TPS | 补充场景 | NOT RUN（本轮） | 旧功能的 24 项 GameTest 已回归；脚本旧结果仍在历史报告，不重报为本轮执行 |
| 自然生成/完整刷怪笼、跨维度、复杂地形/骑乘、干净 clone 无缓存、远端 CI | 补充场景 | NOT RUN | 当前证据不覆盖这些环境 |

45 项 = **20 项新增骷髅测试 + 13 项 Traits 回归 + 11 项装备回归 + 1 项 Fabric 测试**。Gradle test 为 NO-SOURCE，不把它冒称 JUnit 通过。

新增测试覆盖：三物种出生和真实序列化、持久冷却/未知版本、零概率不补抽、旧来源跳过、实际转换继承与物种限制、七项独立开关/Traits/总门控、双手切换引用/耐久/命名/掉率/冷却/存档、悬崖/液体/墙与关闭回退、同队内部旧目标释放、标准 Mod 弓及未知物品原版近战、三物种和测试弓连续射击、真实地面物品拾取消费、两种精确攻击/实际起跳/一次消费、沼骸前摇/旁观者排除/实际后撤/换目标中断、原版迟缓及毒箭效果保留。

在 180 tick 窗口，普通骷髅、流浪者和注册测试弓各观察到 3 支不同 UUID 箭，沼骸 2 支。不是伤害或 TPS 基准。新增高级能力测试使用 1000/1000 确定性出生概率和独立测试场地，不靠低概率等待；普通射击场景显式记录 none。生产不注册测试物品、不强制中奖。

### 持久化与配置证据

- 最终测试世界 run/skeleton-test-20261008-141724-e0a750/。新实体 UUID `18a20e99-d706-48a7-90e7-b0b5ec796488`，特性 skeleton_sniping，level=2，exclusive_group=skeleton_special。
- 14:18:04 初次查询 active=true；移除 forceload 并等待 40 秒，14:18:47 查询返回 No entity was found；重新加载后 14:18:52 UUID 和完整保存记录一致。
- 停服把概率改为 0、七开关改 false，新 JVM 启动后 14:19:12 保存记录完全一致，active=false、七开关均 false。脚本比较实际命令回复，不仅检查文件存在。
- v3→v4 保留 diagnosticProbe=false、diagnosticLines=3，并补入新默认开关与概率。客户端原运行目录为更旧配置，Fzzy 提示新增字段缺失并采用默认值写回，属于升级提示；服务端脚本已检查保留旧值。
- 客户端开发账号不能授权 Realms（FabricMC token），仅出现既有开发登录提示；未使用 Realms 验收。无 Mixin 注入失败或客户端类误载异常。

### 失败与修正

1. 初次测试编译使用了旧名 setInvulnerable，核对 26.3 后改为 setPermanentlyInvulnerable；不修改生产版本或绕过编译。
2. 第一轮运行中无敌测试目标被原版 canAttack 拒绝，导致目标/射击断言失败；改为可攻击目标，保留生产安全检查。
3. NoAI 测试实体没有正常落地物理状态；高级能力场景改用实际服务器物理 Tick。原 GameTest barrier 顶不构成日光遮蔽，场地加真实石屋顶后验证条件成立。
4. 自检发现沼骸第一次接敌前摇可能早于原版横移开始；在原版弓 Tick 尾部应用有界安全后撤，并新增实际位移测试。
5. 自检统一了装备查询与标准 BowItem 的能力识别，补齐 SPEC 的等级/互斥组元数据；所有受影响测试及最终服务端持久化均重新通过。

最终 binary SHA256：`6E7CBE564CF50B9E5215CA9CC09E817696FB6BF8205CCCE0DB1710842EC4BF99`；sources：`5928369D50F7D776598F69366E4530F42419719F9E988BF0E8B7EA64DB91FE55`。仅保留本地 build/libs 产物，不提交、不发布。日志、测试世界和字节码核对输出都被 Git 忽略。

## 风险与遗留

- **自检 PASS WITH RISKS**：必需构建及已执行检查通过，无已知 BLOCKER/MAJOR；新 GUI、实战观感、多人同步和真实第三方组合尚待人工检查。
- 平地局部落脚检查不是完整寻路替换，台阶/狭窄地形可能保守停止横移；不保证阻止原版追击、外部推力造成的坠落。没有性能基准，不声称高密度无 TPS 损失。
- BowItem 支持复用了原版箭矢射击，不保证各 Mod 专用弹药、能量、额外射击协议或其他 AI Mixin 的行为。未知实现保留原版回退；具体 Mod 需逐版本验证。
- 自动掩体选位、全通用装备 Adapter/投射物回收、MC-121706 复现仍未完成；矩阵如实保留状态。沼骸技能为单当前目标中毒重设计，不是上游范围云的逐字复刻。
- 上游许可审计 L1 **仍 BLOCKED（直接复制）**；本轮沿用户 26.3 模板独立实现，项目 MIT，原 CC0 模板及外部依赖许可维持不变。发行 JAR 不是源码基线，未声称最新版源码已公开。历史审计不在本轮重新联网审计。
- API 主要依据目标 JAR 字节码及已解析 Fabric API 源码；[Fabric 官方 Mixin 字节码说明](https://docs.fabricmc.net/develop/mixins/bytecode) 仅作方法参考，不拿其他版本文档代替 26.3 签名。

```text
Code Review: PASS WITH RISKS
Scope: 26.3 本轮所有源码、资源、测试、脚本及文档；共享台账仅更新明确相关项
Findings:
- BLOCKER: None
- MAJOR: None
- MINOR: None
Fixes Applied During Review:
- 沼骸接敌前摇实际后撤；共享 BowItem 识别；特性等级/互斥元数据
Verification:
- Wrapper build / 45 GameTest: PASS
- Dedicated server / v3 -> v4 / real unload-reload-restart: PASS
- Client startup and normal shutdown: PASS
- GUI / world multiplayer / external mod compatibility: NOT RUN
- Update Notes: PASS
Residual Risks:
- 人工实战、复杂地形、第三方协议和性能见本节
```

UPDATE_NOTES.md 追加：`- ✨ feat(combat): 为骷髅、流浪者和沼骸加入可独立关闭的安全横移、目标清理、现有弓剑切换、标准 Mod 弓支持及持久化条件攻击，保留原版特殊箭和未知武器近战回退。`

## 人工验收清单

- [ ] 按 [对照步骤](SKELETON_COMBAT.md) 比较 none 与必中特性，确认 7%/7%/1% 默认概率和 8/8/12 秒冷却符合整合包期望。
- [ ] 单机与独立服务端检查粒子、跳射/后撤、目标有效性和正常连续射击。
- [ ] 逐项关闭七开关、Traits 和总开关；中英文界面、权限与多人同步正常。
- [ ] 原版拾取、弓剑往返切换、命名/耐久/掉率及死亡掉落保持；不补发物品。
- [ ] 悬崖、岩浆、狭窄地形、水中和骑乘人工回归；接受局部安全策略的限制。
- [ ] 实际使用的第三方弓与 AI/优化/保护 Mod 按明确版本验收，未验前不宣称兼容。
- [ ] 确认 SKEL-02 等遗留仍明确保留；新增附魔与 Boss/村民无实现。
- [ ] 人工验收后再授权下一阶段；不以本报告代替用户验收。

## 下一阶段建议

下一任务块：**S2-B：僵尸系与溺尸**。前置条件：S2-A 人工验收与新的明确指令。继续保持矩阵遗留可追踪，实际复制上游代码仍须先解决 L1。按用户持续授权，仅进行本地 commit；不推送、不发布。

**当前停止，等待人工验收与下一条指令。**