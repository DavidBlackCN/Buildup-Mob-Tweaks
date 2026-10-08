# S1-C — 装备与 Data Pack 阶段报告

日期：2026-10-08（Asia/Shanghai）。基于本地提交 2cb3fb9；开始时 26.3 工作区干净。用户明确确认 S1-B 验收通过并授权下一阶段，本轮只执行 S1-C。

## 本阶段完成情况

- 阶段/子批次：S1-C；结果：**PASS（本阶段自动验收）**。完成后停止，等待人工复查，不进入 S2-A。
- CORE-08：原版弓、弩、三叉戟、盾牌和七种剑的按需能力识别，与 Traits 解耦。没有提前实现射击、格挡或武器切换 AI。
- CORE-09：版本化 JSON/Codec、实体 ID/Tag、物品 ID/Tag、权重、成年/维度/难度过滤；新僵尸/尸壳/溺尸的一次性空槽分配。默认独立开关开启、概率 100/1000。
- CORE-10：本轮仅验证单次分配、原装备/掉率保持、转换所有权转移、真实死亡单件掉落、存档及重载不补发。拾取/切换/投射物回收未实现，矩阵仍为 IN_PROGRESS。
- CORE-11：真实服务端热重载、文件/来源包错误定位、单条无效规则禁用而其他有效规则继续、修复恢复、停用包回到内置规则。
- CORE-12：按需识别当前实际物品，无缓存陈旧问题、无 Tick 扫描；未知物品不给予未验证能力，保留原版 AI。正式缓存/第三方 Adapter/行为失败回退未实现，矩阵仍为 IN_PROGRESS。
- S1-A/S1-B 用户验收作为历史事实单独记录；本轮不将用户验收等同于 Agent 执行的测试。

| 变更文件 | 原因 |
|---|---|
| equipment/EquipmentPool.java | 最小版本 1 Codec、选择器和有界权重/列表 |
| equipment/EquipmentPools.java | 两阶段 JSON 加载、26.3 Tag 绑定后解析、服务端资源快照、精确错误回退 |
| equipment/EquipmentService.java | 独立出生分配门控、只填空槽、持久化尝试、转换记录继承 |
| equipment/EquipmentCapabilities.java、command/EquipmentCommands.java | 实际物品识别和等级 4 只读诊断 |
| BuildupMobTweaks.java、config/BuildupConfig.java、feature/FeatureId.java、FeatureRegistry.java | 注册装备模块、配置版本 3、独立开关与千分概率 |
| fabric.mod.json、lang/en_us.json、lang/zh_cn.json | 更新实际功能描述和 61 对中英语言键 |
| data/buildupmobtweaks/buildupmobtweaks/equipment_pool/zombie_demo.json | 默认成年僵尸系主手木剑规则 |
| examples/equipment-demo/ | 可复制到世界的数据包示例，121.0 格式，Tag 驱动副手盾牌；不自动安装 |
| src/gametest/EquipmentTests.java 及测试注册/Tag/翻译资源 | 新增 11 项装备测试，不进入正式 JAR |
| scripts/Test-EquipmentPools.ps1、Test-TraitPersistence.ps1 | 真实重载/持久化复现；旧脚本升级为检查配置 v1→v3 |
| README.md、AGENTS.md、docs/CONFIGURATION.md、S1B_REPORT.md、TRAITS.md、EQUIPMENT_POOLS.md、本报告、UPDATE_NOTES.md | 范围、历史验收、格式约定、验证和发布说明 |
| 父目录 README.md、docs/FEATURE_MATRIX.md、docs/TEST_REPORT.md | 同步共享状态；不在 26.3 Git 根内，不包含在本地提交中 |

Java 主源码均位于 src/main/java/com/davidblackcn/buildupmobtweaks/，GameTest 位于 src/gametest/java/com/davidblackcn/buildupmobtweaks/；主资源位于 src/main/resources/。没有修改依赖版本、Wrapper、Gradle 构建设置、Mixin 或 Access Widener，没有复制上游实现。

## 验证记录

固定环境：Minecraft 26.3 / Oracle JDK 25.0.3（C:\Program Files\Java\jdk-25.0.3）/ Gradle 9.7.1 / Loom 1.18.3 / Loader 0.19.5 / Fabric API 0.162.0+26.3 / Fzzy 0.7.7+fix3+26.3 / Kotlin 1.14.1+kotlin.2.4.20。官方未混淆命名；无 Yarn。脚本实际使用 PowerShell 7.6.6。

