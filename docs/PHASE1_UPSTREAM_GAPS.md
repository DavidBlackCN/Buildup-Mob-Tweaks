# 旧68项之外的上游特性缺口审计

> 当前状态（2026-10-11）：用户确认最新baby体型/持弓修复“验收通过”，E3-a/b/c选定开发范围闭合，可进入下一阶段；[收尾记录](R4E3_CLOSURE.md) / [E4方案](R4E4_PLAN.md)。139声明/102支持/37未生效、v12、QA50不变。完整两版L3、未提供的极端组合、多人/整合包、长期分布/性能仍NOT RUN；下方旧待批准/停止/未验收文字为历史时点。本轮仅完成E3收尾和E4规划批，E4功能实现及新QA/运行验收NOT RUN，未新增支持，不进入Boss/R5/Phase2/3。

> 2026-10-11 **整 R4-E3-c 已实施，停止等待人工验收**：[报告](R4E3C_REBUILD.md)/[清单](R4E3C_ACCEPTANCE_CHECKLIST.md)。G16四项、G33幼体、G34已明确批准Vanilla+散弹，共六项ADAPT；当前139声明/102支持/37未生效、配置v12、独立QA50场。Wrapper build/qaJar及449项必需真实Tick/Goal/Brain测试PASS（40新增），实际客户端含原生网络剪切归还/自动绘制PASS；专服/重启/QA和上游仅按报告限定范围PASS。人工/完整两版L3/长期自然分布/多人整合包/GUI/性能NOT RUN。旧68取舍不变，G33错峰拉弓仍E4。本批不进入E4/Boss/R5/Phase2/3、不推送发布；下文旧数值与“待批准”均为历史时点。


> 2026-10-10 **R4-E3-b-1/2/3/4 整组开发完成，等待人工验收**：[工作报告](R4E3B_REBUILD.md) / [统一人工清单](R4E3B_ACCEPTANCE_CHECKLIST.md)。新增九项 ADAPT，当前 **133 声明、96 支持/37 未生效、配置 v11、独立 QA 44 场景**；最终 Wrapper build/qaJar 与 **397 项必需真实调度测试（47 新增）PASS**。普通专服/冷重启、实际客户端与上游仅在报告列明范围内限定 PASS；人工、完整两版 L3、多人/整合包、自然分布和长期性能 NOT RUN。旧 68 项取舍仍 PORT1/ADAPT47/REPLACE6/DEFER10/EXCLUDE4；G04 由 PORT 精化为 ADAPT 不计入旧68。以下旧计数、BLOCKED、待批准与停止点均为各批历史时点，最新状态以本段为准。整 b 提交后停止，不进入 E3-c/E4/Boss/R5/Phase2/3，不推送发布。

E3-a补审：`rider_friendly_fire` 从上游来源改为 **LEGACY_ONLY**，两版恢复 PersistentProjectileEntityMixin 均无当前骑乘树过滤；是否作为Buildup安全修正保留仍待产品裁决。`undead_horse_sunburn` 已明确 Phase2 DEFER。目标26.3原生SpearUseGoal的坐骑速度/真实武器已实测覆盖尸壳矛，按REPLACE处理，旧ID仍不生效。傀儡雪球误击报复已实际复现并窄修，不再停留于静态推测。这些来源纠正不新增生态功能，不改变其他G候选门槛。

> 2026-10-10 E3-a完整开发范围现已覆盖：[工作报告](R4E3A_REBUILD.md) / [骑乘安全补充](R4E3A_RIDER_ADDENDUM.md) / [统一人工清单](R4E3A_ACCEPTANCE_CHECKLIST.md)。用户已明确批准rider_friendly_fire作为LEGACY_ONLY来源的Buildup窄安全修正；前批产品裁决阻塞解除，十项接入、尸壳矛原版REPLACE、亡灵马日烧已批准Phase2 DEFER。当前124声明、87支持/37未生效、配置v10、独立QA35场景。下文旧计数、BLOCKED和仅规划语句为历史时点；现在停止等待整a人工验收，人工/完整L3/默认自然分布/多人性能NOT RUN。没有进入E3-b/c、E4、Boss、R5或后续Phase，不推送发布。



> 2026-10-10 E3规划补审：[总方案](R4E3_PLAN.md)。G34只在上游OP分支成立，不能当普通默认技能直接移植；如保留需单独批准Vanilla+预算。下文56支持数是E0历史时点，当前77支持/47未生效、v10、QA25不变。本轮未实现候选，E3验证均NOT RUN。

