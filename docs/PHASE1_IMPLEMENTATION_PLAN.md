# Phase 1 后续实施名单（R4-E0冻结）

> 2026-10-10 **R4-E3-b-1/2/3/4 整组开发完成，等待人工验收**：[工作报告](R4E3B_REBUILD.md) / [统一人工清单](R4E3B_ACCEPTANCE_CHECKLIST.md)。新增九项 ADAPT，当前 **133 声明、96 支持/37 未生效、配置 v11、独立 QA 44 场景**；最终 Wrapper build/qaJar 与 **397 项必需真实调度测试（47 新增）PASS**。普通专服/冷重启、实际客户端与上游仅在报告列明范围内限定 PASS；人工、完整两版 L3、多人/整合包、自然分布和长期性能 NOT RUN。旧 68 项取舍仍 PORT1/ADAPT47/REPLACE6/DEFER10/EXCLUDE4；G04 由 PORT 精化为 ADAPT 不计入旧68。以下旧计数、BLOCKED、待批准与停止点均为各批历史时点，最新状态以本段为准。整 b 提交后停止，不进入 E3-c/E4/Boss/R5/Phase2/3，不推送发布。

E3-a用户已授权完整a组连续制作，九项实施和原生矛REPLACE见[R4E3A_REBUILD](R4E3A_REBUILD.md)。现唯一开发裁决门为骑乘过滤保留/暂缓；亡灵马日烧不再阻塞a，已批准Phase2 DEFER。该裁决答复前不新增过滤，完整a状态BLOCKED。先处理裁决及统一人工清单；E3-b/c、E4/Boss未获本批实施授权，不自动推进。68项取舍现PORT1/ADAPT47/REPLACE6/DEFER10/EXCLUDE4（含待裁决rider的历史ADAPT占位），最终更改该项分类需明确记录。

> 2026-10-10 E3-a完整开发范围现已覆盖：[工作报告](R4E3A_REBUILD.md) / [骑乘安全补充](R4E3A_RIDER_ADDENDUM.md) / [统一人工清单](R4E3A_ACCEPTANCE_CHECKLIST.md)。用户已明确批准rider_friendly_fire作为LEGACY_ONLY来源的Buildup窄安全修正；前批产品裁决阻塞解除，十项接入、尸壳矛原版REPLACE、亡灵马日烧已批准Phase2 DEFER。当前124声明、87支持/37未生效、配置v10、独立QA35场景。下文旧计数、BLOCKED和仅规划语句为历史时点；现在停止等待整a人工验收，人工/完整L3/默认自然分布/多人性能NOT RUN。没有进入E3-b/c、E4、Boss、R5或后续Phase，不推送发布。



> 2026-10-10现行状态：本轮仅获准R4-E3规划，见[总方案与11个建议小批](R4E3_PLAN.md)、[首批E3-a-1具体方案](R4E3A1_MIGRATION_PLAN.md)。未开始代码/QA/配置接入，77支持/47未生效、v10、QA25不变。亡灵马日烧来源纠正与G34的OP限定列为待裁决，不擅自撤销已批准范围。E2完整人工/L3等未验证项仍保留；下列停止点及计数按各历史批次理解。

> 2026-10-10 E2统一验收安排：E2-a/b/c既定功能开发均已覆盖，未开发子批0个；采用[E2统一清单](R4E2_ACCEPTANCE_CHECKLIST.md)合并验收。人工/完整L3/自然分布/多人/性能状态保持独立，不称所有验证完成。下文E3仅建议，等待E2统一人工反馈，不在本轮启动。

> R4-E2-c当前完整交付：[报告](R4E2C_REBUILD.md)/[人工清单](R4E2C_ACCEPTANCE_CHECKLIST.md)。四项幻术师/卫道士行为，配置v10、77支持/47未生效、独立QA25场景；298项真实调度验证。原生五波袭击生命周期（受控击杀推进）、实际客户端与专服/冷重启限定PASS；上游仅有限对照。人工连续袭击/完整L3/自然分布/多人整合包/性能NOT RUN。批末本地提交后停止，不进入E3/E4/Boss/R5/Phase2/3。

> R4-E2-b历史完整交付：[报告](R4E2B_REBUILD.md)/[人工清单](R4E2B_ACCEPTANCE_CHECKLIST.md)。五项职业武器及G38，配置v10、73支持/51未生效、QA22；272项真实调度PASS。客户端/独立专服/冷重启限定PASS，上游仅有限对照；人工/完整L3/自然分布/多人整合包/性能NOT RUN。批末本地提交后停止，不进入E2-c/R5/Phase2/3。

> R4-E2-a历史限定交付：[报告](R4E2A_REBUILD.md)/[人工清单](R4E2A_ACCEPTANCE_CHECKLIST.md)。装备池与两类出生武器闭环，配置v10、68支持/56未生效、QA17；231项真实调度PASS。僵尸猪灵弩AI仍E2-b，人工/完整L3/自然刷怪器/多人/性能NOT RUN。批末提交后停止，不进入E2-b/R5/Phase2/3。