| 命令/测试 | 环境 | 结果 | 证据 |
|---|---|---|---|
| `.\gradlew.bat compileJava --console=plain` | 新增模块首次编译 | PASS，exit 0 | `.gradle/s1c/compile.log` |
| `.\gradlew.bat build -PtestEula=true --console=plain` | 最终全部代码与断言 | PASS，exit 0，19s；25 项必需 GameTest | `.gradle/s1c/build-final.log`，13:21:21 All 25 required tests passed |
| `.\scripts\Test-EquipmentPools.ps1 -AcceptEula` | 127.0.0.1:25587 独立服务端 | PASS，3 次正常 stop/exit 0 | `.gradle/equipment-test-20261008-131725-a09535/` |
| `.\scripts\Test-TraitPersistence.ps1 -AcceptEula` | 127.0.0.1:25586 独立服务端 | PASS，3 次正常 stop/exit 0；v1→v3 保留旧值，Traits 重载/重启不变 | `.gradle/trait-test-20261008-132121-cf0884/` |
| v2→v3 配置升级 | 装备脚本第一轮 | PASS | diagnosticProbe=false、diagnosticLines=4、traits.commonMarker=false 保留，新增装备默认值 |
| JSON/中英语言键/产物隔离 | 源码、最终 binary/sources JAR | PASS | 61 对语言键；无测试类、测试 Tag 或内嵌依赖，许可/NOTICE 保留 |
| `git diff --check`、PowerShell AST 语法检查、本地 Markdown 链接 | 全部变更 | PASS | 无格式、语法或本地路径错误；未运行外部 CI |
| 新装备 GUI、客户端启动/单机/多人增量回归 | S1-C | NOT RUN | 本轮未操作 GUI；S1-B 用户验收不替代 S1-C 增量验证 |
| 完整刷怪笼方块、跨维度、第三方装备/AI/保护 Mod、性能压力 | 额外场景 | NOT RUN | 不能据模拟原装备或核心 API 测试宣称具体 Mod 兼容 |

25 项测试为 **11 项新增装备测试 + 13 项 Traits 回归 + 1 项 Fabric 内置测试**；没有把 Gradle test NO-SOURCE 当作 JUnit 验收。

新增测试覆盖：整数/小数/版本/未知字段和注册表引用验证，实体/物品 Tag，权重统计，已有自定义名称及磨损的装备与 0.42 原掉率保持，一次分配/真实序列化/物品移走不补发，总开关/独立开关/0 概率/无规则不补发，成年/维度/难度过滤与四个护甲槽，真实两段转换，真实死亡，旧来源/未知版本/Boss 排除，当前实际物品能力识别，正式资源加载器写入服务端资源存储。

### 统计与真实世界证据

- 装备采样：seed=68241，n=60,000。一个含木剑/弓的 Tag 权重 2，盾牌权重 1。结果木剑 19,860、弓 20,153、盾牌 19,987，均在每类期望 20,000 ±700 内；验证 entry 权重后再从 Tag 中均匀取物品。不是性能基准。
- Traits 原有三组共 600,000 样本再次通过；与装备采样合计 660,000 次。
- 测试世界 `run/equipment-test-20261008-131725-a09535/`，区块 [1000,1000]。13:18:07 基线实体 UUID `3e990b32-463b-4579-b7a8-e68f7e24b405` 保存副手 shield。
- 13:18:20 改 Tag 并 `/reload` 后，新实体副手 bow；原实体仍是 shield。
- 13:18:23 未知 ID、13:18:37 坏 JSON、13:18:49 空 Tag 均被精确拒绝。当前装备池保留另一条 zz_valid_husk；13:18:36 尸壳实际获得该有效池木剑。错误规则不拖垮整服。
- 13:19:11 修复后新实体得到 trident；13:19:12 在无规则期间出生的旧实体仍 no_rule，没有补发。
- 13:19:25 停用示例包后，新实体使用内置 zombie_demo 得到主手木剑；额外规则随包停用而移除。
- 13:20:09 移除 forceload 并等待 40 秒后查不到基线实体；13:20:14 重新加载后 UUID/记录/能力完全一致。停服把概率改为 0，13:20:40 新 JVM 启动后仍完全一致。脚本严格比较五次完整查询结果。
- Traits 回归 UUID `0d14f389-525b-4f23-b9bc-bad29ff23892` 的 demo_rare/level=3 在确认区块卸载、重载与概率置零后的新 JVM 中保持完全相同；配置 v1→v3 保留 diagnosticProbe=false、diagnosticLines=3。
- GameTest 实际杀死装备怪物；通过测试预先设置原槽位保证掉落值 2.0，验证恰好一件木剑。生产代码不设置保证掉落值，也不创建死亡奖励。

### 失败与修正

