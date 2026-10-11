# R4-E4：感知、动作与必要修复

2026-10-11。用户授权连续完成整 E4，三个产品裁决均采用建议值。目标 `26.3/`、分支 `26.3`，起始干净 HEAD `90c3755b90d1076248be9e33d9ead0a4a48c4ba1`。本报告的自动验证只证明列明场景，人工统一验收另见 [清单](R4E4_ACCEPTANCE_CHECKLIST.md)。Boss、R5、Phase 2/3 未启动；不推送、发布或切换分支。

## 范围与兼容决策

17 个旧 ID ADAPT、`strafe_hazard_check` REPLACE，另新增 `animal_sneak_approach`、`skeleton_staggered_draw`。G35 必要部分由既有真实弩链替代，附魔配套部分 EXCLUDE，不造新功能数。声明 **141**，支持 **121**，未生效 **20**；配置 v13，独立 QA 71 场（新增 21）。旧 68 项取舍 PORT1/ADAPT47/REPLACE6/DEFER10/EXCLUDE4 不变；旧报告数字保留历史含义。

Minecraft 26.3、Java 25/JDK25.0.3、Wrapper9.7.1、Loom1.18.3、官方未混淆名、Loader0.19.5、FabricAPI0.162.0+26.3、Fzzy0.7.7+fix3+26.3/Kotlin 原版本不升级。为有类型地比较原版控制器状态，唯一 AW 放开 `MoveControl$Operation` 类，命名空间 **official**，构建与元数据一致；不反射、不关闭 Mixin 校验。

共用入口按方案逐组加入，没有恢复旧 S2 服务/Mixin。感知只服务掠夺者、五种精确骷髅、僵尸/尸壳/溺尸、女巫的初次原生主动发现；保持目标 selector、队伍、可攻击条件、可达性、装备可见度与受伤报复。当前目标维护、FOLLOW_RANGE 属性和物品装备不由 E4 接管。唤魔者、恼鬼仅启用其专项及选定视觉；其他 Mob/Boss/第三方实体不扩大。

所有新门遵守全局、独立配置、`vanilla_ai`、`disable_<id>`、实体排除标签、已知附件版本与 NoAI 边界。瞬时错峰状态不序列化，视觉附件只同步、不持久化，不改现有附件格式/冷却/装备来源。

## 逐项结果与入口

