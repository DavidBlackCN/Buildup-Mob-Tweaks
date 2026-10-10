# R4-E3 规划：关系、骑乘、生态与选定能力

2026-10-10；用户本轮授权“R4-E3的规划”，不是实现授权。目标仅26.3/分支26.3，起始HEAD `1343ad03bc48598ea5681dfad4139b3ddf47a00e`，Git工作区干净。26.3-tests只读。本轮仅写规划/来源纠正文档；不改Mod、配置、白名单、测试工具或依赖，不启动E4/Boss/R5/Phase2/3。

## 基线与完成口径

环境：MC26.3、Java25.0.3、Gradle Wrapper9.7.1、Loom1.18.3、官方未混淆命名、Loader0.19.5、Fabric API0.162.0+26.3、Fzzy0.7.7+fix3+26.3（非传递）、Kotlin1.14.1+kotlin.2.4.20、可选Mod Menu21.0.0。不新增仓库、依赖或框架。当前124个声明ID、77支持/47未生效、配置v10、独立QA25场景。

E2功能开发已覆盖，最近钓线修复及301项真实调度/客户端限定验证见[R4E2_ACCEPTANCE_FIXES_1](R4E2_ACCEPTANCE_FIXES_1.md)。本轮不重跑该验证，也不把用户开始E3规划记成完整E2人工验收通过；统一E2清单及两项既有测试首轮失败的未证实根因仍保留。

E3覆盖**12个旧ID候选**与**12组新候选**（生态9组、选定能力3组）。这不是最终支持数：旧ID中亡灵马日烧来源需纠正，雪傀儡一组要拆独立功能，原版覆盖项可能REPLACE，高风险项可经明确裁决DEFER。全部实现/运行时/人工/L3状态当前**NOT RUN**；不增加支持计数来宣称完成。

## 来源审计中的重要结论

已重核两版官方JAR哈希及18份相关恢复源码；目标原版12个类的实际签名/字节码见外层`docs/rebuild/evidence/r4e3-plan/reference.json`、`native-signatures-bytecode.txt`。证据保存两版来源与SHA256，非重新下载/重新反编译，历史R0/E0证据未改写。

| 项目 | 已核实事实 | 规划决定 |
| --- | --- | --- |
| 灾厄—僵尸 | 两版IllagerEntityMixin用Zombie.class添加优先级5目标；普通开关为真时else-if吞掉僵尸村民独立分支。AbstractZombieEntityMixin反向添加目标优先级2或3，ZombieVillagerEntityMixin用排他条件避免再装一次 | ADAPT；按确切原生类型区分两个独立开关，保留既有目标/玩家优先与受击报复，不照搬继承扩大范围和开关耦合 |
| 傀儡友伤 | 上游IronGolemEntityMixin移除HurtByTargetGoal后重装忽略AbstractGolem的报复Goal；铁/雪傀儡还无条件setTarget过滤Tag。26.3铁傀儡仍有优先级2的原生报复Goal，忽略类数组为空；雪傀儡没有同类报复Goal | 先做原版真实伤害/目标基准；静态代码不足以宣称Bug。复现后窄ADAPT，已覆盖部分REPLACE；不全局免伤、不排除任意AbstractGolem继承类 |
| 亡灵马日烧 | 两版SkeletonHorseEntityMixin里名叫burnInDay的方法没有日照/点火，只处理无持久化自然骑乘马的移除；ZombieHorseEntityMixin记录自然骑乘来源。旧GeneralHostileRules则确有每20tick日光点火的S2自研逻辑 | E0的UPSTREAM来源标注纠正为LEGACY_ONLY；仍不生效。建议Phase2 DEFER，**待用户裁决**，不把改名后的旧逻辑当上游复原，也不另行添加自然马移除功能 |
| 骷髅转凋灵 | Beta SkeletonEntityMixin是在die中，凋零效果或凋灵/凋灵骷髅致死时convertTo，不是普通生长或无条件维度转换 | 死亡/转换边界专项；保留装备与备用物品所有权、禁止重复奖励及转换循环，实施前独立冻结规则；不承诺转换保留同一UUID |
| 旋风人散弹G34 | 两版BreezeShootTaskMixin的额外两发仅在MOBS_ARE_OP分支；Beta从原弹旋转±20°，总计3发 | 不是普通默认技能/原版Bug。若保留，须明确批准独立Vanilla+ ADAPT及数量/冷却/击中预算，不能恢复OP游戏规则。尚未批准数值；不接受改造则建议DEFER |
| 嗅探兽G01 | 上游同步findClosestBiome3d半径6400及大体积地下扫描 | 必须先证明可增量、可取消、不会同步生成区块的查询方案；无法满足则明确建议DEFER，不把查询扔到异步线程直接访问世界 |

