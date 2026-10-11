# Phase 1白名单入口与测试缺口（E0基线56项）

> 2026-10-11：整 **R4-E4 已实施，停止等待统一人工验收**。141声明/121支持/20未生效，配置v13，独立QA71场；17旧ID ADAPT、危地旧ID REPLACE、G07/G33两新ID，G35必要路径由现有功能替代、附魔部分EXCLUDE。旧68取舍总数不变。最终Wrapper build/qaJar及502项必需真实Tick/Goal/Brain PASS，客户端实际绘制/粒子与既有显示回归、独立QA/专服/冷重启按报告限定PASS。人工/完整两版L3/多人整合包/长期自然分布与性能仍NOT RUN；以下旧当前计数/停止声明仅历史。不启动Boss/R5/Phase2/3，不推送发布。 [工作报告](R4E4_REBUILD.md) / [人工清单](R4E4_ACCEPTANCE_CHECKLIST.md)。

> 2026-10-11 **整 R4-E3-c 已实施，停止等待人工验收**：[报告](R4E3C_REBUILD.md)/[清单](R4E3C_ACCEPTANCE_CHECKLIST.md)。G16四项、G33幼体、G34已明确批准Vanilla+散弹，共六项ADAPT；当前139声明/102支持/37未生效、配置v12、独立QA50场。Wrapper build/qaJar及449项必需真实Tick/Goal/Brain测试PASS（40新增），实际客户端含原生网络剪切归还/自动绘制PASS；专服/重启/QA和上游仅按报告限定范围PASS。人工/完整两版L3/长期自然分布/多人整合包/GUI/性能NOT RUN。旧68取舍不变，G33错峰拉弓仍E4。本批不进入E4/Boss/R5/Phase2/3、不推送发布；下文旧数值与“待批准”均为历史时点。


> 2026-10-10 **R4-E3-b-1/2/3/4 整组开发完成，等待人工验收**：[工作报告](R4E3B_REBUILD.md) / [统一人工清单](R4E3B_ACCEPTANCE_CHECKLIST.md)。新增九项 ADAPT，当前 **133 声明、96 支持/37 未生效、配置 v11、独立 QA 44 场景**；最终 Wrapper build/qaJar 与 **397 项必需真实调度测试（47 新增）PASS**。普通专服/冷重启、实际客户端与上游仅在报告列明范围内限定 PASS；人工、完整两版 L3、多人/整合包、自然分布和长期性能 NOT RUN。旧 68 项取舍仍 PORT1/ADAPT47/REPLACE6/DEFER10/EXCLUDE4；G04 由 PORT 精化为 ADAPT 不计入旧68。以下旧计数、BLOCKED、待批准与停止点均为各批历史时点，最新状态以本段为准。整 b 提交后停止，不进入 E3-c/E4/Boss/R5/Phase2/3，不推送发布。

> 2026-10-10 E3-a完整开发范围现已覆盖：[工作报告](R4E3A_REBUILD.md) / [骑乘安全补充](R4E3A_RIDER_ADDENDUM.md) / [统一人工清单](R4E3A_ACCEPTANCE_CHECKLIST.md)。用户已明确批准rider_friendly_fire作为LEGACY_ONLY来源的Buildup窄安全修正；前批产品裁决阻塞解除，十项接入、尸壳矛原版REPLACE、亡灵马日烧已批准Phase2 DEFER。当前124声明、87支持/37未生效、配置v10、独立QA35场景。下文旧计数、BLOCKED和仅规划语句为历史时点；现在停止等待整a人工验收，人工/完整L3/默认自然分布/多人性能NOT RUN。没有进入E3-b/c、E4、Boss、R5或后续Phase，不推送发布。



> R4-E2-c当前完整交付：[报告](R4E2C_REBUILD.md)/[人工清单](R4E2C_ACCEPTANCE_CHECKLIST.md)。四项幻术师/卫道士行为，配置v10、77支持/47未生效、独立QA25场景；298项真实调度验证。原生五波袭击生命周期（受控击杀推进）、实际客户端与专服/冷重启限定PASS；上游仅有限对照。人工连续袭击/完整L3/自然分布/多人整合包/性能NOT RUN。批末本地提交后停止，不进入E3/E4/Boss/R5/Phase2/3。

