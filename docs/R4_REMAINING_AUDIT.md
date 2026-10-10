# R4 剩余旧 S2 功能审计台账

> R4-E2-c当前完整交付：[报告](R4E2C_REBUILD.md)/[人工清单](R4E2C_ACCEPTANCE_CHECKLIST.md)。四项幻术师/卫道士行为，配置v10、77支持/47未生效、独立QA25场景；298项真实调度验证。原生五波袭击生命周期（受控击杀推进）、实际客户端与专服/冷重启限定PASS；上游仅有限对照。人工连续袭击/完整L3/自然分布/多人整合包/性能NOT RUN。批末本地提交后停止，不进入E3/E4/Boss/R5/Phase2/3。

> R4-E2-b历史完整交付：[报告](R4E2B_REBUILD.md)/[人工清单](R4E2B_ACCEPTANCE_CHECKLIST.md)。五项职业武器及G38，配置v10、73支持/51未生效、QA22；272项真实调度PASS。客户端/独立专服/冷重启限定PASS，上游仅有限对照；人工/完整L3/自然分布/多人整合包/性能NOT RUN。批末本地提交后停止，不进入E2-c/R5/Phase2/3。

> R4-E2-a历史限定交付：[报告](R4E2A_REBUILD.md)/[人工清单](R4E2A_ACCEPTANCE_CHECKLIST.md)。装备池与两类出生武器闭环，配置v10、68支持/56未生效、QA17；231项真实调度PASS。僵尸猪灵弩AI仍E2-b，人工/完整L3/自然刷怪器/多人/性能NOT RUN。批末提交后停止，不进入E2-b/R5/Phase2/3。

> R4-E1-c历史限定交付：[报告](R4E1C_REBUILD.md)/[人工清单](R4E1C_ACCEPTANCE_CHECKLIST.md)。两项接入，66支持/58未生效、QA15场景；`stray_omen_snow`纠正为LEGACY_ONLY/Phase2 DEFER。真实调度及客户端/有限上游结果见收据；肉眼/完整L3/自然刷怪器/多人/性能NOT RUN。不进入E2/R5/Phase2/3，旧批次陈述仅历史。


> E1-b历史限定交付：[报告](R4E1B_REBUILD.md)/[人工清单](R4E1B_ACCEPTANCE_CHECKLIST.md)。四项猪灵基础ID已接入，真实Tick/Brain/Goal 178项必需测试PASS，当前64支持/60未生效，QA共12场景。肉眼/完整L3/自然刷怪器/多人/性能NOT RUN；下一批E1-c待具体方案批准。下文E0/E1-a数字与未实施声明只代表历史时点，不进入R5。

> E1-a历史限定交付见[R4E1A_REBUILD](R4E1A_REBUILD.md)和[人工清单](R4E1A_ACCEPTANCE_CHECKLIST.md)。当前支持ID为60（旧56+四项），其余64个ID未生效；wither_skeleton_bow出生发弓仍待E2。本文件原E0数字/结论保留历史语境。

> R4-E0现已将下列68/68 ID逐项裁决，见[最新取舍](PHASE1_FEATURE_DISPOSITION.md)、[56项现有入口](PHASE1_ACTIVE_FEATURES.md)和[额外上游候选](PHASE1_UPSTREAM_GAPS.md)。下表保留R4-D历史状态，不把裁决ADAPT/PORT等写成新AI已实现；当前白名单仍56项。

本表来自 FeatureId 与实际 FeatureRegistry.supported 白名单逐项对照。配置字段保留不表示代码生效。旧入口仍隔离；未完成的内容不作 R4 已完成声明，不自动整体恢复。

## 分组审查