> R4-E2-c当前完整交付：[报告](R4E2C_REBUILD.md)/[人工清单](R4E2C_ACCEPTANCE_CHECKLIST.md)。四项幻术师/卫道士行为，配置v10、77支持/47未生效、独立QA25场景；298项真实调度验证。原生五波袭击生命周期（受控击杀推进）、实际客户端与专服/冷重启限定PASS；上游仅有限对照。人工连续袭击/完整L3/自然分布/多人整合包/性能NOT RUN。批末本地提交后停止，不进入E3/E4/Boss/R5/Phase2/3。

> R4-E2-b历史完整交付：[报告](R4E2B_REBUILD.md)/[人工清单](R4E2B_ACCEPTANCE_CHECKLIST.md)。五项职业武器及G38，配置v10、73支持/51未生效、QA22；272项真实调度PASS。客户端/独立专服/冷重启限定PASS，上游仅有限对照；人工/完整L3/自然分布/多人整合包/性能NOT RUN。批末本地提交后停止，不进入E2-c/R5/Phase2/3。

R4-E0检查已核验两版发行JAR元数据、全部Mixin列表、配置调用/默认值、R0/R1资料及原作者README快照。正文研究仍按MIT与原版权范围；不编辑反编译输出，不恢复Allay。以下不是已实现特性，当前生产白名单仍56项。**IN_PHASE1仅选入后续批次，实施/默认样本/人工/L3均NOT RUN；Boss单独审批，不计E0完成门。**

