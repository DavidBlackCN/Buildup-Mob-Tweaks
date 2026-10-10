# Phase 1 功能逐项取舍（R4-E0）

> R4-E2-c当前完整交付：[报告](R4E2C_REBUILD.md)/[人工清单](R4E2C_ACCEPTANCE_CHECKLIST.md)。四项幻术师/卫道士行为，配置v10、77支持/47未生效、独立QA25场景；298项真实调度验证。原生五波袭击生命周期（受控击杀推进）、实际客户端与专服/冷重启限定PASS；上游仅有限对照。人工连续袭击/完整L3/自然分布/多人整合包/性能NOT RUN。批末本地提交后停止，不进入E3/E4/Boss/R5/Phase2/3。

> R4-E2-b历史完整交付：[报告](R4E2B_REBUILD.md)/[人工清单](R4E2B_ACCEPTANCE_CHECKLIST.md)。五项职业武器及G38，配置v10、73支持/51未生效、QA22；272项真实调度PASS。客户端/独立专服/冷重启限定PASS，上游仅有限对照；人工/完整L3/自然分布/多人整合包/性能NOT RUN。批末本地提交后停止，不进入E2-c/R5/Phase2/3。

> R4-E2-a历史限定交付：[报告](R4E2A_REBUILD.md)/[人工清单](R4E2A_ACCEPTANCE_CHECKLIST.md)。装备池与两类出生武器闭环，配置v10、68支持/56未生效、QA17；231项真实调度PASS。僵尸猪灵弩AI仍E2-b，人工/完整L3/自然刷怪器/多人/性能NOT RUN。批末提交后停止，不进入E2-b/R5/Phase2/3。

> R4-E1-c历史限定交付：[报告](R4E1C_REBUILD.md)/[人工清单](R4E1C_ACCEPTANCE_CHECKLIST.md)。两项接入，66支持/58未生效、QA15场景；`stray_omen_snow`纠正为LEGACY_ONLY/Phase2 DEFER。真实调度及客户端/有限上游结果见收据；肉眼/完整L3/自然刷怪器/多人/性能NOT RUN。不进入E2/R5/Phase2/3，旧批次陈述仅历史。

当前68项取舍计数为 PORT 1 / ADAPT 49 / REPLACE 5 / DEFER 9 / EXCLUDE 4；与下文保留的E0计数区别仅为流浪者不祥项来源纠正。这是取舍分类，不是当前生效ID计数。


> E1-b历史限定交付：[报告](R4E1B_REBUILD.md)/[人工清单](R4E1B_ACCEPTANCE_CHECKLIST.md)。四项猪灵基础ID已接入，真实Tick/Brain/Goal 178项必需测试PASS，当前64支持/60未生效，QA共12场景。肉眼/完整L3/自然刷怪器/多人/性能NOT RUN；下一批E1-c待具体方案批准。下文E0/E1-a数字与未实施声明只代表历史时点，不进入R5。

> E1-a历史限定交付见[R4E1A_REBUILD](R4E1A_REBUILD.md)和[人工清单](R4E1A_ACCEPTANCE_CHECKLIST.md)。当前支持ID为60（旧56+四项），其余64个ID未生效；wither_skeleton_bow出生发弓仍待E2。本文件原E0数字/结论保留历史语境。

基线 26.3 / HEAD `6eec6387a33d23a98f63c15f78a77b1ea0891423`。这是逐项审计决定，不是新增功能验收。68/68 ID 一次归属；当前56白名单不变。本轮只实现开发QA，不移植以下未生效AI。裁决计数：{'REPLACE': 5, 'ADAPT': 50, 'EXCLUDE': 4, 'PORT': 1, 'DEFER': 8}。

原始证据是 R0 已核验官方 CExkBTN8（26.2 Beta）和 bFb56Zw2（1.21.11），MIT采用依据及历史CC0残留保留于[ARTIFACTS](../../docs/rebuild/ARTIFACTS.md)，不删原版权。完整类/方法、注入声明、实际配置调用行、原始默认值、逐文件SHA256和两版差异见[upstream-index.json](../../docs/rebuild/evidence/r4e0/upstream-index.json)与[disposition.json](../../docs/rebuild/evidence/r4e0/disposition.json)。上游类门控不是本项目该ID的默认值；默认数值仅作为设计证据，不能跨版本机械复制。bFb56Zw2方法有intermediary命名；不据字符串差异宣称行为改变。未确定完整运行差异=NOT RUN。