| 组 | 证据 / 决策 | 当前状态 |
| --- | --- | --- |
| 幻术师 | 已读上游 IllusionerEntityMixin 与 26.3 原版 Illusioner；分身箭需独立生命周期、真实目标调度和友伤验证，未实施 | SOURCE_IDENTIFIED / L2 NOT RUN |
| 猪灵、僵尸猪灵 | 两版 Piglin/ZombifiedPiglin Mixin 和原版 Brain/仇恨入口已定位；通用弩 Goal 与装备丢弃、出生闪避尚需逐项恢复审计 | SOURCE_IDENTIFIED / L2 NOT RUN |
| 焦骸、凋灵骷髅 | 两版 Parched/WitherSkeleton 及新原版已定位；旋转 Bug、近战追击和头颅预算未实施 | SOURCE_IDENTIFIED / L2 NOT RUN |
| 额外骑乘/首领 | 旧 RidingCombat / AdvancedHostiles / ZombieCombat 可读参考；不得替代已重建的 Z04，马/史莱姆/鸡骑士尚未完整审计 | NOT_AUDITED |
| 通用感知/休息/敌对关系/装备池/其他视觉 | 旧模块与 ID 引用见逐项证据；全局 Mixin 不整体恢复，未确认的兼容/目标副作用不冒充 PASS | NOT_AUDITED |
| 铁傀儡、中立/被动生物等 | 非本批新增 AI；旧行为与功能引用仅建立台账 | NOT_AUDITED |
| Allay | 最后一次恢复尝试历史 BLOCKED，按用户指令不再尝试 | BLOCKED |
| 附魔、奖励、装备成长、OP、Boss/深度村民 | PLAN/SPEC 已明确排除或仅后续设计；不因配置存在自动接入 | EXCLUDED / 未开发 |

## 完整非生效 ID 清单

下表的 NOT_AUDITED 是默认保守状态；分组 SOURCE_IDENTIFIED 只说明来源已定位，不能升级单项为行为 PASS。独立数据证据位于外层 docs/rebuild/evidence/r4d/remaining-inventory.json，含旧源码引用与 SHA256。