| 候选 | 决策 / 范围 | 真实证据类与玩家表现、主动限制 | 当前验证 |
| --- | --- | --- | --- |
| G01 嗅探兽探索 | **ADAPT** / IN_PHASE1 / E3 | `SnifferEntityMixin`；潜行持物找生物群系/地下方块并指向；上游同步findClosestBiome3d半径6400与33×65×33扫描必须限时预算、Tag白名单和取消机制 | **E3-b限定 PASS**；真实调度/门控/生命周期见R4E3B_REBUILD；人工/完整L3 NOT RUN |
| G02 鱿鱼墨汁 | **ADAPT** / IN_PHASE1 / E3 | `SquidEntityMixin`；直接攻击者失明/荧光和附近范围递减；上游范围分支缺开关一致性需修；100tick半径8只参考 | **E3-b限定 PASS**；真实调度/门控/生命周期见R4E3B_REBUILD；人工/完整L3 NOT RUN |
| G03 荧光鱿鱼注视 | **ADAPT** / IN_PHASE1 / E3 | `SquidEntityMixin`；每60tick注视判定给夜视600tick；需要距离/LOS/冷却/服务端权威而非无限距离玩家扫描 | **E3-b限定 PASS**；真实调度/门控/生命周期见R4E3B_REBUILD；人工/完整L3 NOT RUN |
| G04 鸡逃离威胁 | **ADAPT** / IN_PHASE1 / E3 | `ChickenEntityMixin`；原版AvoidEntityGoal玩家8格优先5、Monster6格优先6，速度.8/1.2；喂食优先/禁用要另测 | **E3-b限定 PASS**；真实调度/门控/生命周期见R4E3B_REBUILD；人工/完整L3 NOT RUN |
| G05 鸡掉羽毛 | **DEFER** / Phase3生态平衡 | `LivingEntityMixin`；额外周期资源生成/50chance，首发不做；并非鸡逃跑附带奖励 | NOT RUN |
| G06 动物群体惊慌 | **ADAPT** / IN_PHASE1 / E3 | `EscapeDangerGoalMixin`；16格传播惊慌，次数/集合大小限额、仅明确原生动物；不永久覆盖Brain | **E3-b限定 PASS**；真实调度/门控/生命周期见R4E3B_REBUILD；人工/完整L3 NOT RUN |
| G07 潜行接近动物 | **ADAPT** / IN_PHASE1 / E4 | `FleeEntityGoalMixin`；sneak_to_approach_animals减少AvoidEntityGoal逃离，限动物不改已有敌对避让 | NOT RUN |
| G08 宠物低侵入恢复 | **ADAPT** / IN_PHASE1 / E3 | `LivingEntityMixin`；有主人且不在排除Tag才每20tick恢复1；需冷却/伤害后等待及存档，不能无条件超速恢复 | **E3-b限定 PASS**；真实调度/门控/生命周期见R4E3B_REBUILD；人工/完整L3 NOT RUN |
| G09 飞行主人宠物传送 | **DEFER** / Phase3宠物专项 | `TameableEntityMixin`；追随飞行主人涉及落点/维度/卸载；首发不接管 | NOT RUN |
| G10 鹦鹉伴飞 | **DEFER** / Phase3宠物专项 | `ParrotEntityMixin`；飞行Goal/飞行跟随不是基础Bug，需专用多人/肩膀生命周期 | NOT RUN |
| G11 猫避弹 | **ADAPT** / IN_PHASE1 / E3 | `CatEntityMixin`；避真实投射物，保留伤害与主人/坐下状态；不复制上游闪避无敌 | **E3-b限定 PASS**；真实调度/门控/生命周期见R4E3B_REBUILD；人工/完整L3 NOT RUN |
| G12 动物刷毛 | **EXCLUDE** / 不计门槛 | `BrushItemMixin`；刷毛新增羽毛/线/鳞甲/骨粉奖励链和工具消耗，当前禁止恢复独立物品玩法 | NOT RUN |
| G13 动物紫颂果 | **DEFER** / Phase3交互 | `AnimalEntityMixin`；喂食传送需领地/落点/主人授权，不纳首发 | NOT RUN |
| G14 山羊角引仇恨 | **ADAPT** / IN_PHASE1 / E3 | `MobAITweaks`；正在使用INSTRUMENT物品时吸引敌对；原版物品只加限定范围/目标有效性，绝不全维度扫描 | **E3-b限定 PASS**；真实调度/门控/生命周期见R4E3B_REBUILD；人工/完整L3 NOT RUN |
| G15 炽足兽临时骑乘 | **DEFER** / Phase3骑乘专项 | `StriderEntityMixin`；无鞍喂食临时控制后强制下马，暂缓玩家骑乘安全与工具兼容 | NOT RUN |
| G16 雪傀儡重做 | **ADAPT** / IN_PHASE1 / E3小批 | `SnowGolemEntityMixin`, `SnowGolemEntityModelMixin`；南瓜归还/防御/精度/真实投球动画分独立项；不把铁傀儡友伤修复当整组已实现 | **E3-c限定 PASS**；拆四独立ID，真实剪切/归还/护甲/雪球/客户端见R4E3C_REBUILD；人工/完整L3 NOT RUN |
| G17 铁傀儡特殊反击/镐弱点 | **DEFER** / Phase2中立战斗规则 | `IronGolemEntityMixin`；独立于68中的同族友伤；desperation攻击/镐额外伤害需规则与平衡 | NOT RUN |
| G18 史莱姆合并/蜂蜜/发射器 | **DEFER** / Phase3变种审批 | `SlimeEntityMixin`, `SlimeDispenserGoal`, `SlimeEntityRendererMixin`, `SlimeEntityRenderStateMixin`, `SlimeOuterLayerMixin`；上游Beta已知死亡/渲染问题；分裂/组件物品所有权/客户端列在common均需专项，不恢复 | NOT RUN |
| G19 史莱姆熔岩转化与岩浆怪 | **DEFER** / Phase3变种审批 | `AbstractSlimeEntityMixin`, `MagmaCubeEntityMixin`；尺寸/副手转移/额外死亡奖励必须独立审计；首发不加入 | NOT RUN |
| G20 幻翼重做/不祥Boss | **DEFER** / Phase3复杂幻翼 | `PhantomEntityMixin`, `PhantomMoveControlMixin`, `PhantomEntityModelMixin`, `PhantomEntityRendererMixin`, `PhantomEyesFeatureRendererMixin`；失眠/尺寸/中立变敌对/Spawner行为和100HP Boss是独立玩法，未获本Phase1Boss清单批准 | NOT RUN |
| G21 潜影贝养殖/染色/丝触 | **EXCLUDE** / 不计门槛 | `ShulkerEntityMixin`, `LivingEntityMixin`；繁殖/掉物与丝触收集是资源奖励链，不以生物AI复原名义恢复 | NOT RUN |
| G22 潜影弹变种/追踪 | **DEFER** / Phase3末地控制者确定 | `ShulkerBulletEntityMixin`, `ShulkerBulletEntityRendererMixin`, `ShulkerBulletRenderStateMixin`；额外药效/追踪/染色与末地整合方案有关，首发暂缓 | NOT RUN |
| G23 流浪商人自卫 | **ADAPT** / IN_PHASE1 / E3 | `WanderingTraderEntityMixin`；受攻击时失明反馈；短窗口/目标有效性/药水物品使用不得无限生成 | **E3-b限定 PASS**；真实调度/门控/生命周期见R4E3B_REBUILD；人工/完整L3 NOT RUN |
| G24 村民饮食/特殊职业 | **DEFER** / Phase3村民智能 | `VillagerEntityMixin`, `VillagerHeldItemFeatureRendererMixin`；完整职业与生活生态不在Phase1；职业僵尸与此不同 | NOT RUN |
| G25 监守者钟声/血条 | **ADAPT** / Boss独立审批 | `WardenEntityMixin`, `WardenSonicBoomTask`；基础声学交互与保存晕眩/血条，仅审计候选；不立即修改AI | NOT RUN |
| G26 凋灵基础模块 | **ADAPT** / Boss独立审批 | `WitherEntityMixin`, `WitherSkullEntityMixin`, `WitherEntityRendererMixin`, `WitherEntityModelMixin`, `WitherEntityRenderStateMixin`；分阶段/冲刺/特殊弹参考，禁用专属下界合金奖励；控制权与破坏预算需用户另批 | NOT RUN |
| G27 远古守卫者基础模块 | **ADAPT** / Boss独立审批 | `ElderGuardianEntityMixin`, `GuardianBeamAttackGoalMixin`；基础Boss/扫射/切目标，150HP/8甲仅历史默认，非批准增伤模板 | NOT RUN |
| G28 劫掠兽迷你Boss | **ADAPT** / Boss独立审批 | `RavagerEntityMixin`；特殊攻击/180tick参考；完整袭击、击杀与农场另审 | NOT RUN |
| G29 龙战基础让位 | **ADAPT** / Boss独立审批 | `EnderDragonEntityMixin`, `EnderDragonFightMixin`, `EnderDragonChargePlayerMixin`, `EnderDragonFlamingMixin`, `EnderDragonHoldPatternMixin`, `EnderDragonLandingApproachMixin`, `EnderDragonSitAttackMixin`, `EnderDragonStrafePlayerMixin`, `EnderDragonTakeOffMixin`, `DragonFireballEntityMixin`；全部阶段、水晶/出口/复活是高风险控制；本轮只库存，上游完整龙重做和Stellarity深度集成DEFER | NOT RUN |
| G30 悦灵 | **BLOCKED** / 历史不阻断Phase1 | `AllayEntityMixin`, `AllayEntityModelMixin`；最终Allay反编译恢复失败；按用户不再尝试，本轮不读取/恢复其正文 | BLOCKED |
| G31 火药物品/火箭坐骑/龟帽 | **EXCLUDE** / 不计门槛 | `FireworkRocketItemMixin`, `FireworkRocketEntityRendererMixin`, `FireworkRocketEntityRenderStateMixin`, `TridentItemMixin`, `EntityMixin`, `LivingEntityMixin`；独立物品使用/附魔/成长/奖励和硬帽伤害分支不恢复；同类中的其他生物钩子分开裁决 | NOT RUN |
| G32 恶魂眼泪 | **EXCLUDE** / 不计门槛 | `GhastEntityMixin`；额外可收集材料生成，每60tick参考；与当前恶魂慢弹/前摇不同 | NOT RUN |
| G33 骷髅幼体/错峰拉弓 | **ADAPT** / IN_PHASE1 / E3幼体/E4错峰 | `AbstractSkeletonEntityMixin`, `RangedBowAttackGoalMixin`, `RangedAttackGoalMixin`；旧ID未覆盖幼体出生、同类错峰射击；默认出生率与成长/装备/原生调度须明确，不双射 | **E3-c幼体限定 PASS**；五种半尺寸/低眼高/一次出生/保存/转换/原生弓；错峰拉弓仍E4 NOT RUN |
| G34 旋风人散弹 | **ADAPT** / IN_PHASE1 / E3-c；用户明确批准独立Vanilla+预算 | 两版`BreezeShootTaskMixin`额外两发**仅MOBS_ARE_OP分支**；Beta原弹±20°、总3发。不是普通默认技能；如保留须明确Vanilla+数量/冷却/击中预算及现有起跳/落地互斥，不恢复OP游戏规则；不接受改造则建议DEFER | **E3-c限定 PASS**；用户已明确批准独立Vanilla+总3发/±20°/100tick共享预算；原版中弹不变，原生Brain/真实碰撞/重载通过；人工/OP等效L3 NOT RUN |
| G35 原版弩/投射物通用修复 | **ADAPT** / IN_PHASE1 / E4必要部分 | `CrossbowItemMixin`, `CrossbowPullPropertyMixin`, `IsUsingItemPropertyMixin`, `RangedCrossbowAttackGoalMixin`, `ProjectileEntityMixin`, `BodyControlMixin`；两种物品属性钩子全为AUTO_RELOAD_TIME附魔配套，不是原版Bug；仅武器子类/手位/朝向等复现后必要部分，其他附魔路径按G37 EXCLUDE | [E4补审](R4E4_PLAN.md) **L1 PASS / L2 NOT RUN**；已有替代必须另以实际行为证明，不新增虚拟支持数 |
| G36 其余共享钩子 | **DEFER** / Phase3单独必要性审批 | `GoalSelectorMixin`, `MobEntityMixin`, `DisplayEntityMixin`, `ItemHolderEntityRenderStateMixin`, `LivingEntityRenderStateMixin`, `LivingEntityRendererMixin`, `ChargeSpearGoalMixin`, `ChargeSpearTaskMixin`；全局Goal/通用数据/渲染辅助不是已迁移功能；已在68或现有模块使用的意图归相应条目；未选剩余不整体复活 | NOT RUN |
| G37 全局OP/新附魔/专属奖励 | **EXCLUDE** / 不计门槛 | `MobAITweaks`, `MobEntityMixin`, `MeleeAttackGoalMixin`, `LivingEntityMixin`；crazyMobs/新附魔/注册药水或物品/奖励推进排除，不能借基础共享钩子夹带恢复 | NOT RUN |
| G38 职业武器渲染配套 | **ADAPT** / IN_PHASE1 / E2-b | 两类僵尸持弩模型、真实owner/钩住包、持竿手钓线和cast；独立来源标记与分包短等待，客户端关门退出；无新配置ID | **限定客户端 PASS**：16弩/8竿夹具、成年幼体及四手组合；肉眼/完整L3/第三方控制者 **NOT RUN**；见R4E2B_REBUILD |

