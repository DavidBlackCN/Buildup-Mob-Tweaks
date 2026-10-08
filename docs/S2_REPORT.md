# S2 统一阶段报告

日期：2026-10-08（Asia/Shanghai）。起点 b8c763f，开工工作区干净；用户明确授权本轮完成所有 S2 后停止，覆盖旧批次暂停要求。唯一工程目标为 26.3；未升级依赖、未移植版本、未进入 S3。

## 本阶段完成情况

- 阶段：S2-A～D 收口。结果：**PASS（本轮独立实现与自动验证）；实机人工验收待完成，不代表 S4 发布放行。**
- 骷髅/僵尸/骑乘/装备补齐；袭击、唤魔者、幻术师、女巫、恼鬼、恶魂、烈焰人、旋风人、蠹虫、末影人及通用敌对行为加入独立配置。完整条件与重设计见 [S2_COMBAT](S2_COMBAT.md)。
- 配置 v8：新增 91 个默认开启独立开关、12 个验证整数；369 对中英文语言键。已有字段、实体特性和实际装备保留。
- 106 条台账更新到明确的本地实现/验证范围；SRC2-04 展示实体清理不适用。FIX-07 龙火球退出、REL1-01 预生成硬冻结、REL1-02 RPG Difficulty 冲突因缺少具体 26.3 复现继续 **BLOCKED**；没有编写猜测性修复，也不伪称上游故障已经解决。
- `TESTED` 只覆盖矩阵行内写明的证据；部分行仅门控/加载已测，实战、视觉或第三方组合仍需人工。这些待验项不冒充已通过。

| 改动文件/范围 | 原因 |
|---|---|
| combat/HostileCombat、CombatPerception、EnvironmentCombat、RestingHostiles | 通用门控/截止时间、感知、环境与休息 |
| combat/AdvancedHostiles、SkeletonExtras、RidingCombat、HostileEquipment、IllagerRelations、GeneralHostileRules、S2Tags | 一次互斥抽取、有限技能、真实装备行为、阵营/骑乘和标签过滤 |
| combat/VexOwnership、RaidCombat、ZombieCombat、SkeletonCombat | 全局恼鬼索引、女巫预告/跳投、真实钻沙、弓退出标签及既有行为衔接 |
| 主 Mixin JSON 与新增 common Mixin | 核实26.3类后窄范围接入 Goal、移动、伤害、装备、投射物与生命周期 |
| client/mixin/ 五个新文件与 client Mixin JSON | 恶魂、女巫、图腾、副手弓、溺尸视觉；common 不引用客户端类 |
| config/BuildupConfig、feature/FeatureId/FeatureRegistry、command/S2Commands、BuildupMobTweaks | v8、独立开关、等级4只读诊断与注册 |
| assets/.../lang、data/.../tags、fabric.mod.json | 双语说明、八个过滤标签和准确产品描述 |
| gametest/ 五个新增测试类、CombatTestItems、ZombieTests、测试入口JSON | 实际Goal/投射物/装备/生命周期及独立开关回归；标准测试弩仅存在测试包 |
| scripts/Test-S2Persistence.ps1 | 独立服配置升级、实际卸载/重启、索引释放回归 |
| 既有六个 Test-*.ps1 | 当前配置版本断言更新为8；本轮未执行其完整历史流程 |
| README、AGENTS、CONFIGURATION、四份历史报告入口、战斗说明、S2_COMBAT/REPORT/ACCEPTANCE、UPDATE_NOTES | 同步当前行为与验收入口，保留历史测试结果 |
| 父目录 README、docs/FEATURE_MATRIX、docs/TEST_REPORT | 同步阶段状态；**位于26.3 Git根外，不随本次commit保存** |

Java主路径为 src/main/java/com/davidblackcn/buildupmobtweaks；客户端和GameTest分别在独立源码集。未新增依赖、框架、Access Widener、实体/物品注册或附魔资源。完整逐文件清单以本次本地提交为准。

## 验证记录

环境：Minecraft26.3 / Oracle JDK25.0.3（C:\Program Files\Java\jdk-25.0.3）/ Gradle9.7.1 / Loom1.18.3 / Loader0.19.5 / Fabric API0.162.0+26.3 / Fzzy0.7.7+fix3+26.3 / Kotlin1.14.1+kotlin.2.4.20。官方未混淆命名，无Yarn。命令在26.3目录执行，JAVA_HOME为指定JDK。