E0时点所有68项 `FeatureRegistry.supported=false`；E1-a/E1-b共8项现已接入，余60项仍未生效，未安装旧S2 Goal/Mixin，配置v9保留读取。ADAPT/PORT未实施状态均NOT RUN；REPLACE只给已有行为归宿，不把旧ID升级为PASS。DEFER/EXCLUDE不计Phase1实施门槛；后续R5逐项更新UI/迁移说明，E0不删字段。每项还必须在所属批次核对26.3原版是否已有修复，给出确切新Goal优先级/Flags或注入描述符及默认配置；本审计不授权新增注入。

统一验证要求：L1加载；L2真实Tick/Goal/Brain、QA定向、独立关闭、创造/旁观、死亡/转换/存档/卸载；L3隔离两版上游对照（不能运行则BLOCKED及替代依据）；默认出生样本与强制QA分开。多人/整合包/性能未验证不得写PASS。

| ID / 用户可见行为及决定理由 | 来源 / 上游证据类 | 决策 / 批次 / Phase1门槛 | 相互影响与验证重点 / 状态 |
| --- | --- | --- | --- |
| `traits`：保留现有各模块出生附件和traits.enabled门 | PRODUCT_SPEC / LEGACY_ONLY旧Marker | **REPLACE** / E2-a确认/R5说明 / 是（替代说明） | 旧Marker服务和旧ID仍未启用；新装备与Trait门独立，模块出生/保存回归 **限定PASS**；不新加一次通用标记抽签 |
| `equipment_assignment`：真实装备池单次出生空槽分配 | PRODUCT_SPEC；`MobAITweaks`（来源意图，采用Buildup严格池） | **ADAPT** / E2-a / 是 | 真实资源标签重载、原版Goal使用、组件/掉率、转换/保存/死亡/禁用/无规则不补发 **L2 PASS**；支持五种精确类型，第三方AI/人工 **NOT RUN** |
| `demo_common`：演示Marker不构成生物技能，后续配置说明弃用 | LEGACY_ONLY；无明确上游对应；旧S2引用见JSON | **EXCLUDE** / 不计门槛 / 否 | 独立门 `disable_demo_common` 仅待实现；不改变现有模块出生概率；**NOT RUN** |
| `demo_advanced`：演示Marker无正式执行能力，排除 | LEGACY_ONLY；无明确上游对应；旧S2引用见JSON | **EXCLUDE** / 不计门槛 / 否 | 独立门 `disable_demo_advanced` 仅待实现；不伪称高级AI生效；**NOT RUN** |
| `demo_rare`：演示Marker无正式执行能力，排除 | LEGACY_ONLY；无明确上游对应；旧S2引用见JSON | **EXCLUDE** / 不计门槛 / 否 | 独立门 `disable_demo_rare` 仅待实现；不伪称稀有技能生效；**NOT RUN** |
| `skeleton_aim_fix`：lookAtTarget意图由SkeletonGoals.Walk及Sniper原版控制承担，不重新注入旧BowGoal | UPSTREAM；`RangedBowAttackGoalMixin` | **REPLACE** / E1-c确认/R5说明 / 是（含替代说明） | 旧ID保持未生效；已有看向/射箭链实际高位横移后再次命中，限定L2 **PASS**；第三方所有武器兼容 **NOT RUN** |
| `skeleton_offhand_visual`：真实新拉弓时挥动实际用弓手的另一只手，由服务端同步门控制模型 | UPSTREAM；`RangedBowAttackGoalMixin`, `SkeletonEntityModelMixin` | **ADAPT** / E1-c / 是（含替代说明） | 独立门、四种主臂/用弓手、仅visual开启原版Goal及真实客户端模型 **PASS**；不复制箭；人眼验收 **NOT RUN** |
| `skeleton_trap_variants`：陷阱坐骑与骑手变体仅在原版陷阱事件追加有限选择 | UPSTREAM；`SkeletonTrapMixin` | **ADAPT** / E3 / 是（含替代说明） | 独立门 `disable_skeleton_trap_variants` 仅待实现；原版队伍/生成为一次/骑乘递归保存；不额外材料；**NOT RUN** |
| `skeleton_wither_conversion`：转为凋灵骷髅保留装备所有权，不原样套用上游转换 | UPSTREAM；`SkeletonEntityMixin` | **ADAPT** / E3 / 是（含替代说明） | 独立门 `disable_skeleton_wither_conversion` 仅待实现；具体触发条件待批次核实；UUID/主副手/备用/冷却；**NOT RUN** |
| `wither_skeleton_bow`：出生真实弓与实际装备使用 | UPSTREAM；`WitherSkeletonEntityMixin` | **ADAPT** / E1-a行为+E2-a出生 / 是 | 真实空初始化手、池/独立概率/门、原版Bow Goal箭伤/耐久/保存/死亡 **L2 PASS**；仅原版弓，完整L3/人工 **NOT RUN** |
| `wither_skeleton_homing`：头颅追踪只对合法既有目标、有期限与半径预算；不复制OP分支 | UPSTREAM；`SkeletonSpecificGoal` | **ADAPT** / E1 / 是（含替代说明） | E1-a原生弹体/移动不祥目标实际弯曲命中、固定归属/退出/独立关闭L2 **PASS**；人工/完整L3 **NOT RUN** |
| `wither_skeleton_skull`：保留有限特殊头颅攻击与前摇/冷却，不复制原版Boss状态机 | UPSTREAM；`WitherSkeletonEntityMixin`, `SkeletonSpecificGoal` | **ADAPT** / E1 / 是（含替代说明） | E1-a30tick前摇/一枚/400冷却/100寿命；真实伤害、保护、预算、关闭/保存L2 **PASS**；人工/完整L3 **NOT RUN** |
| `parched_dodge`：还原焦骸旋转位移；不照搬已发现旋转及速度Bug | UPSTREAM；`ParchedEntityMixin`, `SkeletonSpecificGoal` | **ADAPT** / E1 / 是（含替代说明） | E1-a20tick预警/10tick旋转位移/120冷却；实际Tick/退出及客户端状态L2 **PASS**；肉眼/完整L3 **NOT RUN** |
| `bogged_omen_mushrooms`：已有孢子后撤内补上游已核实的不祥目标双蘑菇分支 | UPSTREAM；`SkeletonSpecificGoal`, `BoggedEntityMixin` | **ADAPT** / E1-c / 是（含替代说明） | 独立门与底层孢子门、真实碰撞/伤害云、两球两云预算、目标退出及保存清理，L2 **PASS**；Beta有限对照 **PASS**，完整L3及人工 **NOT RUN** |
| `stray_omen_snow`：旧S2渐加速雪球；E1-c纠正E0误判，上游FLIP无ominous分支 | **LEGACY_ONLY**；旧SkeletonExtras snow_omen；两版上游仅Bogged有ominous分支，Stray缓慢属OP | **DEFER** / Phase2独立平衡设计 / 否 | ID仍未生效；同一跃射雪球链普通/不祥无额外速度差异，实际REPLACE回归PASS；新自创玩法NOT RUN |
| `stray_snow_barrage`：已有STRAY_JUMP_SHOT承担起跳与三雪球；不双重运行 | UPSTREAM；两版SkeletonSpecificGoal FLIP | **REPLACE** / E1-c已核验 / 是（替代说明） | 旧ID保持未生效；真实一次跃射三雪球，普通/不祥同链PASS；统一由disable_stray_jump_shot控制；无独立不祥分支 |
| `zombified_piglin_anger_fix`：复现原版同类伤害/同伴仇恨后窄修，保留受伤和玩家警戒 | UPSTREAM + LEGACY_ONLY；`ZombifiedPiglinEntityMixin`的HurtByTargetGoal忽略同类。纠正E0“只提供弩”误分类，历史JSON不改 | **ADAPT** / E1-b / 是 | 原Goal不替换；禁用/重新启用缓存目标、实际群体与玩家、创造/旁观L2 **PASS**；Beta有限对照，完整L3/人工 **NOT RUN** |
| `piglin_shot_swing`：真实弩发射后挥实际手，按26.3 swingForAttack适配 | UPSTREAM；`PiglinEntityMixin` | **PORT**（当前API适配） / E1-b / 是 | native Brain真正主副手射击、空射不计、独立关闭；客户端28/28挥手样本 **PASS**；人工/完整L3 **NOT RUN** |
| `piglin_item_dodge`：近距蓄弩后跳，不是掉落物闪避 | UPSTREAM；`PiglinEntityMixin`；旧S2语义偏差 | **ADAPT** / E1-b / 是 | 成人出生一次7%、200冷却、10动作窗口、合法地面一次冲量；真实位移/危险回退、门控/保存L2 **PASS**；自然刷怪器/人工/完整L3 **NOT RUN** |
| `piglin_crossbow_compatibility`：CrossbowItem继承、实际主副手/射程/姿势，保留原Brain和矛 | UPSTREAM；`PiglinBrainMixin`, `PiglinEntityMixin`, `RangedCrossbowAttackTaskMixin` | **ADAPT** / E1-b / 是 | 真正副手弩不被清理搬走，黄金替换仍转移实际物；装填/耐久/交易/拾取/转换/禁用L2 **PASS**；Beta有限对照；任意枪械/完整L3/人工 **NOT RUN** |
| `zombified_piglin_spawn_crossbow`：原版初始化时池选真实稀有弩 | UPSTREAM；`ZombifiedPiglinEntityMixin` | **ADAPT** / E2-a / 是 | 原生空主手、出生概率/独立门、幼体、原金剑/金矛回退、保存/死亡 **L2 PASS**；弩远程AI仍E2-b **NOT RUN** |
| `zombified_piglin_crossbow`：真实主副手弩装填/原版箭/耐久，原生受击仇恨与近战回退，已装弹和装备保存 | UPSTREAM；`ZombifiedPiglinEntityMixin` | **ADAPT** / E2-b / 是 | **L2 PASS**；G38限定客户端、独立QA/保存/生命周期通过；完整L3/人工/整合包 **NOT RUN**；见R4E2B_REBUILD |
| `illager_zombie_conflict`：窄阵营冲突，保留玩家优先而非全局setTarget覆盖 | UPSTREAM；`IllagerEntityMixin`, `AbstractZombieEntityMixin` | **ADAPT** / E3 / 是（含替代说明） | 独立门 `disable_illager_zombie_conflict` 仅待实现；完整袭击/村庄/玩家目标/禁用/互相索敌；**NOT RUN** |
| `illager_zombie_villager`：僵尸村民独立敌对关系与普通僵尸重叠按一次处理 | UPSTREAM；`IllagerEntityMixin`, `ZombieVillagerEntityMixin` | **ADAPT** / E3 / 是（含替代说明） | 独立门 `disable_illager_zombie_villager` 仅待实现；职业/转化/袭击/玩家优先；**NOT RUN** |
| `illager_boats`：自动寻找与使用船会改变袭击/运输控制，首发暂缓 | UPSTREAM；`IllagerEntityMixin`, `RaiderUseBoatGoal` | **DEFER** / Phase3生态/整合包 / 否 | 独立门 `disable_illager_boats` 仅待实现；不阻断Phase1；所有权与玩家船/卸载另审；**NOT RUN** |
| `illusioner_omen_spawn`：becomeIllusioner会丢弃原动物并生成幻术师；不采用这种破坏生态的出生 | UPSTREAM；`LivingEntityMixin` | **EXCLUDE** / 不计门槛 / 否 | 独立门 `disable_illusioner_omen_spawn` 仅待实现；后续幻术师采用明确场景/命令，不替换玩家宠物；**NOT RUN** |
| `illusioner_swap`：分身换位采用有限合法位置和真实目标，保持原版隐身 | UPSTREAM；`IllusionerEntityMixin` | **ADAPT** / E2-c / 是（含替代说明） | 首次原生隐身/受击结束/1200tick刷新，四个有限安全位置；200tick换位、行走同步、NoAI/目标/门退出，真实Tick与客户端 **L2 PASS**；完整L3/人工 **NOT RUN** |
| `illusioner_shoot_pause`：简化弓攻击阶段与换位间暂停；不增加隐藏并行射击 | UPSTREAM；`IllusionerEntityMixin` | **ADAPT** / E2-c / 是（含替代说明） | 保留原生弓Goal及施法；真实拉弓停止走位、射后20/换位后10tick暂停；实际箭伤/独立关闭/实际主副手模型 **L2 PASS**；人工/第三方AI **NOT RUN** |
| `illusioner_clone_arrows`：真实箭从有限分身位置发射，归属同一个幻术师 | UPSTREAM；`IllusionerEntityMixin` | **ADAPT** / E2-c / 是（含替代说明） | 原生出生70/1000一次账本；120tick至多双箭替代一次原箭，真实安全箭源/Power和Flame/伤害/同盟过滤/不可拾取/200tick期限，保存不恢复在途 **L2 PASS**；自然分布/完整L3/人工 **NOT RUN** |
| `vindicator_support`：旧S2卫道士支援是Buildup选择性玩法，非已确认上游技能 | LEGACY_ONLY；无明确上游对应；旧S2引用见JSON | **ADAPT** / E2-c / 是（含替代说明） | LEGACY_ONLY/Buildup；priority5/TARGET，最多8名8格同盟施法者的100tick内攻击者；20tick扫描、16格敌人/LOS、不抢原目标/仅清拥有目标；原生近战及五波袭击生命周期（受控击杀） **L2 PASS**；连续实战/多人 **NOT RUN** |
| `evoker_flee_speed`：旧S2避让速度参数；上游这两类未证明同项独立配置 | LEGACY_ONLY；`EvokerEntityMixin`, `FleeEntityGoalMixin` | **ADAPT** / E4 / 是（含替代说明） | 独立门 `disable_evoker_flee_speed` 仅待实现；26.3原版AvoidEntityGoal实测需求后窄适配，不改已通过火球仲裁；**NOT RUN** |
| `evoker_avoid_target_fix`：旧S2避免目标处理候选，非明确上游独立修复 | LEGACY_ONLY；`EvokerEntityMixin` | **ADAPT** / E4 / 是（含替代说明） | 独立门 `disable_evoker_avoid_target_fix` 仅待实现；复现当前Bug优先，LOS/创造/旁观与真实施法回归；**NOT RUN** |
| `drowned_fishing_pull`：既有真实竿和原版自然发竿，水陆射手/非水下目标/三叉戟在途互斥，原生碰撞拉拽与保存 | UPSTREAM；`DrownedAttackGoalMixin`, `FishingBobberEntityMixin` | **ADAPT** / E2-b / 是 | **L2 PASS**；G38限定客户端、独立QA/保存/生命周期通过；完整L3/人工/整合包 **NOT RUN**；见R4E2B_REBUILD |
| `zombie_fisherman_rod`：精确渔夫职业，一次空手出生竿，原生浮漂命中/拉拽位移、真实磨损/单钩所有权、退出守恒 | UPSTREAM；`ZombieVillagerEntityMixin`, `FishingBobberEntityMixin` | **ADAPT** / E2-b / 是 | **L2 PASS**；G38限定客户端、独立QA/保存/生命周期通过；完整L3/人工/整合包 **NOT RUN**；见R4E2B_REBUILD |
| `zombie_cleric_potions`：精确牧师职业，一瓶真实HARMING消耗治疗受伤亡灵，独立支援目标，关闭/消费后重载不补发 | UPSTREAM；`ZombieVillagerEntityMixin` | **ADAPT** / E2-b / 是 | **L2 PASS**；G38限定客户端、独立QA/保存/生命周期通过；完整L3/人工/整合包 **NOT RUN**；见R4E2B_REBUILD |
| `zombie_fletcher_crossbow`：精确制箭师职业，一次空手出生弩，真实射击、已装填后追赶、职业变更/治愈/转换不补发 | UPSTREAM；`ZombieVillagerEntityMixin` | **ADAPT** / E2-b / 是 | **L2 PASS**；G38限定客户端、独立QA/保存/生命周期通过；完整L3/人工/整合包 **NOT RUN**；见R4E2B_REBUILD |
| `zombie_active_guard`：旧高级格挡由已实现ZOMBIE_SHIELD_USE真实盾牌承担；不叠加第二Trait格挡 | LEGACY_ONLY；无明确上游对应；旧S2引用见JSON | **REPLACE** / R5说明收口 / 是（含替代说明） | 独立门 `disable_zombie_active_guard` 仅待实现；Z01独立门与装备耐久/停用，旧字段后续迁移说明；**NOT RUN** |
| `husk_spear_charge`：现有原版矛及尸壳坐骑条件下有限冲刺；不生成新武器 | UPSTREAM；`ChargeSpearGoalMixin`, `ChargeSpearTaskMixin`, `HuskEntityMixin` | **ADAPT** / E3 / 是（含替代说明） | 独立门 `disable_husk_spear_charge` 仅待实现；ATTACK_RANGE/击中预算/距离/碰撞/骑乘停止；**NOT RUN** |
| `husk_camel_circle`：骆驼尸壳载体环绕原版目标，保留控制乘客 | UPSTREAM；`CamelHuskEntityMixin`, `HuskEntityMixin` | **ADAPT** / E3 / 是（含替代说明） | 独立门 `disable_husk_camel_circle` 仅待实现；原版26.3CamelHusk生命周期/骑手退出/半径；**NOT RUN** |
| `skeleton_horse_charge`：原版骷髅马骑手特殊冲刺，有限冷却 | UPSTREAM；`SkeletonEntityMixin`, `SkeletonHorseEntityMixin` | **ADAPT** / E3 / 是（含替代说明） | 独立门 `disable_skeleton_horse_charge` 仅待实现；骑手和坐骑只有一套移动控制/加载/目标退出；**NOT RUN** |
| `chicken_jockey_charge`：鸡骑士原版ZombieAttackGoal近距扑击，非额外生成骑士 | UPSTREAM；`ZombieAttackGoalMixin` | **ADAPT** / E3 / 是（含替代说明） | 独立门 `disable_chicken_jockey_charge` 仅待实现；同一原生Chicken/NoAI/原版伤害/卸载/恢复速度；**NOT RUN** |
| `zombie_slime_carrier`：史莱姆携带与生成/分裂/递归骑乘风险，首发暂缓 | UPSTREAM；`AbstractZombieEntityMixin` | **DEFER** / Phase3变种审批 / 否 | 独立门 `disable_zombie_slime_carrier` 仅待实现；不阻断Phase1；无限拆分和保存需专项；**NOT RUN** |
| `zombie_horse_leader`：首领与马/召集会增加实体预算及掉落，首发暂缓 | UPSTREAM；`AbstractZombieEntityMixin`, `ZombieHorseEntityMixin` | **DEFER** / Phase3变种审批 / 否 | 独立门 `disable_zombie_horse_leader` 仅待实现；不阻断Phase1；自然样本/刷怪塔/同伴生命周期；**NOT RUN** |
| `rider_friendly_fire`：精准排除自己的坐骑/乘客，保留敌方骑乘命中 | UPSTREAM；`PersistentProjectileEntityMixin` | **ADAPT** / E3 / 是（含替代说明） | 独立门 `disable_rider_friendly_fire` 仅待实现；箭/三叉戟/关系改变/友伤；避免广泛免伤；**NOT RUN** |
| `strafe_hazard_check`：现有SkeletonBehavior.safeStep提供目标骨架范围的危地检查；不恢复全局isWalkable覆盖 | UPSTREAM；`MoveControlMixin` | **REPLACE** / E4确认/R5说明 / 是（含替代说明） | 独立门 `disable_strafe_hazard_check` 仅待实现；火/落差/阻挡/其余原版不接管；并非所有生物同等复原；**NOT RUN** |
| `movement_jump_input`：jumpMotionFix意图只在验证过的重建实体保留跳跃横移输入 | UPSTREAM；`MoveControlMixin` | **ADAPT** / E4 / 是（含替代说明） | 独立门 `disable_movement_jump_input` 仅待实现；禁止反射枚举/吞Throwable；先对照26.3原版输入；**NOT RUN** |
| `movement_clear_strafe`：keepMovingForward/stopSidewaysMovement防遗留横移，只窄控制器适配 | UPSTREAM；`MoveControlMixin` | **ADAPT** / E4 / 是（含替代说明） | 独立门 `disable_movement_clear_strafe` 仅待实现；已通过走射/后撤/连击不得倒退；**NOT RUN** |
| `strafe_obstacle_jump`：水平碰撞时小障碍起跳，门/栅栏/天花板安全优先 | UPSTREAM；`MoveControlMixin` | **ADAPT** / E4 / 是（含替代说明） | 独立门 `disable_strafe_obstacle_jump` 仅待实现；JumpControl真实起跳；不照搬反射+ordinal脆弱钩子；**NOT RUN** |
| `sense_glowing`：有限距离发光可感知，不能绕过团队/不可攻击条件 | UPSTREAM；`LivingEntityMixin` | **ADAPT** / E4 / 是（含替代说明） | 独立门 `disable_sense_glowing` 仅待实现；隔墙/范围/创造/非敌对原版，独立gate；**NOT RUN** |
| `sense_blindness`：失明缩短感知；旧S2系数.25和上游1/3不同 | UPSTREAM；`ActiveTargetGoalMixin` | **ADAPT** / E4 / 是（含替代说明） | 独立门 `disable_sense_blindness` 仅待实现；窄目标选择+保持既有伤害仇恨，不全局覆盖LOS；**NOT RUN** |
| `sense_darkness`：旧S2按周期改变感知距离，未定位上游对应配置 | LEGACY_ONLY；无明确上游对应；旧S2引用见JSON | **DEFER** / Phase2规则 / 否 | 独立门 `disable_sense_darkness` 仅待实现；不阻断Phase1；先制定独立新规则；**NOT RUN** |
| `sense_nausea`：反胃影响视距与弹道；只对正式支持实体 | UPSTREAM；`ActiveTargetGoalMixin`, `ProjectileEntityMixin` | **ADAPT** / E4 / 是（含替代说明） | 独立门 `disable_sense_nausea` 仅待实现；玩家射击不改/真实命中分布/独立gate；**NOT RUN** |
| `sense_invisibility`：隐身前置感知；原版已有隐身距离缩放需避免重复缩放 | UPSTREAM；`ActiveTargetGoalMixin` | **ADAPT** / E4 / 是（含替代说明） | 独立门 `disable_sense_invisibility` 仅待实现；受击追踪/装备可见度/退出目标；**NOT RUN** |
| `sense_crawling`：爬行降低发现距离，上游1/3供参考 | UPSTREAM；`ActiveTargetGoalMixin` | **ADAPT** / E4 / 是（含替代说明） | 独立门 `disable_sense_crawling` 仅待实现；原版姿态与几何高度，不改玩家hitbox；**NOT RUN** |
| `sense_crouching`：潜行降低发现距离，上游1/2供参考 | UPSTREAM；`ActiveTargetGoalMixin` | **ADAPT** / E4 / 是（含替代说明） | 独立门 `disable_sense_crouching` 仅待实现；原版已有潜行规则，不能二次无意倍乘；**NOT RUN** |
| `melee_effect_interval`：急迫/疲劳调节原版攻击间隔，设置下限 | UPSTREAM；`MeleeAttackGoalMixin` | **ADAPT** / E4 / 是（含替代说明） | 独立门 `disable_melee_effect_interval` 仅待实现；上游加减tick与旧S2倍率不同；伤害冷却/多目标；**NOT RUN** |
| `melee_high_target_jump`：困难/小型/近高目标JumpControl起跳 | UPSTREAM；`MeleeAttackGoalMixin` | **ADAPT** / E4 / 是（含替代说明） | 独立门 `disable_melee_high_target_jump` 仅待实现；原版高度/地板/水/骑乘/冷却回退；**NOT RUN** |
| `omen_pressure`：旧S2不祥加速攻击，不能当作上游ominous_mobs全部语义 | LEGACY_ONLY；无明确上游对应；旧S2引用见JSON | **DEFER** / Phase2平衡规范 / 否 | 独立门 `disable_omen_pressure` 仅待实现；不阻断Phase1；避免无条件增伤/加速；**NOT RUN** |
| `hostile_rest`：坐下/关AI影响索敌和实体状态，首发不纳入 | UPSTREAM；`AbstractSkeletonEntityMixin`, `AbstractZombieEntityMixin` | **DEFER** / Phase3生态 / 否 | 独立门 `disable_hostile_rest` 仅待实现；不阻断Phase1；保存/唤醒/多人需专项；**NOT RUN** |
| `rest_sound_wake`：声响惊醒依赖休息，随父功能暂缓 | UPSTREAM；`AbstractSkeletonEntityMixin`, `AbstractZombieEntityMixin` | **DEFER** / Phase3依赖hostile_rest / 否 | 独立门 `disable_rest_sound_wake` 仅待实现；不新增每Tick全世界声学扫描；**NOT RUN** |
| `golem_friendly_fire_fix`：原版仍有可复现同族互相目标时窄过滤 | UPSTREAM；`IronGolemEntityMixin`, `SnowGolemEntityMixin` | **ADAPT** / E3 / 是（含替代说明） | 独立门 `disable_golem_friendly_fire_fix` 仅待实现；不得禁止合法敌对；雪球/铁傀儡/第三方继承另审；**NOT RUN** |
| `drowned_swim_visual`：游泳姿态和模型同步仍独立于已实现三叉戟 | UPSTREAM；`DrownedEntityMixin`, `DrownedEntityModelMixin`, `DrownedEntityRendererMixin` | **ADAPT** / E4 / 是（含替代说明） | 独立门 `disable_drowned_swim_visual` 仅待实现；仅视觉前置；与原版26.3泳姿复核，专服同步；**NOT RUN** |
| `burning_projectile_visual`：真实燃烧弹体粒子，保留原版伤害与点火规则 | UPSTREAM；`PersistentProjectileEntityMixin`, `SmallFireballEntityMixin` | **ADAPT** / E4 / 是（含替代说明） | 独立门 `disable_burning_projectile_visual` 仅待实现；client效果可见/频率/现有no-fire不倒退；**NOT RUN** |
| `burn_freeze_visual`：燃烧/冻结低频视觉，仅已支持实体 | UPSTREAM；`HostileEntityMixin`, `LivingEntityMixin` | **ADAPT** / E4 / 是（含替代说明） | 独立门 `disable_burn_freeze_visual` 仅待实现；原版已有视觉先比较；不影响免疫/伤害；**NOT RUN** |
| `undead_horse_sunburn`：亡灵马日烧仅已决定场景，不破坏玩家坐骑掉落 | UPSTREAM；`SkeletonHorseEntityMixin`, `ZombieHorseEntityMixin` | **ADAPT** / E3 / 是（含替代说明） | 独立门 `disable_undead_horse_sunburn` 仅待实现；原版26.3日晒/装备/骑乘/关闭；**NOT RUN** |
| `hostile_escape_seat`：受伤时有条件离船，限定原生敌对与空安全落点 | UPSTREAM；`HostileEntityMixin` | **ADAPT** / E3 / 是（含替代说明） | 独立门 `disable_hostile_escape_seat` 仅待实现；玩家船/既有特殊坐骑不能脱离/概率另测；**NOT RUN** |
| `hostile_spawn_effect`：随机出生效果与现有出生互斥/概率再设计，首发暂缓 | UPSTREAM；`AbstractSkeletonEntityMixin`, `AbstractZombieEntityMixin` | **DEFER** / Phase2平衡规范 / 否 | 独立门 `disable_hostile_spawn_effect` 仅待实现；不阻断Phase1；旧开启字段不可当作生效；**NOT RUN** |
| `vex_projectile_weakness`：受投射物额外伤害，独立于已实现固定方向冲锋 | UPSTREAM；`VexEntityMixin` | **ADAPT** / E4 / 是（含替代说明） | 独立门 `disable_vex_projectile_weakness` 仅待实现；真实arrow命中/伤害来源/倍数配置；默认200%参考；**NOT RUN** |