> R4-E2-b历史完整交付：[报告](R4E2B_REBUILD.md)/[人工清单](R4E2B_ACCEPTANCE_CHECKLIST.md)。五项职业武器及G38，配置v10、73支持/51未生效、QA22；272项真实调度PASS。客户端/独立专服/冷重启限定PASS，上游仅有限对照；人工/完整L3/自然分布/多人整合包/性能NOT RUN。批末本地提交后停止，不进入E2-c/R5/Phase2/3。

> R4-E2-a历史限定交付：[报告](R4E2A_REBUILD.md)/[人工清单](R4E2A_ACCEPTANCE_CHECKLIST.md)。装备池与两类出生武器闭环，配置v10、68支持/56未生效、QA17；231项真实调度PASS。僵尸猪灵弩AI仍E2-b，人工/完整L3/自然刷怪器/多人/性能NOT RUN。批末提交后停止，不进入E2-b/R5/Phase2/3。

> R4-E1-c历史限定交付：[报告](R4E1C_REBUILD.md)/[人工清单](R4E1C_ACCEPTANCE_CHECKLIST.md)。两项接入，66支持/58未生效、QA15场景；`stray_omen_snow`纠正为LEGACY_ONLY/Phase2 DEFER。真实调度及客户端/有限上游结果见收据；肉眼/完整L3/自然刷怪器/多人/性能NOT RUN。不进入E2/R5/Phase2/3，旧批次陈述仅历史。


> E1-b历史限定交付：[报告](R4E1B_REBUILD.md)/[人工清单](R4E1B_ACCEPTANCE_CHECKLIST.md)。四项猪灵基础ID已接入，真实Tick/Brain/Goal 178项必需测试PASS，当前64支持/60未生效，QA共12场景。肉眼/完整L3/自然刷怪器/多人/性能NOT RUN；下一批E1-c待具体方案批准。下文E0/E1-a数字与未实施声明只代表历史时点，不进入R5。

> E1-a历史限定交付见[R4E1A_REBUILD](R4E1A_REBUILD.md)和[人工清单](R4E1A_ACCEPTANCE_CHECKLIST.md)。当前支持ID为60（旧56+四项），其余64个ID未生效；wither_skeleton_bow出生发弓仍待E2。本文件原E0数字/结论保留历史语境。

来源是实际FeatureId及FeatureRegistry.supported，不按配置字段数推断功能。每项默认gate由FeatureRegistry运行时读取，general.enabled及实体vanilla_ai/disable_id与对应排除Tag都可能拦截；有Trait选择的才受具体出生附件约束。注册链统一BuildupMobTweaks.onInitialize→对应Behavior.register→ENTITY_LOAD/已核实Mixin→原版Selector/Brain。以下相关测试是定位索引，不等于每项独立的默认/确定性/关闭/所有生命周期/多人/性能矩阵全PASS。

当前最小版本MC26.3/Buildup1.0.0/config v9。旧135项本轮全量回归结果在R4E0_REPORT；DIAGNOSTIC_PROBE只读基础设施，无战斗L2含义。历史L3有限、HUMAN_PARTIAL不提升。所有条目共同缺口：自然刷怪器完整样本、完整上游量化对照、实际双人/整合包与性能NOT RUN。JSON逐ID给门控表达式、精确源码行、测试组全部方法名，以便后续分配专项证据。