| 命令/测试 | 环境 | 结果 | 证据 |
|---|---|---|---|
| `.\gradlew.bat build -PtestEula=true --console=plain` | 最终审查源码 | PASS，146项必需GameTest；16s，exit0 | .gradle/s2-final/build-reviewed-3.log |
| `.\scripts\Test-S2Persistence.ps1 -AcceptEula` | 127.0.0.1:25592；隔离世界；EULA已获授权 | PASS，3次启动/正常stop、无ERROR/Mixin异常 | .gradle/s2-final/server.log；.gradle/s2-test-20261008-183326-8e41f7/ |
| `.\gradlew.bat runClient --console=plain` | 无Mod Menu | PASS，资源/音频加载；正常关闭，exit0，1m8s | .gradle/s2-final/client.log，18:44:32加载/18:45:19退出 |
| `.\gradlew.bat runClient -PwithModMenu=true --console=plain` | Mod Menu21.0.0 | PASS，共同加载/正常关闭，exit0，3m20s | .gradle/s2-final/client-modmenu.log，18:45:44加载/18:48:43退出 |
| `javap -c -p` | 已解析的26.3 common/client JAR | PASS，目标类/描述符/原版分支核对 | 本地工具记录及 .gradle/s2-final/ |
| JSON、语言键/占位符、脚本AST、Mixin文件、JAR内容、`git diff --check` | 完整改动 | PASS，369对语言键、19份JSON、7份脚本、版权及生产包隔离 | .gradle/s2-final/static-review.log |
| GUI字段逐项交互、世界视觉、真实双人同步、完整自然袭击 | 人工 | NOT RUN | [统一清单](S2_ACCEPTANCE.md) |
| 第三方领地/AI/动画、真实跨维度转换、密集MSPT/TPS、无缓存clone/远端CI | 补充/S4 | NOT RUN | 不声称兼容、性能或发布验收通过 |
| 旧六份持久化脚本全部流程 | 历史测试 | NOT RUN（本轮） | 既有GameTest已回归，旧结果不重复计为本轮 |

Gradle test 为 NO-SOURCE，不把 GameTest 称为 JUnit。测试使用真实26.3服务器、实体、部分原版Goal及序列化；手动调用Goal的用例不等于完整自然战斗。

### 独立服证据

世界 run/s2-test-20261008-183326-8e41f7。v7→v8保留 diagnosticProbe=false、diagnosticLines=3，并增加S2默认值。固定测试唤魔者 UUID 00000001-0000-0002-0000-000300000004，保存 major_trait=evoker_fireball、major_next=987654321L（三者仅为夹具值）。

远处另一区块三只恼鬼指定该owner。解除其强加载后等待40秒，选择器确认 No entity was found，但owner索引仍为3；重新加载后不重复计数。正常停服后，新JVM保留同UUID、附件与计数。将两个开关设false，查询分别返回false；销毁三只恼鬼后计数0。各次服务端正常退出，无ERROR/注入异常。跨维度共享索引是代码结构，**未实际带实体跨维度运行**。

### 新增回归范围

保留原98项，新增48项，共146项；本轮覆盖恶魂真实Goal/弹速、烈焰人三难度弹数、标准模组弩真实射击、Vex雪球伤害、效果感知、蠹虫寄生/求援、休息事件、女巫预告附件、三火球预算、真实一次图腾与施法/到期边界、互斥和存档、克隆箭预算、恼鬼索引、钓竿耐久、食物容器、僵尸牧师选敌、凋零死亡转化、实际四骑士、有限闪避、真实寻掩体、地形风爆、防点火、原版瞄准/残余横移/友伤目标对照、91项门控独立性、未知附件版本、弱凋灵头owner卸载边界等。不是每个技能都进行了自然生成实战。

### 开发过程失败与修正

保留中间失败日志，没有删除必需用例或降低 Mixin defaultRequire。编译期间核实并修正26.3铲子Tag、彩色旗帜集合、受保护施法枚举、实体Tag API等差异。测试中修正了旧尸壳表面换位时序、三层沙地缺少底部支撑、反射Field身份比较和tick0受伤时间戳夹具。

