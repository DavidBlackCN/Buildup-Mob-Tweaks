# R4 剩余旧 S2 功能审计台账

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