> R4-E1-c历史限定交付：[报告](R4E1C_REBUILD.md)/[人工清单](R4E1C_ACCEPTANCE_CHECKLIST.md)。两项接入，66支持/58未生效、QA15场景；`stray_omen_snow`纠正为LEGACY_ONLY/Phase2 DEFER。真实调度及客户端/有限上游结果见收据；肉眼/完整L3/自然刷怪器/多人/性能NOT RUN。不进入E2/R5/Phase2/3，旧批次陈述仅历史。


> E1-b历史限定交付：[报告](R4E1B_REBUILD.md)/[人工清单](R4E1B_ACCEPTANCE_CHECKLIST.md)。四项猪灵基础ID已接入，真实Tick/Brain/Goal 178项必需测试PASS，当前64支持/60未生效，QA共12场景。肉眼/完整L3/自然刷怪器/多人/性能NOT RUN；下一批E1-c待具体方案批准。下文E0/E1-a数字与未实施声明只代表历史时点，不进入R5。

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
| E1-c 骷髅差异（本批已实施） | `skeleton_offhand_visual`、`bogged_omen_mushrooms`两项；`skeleton_aim_fix`、`stray_snow_barrage`已有链REPLACE；`stray_omen_snow`纠正来源后DEFER | 复用原版拉弓/已有孢子阶段，无新Goal；真实客户端四手臂组合/门盾及生命周期回归。流浪者上游无不祥分支，不编造特性 |

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

现已给出[11个建议小批及统一验收设计](R4E3_PLAN.md)：a-1关系基线→a-2骑乘安全→a-3特殊坐骑动作→a-4陷阱/转换；b-1基础生态→b-2动物/宠物→b-3有限交互→b-4嗅探兽；c-1雪傀儡交互→c-2雪傀儡远程→c-3幼体/散弹。建议首先实施a-1三个旧ID，但本轮仅交付规划。

来源补审：`undead_horse_sunburn`是LEGACY_ONLY，恢复源码的`burnInDay`实际为自然骑乘马移除；建议Phase2 DEFER待用户裁决，未据此改动已批准取舍计数。G34额外两发仅在上游MOBS_ARE_OP分支，若要保留须明确Vanilla+独立改造预算，不能当普通默认特性直接恢复。两项仍不生效，具体证据/决策门见总方案。

## R4-E4：最后处理共享钩子

E4-a感知：`sense_glowing`、`sense_blindness`、`sense_nausea`、`sense_invisibility`、`sense_crawling`、`sense_crouching`，G07潜行动物接近。E4-b动作：`movement_jump_input`、`movement_clear_strafe`、`strafe_obstacle_jump`、`melee_effect_interval`、`melee_high_target_jump`；`strafe_hazard_check`按已有安全步REPLACE核验。E4-c修复/视觉：`evoker_flee_speed`、`evoker_avoid_target_fix`（先实际Bug复现）、`drowned_swim_visual`、`burning_projectile_visual`、`burn_freeze_visual`、`vex_projectile_weakness`；G33错峰/G35原版通用修复仅确认必要的部分。

每次≤2个共享钩子，先掠夺者/骷髅/僵尸/女巫/原版基准回归；禁止旧Mob/LivingEntity全局Mixin重装、反射枚举、吞Throwable。原版已修项转REPLACE并记录证据。`sense_darkness`、`omen_pressure`、休息/声醒、出生效果已DEFER。高级主动盾按ZOMBIE_SHIELD_USE REPLACE。未生效配置解释/隐藏/迁移在之后收口处理，本轮不删v9字段。

## Boss/R5边界

完成E批后才给Boss范围决定并独立审批。凋灵/远古守卫者/劫掠兽/龙让位/监守者只是本Phase1候选，未实现；专属奖励/完整Stellarity/幻翼Boss排除或暂缓。R5集成性能和发布收口未进入；Phase2/3未执行。每个子批结束本地提交并停，不把路线批准等同所有后续代码授权。

## E1-b批末与下一具体任务

E1-b已获明确批准并完成四项实现、24项新真实调度测试（全178）、独立QA三场景、专服/客户端探针及有限上游对照，见[R4E1B_REBUILD](R4E1B_REBUILD.md)。本批停止，尚未收到E1-c实施批准。

下一建议E1-c：先核对26.3原版Skeleton模型/实际使用手和现有SkeletonGoals；编写具体Migration Plan。`skeleton_offhand_visual`只补真实左右手视觉；`stray_omen_snow`复用已有跃射雪球链，只补不祥目标差异；`bogged_omen_mushrooms`复用已有孢子阶段，防双弹体。`skeleton_aim_fix`、`stray_snow_barrage`按REPLACE核验并说明旧ID归宿，不能只加白名单。新真实Tick/原阶段仲裁测试、独立门控/保存退出、左右手客户端以及原门盾/生成剑回归必须随批验证；不祥效果/次数数值待源码和具体方案冻结。E2装备/出生发弩仍未实施。

