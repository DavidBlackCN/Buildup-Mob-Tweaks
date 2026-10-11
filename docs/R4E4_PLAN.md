# R4-E4 规划：感知、动作与必要修复

> 2026-10-11：整 **R4-E4 已实施，停止等待统一人工验收**。141声明/121支持/20未生效，配置v13，独立QA71场；17旧ID ADAPT、危地旧ID REPLACE、G07/G33两新ID，G35必要路径由现有功能替代、附魔部分EXCLUDE。旧68取舍总数不变。最终Wrapper build/qaJar及502项必需真实Tick/Goal/Brain PASS，客户端实际绘制/粒子与既有显示回归、独立QA/专服/冷重启按报告限定PASS。人工/完整两版L3/多人整合包/长期自然分布与性能仍NOT RUN；以下旧当前计数/停止声明仅历史。不启动Boss/R5/Phase2/3，不推送发布。 用户最新授权整E4连续执行，覆盖下方原逐子批停止建议。三个裁决：恼鬼正值投射物总3倍、有限发光且失明优先/不能隔墙攻击、反胃等级0～255且限本批支持射手，均已明确采用。[结果](R4E4_REBUILD.md) / [统一验收](R4E4_ACCEPTANCE_CHECKLIST.md)。

以下是实施前的原始规划与审计收据，NOT RUN/待裁决仅代表当时状态；本批未扩到Boss或后续Phase。

2026-10-11。用户已通过 E3 最新补验并批准进入下一阶段；[E3 收尾记录](R4E3_CLOSURE.md)。按上一批列出的下一具体任务，本轮完成 **E4 规划批**：源码/原版审计、范围冻结建议、分批顺序和验收设计。**E4 功能实现及行为验收均 NOT RUN**，没有用源码证据增加支持数。本方案不是一次恢复全部共享 Mixin 的计划。

## 环境与来源

唯一目标 `26.3/`、分支 `26.3`，起始干净 HEAD `b56088d42f419ae88b76d35c11a7c8ec28ab8035`。旧 `26.3-tests/` 严格只读，保留现有 S2 编译隔离、历史及已通过 R4 功能。此次不升级 Minecraft 或工具链；`VERSION_MIGRATION.md` 的版本迁移审批门不因同版本内功能规划自动触发。

| 项目 | 实际配置/证据 |
| --- | --- |
| Minecraft / Java | 26.3 / toolchain、release、source、target 均 25；既有 JDK 25.0.3 |
| Wrapper / Loom | 9.7.1 / 1.18.3；Wrapper 校验值保留 |
| 命名 | `build.gradle` 无 Yarn/mappings 依赖，26.3 官方未混淆名称 |
| Loader / Fabric API | 0.19.5 / 0.162.0+26.3 |
| 第三方 | Fzzy 0.7.7+fix3+26.3 非传递；Kotlin 1.14.1+kotlin.2.4.20；可选 Mod Menu 21.0.0 |
| 注册/隔离 | `BuildupMobTweaks.onInitialize → FeatureRegistry → 各 rebuild 模块`；common/client 分开；独立 QA/GameTest 不进正式/source JAR |
| 当前交付基线 | 139/102/37，配置 v12，QA50；既有 449 测试 PASS 是上一批证据，本轮没有重跑 |

上游仍用 R0 已核验两版官方发行 JAR：`CExkBTN8`（1.11.0-beta）与 `bFb56Zw2`（1.11.3-1.21.11）。本轮只读核验哈希、恢复源码和目标已解析 JAR，不下载依赖、不再尝试 Allay、不编辑反编译目录或缓存。

源审计的逐文件哈希与 **24 个原版类**完整 `javap -p -c -s` 见外层 [reference.json](../../docs/rebuild/evidence/r4e4-plan/reference.json)；两版源码文本差异见 [version-comparison.json](../../docs/rebuild/evidence/r4e4-plan/version-comparison.json)。两版命名空间不同，文本不同不等于行为不同；下面按已恢复的语义对照。该证据只是 L1，不是上游实战 L3。