| Feature ID | 当前状态 | 旧源码引用（短类名，精确路径见 JSON） |
| --- | --- | --- |
| skeleton_aim_fix | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, SkeletonBowGoalMixin |
| golem_friendly_fire_fix | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, GeneralHostileRules |
| zombified_piglin_anger_fix | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, GeneralHostileRules |
| strafe_hazard_check | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, GroundStrafeMixin |
| movement_jump_input | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, GroundStrafeMixin |
| movement_clear_strafe | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, GroundStrafeMixin |
| strafe_obstacle_jump | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, GroundStrafeMixin |
| piglin_shot_swing | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, PiglinWeaponMixin |
| piglin_item_dodge | NOT_AUDITED / 不生效 | AdvancedHostiles, FeatureId, FeatureRegistry, GeneralHostileRules |
| skeleton_offhand_visual | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, SkeletonArmsMixin |
| drowned_swim_visual | NOT_AUDITED / 不生效 | DrownedSwimMixin, FeatureId, FeatureRegistry |
| burning_projectile_visual | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, GeneralHostileRules |
| burn_freeze_visual | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, GeneralHostileRules |
| undead_horse_sunburn | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, GeneralHostileRules |
| hostile_escape_seat | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, GeneralHostileRules |
| hostile_spawn_effect | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, GeneralHostileRules |
| illusioner_omen_spawn | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, IllagerRelations |
| illager_zombie_conflict | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, IllagerRelations |
| illager_zombie_villager | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, IllagerRelations |
| illager_boats | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, IllagerRelations |
| husk_spear_charge | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, RidingCombat |
| husk_camel_circle | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, RidingCombat |
| skeleton_horse_charge | NOT_AUDITED / 不生效 | AdvancedHostiles, FeatureId, FeatureRegistry, RidingCombat |
| chicken_jockey_charge | NOT_AUDITED / 不生效 | AdvancedHostiles, FeatureId, FeatureRegistry, RidingCombat |
| zombie_slime_carrier | NOT_AUDITED / 不生效 | AdvancedHostiles, FeatureId, FeatureRegistry, RidingCombat, S2RegressionTests |
| zombie_horse_leader | NOT_AUDITED / 不生效 | AdvancedHostiles, FeatureId, FeatureRegistry, RidingCombat |
| piglin_crossbow_compatibility | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, HostileCombat |
| drowned_fishing_pull | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, HostileEquipment |
| zombified_piglin_spawn_crossbow | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, ZombifiedPiglinEquipmentMixin |
| zombified_piglin_crossbow | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, HostileEquipment |
| zombie_fisherman_rod | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, HostileEquipment |
| zombie_cleric_potions | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, HostileEquipment |
| zombie_fletcher_crossbow | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, HostileEquipment |
| rider_friendly_fire | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, SkeletonExtras |
| skeleton_trap_variants | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, SkeletonTrapMixin |
| skeleton_wither_conversion | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, SkeletonExtras |
| wither_skeleton_bow | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, SkeletonExtras, WitherSkeletonEquipmentMixin |
| wither_skeleton_homing | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, SkeletonExtras |
| wither_skeleton_skull | NOT_AUDITED / 不生效 | AdvancedHostiles, FeatureId, FeatureRegistry, S2CompletionTests, SkeletonExtras |
| parched_dodge | NOT_AUDITED / 不生效 | AdvancedHostiles, FeatureId, FeatureRegistry, S2RegressionTests, SkeletonExtras |
| bogged_omen_mushrooms | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, SkeletonExtras |
| stray_omen_snow | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, SkeletonExtras |
| stray_snow_barrage | NOT_AUDITED / 不生效 | AdvancedHostiles, FeatureId, FeatureRegistry, S2RegressionTests, SkeletonExtras |
| illusioner_swap | NOT_AUDITED / 不生效 | AdvancedHostiles, FeatureId, FeatureRegistry |
| illusioner_shoot_pause | NOT_AUDITED / 不生效 | AdvancedHostiles, FeatureId, FeatureRegistry |
| illusioner_clone_arrows | NOT_AUDITED / 不生效 | AdvancedHostileTests, AdvancedHostiles, FeatureId, FeatureRegistry |
| vex_projectile_weakness | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, HostileCombat |
| sense_glowing | NOT_AUDITED / 不生效 | CombatPerception, FeatureId, FeatureRegistry |
| sense_blindness | NOT_AUDITED / 不生效 | CombatPerception, FeatureId, FeatureRegistry |
| sense_darkness | NOT_AUDITED / 不生效 | CombatPerception, FeatureId, FeatureRegistry |
| sense_nausea | NOT_AUDITED / 不生效 | CombatPerception, FeatureId, FeatureRegistry |
| sense_invisibility | NOT_AUDITED / 不生效 | CombatPerception, FeatureId, FeatureRegistry |
| sense_crawling | NOT_AUDITED / 不生效 | CombatPerception, FeatureId, FeatureRegistry |
| sense_crouching | NOT_AUDITED / 不生效 | CombatPerception, FeatureId, FeatureRegistry |
| melee_effect_interval | NOT_AUDITED / 不生效 | CombatPerception, FeatureId, FeatureRegistry |
| melee_high_target_jump | NOT_AUDITED / 不生效 | CombatPerception, FeatureId, FeatureRegistry |
| omen_pressure | NOT_AUDITED / 不生效 | CombatPerception, FeatureId, FeatureRegistry |
| evoker_flee_speed | NOT_AUDITED / 不生效 | EvokerFleeMixin, FeatureId, FeatureRegistry |
| evoker_avoid_target_fix | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, HostileCombat |
| hostile_rest | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, RestingHostiles |
| rest_sound_wake | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, RestingHostiles |
| traits | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, TraitService |
| vindicator_support | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, RaidCombat, RaidCommands, RaidTests |
| zombie_active_guard | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, ZombieCombat, ZombieTests |
| equipment_assignment | NOT_AUDITED / 不生效 | EquipmentService, FeatureId, FeatureRegistry |
| demo_common | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, TraitKind |
| demo_advanced | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, TraitKind |
| demo_rare | NOT_AUDITED / 不生效 | FeatureId, FeatureRegistry, TraitKind |