1. 默认沙箱执行器初始化失败（helper setup refresh error）。使用获自动审核允许的执行环境继续；没有自动审核拒绝或待确认操作。
2. 首次完整启动失败：apply 阶段构造 ItemStack 时组件未绑定。核对 ItemStack.isItemEnabled 与 Item.isEnabled 字节码，改为直接检查物品功能标志；仅在运行时分配时创建 ItemStack。
3. 第一轮真实 Tag 数据包启动失败：pending Named Tag 尚未绑定。核对 MappedRegistry.prepareTagReload、WorldLoader 和 MinecraftServer 的标签提交时序，改为资源存储保存定义，SERVER_STARTING/成功 END_DATA_PACK_RELOAD 后解析本次已绑定成员。不是用捕获异常掩盖问题；最终真实 Tag 首次启动与多轮热重载通过。
4. 审查补充小数测试后，发现 Codec.INT 会截断小数。增加严格整数检查并拒绝超出 32 位范围的数；小数版本和权重断言最终通过。
5. 原测试 Tag/示例 Tag 曾有开发环境缺翻译提示；补充测试专用翻译及示例 Tag 中英文名称，不关闭检查器。

最终本地 binary JAR SHA256：`9EA14F34F2F1672C3E1EFDFA60F8E0473990F185B6CC171C9FFE39FA6523C3BF`；sources JAR：`98BEE61E347E02D017F2AE2595B1C0F7368BB954934D8E15E555574E6F462B38`。产物仅在 build/libs 本地保留，不提交、不发布。两个最终脚本的六次服务端均正常退出，无遗留游戏进程。

## 风险与遗留

- 此阶段是装备分配与识别最小闭环。仅支持僵尸/尸壳/溺尸，支持六个手持/人形护甲槽；其他实体/Boss 不参与。没有正式武器 AI、缓存系统或第三方 Adapter 注册框架。
- 模拟已有自定义装备的保留已测；真实外部 Mod 未安装验收，后续 Mod 主动覆盖装备的顺序冲突仍需具体版本测试。
- 数据包规则采用第一条匹配规则，不叠加、不在空槽失败后换池；未知 Tag/不受支持实体会使整条规则禁用。该明确策略与默认 10% 木剑演示须人工确认。
- 原版整个 reload 事务失败时保持旧资源，设计基于已核对的资源存储契约；没有故意构造其他 Mod 重载器异常来实测该额外分支。
- 新 GUI/客户端/联机、完整刷怪笼、跨维度、性能/旧真实世界仍未测，不扩大兼容声明。
- 本轮独立实现，没有移植跨版本源码，不触发 Migration Plan 实施门。项目 MIT 不变，Fabric 模板 CC0 记录保留，外部依赖未内嵌。上游 L1 直接复制边界仍按原审计处理；不把发行 JAR 当源码。
- 未新增附魔、附魔战利品、Boss 控制权、Stellarity ID、村民机制；没有发布或推送。

## 自检

Code Review: **PASS WITH RISKS**。

- Scope：目标仅 26.3 与三份明确相关共享文档；Reviewed files 为上述变更表全部文件。
- BLOCKER：无；必需构建和装备核心验收通过。
- MAJOR：无已确认缺陷；尚未运行的额外场景明确披露。
- MINOR：无待修正项；期间修正 Tag/组件加载时序、严格整数校验及翻译提示。
- common/client/server：服务端事件和资源存储；无客户端类引入 common，无新 Mixin、反射或 Tick 扫描。两个新增命令只读且等级 4。
- 注册/数据：资源快照归当前服务端，尝试记录持久化；不改 Traits 格式，不重建已存在物品，不修改掉率。坏规则失败时禁用，不静默沿用旧池。
- Verification：最终 build/GameTest、装备脚本、Traits 回归、JSON/产物/语言、格式/语法/本地链接检查均 PASS。
- Update Notes：已追加 `✨ feat(equipment)`，描述已验证的分配、查询、保持与坏规则停用；同本阶段提交一起保存。
- 残余风险：新增 GUI 和实际第三方环境未验收。自动验收 PASS 不代替进入下一阶段前的人工确认。

## 人工验收清单

- [ ] 中英 GUI 展示 equipment.poolAssignment、assignmentChance，范围与默认 10% 正确；关闭分配后新实体不配装，旧实体装备保留。
- [ ] 按 EQUIPMENT_POOLS.md 安装示例，临时把概率设为 1000；新僵尸空副手获得盾牌，已有装备不覆盖。
- [ ] 修改 Tag 并 `/reload` 后只影响新实体；错误文件有可定位日志，其他规则继续；停用示例包恢复内置主手木剑。
- [ ] 确认第一条匹配、失败不补发、原掉率保持、旧实体跳过符合整合包预期。
- [ ] 单机与多人增量配置/权限复查；用实际第三方装备 Mod 和转换/跨维度/刷怪笼场景继续兼容回归。
- [ ] 接受本报告后再明确授权 S2-A。

## 下一阶段建议

下一任务块：**S2-A：骷髅系与基础装备 AI**。前置条件是 S1-C 人工验收通过及新的明确指令；上游复用仍须遵循审计许可边界。当前不执行。

**当前停止，等待人工验收与下一条指令。**