| ID / 候选 | 实现、限制与证据 |
| --- | --- |
| G07 `animal_sneak_approach` | 原生 AvoidEntityGoal 候选 selector 排除潜行玩家，仅 Cat/Ocelot/Rabbit；进行中按原生调度退出。另一个站立玩家不被遮蔽，受伤 Panic 保留。三类普通/潜行/禁用实际逃跑路径 PASS。 |
| G33 `skeleton_staggered_draw` | 普通原生弓与重建狙击共享瞬时等待；同种/同实际目标/8格，先限候选32、4tick轮询、最多等40tick再原生拉弓。不提前释放、不复制箭。2/6射手、五种类型、狙击/禁用 PASS；真实拉弓20tick，六者均有箭。 |
| `sense_blindness` | 原主动发现范围上限 /3；受伤报复实际取得目标并射箭 PASS。不是全局 LOS=false。 |
| `sense_crawling` | 干地原生爬行上限 /3；水中不走此分支。真实低顶棚姿态输入+原生索敌 on/off PASS。 |
| `sense_invisibility` | 上限 /4，原生装备可见度继续独立限制；不把原生缩放再乘一次。带装备隐身 on/off、最近不合格目标不遮蔽其他目标 PASS。 |
| `sense_crouching` | 潜行上限 /2；原生可攻击/姿态保留，on/off PASS。上述目标姿态按隐身→爬行→潜行互斥；动作暴露1.2倍，但不超原范围，最小2。 |
| `sense_glowing` | 初次发现至多16格，可有限帮助隔墙发现；失明优先，原生攻击 LOS 保留。真实墙后取得目标但无箭、拆墙后发箭、禁用/失明对照 PASS。 |
| `sense_nausea` | 失明优先，否则发现上限×.8；投射不确定度按 `sqrt(clamp(amp,0,255)/255)×122.5+10` 加到原值，限选定 Mob owner，非有限/负原值不改。六射手实际初始速度分布与禁用对照 PASS；玩家/无owner/非支持者范围由代码限制，完整客户端玩家射击专项 NOT RUN。 |
| `movement_jump_input` | 仅已管理核心三种骷髅普通走射/狙击阶段，保留起跳横移输入最多12tick，目标/阶段/骑乘/水中退出。真实跨障碍 XZ 与高度 PASS。 |
| `movement_clear_strafe` | 上述范围内原生 WAIT 清理残留横移；目标退出实际输入归零 PASS；不停止特殊翻滚/坐骑动作。 |
| `strafe_obstacle_jump` | 实际投影脚部包围盒先判断安全单格支撑，原 JumpControl 起跳；门/栅栏/栅栏门、无头顶空间、危险支撑回退。真实高度约1.25格，禁用0；不能只检查目的地中心漏掉障碍近缘。 |
| `strafe_hazard_check` | **REPLACE PASS**：沿用 `skeleton_safe_strafing`，补全脚部投影危地与控制器原生向前回退的真实输出复核。火/熔岩坑/细雪/悬崖/栅栏实际移动无损伤。旧 ID 不支持、不宣称其 disable 标签生效，不恢复全局 isWalkable 覆盖。 |
| `melee_effect_interval` | 选定原生近战 Goal，疲劳每级+2、急迫每级-2，等级0～255，间隔6～200；不更改原生伤害冷却。实际稳定命中间隔疲劳30、急迫16、禁用20tick PASS，启动过渡保留原生节奏。 |
| `melee_high_target_jump` | 困难、地面、非水/非骑乘、小宽深/成人高度、近高目标、LOS/头顶空间、原攻击冷却、额外20tick起跳预算。无法寻路的原生 canUse 候选检查也可请求 JumpControl，但返回原值，不伪造路径/启动 Goal。自然玩家目标 on/off 物理高度 PASS。 |
| `evoker_flee_speed` | 独立风格门，原生避让近 .8/远 .4；不是把原版速度称为 Bug。真实导航近 .8，禁用1.0 PASS。 |
| `evoker_avoid_target_fix` | **LEGACY_ONLY 的 Buildup 窄修正**：在原生开始/续行及 tick 边界排除非法威胁、施法与已占用专项状态；观察到施法时逃跑导航归还，创造退出 PASS。原生续行判定不是每tick，故补 tick 边界，不争抢其他 Goal。 |
| `drowned_swim_visual` | 同步视觉位→原生 renderer -10°改 -80°、按游泳量插值→模型头部俯角；成人/幼体、禁用实际客户端角度/动画 PASS。不改碰撞箱、移动或三叉戟伤害。 |
| `burn_freeze_visual` | 已选实体真实燃烧且非火免疫橙色，否则完全冰冻浅蓝；保持原颜色乘法/伤害闪红。同步类型在初始注册阶段建立；禁用清位而不清火/冰。真实客户端模型提交与服务端火伤 PASS。 |
| `burning_projectile_visual` | 已支持 owner 的真实燃烧弹体，每4tick至多1粒追加 FLAME，不改火时长、伤害或方块点火。实际客户端接收追加粒子包、禁用 owner 和关闭后停止 PASS；探针用独立世界中的真实静止燃烧箭，不手动 tick/渲染。 |
| `vex_projectile_weakness` | 用户采用额外+200%=总3倍，进入原生护甲前放大正有限 projectile-tag 伤害，有限溢出截断；不复制上游非正值加法/倍率<=0全伤害拒绝。真实箭碰撞有头盔扣血5.904，禁用1.92；普通非投射物保持原生 PASS。 |
| G35 收口 | 掠夺者/猪灵/职业弩的武器子类、实际主副手、装填/真实箭碰撞及装备守恒由既有路径 REPLACE，全量回归包含这些用例。原生 Look/BodyControl 不新增覆盖；未复现必要新缺陷。AUTO_RELOAD_TIME/新弹药/物品属性附魔分支按 G37 EXCLUDE；全第三方枪械 NOT RUN/不支持。 |

## 自动证据与局限

证据主目录为外层 `docs/rebuild/evidence/r4e4/`，不在目标 Git 根内。失败尝试保留，成功记录不会覆盖失败。

- `full-1.log`：第一轮501项必需真实 Tick/Goal/Brain PASS，当时新增52项。最终 `build-final-6.log` 为502项，新增53项，包括台面上有火时拒绝额外跳跃。输入是世界/实体/效果/伤害/真实网络玩家，未直接调用 Goal 或手动实体 tick。
- `client-5.json`：真实原生客户端与独立探针 PASS；实际模型提交 fire225/freeze225/normal4162，泳姿 on325/off820，完成的头部动画3435，追加粒子包11；关闭后物理火/冰与原生姿态保留，截图 on/off 已查看。
- 早期失败对应已核实根因：原生控制器 unsafe→forward 回退、障碍边缘支撑、错误流动熔岩输入、无法寻路的近战入口、不可达铁傀儡未自然索敌、Helmet 原生护甲、延迟注册同步附件，以及探针在排队提交/同 TAIL 顺序下过早看动画。未删断言或把失败隐藏。
- `26.3-tests` 的604份源码/构建相关文件与规划快照逐项 SHA256 相同；`s2-integrity.json` PASS。旧 S2 不运行、不恢复。

