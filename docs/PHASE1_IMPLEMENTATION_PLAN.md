# Phase 1 后续实施名单（R4-E0冻结）

> R4-E1-a已按逐批批准实施，见[方案](R4E1A_MIGRATION_PLAN.md)/[结果](R4E1A_REBUILD.md)。下一批建议E1-b四项猪灵/僵尸猪灵，仍待批准；下列E0冻结名单保留，不表示后续已授权。

本轮只交付R4-E0；以下均待用户逐批批准，不是本轮实现。裁决源[68项](PHASE1_FEATURE_DISPOSITION.md)与[额外候选](PHASE1_UPSTREAM_GAPS.md)。56白名单保持；50 ADAPT /1 PORT是待实施或经原版核验后可安全替代的意图，不可只加白名单。REPLACE项需补说明和必要的差异回归，不重装旧S2。所有批次实施前写具体Migration Plan、查26.3签名/控制链；批末实际验证、自检、本地提交、停止。

## 下一批：R4-E1-a 焦骸与凋灵骷髅

具体代码目标：`parched_dodge`、`wither_skeleton_bow`的既有真实弓使用部分、`wither_skeleton_skull`、`wither_skeleton_homing`。新原版类型现在不在SkeletonBehavior.supported，先独立生命周期/备用物来源，再追加专用阶段；不让现有三种骷髅突然使用新技能。自然凋灵骷髅发弓交E2装备闭环，E1-a只对已持弓者生效。

参考：两版ParchedEntityMixin/WitherSkeletonEntityMixin/SkeletonSpecificGoal；正式版运动修复优先，Beta不作黄金基准。先核对26.3Parched/WitherSkeleton实际Goal与原近战/瞄准/弓路径。建议最多四项，独立配置、强制QA、原生调度和实体状态边界。头颅前摇/次数/冷却/期限/受保护方块和目标退出，禁止可拾取高价值物；具体数值由本批方案冻结，E0不凭旧数值编新AI。

验收：实际移动/旋转→退出；真实弓/箭与近战切换；有限头颅生成、运动、碰撞伤害及取消；非玩家目标/创造旁观退出/NoAI/禁用/保存重启/真实卸载；原R4-A与备用剑回归；可运行上游对照不充分则标受限。人工只需QA各一场，正常默认概率独立检测。

## E1余下子批

| 子批 | 明确名单 | 要求 |
| --- | --- | --- |
| E1-b 猪灵基础 | `piglin_shot_swing`、`piglin_item_dodge`（改正名称说明：近距用弩后跳）、`piglin_crossbow_compatibility`、`zombified_piglin_anger_fix` | 真实Piglin Brain/原仇恨，物品丢弃/主副手、近距躲避与关闭；仇恨修复先复现26.3缺陷，不凭旧代码判断Bug |
| E1-c 骷髅差异 | `skeleton_offhand_visual`、`stray_omen_snow`、`bogged_omen_mushrooms`；`skeleton_aim_fix`、`stray_snow_barrage`按REPLACE核验 | 共用现有跃射/孢子阶段，只补不祥目标独立差异，禁止双启动；实际客户端左右手/保存门控与原功能回归 |

## R4-E2：先装备闭环，后职业及袭击

1. E2-a：`equipment_assignment`（实际来源/池/空槽/组件/死亡闭环），`wither_skeleton_bow`出生部分、`zombified_piglin_spawn_crossbow`。先确定支持实体/数据格式/原版finalizeSpawn后顺序；无规则/无空槽不得补发。`traits`按REPLACE保留模块出生状态，不复活Marker抽签。
2. E2-b：`drowned_fishing_pull`、`zombie_fisherman_rod`、`zombie_cleric_potions`、`zombie_fletcher_crossbow`、`zombified_piglin_crossbow`；上游主体是职业僵尸村民，不能误套所有普通僵尸。加G38客户端浮漂/钓竿cast/持弩配套，实际弹体/耐久/守恒和转换验证。
3. E2-c：`illusioner_swap`、`illusioner_shoot_pause`、`illusioner_clone_arrows`、`vindicator_support`。有限分身箭、阶段互斥、同队支援；全流程袭击与多目标，目标退出/卸载，不增加动物变幻术师出生。

## R4-E3：稀有骑乘、基础生态和差异

| 子批 | 68项内名单 | 独立上游候选 |
| --- | --- | --- |
| E3-a 骑乘/关系 | `husk_spear_charge`、`husk_camel_circle`、`skeleton_horse_charge`、`chicken_jockey_charge`、`skeleton_trap_variants`、`skeleton_wither_conversion`、`rider_friendly_fire`、`illager_zombie_conflict`、`illager_zombie_villager`、`undead_horse_sunburn`、`hostile_escape_seat`、`golem_friendly_fire_fix` | 分为≤4项小批；坐骑控制/实体预算/物品转移/自然和载入一次 |
| E3-b 低侵入生态 | 无重复旧ID | G01嗅探兽（扫描预算先设计）、G02/03鱿鱼、G04鸡逃跑、G06群体惊慌、G08宠物恢复、G11猫避弹、G14山羊角、G23商人自卫；每次≤3项，各子项新ID |
| E3-c 选定能力 | 无重复旧ID | G16雪傀儡拆分独立项、G33骷髅幼体部分、G34旋风人散弹；真实Brain/渲染/生成与原已实现能力互斥 |

E3每次需提交具体名单，不一次实施全部。高风险扫描、生态生成、骑乘递归若无可接受证明，报告并明确改DEFER，保留台账。`zombie_slime_carrier`、`zombie_horse_leader`、`illager_boats`已DEFER，不是这批隐性必做。史莱姆/幻翼/新奖励/完整村民系统不自动纳入。

## R4-E4：最后处理共享钩子

E4-a感知：`sense_glowing`、`sense_blindness`、`sense_nausea`、`sense_invisibility`、`sense_crawling`、`sense_crouching`，G07潜行动物接近。E4-b动作：`movement_jump_input`、`movement_clear_strafe`、`strafe_obstacle_jump`、`melee_effect_interval`、`melee_high_target_jump`；`strafe_hazard_check`按已有安全步REPLACE核验。E4-c修复/视觉：`evoker_flee_speed`、`evoker_avoid_target_fix`（先实际Bug复现）、`drowned_swim_visual`、`burning_projectile_visual`、`burn_freeze_visual`、`vex_projectile_weakness`；G33错峰/G35原版通用修复仅确认必要的部分。

每次≤2个共享钩子，先掠夺者/骷髅/僵尸/女巫/原版基准回归；禁止旧Mob/LivingEntity全局Mixin重装、反射枚举、吞Throwable。原版已修项转REPLACE并记录证据。`sense_darkness`、`omen_pressure`、休息/声醒、出生效果已DEFER。高级主动盾按ZOMBIE_SHIELD_USE REPLACE。未生效配置解释/隐藏/迁移在之后收口处理，本轮不删v9字段。

## Boss/R5边界

完成E批后才给Boss范围决定并独立审批。凋灵/远古守卫者/劫掠兽/龙让位/监守者只是本Phase1候选，未实现；专属奖励/完整Stellarity/幻翼Boss排除或暂缓。R5集成性能和发布收口未进入；Phase2/3未执行。每个子批结束本地提交并停，不把路线批准等同所有后续代码授权。