## E1-c批末与下一批具体任务

本轮用户授权方案确认后直接实施，方案见[R4E1C_MIGRATION_PLAN](R4E1C_MIGRATION_PLAN.md)，交付及停止点见[R4E1C_REBUILD](R4E1C_REBUILD.md)。上文“E1-c待批准”的叙述仅E1-b历史时点，不再作为现行状态。

下一批仅建议 **E2-a**，本轮未实施：

1. 核对现有EquipmentPools与上游finalizeSpawn顺序；冻结equipment_assignment支持实体/规则/池、真实来源、空槽与NBT格式，不覆盖原版/外部装备，不在load/转换重复发放。
2. 完成 `wither_skeleton_bow` 出生装备部分（已有弓行为已在E1-a完成），以及 `zombified_piglin_spawn_crossbow` 成人出生装备；明确与下一E2-b用弩行为的边界，不因发弩偷偷接入AI。
3. `traits`按现有模块birth附件REPLACE，保持持久选择；不恢复S2 Marker抽签。新增独立QA验装备来源/组件/掉落/无空槽/重启转换与抽样，真实Server Tick及既有技能回归。

先出具体Environment/Migration Plan并核对API；沿用户授权的批次边界推进，不以本批完成自动启动下一批。E2-b职业、E2-c袭击、E3/E4/Boss/R5未进入。

## E2-a完成与下一具体子批建议

E2-a限定闭环已完成，范围/证据/风险见R4E2A_REBUILD；旧E2-a待实施文字仅历史。下一建议E2-b-1最多两项：`zombified_piglin_crossbow`与`zombie_fletcher_crossbow`。先恢复上游真实Goal/职业触发证据、核对26.3弩Goal及原仇恨；实际主副手/装填/弹体/耐久/退出/禁用回退、保存和死亡守恒。不同时实现渔夫钓竿/牧师药水，分别拆后续E2-b-2/3；不恢复旧S2整个职业框架。下一批具体方案还须核验，本轮未实施。

## E2-b完成与下一具体批次

用户本轮明确授权完整E2-b，替代上面当时建议的E2-b-1/2/3拆分。五ID/G38已完成限定验证、自检，批末本地提交后停止。下一建议仅E2-c四项illusioner_swap/illusioner_shoot_pause/illusioner_clone_arrows/vindicator_support：先恢复上游真实分身/箭与同队支援链，限定分身箭预算、原生目标/阶段互斥、独立开关和载入退出；补真实Server Tick、完整袭击与多目标验收。须写下一方案，本轮未实施；E3/E4/Boss/R5/Phase2/3不进入。

## R4-E2-c 本批结果

用户本轮授权一次完成整个E2-c，四ID已实施；具体方案、结果和人工步骤见[R4E2C_MIGRATION_PLAN](R4E2C_MIGRATION_PLAN.md)、[R4E2C_REBUILD](R4E2C_REBUILD.md)、[R4E2C_ACCEPTANCE_CHECKLIST](R4E2C_ACCEPTANCE_CHECKLIST.md)。77/47、QA25、配置v10；全量298项及新26项真实Server Tick/Goal验证，客户端四实际持弓手组合；原生普通难度五波生命周期采用受控击杀推进，不等于连续实战验收。本地提交后停止。

下一批具体建议 **E3-a-1（最多三ID）**：`illager_zombie_conflict`、`illager_zombie_villager`、`golem_friendly_fire_fix`。先审计两版官方发行源码的阵营/目标取得/友伤链，核对26.3原版已修内容与已有本项目Goal；再确定“原版覆盖/REPLACE”或窄ADAPT。使用真实阵营/村庄/铁傀儡、双向伤害/目标、关闭回退和目标生命周期测试，避免全局LivingEntity钩子、广域扫描及新增伤害。若当前原版已覆盖，不应仅加白名单制造已实现状态。骑乘、陷阱转换、生态生成和Boss另批。本轮只输出建议，没有实施E3；后续仍由用户确认具体批次。

E3-a rider decision resolved: user explicitly retains `rider_friendly_fire` as Buildup safety / LEGACY_ONLY ADAPT. Implemented and actual native Tick collision tests verified; prior pending/BLOCKED/inactive remarks are history. Current 87 supported / 37 inactive, v10, QA35; whole a development covered, human acceptance pending. No E3-b/c/E4 or later phase. Details: [rider addendum](R4E3A_RIDER_ADDENDUM.md).

## E3-b 批末及下一具体任务

本次连续完成 b-1/2/3/4，按授权停止等待整b人工验收。下一建议沿既定 [E3规划](R4E3_PLAN.md) **c-1雪傀儡南瓜归还/防御（G16）**：先核对剪切和原生交互、冻结实际消费/掉落守恒及条件护甲数值、独立门控/存档，再补真实交互与伤害和QA。c-2投球视觉与c-3幼体骷髅另批；G34仅OP来源，Vanilla+预算仍待产品裁决。没有在本批实施。