## 覆盖范围

现行实施计划中的 **18 个旧 ID**（17 ADAPT、1 暂定 REPLACE）与 **3 组 G 候选**纳入本次规划。G 候选不是三个已经新增的功能 ID，G35 可能全部由现有功能替代或排除，没有预定支持数。旧 68 项取舍总数不变：PORT1/ADAPT47/REPLACE6/DEFER10/EXCLUDE4。

| 分组 | 完整名单 |
| --- | --- |
| 感知，6 ID | `sense_glowing`、`sense_blindness`、`sense_nausea`、`sense_invisibility`、`sense_crawling`、`sense_crouching` |
| 动作，6 ID | `movement_jump_input`、`movement_clear_strafe`、`strafe_obstacle_jump`、`melee_effect_interval`、`melee_high_target_jump`；`strafe_hazard_check` 按 REPLACE 候选复核 |
| 修复/视觉，6 ID | `evoker_flee_speed`、`evoker_avoid_target_fix`、`drowned_swim_visual`、`burning_projectile_visual`、`burn_freeze_visual`、`vex_projectile_weakness` |
| 上游缺口候选，3 组 | G07 潜行接近动物；G33 骷髅错峰拉弓（幼体已在 E3）；G35 必要弩/投射物/朝向修复 |

已批准的 `sense_darkness`、`omen_pressure`、`hostile_rest`、`rest_sound_wake`、`hostile_spawn_effect` 继续 DEFER；E4 总标题中的休息/声醒只审查取舍，不据此接入。亡灵马日烧 Phase 2 DEFER。原版矛、已有主动盾、已交付走射/后撤/装备守恒保持原归宿。Boss 范围另批审批，R5/Phase 2/3 不在 E4 实施范围。

## 源码审计与差异