Horse/G34的取舍建议没有删除生产功能，也没有在本轮擅自改变已批准的Phase1必做集合；这里只纠正来源并列出决策门。

## 建议分批顺序

默认建议11个小批，每批给具体方案、独立验证、报告/人工路径，验证自检后本地commit并停止。用户可另行授权一次完成指定组并统一验收；本次规划不等于对11批全部代码授权。表内新ID为规划草案，不在本轮注册；后续逐批核对最终命名、默认值与配置迁移。

| 顺序 | 子批 | 具体范围 | 依赖/主要验收 |
| --- | --- | --- | --- |
| 1 | **E3-a-1 关系基线** | illager_zombie_conflict、illager_zombie_villager、golem_friendly_fire_fix | 首批建议；[具体方案](R4E3A1_MIGRATION_PLAN.md)。自然取得目标、真实互击/投射物、独立开关、玩家/村民优先及原生袭击 |
| 2 | E3-a-2 骑乘安全 | rider_friendly_fire、chicken_jockey_charge、hostile_escape_seat | 精确同一骑乘树的箭/三叉戟过滤；既有鸡骑士扑击；仅受伤且有安全落点的原生船脱离。不生成额外坐骑、不让任意乘客免伤 |
| 3 | E3-a-3 特殊坐骑动作 | husk_spear_charge、husk_camel_circle、skeleton_horse_charge | 在a-2后；实际原版矛、已存在CamelHusk/骷髅马，一套移动控制。环绕/冲刺互斥、有限次数/冷却、碰撞/卸载/下马恢复，不借此重开被DEFER的出生坐骑生成链 |
| 4 | E3-a-4 稀有事件/转换 | skeleton_trap_variants、skeleton_wither_conversion；undead_horse_sunburn只做待裁决复核 | 原生陷阱事件一次，骑手/坐骑预算和递归保存；实际死亡转换中的主副手/备用装备、XP/掉落只结算一次。日烧未获取舍确认前不写实现 |
| 5 | E3-b-1 基础生态 | G02鱿鱼墨汁、G03荧光鱿鱼注视、G04鸡逃离威胁 | 新ID草案squid_ink_defense/glow_squid_gaze/chicken_threat_escape。直接攻击者/有限范围、距离LOS和冷却；鸡保留喂食/繁殖，不额外羽毛掉落 |
| 6 | E3-b-2 动物/宠物 | G06群体惊慌、G08宠物恢复、G11猫避弹 | animal_group_panic/tameable_recovery/cat_projectile_dodge。惊慌传播每次有集合/次数预算，不反复扩散；宠物伤后等待、主人/坐下/载入有效；猫真实移动避弹而非无敌 |
| 7 | E3-b-3 有限交互 | G14山羊角引仇恨、G23流浪商人自卫 | goat_horn_aggro/trader_self_defense。实际INSTRUMENT使用者、限定距离/目标优先；商人短窗口实际受伤反馈，不生成无限药水/奖励，不接管完整村民生活 |
| 8 | E3-b-4 嗅探兽 | G01探索独立专项 | sniffer_search；先冻结已加载区块/白名单/请求队列/每tick预算/取消和超时语义。生态/大世界验证不通过则不接入正式白名单 |
| 9 | E3-c-1 雪傀儡交互 | G16拆项：南瓜归还、南瓜防御 | snow_golem_pumpkin_return/snow_golem_pumpkin_defense。真实消费/剪切/掉落数量守恒，独立门控；防御是条件属性不是全局基础加甲，数值待专项批准 |
| 10 | E3-c-2 雪傀儡远程 | G16拆项：投球/精度、实际投球视觉 | snow_golem_ranged_behavior/snow_golem_throw_visual。原生Goal/雪球一次释放、效果实际命中；客户端真实submit/动画而非只提取坐标；临时雪球不跨重启复演 |
| 11 | E3-c-3 选定能力 | G33幼体骷髅部分、G34散弹待预算决策 | skeleton_baby_variant/breeze_shotgun。先出生/加载一次选择、尺寸/视线/碰撞、实际装备/客户端；是否成长单独说明。散弹走原生Shoot Brain，和起跳/落地预算互斥。错峰拉弓仍属E4 |