| ID | 真正执行引用（不是声明） | 相关真实Tick测试组 / 缺口 |
| --- | --- | --- |
| `pillager_target_lifecycle` | `PillagerBehavior:148`；`PillagerLifecycleMixin:21` | `PillagerBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `pillager_spawn_supplies` | `PillagerBehavior:92` | `PillagerBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `spider_hunts_pests` | `RemainingBehavior:45,46,48` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `creeper_fire_visual` | `RemainingExtras:21` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `creeper_hit_delay` | `CreeperRebuildMixin:14` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `creeper_embedded_arrows` | `RemainingExtras:18` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `pillager_food_heal` | `PillagerBehavior:104,140,232`；`PillagerGoals:49` | `PillagerBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `pillager_shield_break` | `PillagerBehavior:189` | `PillagerBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `skeleton_shelter` | `SkeletonGoals:75` | `SkeletonBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `enderman_combo_animation` | `EndermanCombo:27` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `enderman_combo` | `EndermanCombo:27`；`RemainingBehavior:29,31,48` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `enderman_speed` | `RemainingBehavior:53` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `evoker_fireball_no_fire` | `EvokerBehavior:71` | `RaidBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `evoker_totem` | `EvokerBehavior:24,44,48,51` | `RaidBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `evoker_fireball` | `EvokerBehavior:23,44,47,51,67,71` | `RaidBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `ghast_slow_fireball` | `RemainingBehavior:50,65` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `ghast_cooldown` | `RemainingBehavior:48,50,64,66`；`GhastRebuildGoalMixin:19` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `ghast_telegraph` | `RemainingBehavior:48,50`；`GhastRebuildGoalMixin:23` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `blaze_orbit` | `RemainingBehavior:48,51,69,78`；`BlazeRebuildGoalMixin:23` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `blaze_difficulty_volley` | `RemainingBehavior:67`；`BlazeRebuildGoalMixin:23` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `blaze_strafe` | `RemainingBehavior:48,85`；`BlazeRebuildGoalMixin:23` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `pillager_crossbow_compatibility` | `PillagerBehavior:124`；`PillagerCrossbowGoalMixin:27,45`；`PillagerCrossbowHandMixin:21`；`PillagerWeaponMixin:16` | `PillagerBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `pillager_range` | `PillagerCrossbowGoalMixin:34` | `PillagerBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `ranged_reposition` | `SkeletonGoals:44` | `SkeletonBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `silverfish_burrow` | `RemainingBehavior:48,52`；`SilverfishHide:12,15`；`SilverfishRebuildHelpMixin:23` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `silverfish_call_pause` | `SilverfishRebuildHelpMixin:22,26` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `silverfish_call_particles` | `SilverfishRebuildHelpMixin:29` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `silverfish_shovel_weakness` | `SilverfishRebuildMixin:15` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `breeze_takeoff_burst` | `RemainingExtras:24` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `breeze_landing_burst` | `RemainingExtras:24` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `witch_leaping_potion` | `WitchBehavior:40,42,45` | `RaidBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `witch_jump_throw` | `WitchBehavior:39,42` | `RaidBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `diagnostic_probe` | `DiagnosticCommands:26,27,32` | `RemainingBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `vex_fixed_charge` | `VexBehavior:17,21,34,36` | `RaidBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `vex_recovery_pause` | `VexBehavior:17,19,26,27,30,34` | `RaidBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `vex_close_range_guard` | `VexBehavior:17,18,34` | `RaidBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `pillager_retreat` | `PillagerBehavior:147,196` | `PillagerBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `pillager_weapon_switch` | `PillagerBehavior:103,130,141,172,189`；`PillagerGoals:39`；`PillagerCrossbowGoalMixin:56` | `PillagerBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `evoker_vex_limit` | `EvokerBehavior:36,38,40` | `RaidBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `evoker_summon_cooldown` | `EvokerBehavior:36,39,41` | `RaidBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `witch_windup` | `WitchBehavior:32,42,44` | `RaidBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `witch_throw_cooldown` | `WitchBehavior:30,38,42` | `RaidBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `zombie_shield_use` | `ZombieBehavior:86`；`ZombieGoals:45,50`；`ZombieShieldWearMixin:21` | `ZombieBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `zombie_door_guard` | `ZombieBehavior:48,82,129` | `ZombieBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `husk_sand_burrow` | `ZombieBehavior:86`；`ZombieGoals:58` | `ZombieBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `zombie_baby_rider` | `ZombieBehavior:49,85,95` | `ZombieBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `drowned_trident_conservation` | `DrownedBehavior:52,54,59,75,87,102,106`；`DrownedLifecycleMixin:13` | `DrownedBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `drowned_trident_recovery` | `DrownedBehavior:75,102,106` | `DrownedBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `drowned_trident_player_pickup` | `DrownedBehavior:63,83,95,96` | `DrownedBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `skeleton_safe_strafing` | `SkeletonBehavior:50`；`SkeletonStrafeMixin:16` | `SkeletonBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `skeleton_target_validation` | `SkeletonBehavior:106`；`SkeletonAiMixin:17` | `SkeletonBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `skeleton_weapon_switching` | `SkeletonBehavior:49,86,105,124`；`SkeletonGoals:66` | `SkeletonBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `skeleton_bow_compatibility` | `SkeletonBehavior:36,50`；`SkeletonBowHandMixin:17`；`SkeletonLifecycleMixin:34` | `SkeletonBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `skeleton_sniping` | `SkeletonBehavior:42,49` | `SkeletonBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `stray_jump_shot` | `SkeletonSpecificGoals:28`；`SkeletonSnowballMixin:17` | `SkeletonBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |
| `bogged_spore_retreat` | `SkeletonBehavior:113`；`SkeletonSpecificGoals:28` | `SkeletonBehaviorTests`；限定受控L2，默认概率/全生命周期组合/HUMAN NOT RUN |