## 56 项已启用范围及证据缺口

逐ID代码注册链、相关生产源码和独立门控、测试方法名见[PHASE1_ACTIVE_FEATURES.md](PHASE1_ACTIVE_FEATURES.md)。原135项本轮重新回归，新增QA不是额外正式生物能力。全局禁用/独立门的受控测试不能外推到每项所有组合。现有HUMAN反馈保持PARTIAL；完整L3、自然刷怪器分布、多人和性能NOT RUN。门盾和备用剑本轮复核另见[R4E0_REPORT.md](R4E0_REPORT.md)。

## 原旧ID之外的上游候选

独立于68项的逐候选裁决及全部136/135 Mixin覆盖见[PHASE1_UPSTREAM_GAPS.md](PHASE1_UPSTREAM_GAPS.md)。Allay保持历史BLOCKED，不重试；Boss只审计且独立审批，不算本轮获准开发。

## 装备分配迁移方案（只分析）

入口 `BuildupMobTweaks.onInitialize → EquipmentPools.register → DataResourceLoader → SimpleReloadListener.prepare/apply → server DataResourceStore → SERVER_STARTING/END_DATA_PACK_RELOAD → Snapshot.rules → /buildupmobtweaks equipment` 已接入。规则当前仅zombie/husk/drowned，v1 JSON选择实体/维度/难度/成人/空槽、Item/Tag权重，不支持任意组件或覆盖槽。匹配按规则ID排序后首条，不是多池叠加。官方26.3命名，无Yarn；数据错误按单规则拒绝。