| 项目 | 已核实事实 | 实施约束/待验证缺口 |
| --- | --- | --- |
| G07 | 两版 `FleeEntityGoalMixin` 仅在原生 `AvoidEntityGoal.canUse` 成功后，动物且候选玩家潜行时阻止开始逃离；另一个“有控制乘客则全局不逃离”分支不受该配置控制。26.3 Cat/Ocelot 子 Goal 保留驯服/信任判断，Rabbit 有玩家、狼及怪物避让 | 只补玩家潜行条件；先限 Cat/Ocelot/Rabbit 原生避让链，不给牛鸡新增逃离 Goal，不抑制受伤 Panic、不改变敌对避让、不照搬全局乘客分支。最近潜行者不能遮蔽第二名站立威胁，需在候选过滤阶段处理并测试 |
| 隐身/潜行 | 上游初次索敌的附加上限分别为 followRange/4、/2，并为爬行 /3；是互斥分支，目标挥手/使用物品/旋转时缩减后的距离再 ×1.2，受伤等路径可绕过。26.3 `LivingEntity.getVisibilityPercent(ServerLevel,Entity)` 已按 isDiscrete ×.8、隐身 ×(.7×max(护甲覆盖,.1))，还应用 MOB_VISIBILITY 装备组件；`TargetingConditions.test` 使用这些值并保留最小 2 格 | 禁止把 /4、/2 直接乘在原版结果上；优先保留原版组件和更严格的既有隐身限制，附加规则必须明确是距离上限。是否需要 ADAPT 或可 REPLACE，等待匹配原版/on/off 行为证据，当前不改取舍计数 |
| 失明/反胃/爬行 | 上游 `ActiveTargetGoalMixin` 在成功的 canUse 尾部加初次发现距离限制：失明 /3 优先于反胃 ×.8，爬行 /3；不是直接缩 FOLLOW_RANGE 属性 | 不能全局把 hasLineOfSight 判 false、破坏所有原生攻击/受击仇恨。先做初次主动发现的窄适配，不主动抹掉当前目标；原目标保持/退出走既有生命周期。过滤须在候选选择中生效，不能让一个不合格最近目标挡住其他目标 |
| 发光 | 上游 `LivingEntity.hasLineOfSight` 尾部在无当前目标时将发光目标设为可见，且发光能覆盖前面的失明 false | 建议只放宽有限距离的主动发现，保留 canAttack、队伍、创造/旁观、类型选择与寻路要求；不赋予隔墙射击/近战，不覆盖玩家 LOS，不照搬发光压过失明的顺序。优先级需在该批方案明确 |
| 反胃弹道 | 两版 `ProjectileEntityMixin` 对 Mob owner 的 shoot 不确定度增加 `sqrt(amplifier/255)*122.5+10`，没有独立配置，也不是“只降低感知” | 拆清感知与弹道两个作用路径，但共享旧 ID 独立门；玩家/发射器/无 owner/外部实体不改，等级输入有界；必须测真实方向/命中分布，不能只测参数调用。原公式强度是否保留在该批前冻结 |
| 横移动作 | 上游 `MoveControlMixin` 直接改变危地判定、ordinal 和反射枚举操作，吞 Throwable。26.3 MOVE_TO 已有碰撞形状/台阶起跳及门栅栏排除，JUMPING 维持速度，WAIT 只清前进输入 | 禁止整份复活；先用真实 MoveControl 调度记录 Xxa/Zza/速度/位置，区分导航起跳和横移起跳，不把原版 MOVE_TO 已支持当作 STRAFE 已支持。只在正式重建实体和 Buildup 拥有的运动阶段补需要的输入/清理 |
| 危地 REPLACE | `SkeletonBehavior.safeStep` 当前检查已加载块、脚下支撑、目的碰撞；`SkeletonStrafeMixin`/跳射已用它。原版 `MoveControl.isWalkable(FF)Z` 使用节点类型 WALKABLE | 尚不能证明 safeStep 覆盖火、岩浆、粉雪等全部危地；保持原 REPLACE 为待核验，真实失败则修当前安全步或明确差异，不仅把旧 ID 加 supported。非骷髅不能宣称同等替代 |
| 近战节奏/高目标 | 上游 resetAttackCooldown 对疲劳每级 +2 tick、急迫每级 -2，急迫路径最低6；高目标起跳还限困难、小宽度/深度、非水、地面、LOS和几何高度。26.3原生 resetAttackCooldown/getAttackInterval 为 adjustedTickDelay(20) | 与现有连击/扑击/矛/骑乘动作仲裁；最小6是调度间隔语义，不能直接宣称真实伤害每6世界tick。上下限、等级截断及困难/小型白名单在 b 批冻结并测实际命中时间，保护无敌帧 |
| 唤魔者两个旧 ID | 取舍表注明 LEGACY_ONLY 独立配置；上游火球配置下重装 8 格、.4/.8 速度避让，并在 canUse 禁止施法/火球状态，canContinue 检查目标。当前 EvokerBehavior 仍保留原避让，优先级1特殊 Goal 使用 MOVE/LOOK | 不凭旧 ID 名字判原版 Bug；实际复现施法与避让竞争/目标切换，已有仲裁覆盖则说明 REPLACE，不把速度风格调整写成错误修复。两项独立门，既有召唤/火球/图腾必须回归 |
| 溺尸泳姿 | 上游不只是视觉：DrownedEntityMixin 还改变姿态碰撞箱/出生碰撞箱、游动运动；renderer -10 改 -80，model 调头。26.3 DrownedRenderer 仍按 swimAmount 以 -10-xRot 旋转；模型已走新版分离动画方法 | 只补已选视觉，不恢复碰撞箱/游速改动、不照搬旧 setupAnim 签名。实际水中/出水/持三叉戟/幼体/关门绘制与专服同步验收 |
| 燃烧弹体 | 恢复源码的箭 shoot 入口发3类粒子；SmallFireball 中的 particleExplosion 涉及撞风弹的额外爆炸，不是普遍尾迹 | 不把碰撞爆炸/吞异常夹入视觉项；仅有真实燃烧状态的已支持 owner 弹体，有限频率/数量；原 no-fire、伤害/火时长/方块点火全部保留，关闭没有粒子残留 |
| 燃烧/冻结视觉 | 补审新增两版 `LivingEntityRendererMixin`：extract 保存 onFire且非免疫、fullyFrozen，submit 分别橙色/浅蓝乘色；`HostileEntityMixin` 是燃烧粒子。旧表仅列 Hostile/LivingEntity，缺了实际冻结绘制来源 | 纠正来源；不能仅加粒子便宣称两类恢复。优先实际客户端有界乘色，保留原红伤害叠层、透明/隐身/装备和他模颜色；不改火免疫/冻结伤害，不扩到所有 LivingEntity |
| 恼鬼弱投射物 | 两版 `VexEntityMixin.hurtServer` 用 `1+200*.01`，所以默认总量 **3倍**，旧“200%参考”不是总2倍；上游非正伤害也额外加值、乘数非正时挡所有伤害 | 只对正值且真实 IS_PROJECTILE 伤害调整，其他伤害原样；该批前明确“额外百分比”还是“总倍率”及默认值。建议保留上游额外200%/总3倍并清晰标注，不移植免疫/负伤害分支。此数值尚未作为用户裁决 |
| G33 | 两版在准备开始拉弓时查询同种类、同一目标、其他正在使用物品者；RangedBow/RangedAttack 用相近逻辑。持续等待没有上限，后者用 attackTime++ 延后；当前 SniperGoalMixin 已有真实20tick拉弓桥接 | 五类精确原生骷髅、真实持弓，单独新 ID；普通弓与既有狙击桥接共享协调，不双拉/双箭，不取消整段导航 tick，不套在雪傀儡/幻术师/玩家。必须有有界查询与防饥饿预算 |
| G35 | CrossbowPullProperty/IsUsingItemProperty 全部服务 AUTO_RELOAD_TIME/新附魔；CrossbowItem 主要为新弹药/自动装填附魔。RangedCrossbowGoal 另有武器子类/手位修正；BodyControl 扩展使用物品转身 | 前三类新附魔路径按原 G37 EXCLUDE，不能列成缺失的原版修复。真实双手/自定义 CrossbowItem 子类/目标退出/朝向先复现，已有掠夺者/猪灵/职业弩链可 REPLACE，必要项单独窄修。没有证据则不新增覆盖 |