合计 124 个 ID，56 个当前白名单 ID，68 个非生效 ID。白名单表示接入范围，不能替代逐行为证据及人工验收。

## E1-b现行覆盖

历史68项列表中 `piglin_shot_swing`、`piglin_item_dodge`、`piglin_crossbow_compatibility`、`zombified_piglin_anger_fix` 已在本批接入，限定L2 PASS，当前64/60。没有新增通用弩Goal或恢复旧服务。上游ZombifiedPiglinEntityMixin也有HurtByTargetGoal同类排除，E0来源误分类已在取舍表纠正。出生僵尸猪灵弩/用弩仍E2 NOT RUN；见[R4E1B_REBUILD](R4E1B_REBUILD.md)。

## E1-c现行覆盖与来源纠正

`skeleton_offhand_visual`、`bogged_omen_mushrooms`两项ADAPT已接入；两个旧重复ID `skeleton_aim_fix`、`stray_snow_barrage`通过已有原版看向/STRAY_JUMP_SHOT链REPLACE，不开启旧ID。`stray_omen_snow`改为LEGACY_ONLY/Phase2 DEFER：E0误把上游OP流浪者效果当不祥，实际两版唯独Bogged有该不祥分支。E0原始JSON/证据保持历史快照，当前取舍以上述纠正行和[R4E1C_REBUILD](R4E1C_REBUILD.md)为准。当前66/58、QA15；新功能受控L2和客户端/有限Beta对照与完整L3/人工验收分开。

## R4-E2-a账本增量

新增支持equipment_assignment与zombified_piglin_spawn_crossbow，补齐wither_skeleton_bow出生部分；traits采用模块出生状态REPLACE，旧ID不加白名单。当前68支持/56未生效，68项原审计表仍为E0历史索引。具体取舍及人工边界见R4E2A_REBUILD。

## R4-E2-b 当前增量

完整五项及G38已接入，范围、ADAPT和证据见[R4E2B_REBUILD](R4E2B_REBUILD.md)，73/51、配置v10、独立QA22。以上E0/旧批数字与未实施声明只代表历史。外层原E0 JSON不改写为当前验收。

执行链：ProfessionBehavior.register/born/tick → 原GoalSelector的ProfessionGoals.Crossbow/Fishing/Cleric → 原版CrossbowItem/FishingHook/ThrownSplashPotion。
- `zombified_piglin_crossbow`：真实主副手弩装填/原版箭/耐久，原生受击仇恨与近战回退，已装弹和装备保存。
- `zombie_fletcher_crossbow`：精确制箭师职业，一次空手出生弩，真实射击、已装填后追赶、职业变更/治愈/转换不補发。
- `zombie_fisherman_rod`：精确渔夫职业，一次空手出生竿，原生浮漂命中/拉拽位移、真实磨损/单钩所有权、退出守恒。
- `drowned_fishing_pull`：既有真实竿和原版自然发竿，水陆射手/非水下目标/三叉戟在途互斥，原生碰撞拉拽与保存。
- `zombie_cleric_potions`：精确牧师职业，一瓶真实HARMING消耗治疗受伤亡灵，独立支援目标，关闭/消费后重载不补发。

## R4-E2-c 本批结果

本批完整接入四ID，当前77支持/47未生效；原68项取舍归属不改变（1 PORT/49 ADAPT/5 REPLACE/9 DEFER/4 EXCLUDE）。新三个幻术师为UPSTREAM ADAPT，卫道士仍LEGACY_ONLY/Buildup ADAPT，不伪称官方技能。E0原始JSON/历史表不重写。原生五波Raid推进验证采用受控击杀，不能提升为人工连续战斗PASS；原生幻术师不在原版袭击出生名单，不增加动物转换/袭击生成。下一批仅建议E3-a-1关系过滤，尚未实现，详见PHASE1_IMPLEMENTATION_PLAN。