旧 `EquipmentService.initialize` 编译入基础设施但**未register**；实体出生附件→来源检查→装备gate→首规则→空槽→assignmentChance→真实单Stack→原dropChance的链当前未触发。仅把ID加白名单或只register并不能解决跨模块顺序：Skeleton先生成备用剑、Pillager容器发补给、门盾出生互斥及原版finalizeSpawn装备必须优先明确。Service来源范围还包括COMMAND/刷怪笼等，不能凭旧代码视为产品已批准。

E2方案：先扩展受支持原版实体与必要组件受控Codec（若改v1格式须单列Migration Plan）；定义finalizeSpawn后一次空槽分配与rebuild安装顺序，NATURAL/SPAWN_ITEM_USE/SPAWNER/COMMAND/CONVERSION/LOAD/DIMENSION_TRAVEL分别决策；附件记录单次尝试/来源/池/物品，未知版本不写/不补发；应用装备后让真实Goal/Brain发现武器，不复制虚拟库存；保存实际Stack和组件，死亡沿原版规则结算，来源标记区分工具/物品。随后实现职业竿/弩/药水。QA固定装备不提供自然池概率证据；默认样本须预先确定N/容差、真实生成方式及配置快照。

## E1-c现行覆盖与来源纠正

`skeleton_offhand_visual`、`bogged_omen_mushrooms`两项ADAPT已接入；两个旧重复ID `skeleton_aim_fix`、`stray_snow_barrage`通过已有原版看向/STRAY_JUMP_SHOT链REPLACE，不开启旧ID。`stray_omen_snow`改为LEGACY_ONLY/Phase2 DEFER：E0误把上游OP流浪者效果当不祥，实际两版唯独Bogged有该不祥分支。E0原始JSON/证据保持历史快照，当前取舍以上述纠正行和[R4E1C_REBUILD](R4E1C_REBUILD.md)为准。当前66/58、QA15；新功能受控L2和客户端/有限Beta对照与完整L3/人工验收分开。
