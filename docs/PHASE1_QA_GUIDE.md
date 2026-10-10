# Phase 1 确定性 QA 使用说明

## E3-a 独立场景补充

原25场景及原命令不变；新增9场景通过 `/buildupqa e3 spawn <scene>`、`check`、`off`、`cleanup` 操作，完整路径见 [E3-a人工清单](R4E3A_ACCEPTANCE_CHECKLIST.md)。场景名为 relation_zombie、relation_villager、golem_misfire、chicken、boat、camel、horse、trap、conversion。使用25×25×7已加载空空气场，中心仍固定Z+8；地板/屋顶恢复，仅清理本会话及对应转换/陷阱事件后代，60秒自动结束。两套工具互斥，每台服务器仍只允许一个会话，要求owner权限4。

E3工具不修改全局配置。鸡/马直接固定本夹具已抽中选择（正式出生仍10/1000），不是更改全世界概率；boat第15tick真实伤害、conversion第15tick真实凋零致死输入。trap必须实际生存玩家近场，不能由控制台模拟Goal。`off` 是添加vanilla_ai标签，不能撤销已受伤的原版报复、已完成转化/陷阱，也不能禁用原生矛。check显示实际UUID/目标/被攻击者/Goal/动作计数和保存冷却，关系queries预算、targetHP、未触发理由；场外观察避免玩家优先目标影响夹具。

BLOCKED的骑乘过滤与Phase2日烧没有QA接口。E3场景不改变旧场景的全局概率/权限，正式包与源码包均不含QA。

> 2026-10-10 E3-a 当前实施：[工作报告](R4E3A_REBUILD.md) / [统一人工清单](R4E3A_ACCEPTANCE_CHECKLIST.md)。九项接入、尸壳矛由26.3原版REPLACE，亡灵马日烧已由用户裁决Phase2 DEFER；骑乘友伤过滤来源纠正为LEGACY_ONLY，其保留/暂缓裁决待答复，BLOCKED、未实现。当前124声明、86支持/38未生效、配置v10、独立QA34场景。下文较早的“当前”数字/仅规划/待授权语句为各历史时点；完整E3-a不能宣称完成。真实Tick/运行边界及最终证据见报告，人工/完整L3/自然分布/多人性能NOT RUN。本批收尾停止，不进入E3-b/c、E4、Boss、R5或后续Phase，不推送发布。


> R4-E2-c当前完整交付：[报告](R4E2C_REBUILD.md)/[人工清单](R4E2C_ACCEPTANCE_CHECKLIST.md)。四项幻术师/卫道士行为，配置v10、77支持/47未生效、独立QA25场景；298项真实调度验证。原生五波袭击生命周期（受控击杀推进）、实际客户端与专服/冷重启限定PASS；上游仅有限对照。人工连续袭击/完整L3/自然分布/多人整合包/性能NOT RUN。批末本地提交后停止，不进入E3/E4/Boss/R5/Phase2/3。

目标 MC Fabric **26.3** / Java25 / Loader0.19.5 / API0.162.0+26.3 / Fzzy0.7.7+fix3+26.3 / Kotlin1.14.1+kotlin.2.4.20；Buildup1.0.0/config v10。工具使用服务器当前配置，拒绝已关闭能力，**不修改全局配置、概率或出生规则**。只在废弃测试世界安装独立QA Mod。正式包没有`buildupqa`命令、QA类或GameTest入口。

## 安装及运行

