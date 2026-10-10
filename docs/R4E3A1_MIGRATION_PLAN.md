# R4-E3-a-1 具体规划：灾厄关系与傀儡报复

状态：2026-10-10 **仅规划，实施待用户明确授权**。总范围/待裁决事项见[R4E3_PLAN](R4E3_PLAN.md)。基线1343ad0，26.3-tests只读，当前三个ID均有旧配置声明但FeatureRegistry.supported为false；不能据此当成已实现。

## 环境与来源

| 项目 | 来源 | 26.3目标 |
| --- | --- | --- |
| 游戏/映射 | 官方CExkBTN8（1.11.0-beta/26.2，官方名）、bFb56Zw2（1.11.3/1.21.11，中间名） | 26.3官方未混淆命名，不能按中间名直接写Mixin |
| 研究输入 | R0核验的两发行JAR与恢复的MIT源码；未把旧S2当核心来源 | IllagerEntityMixin、AbstractZombieEntityMixin、ZombieVillagerEntityMixin、IronGolemEntityMixin、SnowGolemEntityMixin |
| 工具链 | 已有项目配置与解析缓存；源JAR不是待升级Git工程，不假造其Loom/Gradle版本 | Java25.0.3、Gradle9.7.1、Loom1.18.3、Loader0.19.5、Fabric API0.162.0+26.3；Fzzy/Kotlin不变 |
| 现行链 | BuildupMobTweaks注册rebuild模块，原combat/IllagerRelations、GeneralHostileRules未注册/未进入新实现源集 | 新窄关系模块；不能恢复旧全局setTarget覆盖、扫描与免伤逻辑 |
| 保存/配置 | 当前v10与现有两个关系开关、傀儡开关，均默认true但未生效 | 先复用独立门；不需先升级配置。瞬时目标归属不持久化，不能把关闭的旧配置偷偷开回true |

## 三项行为边界

| ID | 分类 | 计划表现/限制 |
| --- | --- | --- |
| illager_zombie_conflict | ADAPT | 原生Pillager/Vindicator/Evoker/Illusioner与**确切Zombie**双向主动索敌；不自动扩到Husk/Drowned/ZombifiedPiglin、女巫、恼鬼或第三方子类 |
| illager_zombie_villager | ADAPT | 同四类灾厄与确切ZombieVillager双向主动索敌，独立于普通Zombie开关；两个门同时开仅安装一套复合关系Goal，不重叠攻击/查询；职业装备链不改，治疗开始/转为村民后退出自有关系目标 |
| golem_friendly_fire_fix | 条件ADAPT或REPLACE | 先原版复现真实铁/雪傀儡误击导致的自相残杀；只收紧确认有问题的原生报复取得/延续，不取消真实伤害或玩家合法仇恨，不禁止傀儡打敌对生物，不继承性排除任意自定义AbstractGolem |

这是对上游继承扩大范围的主动安全收窄。其他僵尸变体/中立阵营要另外有具体批准；不靠字符串“zombie”匹配。vanilla_ai/单实体disable标签只让当前实体本Mod主动关系Goal退出，**不授予被攻击者免伤或禁止原生受击报复**。总门关闭则双方主动关系Goal退出。

原版目标优先：已有有效目标不抢。新增主动关系建议优先级6/TARGET（原版灾厄玩家2、村民/傀儡3、Johnny4、已实现卫道士支援5；Zombie玩家2、村民/傀儡3、幼龟5，均由当前字节码核验）。本Goal不持MOVE/LOOK，不主动推进近战/弩/施法；原生和现有攻击Goal使用取得的合法目标。实施时实际加载Goal表若有第三方目标控制器，应让位并报告，不假称任意AI兼容。

查询预算草案：每主体至多20世界tick一次、距离上限min(16格,原生FOLLOW_RANGE)，最多16个合格候选，选其中最近可视目标；大密度场景不承诺全局最近。采用原生TargetGoal约束及有界findTarget，不添加全世界管理器或每tick扫描。canUse/canContinue/start/stop由GoalSelector调度。获得目标记录瞬时归属，stop只在mob.getTarget仍为本Goal候选时清除；不能清除被其他Goal/Mod替换的目标。视线丢失/退出宽限沿原生明确冻结，不自行增加永久锁敌。

傀儡实施选择以基准决定：优先仅限确切IronGolem的原生HurtByTargetGoal条件；若新增类/窄Mixin，不能移除所有报复Goal重装，更不能全局Mob.setTarget过滤。雪傀儡原版只有Monster目标Goal，现无证据需要另补报复过滤。原生防卫村庄、玩家声望/仇恨、敌对受击必须保留；若原版覆盖，则REPLACE记录而不新增虚假支持ID。已有PiglinRetaliationMixin和ContinuationMixin触及同一原生Goal，本批必须检查两者早返回/优先级以及猪灵回归。

## 已核对API与仍待实施冻结的点