上述均是源码事实或明确的建议；E4 的“真实缺陷”判定、原版替代成立、上游匹配、实际兼容尚未经过本批运行，保持 **NOT RUN**。

## 分批顺序与边界

保留原 E4-a 感知、E4-b 动作、E4-c 修复/视觉的分组；优先先做局部入口，再碰共享感知/移动钩子。每个子批 **最多2个共享钩子**，完整实现、真实调度、独立 QA、报告、自检、本地提交后停止。下表是后续批次名单，本轮未实施。

| 顺序 | 子批 | 实施内容与最小验收 |
| ---: | --- | --- |
| 1 | **E4-a-1** | **G07潜行接近动物**，独立门/精确三类型。真实玩家普通接近逃离、潜行接近不启动玩家避让、再次站立恢复、受伤 Panic不被压制；第二站立威胁/非玩家威胁/驯服信任/关闭/专服同步 |
| 2 | E4-c-1 | G33错峰拉弓，五类普通弓/狙击、成人/幼体/主副手；2与6射手同目标逐射手真实发箭时序、等待上限、LOS/目标/死亡/卸载/开关退出。不改变单射手最小拉弓时长 |
| 3 | E4-a-2 | 失明+爬行初次主动发现；原版/on/off及另一合法目标、受击报复、清除效果、物理LOS、特殊技能退出 |
| 4 | E4-a-3 | 隐身+潜行绝对视距上限或经实测 REPLACE；裸身/装备/组件、姿态互斥、最近不合格目标不能遮蔽另一目标 |
| 5 | E4-a-4 | 发光有限主动发现，专测隔墙发现而不能隔墙发射、失明优先、队伍与不可攻击条件；不改全局LOS |
| 6 | E4-a-5 | 反胃感知+弹道，等级与强度先冻结；真实射手多次弹道/命中分布，负例玩家/无owner/非支持者；不扩 OP |
| 7 | E4-b-1 | 跳跃输入+横移清理；记录真实XZ位移和落地/阶段退出，不只读输入变量，原走射/后撤/旋转/鸡骑士/骷髅马回归 |
| 8 | E4-b-2 | 横移障碍起跳+危地REPLACE核验；火/岩浆/落差/粉雪/门/栅栏/天花板/液体真实场景，已有安全步范围精确说明 |
| 9 | E4-b-3 | 近战效果间隔+近高目标起跳；用原生Goal真实命中时间与难度/体型/无敌帧/水/骑乘/配置回退证明 |
| 10 | E4-c-2 | 两项唤魔者旧ID；先复现再决定窄ADAPT/REPLACE，保持真实召唤与火球仲裁，不用风格差异冒称Bug |
| 11 | E4-c-3 | 溺尸游泳视觉；仅姿态/模型，实际客户端持三叉戟与恢复测试，不改服务端碰撞箱 |
| 12 | E4-c-4 | 燃烧弹体+燃烧/冻结视觉；≤2共享钩子，超出则拆分。帧内颜色/粒子预算/无状态泄露及伤害负对照 |
| 13 | E4-c-5 | 恼鬼投射物弱点；数值明确后实际arrow/trident/tag伤害与普通伤害负例，真实死亡/击退/冲锋回归 |
| 14 | E4-c-6 | G35必要部分收口；逐候选复现/REPLACE/EXCLUDE证据，不承诺虚拟功能数，不泛化第三方枪械支持 |