在`26.3/`运行：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.3'
.\gradlew.bat build qaJar -PtestEula=true --console=plain
```

`testEula=true`接受测试服务器EULA。生成正式`build/libs/buildup-mob-tweaks-1.0.0.jar`和独立`build/libs/buildup-mob-tweaks-1.0.0-qa.jar`。只把两包及上述运行依赖放进**测试实例**，QA不内嵌于正式包。没有增加任何依赖。专服只需安装服务器；客户端保留正式Buildup来同步既有渲染附件。

开发启动可用`.\gradlew.bat runQaServer -PqaHarness=true --args=nogui`或`runQaClient -PqaHarness=true`；启动前把QA包放到相应`run/qa-server/mods/`或`run/qa-client/mods/`，建立独立测试世界并处理其EULA。`-PqaHarness`只创建隔离运行配置，**不会自动把QA打入运行或正式包**。本轮已实测独立包从专服mods目录加载；独立QA包在普通客户端实例的GUI观感待人工复验。

单机先打开作弊或专服给操作员权限。所有工具命令要求原版owner权限（等级4），普通玩家不能触发变更。每台服务器最多一个QA会话，先清理再开新场景。推荐新超平坦世界，普通难度，创造旁观场景；场景目标是1000HP静止铁傀儡，不要求玩家进入生存受击。

## 一键场景与观察窗口

在已加载空旷处运行`/buildupqa spawn <scene>`，场地中心在执行位置前方固定世界Z+8（不是视线方向）。需要29×29×5的空空气空间和上下已加载区块；拒绝方块实体、流体或被占用的空间。工具布置石质地板与四格高屋顶，结束恢复原方块；不要在场地内建造或使其卸载。

也可在测试空中执行（先确认周围区块加载）：

```mcfunction
/execute positioned 0 120 0 run buildupqa spawn pillager
/buildupqa check
/buildupqa inspect @e[tag=buildupmobtweaks_qa_fixture,type=!minecraft:iron_golem,limit=1]
/buildupqa cleanup
```

以上tag使用不含冒号的开发标记，依据26.3 `EntitySelectorOptions.tag`实际`readUnquotedString`实现；亦可用生成信息中的UUID查询。

| scene | 确定的装备/状态/目标 | 看什么 / 最多等多久（20TPS） |
| --- | --- | --- |
| `pillager` | 实际主手弩、原版容器石斧，禁用随机出生补给；近距铁傀儡65tick后移至12格 | 先真实斧头近战，再换回弩并发射箭；约12秒。不是伪装备动画 |
| `skeleton` | 实际弓和生产入口创建的来源标记备用木剑；只关sniping来显示走射 | 走射和箭→140tick移目标近处切剑→230tick移远切回弓；约18秒。生成剑不变成额外战利品 |
| `enderman` | 单项保存birth=combo，使用原版受击建立铁傀儡仇恨；地板允许原版安全传送 | 一个阶段三次真实传送和三次近战命中，约13秒。400tick冷却不返还 |
| `evoker` | 单项birth=fireball，实际原版施法仍可先占用；运动速度0只是定向夹具 | 三枚真实火球绕身并释放，约14秒；不强行跳过原版施法仲裁 |
| `door` | 单项birth=door，oak_door/40耐久；头盔仅防晒，AI仍运行 | 上下门连续，随身和转向，半扇在脚部及+1；约3秒。可用`off`确认清理 |

| scene（E1-a新增） | 输入 | 观察 |
| --- | --- | --- |
| `parched` | 已持实际弓、birth=spin，原生Goal自行拉弓及仲裁 | 20tick预警后旋转跳跃横移；约15秒，保留原版虚弱箭 |
| `wither_bow` | 已持实际原版弓、birth=none，不分配出生弓或备用剑 | 原版主手燃烧箭/真实耐久，约10秒；off仅关新增耐久消耗，原版仍射击 |
| `wither_skull` | 真实石剑、birth=skull；静止射手/1000HP铁傀儡仅是场景输入，AI正常 | 首次400tick后30tick前摇，一枚实际头颅与伤害；约25秒，无持续凋零/方块破坏 |
| `wither_homing` | 同头颅，目标附Trial Omen；同时要求头颅配置开启 | 最初40tick轻微追踪，约25秒；off仅停止追踪，弹道仍保留 |

新模块inspect显示phase/spins/skulls/homing/hits/shots/inFlight及保存冷却；check解释实际弓、排除门控及不祥前置。已有实体force同样不发装备、未知版本拒绝；已有冷却不返还。四项独立门语义不同，不能用off后仍原版射击/弹道判断关闭失败。详见[R4-E1-a短清单](R4E1A_ACCEPTANCE_CHECKLIST.md)。

场景使用固定实体RNG种子480000+scene序号，并明确设置装备/位置/屋顶；不会强制随机特性中奖率为100%。记录`R4E0_QA`的实际world tick、目标、Goal运行标志/优先级/Flags、阶段/冷却/计数到服务端日志。自动撤销期限1200tick（20TPS为60秒）；时间流逝是原版服务器时钟，不手工tick AI。无玩家专服启用正常空服tick，脚本加速100TPS仅缩短墙钟观察时间。

## 选取现有实体、退出与诊断

```mcfunction
/buildupqa inspect <实体UUID>
/buildupqa force door <僵尸UUID>
/buildupqa target <合法目标UUID>
/buildupqa check
/buildupqa off
/buildupqa cleanup
```

`force`支持下表九种场景及其精确原版类型，要求实体存活、闲置、NoAI=false、无目标/乘客/骑乘/使用物品；未知附件版本拒绝。只赋本场景单项出生状态，**现有装备仅校验，绝不替换/复制**。Pillager/Skeleton必须已有真实弩+容器斧或持弓+备用剑；缺少时`check`给装备原因，请用原版命令在测试世界先设置或直接使用spawn场景。创造/旁观、死亡、同盟、跨维度目标拒绝。

`inspect`可检查任何Mob的Goal列表，已支持场景额外显示持手/容器/备用、出生来源/fromDisk、附件Trait、阶段和保存冷却；只显示原版Brain对象身份，不宣称支持所有Brain记忆查询。`check`显示无目标、装备不符、NoAI/dead、gate关闭、LOS和剩余冷却；前置满足仍可能在原版Goal仲裁、距离条件或施法中，查看running/Flags即可。排除类型Tag/其他Mod控制者的全部原因未提供通用推断；用现有只读`buildupmobtweaks`诊断和日志核对。

`off`只加本场景对应单项disable标签；目标和其他能力保持正常，等数tick看独立回退。`cleanup`删除自己生成的生物/目标以及会话期间观察到的该生物所属新弹体/恼鬼，清理门展示并恢复工具布置的石板/屋顶。原版已有子实体和外部新标签不删；修改过的场地/方块实体不覆盖。选中现有实体则先临时让原版跑两个清理tick，再恢复QA改写的birth/door字段与自己加入的退出标签；实际物品/耐久/健康和已产生冷却不回滚、不补发。

保存/正常关服通过SERVER_STOPPING退场。**工具会话不跨崩溃或强制结束保存**；不要用它改实际存档。区块卸载、被移出的实体或未观察到的远处弹体不承诺全量撤销；本轮QA限加载的隔离场地。测试实体的原版战利品遵循正式代码/游戏规则，工具不制造奖励副本；不要把测试场景作为资源农场。已有真实卸载/保存夹具独立于QA视觉演示。

## 没有安全强制接口的机制

工具现实现九种场景，未给其他ID伪造“强制技能”按钮。其他已实现机制使用`src/gametest`现有真实Tick夹具：女巫用RaidBehaviorTests的高目标饮药/预告场景；尸壳用ZombieBehaviorTests三层沙与mobGriefing场景；溺尸用DrownedBehaviorTests实际三叉戟发射回收；烈焰人/蠹虫/旋风人用RemainingBehaviorTests原生Goal/Brain场景。入口、方法清单见[现有功能索引](PHASE1_ACTIVE_FEATURES.md)和其JSON。构建运行这些夹具，明确标为自动L2；玩家不能用未经实现的CLI强制这些技能，人工专项仍按原清单。

## 5～10分钟人工短清单

- [ ] 测试实例同时装正式包与独立QA包，普通玩家无法使用`buildupqa`；生产实例只装正式包时命令不存在。
- [ ] 依次运行五场景，按上表看到真实装备/动作/弹体；每次`check`及`cleanup`，场地恢复且无工具生物/门/火球/恼鬼残留。
- [ ] 门盾连续不重叠在头部，随转向；关闭、死亡、测试存档重登无残门，无关服集合修改异常。重登应先cleanup，真实门持久化视觉另用原验收夹具。
- [ ] 骷髅近战切剑后死亡：默认生成木剑不掉；真实外部木剑仍按原掉率且保留组件。自动强制掉落/重载证据不替代这项肉眼复验。
- [ ] 选空闲已有僵尸force/check/cleanup，实际装备不变，原生状态恢复。记录服务端日志、场景、配置和是否有其他Mod。

自动结果见[R4E0_REPORT.md](R4E0_REPORT.md)。自然概率、完整上游L3、双人和整合包性能为NOT RUN，不用本工具的固定场景推断这些结果。

## E1-b：猪灵三场景（当前共12场景）

| scene | 夹具输入 | 20TPS观察 |
| --- | --- | --- |
| `piglin` | 成人、真实主手弩、免疫转换、原版受伤建立Brain仇恨；140tick把同一弩转到副手 | 约14秒：真实主/副蓄弩、箭扣血、实际手挥动；兼容关后可原版搬回主手，射击不必停止 |
| `piglin_dodge` | 保存birth=dodge，保留默认200tick初始冷却；205tick后在原版实际用弩时把目标移近 | 约16秒：一次安全后跳、真实位移、未来冷却；off只关此项，不能退款已发生位移 |
| `piglin_anger` | 源ZP NoAI/1000HP、受害ZP、同伴；第5tick真实1点同类伤害 | 约3秒：受害者扣血，但无同类目标/怒气；off后再伤害，受害者恢复原版报复；仍开启保护的同伴不必跟随 |

命令与短清单见[R4E1B_ACCEPTANCE_CHECKLIST](R4E1B_ACCEPTANCE_CHECKLIST.md)。前两场目标仍1000HP静止铁傀儡；anger例外为僵尸猪灵。每场使用spawn/check/inspect/off/cleanup，不改变全局出生概率。inspect增加实际Brain ATTACK_TARGET/ANGRY_AT、活动、姿势、shots/swings/dodges/cancels/保存冷却；ZP显示真实持久仇恨。anger保护后的无目标是预期，检查提示不能当失败。

已有猪灵force/target必须是已知版本且空闲，不正在战斗/有ANGRY_AT；force只改出生结果，不发装备/退款冷却。target通过真实1点受伤输入让原版Brain获取目标，会实际扣血，cleanup不恢复血量或真实装备消耗；不用setTarget伪造Brain目标。QA新增三场在真实专服从独立qaJar加载，off/清理及地形恢复通过，已有9场回归；主副手原版客户端状态探针通过，肉眼仍待人工。

## E1-c新增三场景（当前15场景）

| 场景 | 确定性夹具 | 人工观察 |
| --- | --- | --- |
| `skeleton_draw` | 同一真实弓，默认屋顶；140tick移副手、280tick切左主臂、420tick移回主手 | 四种组合真实拉弓反手挥动/箭伤；off仅停止新增挥动，装备/箭仍正常 |
| `bogged_omen` | 3格NoAI铁傀儡目标带TRIAL_OMEN，实际石剑，生成场初始special_ready=+40，原120冷却 | 现有孢子后撤阶段红/棕两枚蘑菇、紫色伤害云；普通目标没有新增蘑菇，off仍保留原孢子 |
| `stray_flip` | 实际石剑以稳定满足现有无弓跃射，生成场初始+40，原120冷却 | 一次跃射三雪球；普通/不祥同链，旧ID不启动第二Goal；off为STRAY_JUMP_SHOT |

新三场 `force` 只接入已有空闲生物的QA会话，不改birth/冷却/装备/目标效果；真实装备、距离、不祥目标和原技能门由操作者设置。生成场的+40初始夹具冷却不修改全局默认，也不能推断自然发生概率。inspect显示swings/flips/snow/dodges/mushrooms/mushroomHits/mushroomClouds/visual；check明确底层spore门/不祥效果/实际弓前提。cleanup只退役QA生成的自有弹体/云，恢复地形，不回滚消耗。新场和原12场的实际调度证据分列，见[R4E1C_REBUILD](R4E1C_REBUILD.md)/[人工清单](R4E1C_ACCEPTANCE_CHECKLIST.md)。

## R4-E2-a 两个原生出生场（共17场）

`/buildupqa spawn wither_birth`、`/buildupqa spawn zombified_birth`：最多512个尚未入世界的成年原生候选，用现有规则/概率真实finalizeSpawn，仅实际命中者进入场景；不改全局配置、不手动发弓/弩，不产生鸡骑士副作用。默认两场163次命中，inspect显示equipmentLedger；零概率/关门/缺规则需报阻断，失败前不改地形。出生场不支持force已有实体。off不删除已获得装备，cleanup退役夹具/弹体和恢复场地；ZP弩AI仍E2-b，不冒充本批射击完成。人工步骤见R4E2A_ACCEPTANCE_CHECKLIST。

## R4-E2-b 五个职业场景（当前共22场）

| scene | 原生输入与观察 | 常见前置 / 20TPS窗口 |
| --- | --- | --- |
| zombified_crossbow | 沿已有出生池有界选出实际弩；原仇恨/Goal、真实箭/耐久、手变更 | 同时要求出生发弩门/概率有效；约8秒首射，7/14/21秒移实际弩副手/改左手/移回主手 |
| fletcher | 先设置实际制箭师职业，再原生finalizeSpawn空手发弩 | 不改所有普通僵尸；同上装填/手变化。占槽阻断不手动补发 |
| fisherman | 实际渔夫职业原生出生竿，原生浮漂线/钩住/拉拽 | 9格目标保留原版物理，停止其主动Goal；约3秒抛、6秒收，真实3耐久；启动距离严格为6<d<13，拉近至5格后不再启动抛竿是正常现象 |
| drowned_fishing | 至多512原生成年候选选实际自然竿，不手动发竿 | 抽样有界、失败报阻断；默认陆地目标，同上牵引及6<d<13启动范围；水下/三叉戟专项另测 |
| cleric | 实际牧师职业原生发一瓶HARMING，目标为900/1000HP亡灵 | 无敌对目标预期；约4秒投药、真实消耗回血，一场一瓶，不补发 |

新增场景固定位置/输入与运行RNG，稀有出生装备采用有界真实出生选样，不保证任意配置必命中。不会修改全局概率、冷却或追加隐藏装备。无匹配门/出生结果时在改场地前拒绝。手移动只往空槽，不能覆盖外部物品；原生出生若已有另一手武器，不保证自动演示全部组合，手组合已由独立客户端探针验证，肉眼仍按清单复验。

`force`可以选择同类型/同职业、已持所需实际物品的闲置实体；不改职业、不发装备、不返还冷却。出生类旧场景仍不支持force。牧师target命令拒绝把支援目标写为敌对目标，实际Goal选择附近受伤亡灵。inspect增加职业账本/阶段/计数/hook，check区分正常消费与缺少前置。off只关本场单项，cleanup退场恢复场地；会话不跨正常关服。完整步骤见[R4E2B_ACCEPTANCE_CHECKLIST](R4E2B_ACCEPTANCE_CHECKLIST.md)。本节与最新报告优先于上文旧批的场景数量/未实现声明。

## R4-E2-c 本批结果

当前共25场；新增场景如下。仅安装独立`buildup-mob-tweaks-1.0.0-qa.jar`于备份/临时世界（配合正式包）；正式发行及sources.jar不含QA/GameTest/客户端探针。`./gradlew.bat qaJar`生成工具；开发专服运行`./gradlew.bat runQaServer -PqaHarness=true --args=nogui`。

| scene | 输入 | 20TPS人工观察 |
| --- | --- | --- |
| illusion | 原生finalizeSpawn的真实弓；只定向clone_trait=false；静止1000HP铁傀儡9格 | 先约2秒原生隐身施法，再真实幻影/安全换位/拉弓/箭伤，观察10秒 |
| illusion_arrows | 同上，只定向clone_trait=true；未修改全局概率或冷却 | 每120tick至多一次双箭替代原箭；冷却期间本体原版单箭正常，未隐身或无安全幻影发箭位置时也回退单箭；四幻影不是实体，分身箭不可拾取，期限200tick |
| vindicator | 原生实际斧卫道士、8格内同盟唤魔者；第5tick受附近僵尸真实1点伤害 | 支援Goal取得攻击者，原生近战扣血；不是强制给敌对目标 |

每场顺序`/buildupqa spawn <scene>` → 观察 → `/buildupqa inspect`/`check` → `off` → `check` → `cleanup`。新增inspect包含swaps/shots/volleys/cloneArrows/supports/positions、账本和targetHealth。场景中的静止与1000HP是QA输入，不能据此推断自然分布、难度平衡或追逐表现；移动已另用实际Tick测试验证。

`force`只选择相应已有闲置实体，幻术师仅暂存/改其clone_trait，结束恢复该选择；不发弓/斧、不改全局概率、已赚取冷却或实际耗材。`illusion`的off只关swap，`illusion_arrows`只关clone_arrows，`vindicator`只关support。单项off可能仍有原生隐身/射箭或其他已开启行为，不能误判为关闭失败。总体让位用原有vanilla_ai标签；要测试shoot_pause单项请按人工清单使用对应disable标签/配置。支援target命令拒绝直接写目标，请使用真实同盟受伤；cleanup退役自有孩子/箭并恢复地形。会话不跨关服，非QA生物不恢复在途幻影/箭。