最终验证收据见下节。人工、完整两版上游 L3、多人整合包、长期自然分布/性能、所有极端组合 **NOT RUN**；这些不从测试数推导完成。Allay 旧阻塞不重试，不将其转为支持项。

## 最终验证与收尾

| 验证 | 命令 / 实际结果 |
| --- | --- |
| Wrapper构建、独立QA与全量真实运行 | 在26.3使用 `gradlew.bat build qaJar -PtestEula=true -I ../docs/rebuild/evidence/r4e4/full-5.gradle --console=plain --no-configuration-cache`；**PASS**，502 required、0失败；`build-final-6.log`。初始化脚本只指定独立测试世界，没有过滤/跳过必需测试。 |
| 新视觉真实客户端 | `python docs/rebuild/scripts/r4e4_client.py 5`；**PASS**，原生自动提交/完成动画/粒子包、开启/关闭截图；不是只启动窗口。 |
| 幼体回归 | `python docs/rebuild/scripts/r4e4_baby_client.py 1 fixed`；**PASS**，五种adult/baby/off真实比例1/.5/1，握持误差0，错误scale/grip/UV均0，41支真实箭；各幼体类型都有箭。 |
| 门盾、职业弩与渔线 | `python docs/rebuild/scripts/r4e4_regression_client.py 1`；**PASS**，门盾16样本、原生挥击/模型、职业弩16手位组合、八渔线实际提交/顶点和玩家钩负对照。 |
| 普通专服/配置升级/冷重启 | `python docs/rebuild/scripts/r4e4_servers.py target 1`；**PASS**，v12输入迁移为v13，weaponSwitching=false保留，两新字段true；重启后恼鬼Health994，目标/装备保存按实际NBT记录。控制台原始输入2点arrow-tag伤害→6点实际伤害，无头盔。 |
| 独立QA专服 | `python docs/rebuild/scripts/r4e4_servers.py qa 2`；**PASS**，最终工具21场spawn/check/off/cleanup、互斥/超时/地形与自有实体归还见 `qa-2.log` / 最终收据；批量控制台执行不替代manual场玩家操作。 |
| 上游有限对照 | `python docs/rebuild/scripts/r4e4_servers.py upstream 1`；官方CExkBTN8、原目标26.2及原默认配置。恼鬼同样Health1000→994；两持弓骨架真实运行、目标扣血和弹体/装备NBT **限定PASS**，未建立逐箭错峰时序等价。目标侧快照不总保留已碰撞的箭，不能据箭数量推断射击率。两环境目标最终HP分别999/997（实际原生上限1000），不能直接当作平衡对照。 |
| 完整两版L3、人工、多人/整合包/长期性能 | **NOT RUN**。不声明所有上游行为完全等价；官方Beta运行有非阻断的旧 advancement/entity_sub_predicate 解析ERROR，原始日志保留，本批不修改官方JAR来掩盖错误。 |

最后几轮全量验证发现旧夹具的非确定性输入，失败日志同样保存：流浪者现固定随机源并观察原始120tick冷却/位移/雪球；水池加实体碰撞背墙，原生投掷偏差不会把可回收武器送出场地；马接触姿态夹具只截获该场射手的在途箭，隔离箭伤原生无敌帧，碰撞伤害/姿态原断言不放宽，骑乘正常射箭由独立原生场景验证。这些仅为测试输入，未修改正式翻滚、三叉戟时限或坐骑伤害规则。最终新增组合危地测试还核验了障碍上方的火/液体拒绝路径。

自有错峰QA仅冻结六射手的普通步行速度并隔开站位，原生Goal与拉弓/发箭仍工作；不设NoAI、不改团队或压制正式误伤报复。`off`整体让位和单项配置对照分开。正式包/source包无QA/GameTest/探针，三包SHA256与隔离收据见外层 `artifacts.json`，许可/来源维持MIT与原NOTICE。

完整自检见 [R4E4_CODE_REVIEW.md](R4E4_CODE_REVIEW.md)，必需验证通过后一次本地提交；根目录计划、功能矩阵、行为证据和脚本不在26.3 Git根内，未被该提交覆盖。新发布说明追加于UPDATE_NOTES.md。当前开发范围完成后停止，等待整E4人工反馈；未开始下一阶段。