寻掩体回归发现真实问题：尚未落地时路径请求会失败却预占冷却，已要求落地再搜索，并由MOVE Goal保持路径。验证统计实际曾到达遮蔽，而非强求随后原版走位永久停在屋顶。审查后重复测试发现同tick移除屋顶后读取旧光照会造成露天前置断言不稳定；夹具现清空顶部、建立指定遮蔽，并等待5tick光照更新后才启动AI，随后跟踪实际曾抵达遮蔽。该修正后146项在不同随机位置连续两次通过。服务端新脚本初版PowerShell条件换行解析失败，修正后才完成三次有效运行。

客户端第一次升级旧配置时，Fzzy以WARN提示缺失extended后补默认值；第二次Mod Menu客户端已无该提示。开发身份FabricMC无法Realms授权的INFO仍存在，不是Mixin或Mod崩溃，不声称Realms已验证。mock-player弃用警告保留。

## 源码与许可证审计结论

本项目MIT不变；本轮全部Java/资源为本地独立实现，未复制上游代码、素材或发行JAR反编译实现。功能语义参考[上游公告](https://modrinth.com/mod/mob-ai-tweaks)，API以实际26.3已解析类为证据。上游候选ZIP、分支与L1许可边界沿用 [UPSTREAM_AUDIT](../../docs/UPSTREAM_AUDIT.md)，不把1.11.x发行物称为完整公开源码，不因本轮采用MIT而消除第三方授权疑问。LICENSE、NOTICE无需改动。

新增附魔、专用战利品、失明药水、新武器玩法与相关奖励全部排除；不恢复上游GoalSelector吞Throwable。Boss/深度村民/未来装备附魔项目未实现。

## 风险与遗留

- 未确证旧修复候选3项BLOCKED，详见矩阵；不是当前独立功能已有已知崩溃。
- 实机模型、跳投、骑乘、完整袭击、联机同步及性能需人工；自动门控/加载不替代行为验收。
- 升级前未加载的旧恼鬼只能首次加载后补登记；直接离线删除实体数据可能留下保守名额；不自动全世界扫描清理。
- mobGriefing不等同于第三方领地保护；部分药水/风爆仍会影响其他实体；请按整合包实际组合验收。
- 通用射后侧移是有限导航请求；自定义Brain/控制器与后续原版Goal可能改变实际移动。标准BowItem/CrossbowItem只代表标准协议。
- 关闭不删除已分配的普通装备/坐骑，不重抽旧特性；在途特殊弹体仍保持自身寿命及安全预算。具体契约见S2_COMBAT。

## 人工验收清单

使用 [S2_ACCEPTANCE.md](S2_ACCEPTANCE.md) 的十项统一清单，含操作、期望与失败现象；未代用户勾选。

## 下一阶段建议

S2实机验收后再授权S3-A：Boss控制权骨架与凋灵。当前不实现Boss、不推送、不发布JAR。本轮按持续授权，在验证/自检后仅做一次本地commit。

**当前停止于S2，等待人工验收与下一条指令。**
## 最终摘要与自检

最终构建：build-reviewed-3.log（19:03:28，16s，146项）和 build-confirmed.log（22s，146项）连续通过。第二次专为核实光照夹具稳定性，无源码变化。客户端验证后仅追加通用弱凋灵头预算修正与服务端测试；没有新增客户端Mixin或资源变更。独立服索引/迁移测试之后没有再改该持久化逻辑。

Code Review: PASS WITH RISKS

- Scope：26.3完整本轮差异；共享README/矩阵/测试报告仅同步说明。
- BLOCKER：无；MAJOR：无已知未解决实现缺陷；未确证历史候选与人工验收边界已单列。
- Fixes：寻掩体MOVE控制/落地条件，食物原版容器，真实座椅出口，史莱姆前摇，未知附件保留，弱凋灵头owner卸载预算，测试光照夹具。
- Verification：146项连续通过，独立服三次正常启动/保存，两个客户端环境加载/退出；静态检查通过。
- Update Notes：已追加面向用户的已验证变更，与本地提交一起保存。
- Residual Risks：十项人工清单、三条BLOCKED历史候选、第三方组合/性能/真实跨维度未验证，不能当作发布放行。

生产JAR SHA256：7727CF89E9678689E89BD05D2CDBBB8440BD99C50D0A83C0111BC601B10E55C2。sources JAR：E0A859B1B11C2E8345B0E074F8E6F337DD443B7F486183F7DE5D4CF6EBA83937。构建产物仅本地验证，不提交、不发布。