详细入口与行为说明仍以[R3](R2A_REBUILD.md)、[R4-A](R2B_REBUILD.md)、[R4-B](R2C_REBUILD.md)、[R4-C](R4C_REBUILD.md)、[R4-D](R4D_REBUILD.md)和反馈报告为准。[active-features.json](../../docs/rebuild/evidence/r4e0/active-features.json)是可复查索引，不能代替行为日志。当前QA最小工具仅直接强制五个标杆；其他机制继续用现有真实Tick夹具或原版命令+状态附件，不编造未实现强制接口。

## R4-E1-a追加入口（旧56项索引保持）

| ID | 实际入口与门控 | 状态 |
| --- | --- | --- |
| parched_dodge | ExtendedSkeletonBehavior ENTITY_LOAD→ExtendedSkeletonGoal→SPIN同步/客户端Renderer；traits/general/独立项/排除标签 | L2 PASS，肉眼NOT RUN |
| wither_skeleton_bow | 原版AbstractSkeleton bowGoal/performRangedAttack；ExtendedSkeletonMixin RETURN真实耐久/计数 | 既有普通弓PASS；出生发弓E2 NOT RUN |
| wither_skeleton_skull | ExtendedSkeletonGoal前摇→原版WitherSkull→局部Tick/碰撞保护 | L2 PASS，完整L3 NOT RUN |
| wither_skeleton_homing | ExtendedSkeletonBehavior.skullTick固定不祥目标与预算；独立配置/标签 | L2 PASS，完整L3 NOT RUN |

四项原始失败/实际证据与兼容性细节见[R4E1A_REBUILD](R4E1A_REBUILD.md)。支持ID60并不表示Phase1完成。

## E1-b新增四个实际入口

| ID | 实际控制链 | 当前证据 |
| --- | --- | --- |
| `piglin_crossbow_compatibility` | PiglinBehavior + Piglin/PiglinAi/CrossbowAttack/ProjectileUtil/BehaviorUtils窄Mixin，原版Brain | 真实继承弩主副手/装填射击、组件/交易/保存/转换、独立门L2 PASS |
| `piglin_shot_swing` | Piglin.performRangedAttack前后实际组件观测→swingForAttack实际手 | L2/实际客户端Swing PASS；肉眼NOT RUN |
| `piglin_item_dodge` | ENTITY_LOAD出生附件→customServerAiStep TAIL，近距实际蓄弩一次冲量 | 7%默认抽签样本/200冷却/真实物理/危险地面退出L2 PASS；自然刷怪器NOT RUN |
| `zombified_piglin_anger_fix` | 原HurtByTargetGoal起步/TargetGoal继续/setTarget窄门及已加载怒气清理 | 原版同类群体复现、玩家保留、禁用/重启门L2 PASS；上游Beta仅有限对照 |

当前完整证据见[R4E1B_REBUILD](R4E1B_REBUILD.md)，旧56项索引和E1-a历史结果不重写。

## E1-c新增执行入口

| ID | 执行入口 | 实际证据/缺口 |
| --- | --- | --- |
| `skeleton_offhand_visual` | SkeletonDrawMixin → SkeletonDifferences.draw；同步VISUAL/BOW_VISUAL → SkeletonRendererMixin | 真正左右主臂×主副手、原版Goal单独开启、单项关闭、实际客户端模型PASS；人眼/第三方组合NOT RUN |
| `bogged_omen_mushrooms` | SkeletonSpecificGoals DODGE → release；Snowball.onHit → impact；BoggedCloudMixin.serverTick → affects；END_SERVER_TICK退役 | 真实两色碰撞云/伤害/亡灵盟友保护/保存退出PASS；Beta有限对照、正式版源码；完整L3 NOT RUN |