G33建议预算：同原生类型、同一实际目标、8格内，协调轮询最多每4tick；每次只需判断是否有合格拉弓者，避免建立全世界常驻实体索引。等待最多40世界tick，达到上限允许本射手正常进入20tick拉弓；不是严格“一箭接一箭”，密集群体仍可有限重叠。该预算是安全建议，在 c-1具体方案冻结，不冒称上游原值。禁用、NoAI、未知附件版本、外部控制标签、卸载/目标退出及时放弃自有等待状态，不存储或恢复在途动画。

共享感知建议首批范围：掠夺者、五类精确骷髅、僵尸/尸壳/溺尸、女巫；仅正式支持、非NoAI、服务端、无外部退出标签。后续扩大实体名单需要本批验证和范围说明，不能凭 `instanceof Mob` 涵盖 Boss/所有第三方。逐候选保留原生 selector、队伍/可攻击条件、装备可见组件、距离最小值与可达性；主动发现和当前目标保持分开，不修改 FOLLOW_RANGE属性本身。

## 下一具体批：E4-a-1 制作方案

G07 暂定新 ID `animal_sneak_approach`，不能复用 `sense_crouching` 或 `animal_group_panic` 开关；首发三类型 Cat/Ocelot/Rabbit，其他动物保持原生行为，不宣称“所有动物不怕潜行玩家”。配置分组 passive，默认启用、独立 disable 标签与全局/实体 vanilla_ai 回退沿用已发布约定。最终字段/version迁移方案在实现时核对 Fzzy 真实配置链，不借此清理 v12 旧字段。

调用链已核验：原生动物注册避让 → Cat/Ocelot/Rabbit 子 Goal.canUse → AvoidEntityGoal.canUse → avoidEntityTargeting/原生最近候选/随机逃离点 → 路径与 start → GoalSelector原生tick。推荐在原生候选 selector 内添加潜行玩家排除，按正式 gate 动态查询，保留原有匹配条件；不能仅 TAIL拒绝最近玩家，产生“潜行玩家遮住站立玩家”的漏洞。进行中只退出确为潜行玩家的自有附加规则，站立/禁用重新由原生调度开始避让；不停止任意导航路径或已有 Panic。