不保证每子批一定增加表中数目的ID：REPLACE不伪装成新实现，候选未完成源码/API/数值冻结不得默认启用。每项最终报告必须指出属于上游核心、主动安全修正或Buildup新增设计。

## 全E3共同交付要求

1. **实现**：仅新rebuild模块与必要窄Mixin；保持原版GoalSelector/Brain，目标/导航/攻击由正常Server Tick推进。查询必须有空间、频率、数量/请求预算，不全维度扫描、不无界每tick遍历。
2. **物品/实体守恒**：不覆盖外部装备、不隐藏第二份物品；自然/指令/刷怪蛋/刷怪笼/转换/LOAD输入分别核验。转换记录旧→新UUID/所有权，不重抽已赚取状态，不虚构跨重启在途动作；乘客树不得复制/无限拆分。
3. **配置**：每独立能力有门控、准确描述/双语、总门/vanilla_ai/disable_ID回退；旧v10字段保留。新增生态ID按子批给配置升级方案，未知附件不透明保留；不在规划时先升版本。
4. **QA**：沿独立src/qa与qaJar扩展真实场景，暴露Actor/Target/Vehicle UUID、门控、原生Goal/Brain、阶段/世界冷却/计数/未触发原因。只固定已核实输入，不手动调用技能、发复制掉落或全局改100%概率；多人共享会话/清理限制按当前工具如实说明。
5. **验证**：每代码批必跑Wrapper build（含GameTest）及qaJar。原版/Buildup关闭/启用三组同输入；实际伤害/位移/转换/生成结果、独立关闭、目标退出、NoAI、死亡、转换、卸载/冷重启、物品守恒；相关客户端还要真实网络+渲染提交/几何或可见动画。对照可运行上游，无法对照标NOT RUN/LIMITED。
6. **回归**：既有E1/E2装备、目标、袭击与持手链；门盾、生成备用木剑死亡、真实钓线/普通玩家浮漂、猪灵原仇恨。完整人工袭击、默认自然概率、多人整合包/性能不能由GameTest数量代替。
7. **概率/性能**：在运行前写N/容差/配置快照；自然概率和QA定向分开。高成本检索给实际最大查询数/执行时间/取消证据；不以测试强制加速或卸载实际合法子实体掩盖问题。
8. **收尾**：功能矩阵、来源/偏差、PASS/FAIL/NOT RUN/BLOCKED、日志/哈希、5～10分钟主要人工路径（持久化/专项耗时另计）、CODE_REVIEW、适用UPDATE_NOTES、本地commit；停止而非自动进入下一批。

建议未来整E3人工合并清单按“关系→骑乘安全→特殊坐骑→陷阱/转换→生态→宠物/交互→嗅探兽→雪傀儡→幼体/散弹”串联；每场cleanup，生命周期/保存/自然概率单列。该清单此时只是验收设计，不冒充现已存在的QA命令。

## 本轮校核与停止点

文档规划自检 **PASS**：范围、来源纠正、依赖拆分、签名证据、链接、计数和状态一致；未发现本轮文档BLOCKER/MAJOR。上游两JAR及只读旧工程哈希核验PASS。E3代码、L1/L2/L3/HUMAN均NOT RUN。`gradlew build`/运行时未执行（纯文档，不复用历史结果冒充新验证）；UPDATE_NOTES不适用。

外层PHASE1_RESTORATION_PLAN的现行提示与外层规划证据不在26.3 Git根内，须另行保管。计划文档按持续授权本地提交，不推送/发布。建议下一次明确授权只实施**E3-a-1**；其余子批数值/签名/具体方案在相应批开工前补齐。到此结束规划，不开始代码。