实际FeatureRegistry现为66个支持ID。旧瞄准/雪球重复ID仍未生效，流浪者不祥雪球DEFER，见[R4E1C_REBUILD](R4E1C_REBUILD.md)。E0 JSON索引仍是旧56项定位快照。

## R4-E2-a 当前增量

`equipment_assignment`：EquipmentPools.register → EquipmentService.register/initialize/defaultWeapon → 两类出生WrapOperation及ENTITY_LOAD；`zombified_piglin_spawn_crossbow`：同一真实出生链并带独立门。`wither_skeleton_bow`已支持ID补齐出生部分；`traits`仍未生效旧ID、按模块附件REPLACE。具体24项行为及231总回归见R4E2A_REBUILD；当前68/56，不改E0原始JSON。

## R4-E2-b 当前增量

完整五项及G38已接入，范围、ADAPT和证据见[R4E2B_REBUILD](R4E2B_REBUILD.md)，73/51、配置v10、独立QA22。以上E0/旧批数字与未实施声明只代表历史。外层原E0 JSON不改写为当前验收。

执行链：ProfessionBehavior.register/born/tick → 原GoalSelector的ProfessionGoals.Crossbow/Fishing/Cleric → 原版CrossbowItem/FishingHook/ThrownSplashPotion。
- `zombified_piglin_crossbow`：真实主副手弩装填/原版箭/耐久，原生受击仇恨与近战回退，已装弹和装备保存。
- `zombie_fletcher_crossbow`：精确制箭师职业，一次空手出生弩，真实射击、已装填后追赶、职业变更/治愈/转换不补发。
- `zombie_fisherman_rod`：精确渔夫职业，一次空手出生竿，原生浮漂命中/拉拽位移、真实磨损/单钩所有权、退出守恒。
- `drowned_fishing_pull`：既有真实竿和原版自然发竿，水陆射手/非水下目标/三叉戟在途互斥，原生碰撞拉拽与保存。
- `zombie_cleric_potions`：精确牧师职业，一瓶真实HARMING消耗治疗受伤亡灵，独立支援目标，关闭/消费后重载不补发。

## R4-E2-c 本批结果

当前124个声明ID中77个支持、47个未生效（E2-b的73+本批4）；这不是所有ID的完整验收。新增入口为BuildupMobTweaks → IllusionState/IllusionBehavior.register → 精确类型加载及原生Mixin。三个illusioner ID进入原生施法/弓路径，vindicator_support为原targetSelector上的5/TARGET。四项均见IllusionBehaviorTests（26项），原生Server Tick/Goal及实际伤害验证；专服/客户端范围和未验证项见R4E2C_REBUILD。上面的56/60/64/66/68/73及config v9是对应历史快照，不覆盖当前v10。

| 新增ID | 真正执行链 | 边界 |
| --- | --- | --- |
| illusioner_swap | IllusionRebuildMixin.aiStep → IllusionBehavior.tick | 原生隐身、安全点/200tick、同步行走/客户端幻影 |
| illusioner_shoot_pause | IllusionBowGoalMixin.tick → pause/shot | 原生弓Goal实际拉弓/发箭、停止输入/20及10tick |
| illusioner_clone_arrows | IllusionRebuildMixin.performRangedAttack → clones；IllusionArrowMixin | 真实箭源/owner/原生附魔/伤害、替代双箭、120/200tick |
| vindicator_support | ENTITY_LOAD → SupportGoal，VindicatorSupportExitMixin | 保留原targetSelector优先级，仅拥有自己的支援目标 |

## R4-E3-a execution entries