| 目标 | 已核对签名 | 用途/限制 |
| --- | --- | --- |
| AbstractIllager、Zombie、IronGolem、SnowGolem | registerGoals()V；Zombie另有addBehaviourGoals()V | 当前原版入口存在；优先沿ENTITY_LOAD幂等安装，避免构造期读未注册服务。未知附件版本不得借安装覆盖 |
| NearestAttackableTargetGoal | (Mob,Class,boolean)及(Mob,Class,int,boolean,boolean,TargetingConditions.Selector)构造器 | 可用于原生筛选/有界适配；不猜Java旧Predicate参数，不直接在测试调用canUse/findTarget |
| TargetGoal | canContinueToUse()Z、start()V、stop()V | 目标归属与正常调度；继续条件中不要清其他来源目标 |
| HurtByTargetGoal | (PathfinderMob,Class[])V、canUse()Z、start()V | 铁傀儡当前priority2传空忽略数组，不据此宣称实际误击一定启动报复 |
| 原有Mixin/事件 | RaidAiMixin、ZombieAiMixin及PiglinRetaliation两Mixin；ServerEntityEvents安装在现有模块已有用法 | 若需共享serverAiStep注入须说明窄类型与调用顺序；不引入全局LivingEntity重写。具体新Mixin选择/完整注入点在复现后冻结 |

证据在外层`docs/rebuild/evidence/r4e3-plan/`。实体事件在项目中已有可编译用法；上述签名只证明候选接口存在，不是L1或L2通过。

## 执行顺序与预期文件（尚未创建代码）

| 顺序 | 单元 | 计划文件/范围 | 验证结果要求 |
| --- | --- | --- | --- |
| 1 | 原版/关闭基准，审计关系优先级及傀儡Bug | src/gametest的RelationBehaviorTests与原版隔离专服夹具，原有测试断言保留 | 原版无主动关系/合法报复行为、真实雪/铁投射物和HP/目标链；确认ADAPT/REPLACE，不人为setTarget制造“自然误击Bug” |
| 2 | 两门分离的主动关系 | rebuild/relations/RelationBehavior、RelationGoals、必要瞬时Runtime；onInitialize注册，FeatureRegistry仅接入实际验证项 | 原生Goal自然选敌/互击，玩家等更高优先目标介入、四种开关组合，不引入新增伤害 |
| 3 | 条件傀儡修复 | 仅步骤1证实需要的窄Goal/Mixin及资源登记 | 修前后同场复现，真实伤害仍在、无持续相杀，关门回原版；原有猪灵报复回归 |
| 4 | 独立QA | QaHarness拟增relation_zombie、relation_villager、golem_retaliation；若REPLACE只提供原版覆盖诊断 | 命令名只是计划，不现在宣传可用；真实目标UUID/Goal/查询预算、off原因、cleanup；默认不开NoAI冒充主动行为 |
| 5 | 完整验证与交付 | 功能/来源矩阵、报告、人工清单、CODE_REVIEW、适用UPDATE_NOTES | Wrapper build+qaJar、隔离专服/冷重启、真实原生Raid、上游可行对照；提交后停止，不自动a-2 |

## 必测矩阵与人工路线设计

- 两关系ID分别on/on、on/off、off/on、off/off；双方自然原生Goal取得目标、真实近战/弩/箭/施法伤害，不能只核对目标引用。年龄/职业、己方盟友与team、第三方子类负例另列。
- 玩家/村民/铁傀儡与关系候选同场；玩家由创造/旁观切回生存后由原生Goal取得，不允许夹具先setTarget证明“优先”。已有卫道士支援/Johnny目标不抢，目标被外部替换后退出不得清掉。
- 单门/总门、vanilla_ai、死亡、治疗开始/完成、卸载冷重启、NoAI、LOS/距离退出；恢复只是重新可自然选敌，不恢复在途技能、不补发装备。
- 傀儡基准实际雪傀儡雪球误中铁傀儡，铁→铁真实攻击及合法玩家/敌对攻击；不能先强设目标再称原版自然友伤复现。真实伤害/lastHurtBy/目标/Goal日志同时记录；证明修复不等于全伤害免疫。
- 原生村庄/普通难度五波袭击保留胜利/失败/退出生命周期；受控击杀只能证明生命周期，完整玩家连续实战仍人工验收，不增加原版袭击中不存在的幻术师自然生成。
- 冷重启保持名称/装备组件/数量/原生状态，三个拟定QA场景清理实体和地形，正式包/源码包无QA。每场命令与目标预期在实际实现后写入QA指南。

拟人工主路径：临时世界relation_zombie观察互击→加入生存玩家看原生优先→off/cleanup；relation_villager分门与治愈专项→cleanup；golem_retaliation观察误击仍扣血但不持续互相锁敌→off/cleanup。门盾、骷髅生成剑死亡、钓线与普通玩家行为回归另列。所有新场景和结果当前NOT RUN。

阻断处理：若只能用全局目标覆盖才能成立、伤害/保存复制、持续袭击死锁或无法解释两个开关的独立语义，不宣称完成，给出具体根因/缩小方案；原版已修是REPLACE，不是失败，也不为了凑80支持而开启无实现ID。完整L3不可运行时保留NOT RUN并用静态对照+原版基准+L2限定说明。

本轮未执行以上实施步骤。规划文档自检PASS，实际构建/运行时/人工NOT RUN，UPDATE_NOTES不适用。按用户仅规划的范围，到此停止；需收到下一次明确实施授权才开始E3-a-1代码。