## 审计覆盖及限制

[upstream-gaps.json](../../docs/rebuild/evidence/r4e0/upstream-gaps.json)逐条映射两版共271个Mixin声明（26.2为136，1.21.11为135）；一个类允许关联多个旧ID/候选或已选模块。这是**声明库存覆盖**，不能把一个类中已迁移部分当全部分支PASS。[upstream-index.json](../../docs/rebuild/evidence/r4e0/upstream-index.json)记录非Allay类的方法、注入声明、配置调用行与SHA256；未直接作为Mixin声明的Goal（SkeletonSpecificGoal/SlimeDispenserGoal/RaiderUseBoatGoal等）也索引。被动生态、Boss、奖励与工具边界已分别列出；没有把未选的全局钩子整体恢复。

公开README只指向源码ZIP，没有完整权威功能列表；本表以发行代码为证据，而非早期讨论。类内未门控分支、OP代码、回报资源链和辅助渲染不是独立启用承诺。两版默认配置差异/方法命名见JSON，逐候选等效行为对照仍NOT RUN。正式新增机制在相应批次须拆成独立ID/配置、校验目标26.3 API及确定的优先级/Flags。

E3候选较多，应拆为E3-a骑乘/关系、E3-b低侵入生态、E3-c选定视觉/Brain能力，每个子批独立审批、完成提交并停。E4再处理共享感知/控制钩子。未能满足性能/资源安全的嗅探兽或其他候选可以在实施方案明确改DEFER，不能暗中省略。独立Boss方案需先写BOSS_PHASE1_SCOPE_DECISION并请用户明确批准；不从候选ADAPT推断开发授权。

## R4-E2-c 本批结果

本批不新增候选G编号，不改变既有G38归属或高风险DEFER。两版官方IllusionerEntityMixin逐项采用ADAPT，保留原版隐身/施法与真实弓；不复制OP轴错误、引用比较ZERO、无限齐射/拾取与不安全换位。vindicator_support来源仍为LEGACY_ONLY/Buildup，未找到上游同技能；不能因原生Raid跑通改写来源。原始upstream-index.json和反编译输出保持只读。本批有限Beta26.2运行仅印证真实幻影/箭源/装备，完整两版量化L3及默认出生分布NOT RUN。Allay历史BLOCKED保持，没有重试。

E3-a rider decision resolved: user explicitly retains `rider_friendly_fire` as Buildup safety / LEGACY_ONLY ADAPT. Implemented and actual native Tick collision tests verified; prior pending/BLOCKED/inactive remarks are history. Current 87 supported / 37 inactive, v10, QA35; whole a development covered, human acceptance pending. No E3-b/c/E4 or later phase. Details: [rider addendum](R4E3A_RIDER_ADDENDUM.md).