| ID | Entry / gate | Evidence |
| --- | --- | --- |
| illager_zombie_conflict / illager_zombie_villager | RelationBehavior ENTITY_LOAD -> native targetSelector 6/TARGET; Mob.tick boundary | L2 PASS; [scope / NOT RUN](R4E3A_REBUILD.md) |
| golem_friendly_fire_fix | GolemRetaliationMixin / TargetGoal continuation; exact Iron/Snow revenge only | L2 PASS; [scope / NOT RUN](R4E3A_REBUILD.md) |
| chicken_jockey_charge / skeleton_horse_charge / husk_camel_circle | RidingBehavior ENTITY_LOAD -> Goal8 RidingGoal; independent gates / actual existing vehicle | L2 PASS; [scope / NOT RUN](R4E3A_REBUILD.md) |
| hostile_escape_seat | AFTER_DAMAGE -> Goal3 BoatEscape; actual hurt / safe loaded landing | L2 PASS; [scope / NOT RUN](R4E3A_REBUILD.md) |
| skeleton_trap_variants | SkeletonTrapVariantsMixin -> native addFreshEntityWithPassengers; actual equipment transfer | L2 PASS; [scope / NOT RUN](R4E3A_REBUILD.md) |
| skeleton_wither_conversion | LivingEntity.die HEAD -> native convertTo; actual reserve ownership | L2 PASS; [scope / NOT RUN](R4E3A_REBUILD.md) |

124 declared / 86 supported / 38 inactive; v10 unchanged. Rider filter BLOCKED; horse sunburn DEFER; husk spear native REPLACE, old ID inactive.

Rider addition: `rider_friendly_fire` -> RiderProjectileMixin / Projectile.canHitEntity(Entity) HEAD; server AbstractArrow, active Mob owner, current same-root tree only. No generic hurt immunity or saved old-tree references. Source LEGACY_ONLY, user-approved Buildup ADAPT. 87 supported / 37 inactive, v10 / QA35. Actual native arrow/trident collision / Goal / disabled lifecycle evidence: [addendum](R4E3A_RIDER_ADDENDUM.md).

## E3-b 新增白名单入口

九ID通过 `config.passive.<id>` 独立配置；EcologyBehavior事件与EcologyGoals原生调度、SnifferSearch主线程队列，五窄Mixin挂接原生事件。支持96/未生效37，未知附件版本不运行。入口、边界及47项真实调度覆盖见 [E3-b报告](R4E3B_REBUILD.md)；人工/L3/多人/性能NOT RUN。

## E3-c 六个独立执行入口

G16四ID：AbilityBehavior.register ENTITY_LOAD/Mob.tick boundary → SnowGolemAbilityMixin原生mobInteract/performRangedAttack → SnowAttackGoal原GoalSelector；SnowRenderer/Model/RenderStateMixin → SnowballHandLayer实际提交。G33：SkeletonLifecycleMixin原生finalizeSpawn → 一次baby_choice、原生SynchedEntityData/refreshDimensions；BabyConversionMixin实际convertTo保留。G34：BreezeShotgunMixin原生Shoot.tick真实中弹后 → 两发真实BreezeWindCharge、RemainingExtras共享期限、ShotgunFlight/Griefing窄退出。六ID、独立gate、未知版本回退均在真实调度验证；没有仅加白名单冒充实现。

## 整E4收口与下一批

2026-10-11：整 **R4-E4 已实施，停止等待统一人工验收**。141声明/121支持/20未生效，配置v13，独立QA71场；17旧ID ADAPT、危地旧ID REPLACE、G07/G33两新ID，G35必要路径由现有功能替代、附魔部分EXCLUDE。旧68取舍总数不变。最终Wrapper build/qaJar及502项必需真实Tick/Goal/Brain PASS，客户端实际绘制/粒子与既有显示回归、独立QA/专服/冷重启按报告限定PASS。人工/完整两版L3/多人整合包/长期自然分布与性能仍NOT RUN；以下旧当前计数/停止声明仅历史。不启动Boss/R5/Phase2/3，不推送发布。

新入口：`rebuild/general/E4Behavior`、原生Goal/Melee/MoveControl窄Mixin、服务端同步视觉附件与独立客户端模型。19个接入ID、1个旧ID替代边界均见[R4E4_REBUILD](R4E4_REBUILD.md)。现先按[清单](R4E4_ACCEPTANCE_CHECKLIST.md)统一人工验收；下一具体工作仅是验收反馈修复与尚余20个未生效ID的归类复核。Boss基础批须另写范围并获明确批准，不在本轮实施；R5总验收也未开始。
