# 56项当前白名单：入口与测试缺口

> R4-E1-a最新限定交付见[R4E1A_REBUILD](R4E1A_REBUILD.md)和[人工清单](R4E1A_ACCEPTANCE_CHECKLIST.md)。当前支持ID为60（旧56+四项），其余64个ID未生效；wither_skeleton_bow出生发弓仍待E2。本文件原E0数字/结论保留历史语境。

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