目标文件预计：独立 `rebuild/ecology` 小策略、一个窄 `mixin/rebuild` 避让入口、FeatureId/Registry、passive配置与双语、独立QA场景/生态GameTest、报告与清单。不替换原 GoalSelector，不反射、不新增第三方依赖，不修改物品、装备、出生概率或持久化附件。

独立 QA 计划命令族 `/buildupqa e4 ...`，场景 `sneak_cat/sneak_ocelot/sneak_rabbit`；**当前命令尚不存在，勿作为已交付命令执行**。保持单会话、世界/地形归还和1200tick自动清理，玩家仍亲自潜行/站立/攻击，QA不能把 mob设NoAI或直接调用canUse来制造PASS。实际测试使用原生 ServerPlayer/GoalSelector/Server Tick，记录逃离Goal、路径和真实距离变化，匹配测试随机源，不以变量计数代替逃离行为。

E4-a-1人工短清单（当前 NOT RUN）：临时世界站立靠近三类未驯服/未信任动物观察逃离；cleanup后潜行重复应能靠近；站起应恢复避让；伤害后仍会正常惊逃；第二名站立生存玩家存在时不能被潜行玩家遮蔽；off后恢复原避让。完成后才追加已验证 UPDATE_NOTES、本地提交并停，单人不能代替第二玩家专项。

## 验证与交付纪律

每项实施需独立门控与真正的运行链，NOT RUN/BLOCKED不得提升为PASS。任何共享变化必须回归掠夺者、骷髅、僵尸、女巫和原版负对照；同时关注门盾绘制、生成备用剑零掉落、幼体半尺寸与弓握持不回退。至少原版/关闭/开启匹配输入，真实Server Tick+Goal/Brain调度；新增动作观察实际位置/伤害/弹体，视觉观察实际客户端提交和截图，尽可能运行上游同场景对照。完整L3未完成则明确记录。

实现批命令在目标版本使用 Wrapper `build qaJar -PtestEula=true --console=plain --no-configuration-cache`，另有普通专服/客户端/冷重启；真实多人/整合包/性能未执行不得隐去。旧449用例必须随行为变化回归，不能通过删测试、放宽断言、直接技能调用或只报测试总数验收。

本轮纯文档与只读审计：JAR/源码/native签名及文档内容核验 **PASS**；E4构建/新QA/运行时/人工/L3 **NOT RUN**；恼鬼倍数、发光与失明优先、反胃强度属于后续具体批的待冻结决策，不阻断首批G07规划。无需重新构建，不适用UPDATE_NOTES。自检与范围收据见外层 `docs/rebuild/evidence/r4e4-plan/verify.json`。最终进行一次本地文档commit并停在规划批，等待下一具体制作批确认；不推送发布，不进入Boss/R5/后续Phase。外层计划/证据未被目标Git提交覆盖。

## 本轮自检结果

**Code Review: PASS（纯文档/审计范围）**。无 BLOCKER/MAJOR；未覆盖用户改动，起始干净。`python docs/rebuild/scripts/r4e4_plan_verify.py` 最终 **79项检查、43个链接 PASS**，`git diff --check` PASS。两官方JAR、44份恢复源码及604份旧S2源码/构建相关文件哈希保持；目标已跟踪非Markdown文件与上一批正式JAR保持不变，139/102/37由当前实际枚举/白名单重核。普通构建/新增运行验证本轮NOT RUN，纯文档不要求重复上批449测试。

校验工具前两次分别漏算白名单中 `PILLAGER_` 前缀规则、漏匹配无目录前缀的新文档链接；已修正审计脚本后完整重跑，未通过修改生产白名单、删掉文档或放宽行为断言使检查通过。完整上游/多人/性能风险继续披露；E4功能尚未实施，不宣称本方案完成了E4行为。UPDATE_NOTES不适用，没有重复写入上一批baby修复说明。
