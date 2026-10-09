# R2-C：普通僵尸、尸壳、溺尸

2026-10-09。用户批准 [Migration Plan](R2C_MIGRATION_PLAN.md)，本批实现 Z01～Z04 / D01；停止等待人工验收。R2-A/B 保留。未开发高级主动盾、僵尸首领、混合骷髅/史莱姆乘客、骆驼/矛、僵尸村民/猪灵、附魔、其他生物或 S3；没有 push / 发布 / 切换分支。

## 环境、范围和文件

唯一目标 Git 根 `26.3/`、分支 `26.3`；开始 HEAD `417ebc6c92cfafe89baa609fcb832ac136b895a5`，工作树干净。MC 26.3、JDK 25.0.3、Gradle 9.7.1、Loom 1.18.3、Loader 0.19.5、API 0.162.0+26.3、Fzzy 0.7.7+fix3+26.3、Kotlin 1.14.1+kotlin.2.4.20，官方未混淆命名；未升级依赖。配置保持 v9。

- `rebuild/zombie/`：首次出生、材质门盾、已有盾 Goal、尸壳三阶段下潜、安全恢复与只读诊断。
- `rebuild/drowned/`：实际投射物所有权、回收导航、释放/死亡/转化结算与只读诊断。
- `mixin/rebuild/` 中新增 ZombieLifecycle、ZombieAi、DoorPosition、ZombieShieldWear、DrownedLifecycle、OwnedTrident；common 共 18 项，client 保持 3 项。门展示使用原版 BlockDisplay，并同步自有标记/上下半部以供客户端定位。
- 初始化器、FeatureRegistry、Mixin JSON、空 `zombie_ai_excluded` 标签、元数据、中英文配置说明、NOTICE / README / AGENTS 同步当前范围。
- 新 `ZombieBehaviorTests` 14 项、`DrownedBehaviorTests` 10 项；复用既有 GameTest 和真实 ServerPlayer 网络连接 fixture，新增原版递归乘客保存加载 helper。
- 旧 S2 仍在源码白名单之外；`26.3-tests` 只读。旧实现和报告未被删除，未注册旧战斗服务。

## 实现及主动差异

**Z01**：门盾仅成年普通 Zombie/Husk、无乘客/坐骑、自然出生、局部难度 >2.5，且门盾和 Traits 开关开启时抽取。千分制默认 30，与幼体乘客互斥；总权重大于 1000 时按总权重抽取。上游默认概率和比较符号不直接照搬。门材质按高度选择铜门氧化阶段，按村民生物群系类型选木门；不移植上游 OP 铁门模式，但已有合法铁门状态能按材质结算。两块原版门展示形成上下半部，方向采用原作者门朝向修正。26.3 使用 `BlockState.CODEC`，不能沿用旧 `Name/Properties` NBT。预算 40 个橡木门等效伤害，按材质硬度比例损耗；仅 10° 正面、非绕盾伤害成功格挡才损耗。破盾那一击仍被挡住，之后移除实体和属性；不清空已有双手装备，不产生门物品掉落。死亡、卸载、禁用、转化及保存展示加载均清理自有实体；剩余耐久不补满。

**Z02**：Buildup 独立行为，并非恢复出的上游普通盾移植。成年 Zombie/Husk 使用已有副手 `Items.SHIELD`，对 3 格内可见有效目标举盾 15 tick，冷却 80 tick。用物和格挡走原版链路；26.3 的 `BlocksAttacks.hurtBlockingItem` 只处理 Player，因此在真实阻挡回调为本模块僵尸盾补上原版组件规定的耐久损耗。不会生成盾牌，玩家/其他生物不受注入影响。禁用后停止自有使用并恢复原版攻击。

**Z03**：目标 >12 格、同高度、三层连续沙、路径头部空间、已加载区块、成年/无骑乘/不在水中、mobGriefing 开启，且没有被采掘疲劳阻止时触发。40 次原版 Goal 更新：下沉 19 步、按目标位置快照换位、上浮并恢复地表。典型调度约 80 Server Tick，但索敌开始时间不固定。实测深度 3.04 格。换位前重新检查路线；不照搬上游穿墙/无地表恢复的路径。退出、禁用、创造/旁观、存档加载、卸载及死亡恢复碰撞/重力，并保留世界时间冷却。极端无已加载安全落点时进入恢复态重试，不向未知区块强行传送。

主动差异：Buildup 首次满足条件可用，结束后默认 200 **世界 tick** 冷却；上游初始为 200 次条件检查、只在有目标/落地等条件下递减，默认近距场景往往在冷却结束前已走近。本批不使用旧 burrowChance，不受旧随机 Trait 控制；不复制上游强制乘客无敌或全部头部动画。高度图恢复、三层沙和 mobGriefing 是额外安全限制。

**Z04**：自然/刷怪蛋合格成年出生最多生成一个同族幼体，默认 10/1000，与门盾互斥，受 Traits 总开关控制。依已批准方案不移植上游局部难度 1.5 限制、混合骷髅/变种乘客。子体用 JOCKEY 出生与禁止原版鸡骑士的 group data，不递归生成新乘客。读取、禁用再开启和转化不重抽、不招募已有坐骑。禁用只拆离自有 UUID 乘客，不删除子体；拆离后遵循原版消失规则，不强制永久保存。乘客可能抑制坐骑 AI 步进，清理另在 Zombie.tick 收尾执行。

**D01**：实际主手单支原版三叉戟转入一个原版 ThrownTrident，手变空，投出由原版 DrownedTridentAttackGoal 调度。普通武器组件、耐久和掉率保留。投射物已命中/落地后，在 32 格内以实际地面/游泳导航找回，1.5 格内且有视线时只归还空主手。附件只保存版本、双向 UUID、期限和结算策略，不保存备用 ItemStack。死亡时结算实际已加载投射物；转化、禁用、过期或不再归属时释放实际投射物，不补发。所有者/投射物卸载不会强制加载区块，重载按世界期限结算。独立玩家拾取开关、原掉率、mobDrops、装备消失效果共同决定释放后的拾取策略，只抽一次、不重抽；不照搬上游随机改耐久和无目标直接删武器。开启守恒时验证死目标、创造/旁观、距离及丢视线；关闭则保留原版未来攻击。

## 配置与兼容边界

`general.enabled` 总开关保留。实际使用 `hostile.zombie.shieldUse/doorGuard/sandBurrow/babyRider/doorChance/riderChance/burrowCooldown`，以及 `hostile.drowned.tridentConservation/tridentRecovery/tridentPlayerPickup/recoveryTimeout`。门盾/出生乘客还受 `traits.enabled` 控制，普通盾/下潜/三叉戟独立。旧 activeGuard、guardChance/guardCooldown、burrowChance、doorCooldown、riderCooldown 原样保留但本批不生效，UI 描述已明确。

实体标签 `buildupmobtweaks:vanilla_ai`、`buildupmobtweaks:disable_<feature_id>` 退出对应模块；实体类型标签 `buildupmobtweaks:zombie_ai_excluded` 排除本批接管。未知版本僵尸/溺尸附件原样保留；接入限定原版实体类型，不接管第三方溺尸子类或未批准变种。原版转化清理只处理已知版本的自有附件。没有宣称支持非标准盾、其他三叉戟系统、领地插件、第三方整合包或旧 S2 附件迁移。

管理员 LEVEL_OWNERS 只读诊断：`/buildupmobtweaks zombie <实体>`、`/buildupmobtweaks drowned <实体>`。计数用于辅助：hits 是投射物碰撞次数，并不保证每次造成生命值损失；recoveries 也包含死亡或禁用时的实际物品结算。PASS 依据实际位置、物品/实体、玩家模式、伤害和保存结果。

## 实际验证

完整日志在外层 `docs/rebuild/evidence/r2c/`，不纳入本 Git 根。

| 项目 | 状态 | 实际证据与限制 |
| --- | --- | --- |
| Wrapper `./gradlew.bat build -PtestEula=true --console=plain` | PASS | `build-22.log`，62 项必需测试：24 本批 +16 掠夺者 +21 骷髅 +1 Fabric。没有直接调用生产 Goal、手动 tick AI 或注入攻击目标 |
| Z01 内容/伤害/材质/破盾 | PASS | 真正序列化出的上下半部橡木门；铁门少损耗、正面减预算、侧面伤害、装备保留、破盾无残留 |
| Z01 重载/独立关闭/死亡/转化 | PASS | 真保存展示加载即清理；只重建两块；实际原版入水 600+300 tick 转化为溺尸，原门清理、不拷贝出生技能 |
| Z02 原版盾链 | PASS | 6 伤害被挡、盾损耗 7；独立关闭后真实近战继续 |
| Z03 实际下潜/移动目标 | PASS | 3.04 格下沉、快照换位、上浮/物理恢复；薄沙原版步行近战、改变路线取消、地下关闭/保存、玩家创造/旁观、mobGriefing false/总关闭 |
| Z01/Z04 默认概率设置采样 | PASS（受控出生） | n=1000，高局部难度、NATURAL finalizeSpawn、固定种子、成年 group data；门 30、乘客 13，真实同族幼体。不是 NaturalSpawner 实测，不能代表任意地区全部出生比例 |
| Z04 定向出生/真实乘客加载/禁用 | PASS | riderChance=1000 的短暂定向 fixture 随即恢复默认；实际添加原版乘客，保存后同 UUID，不删除/不招募。只在测试中给子体持久性，隔离原版远距消失 |
| D01 岸上/水下 | PASS | 原版投射物、真实碰撞伤害、寻路/游泳回收；逐 tick 每个武器身份仅一处，名称/耐久保留 |
| D01 保存/死亡/超时/拾取/回退 | PASS | 在途实际实体序列化；死亡只掉一支；真实 Survival 玩家碰撞拾取；独立拾取关闭不允许拾取；守恒关闭时原版 Goal 真投射；水中玩家模式退出/重索敌 |
| D01 未知版本/原版转化 | PASS | version=999 状态与在途附件保持不透明，原版 Goal 仍投射；真实在途之后由 Server Tick 回调调用原版 Mob.convertTo，原投射物仅释放一次、新实体空手且无在途副本。该项是定向生命周期 fixture，不是溺尸自然入水转化 |
| 当前最终生产代码专服启动 | PASS | `target-final-server-v3.json`：已加载场景与门内容查询、保存退出 0，无 Mixin 注入错误 |
| 隔离 26.3 专服重启 | PASS | `target-server-v3.json`：首次空手/实际投射物/在途 UUID，重启同实体及投射物，随后回收；该次初射因原版散布未命中，命中另有 GameTest 和 v2 日志证据。日志初始/重启均退出 0 |
| 实际在途跨区块 | PASS | `target-chunks-server-v3.json`：卸载前一支，移走世界出生点/取消强加载后两实体均查找不到；重载同 UUID 投射物，一支、空手、不补发、引用清空并释放归属 |
| 上游 D01 核心语义对照 | PASS（限定场景） | `upstream-server-v2.json`：官方 26.2 JAR 实际空手、命中、回收原命名耐久武器；原版 26.3 `vanilla-server-v3.json` 保留手中武器并投射 |
| 上游 Z03 轨迹对照 | PASS（定向远距） | `upstream-burrow-server-v3.json`：保留上游默认初始冷却，移动真目标使其保持 >12 格；160 个位置采样，Y 最低 60.96，原位置下沉→约 15 格快照位移→上浮。默认近距 v2 只观察到步行，不能伪称触发 |
| 客户端实际世界/保存退出 | PASS（加载） | `client-world-final.json`：隔离已停止专服的存档副本，实际登入，正常保存退出 0，无强制终止；不等于人工视觉/动画或多人同步验收 |
| 旧工程/产物/许可/JSON/差异审查 | PASS | `verification.json`，旧 169 个受跟踪文件和 HEAD 不变；无旧 combat、无测试类打包、无上游入口/新附魔，MIT/NOTICE 保留 |
| 自然刷怪器完整出生、人工视觉、多人、完整 L3、第三方整合包、旧 S2 数据迁移 | NOT RUN | 保留为人工/专项验收，不以采样、窗口启动或测试数量替代 |

上游 26.2 自带若干 advancement 旧格式解析 ERROR；没有修改官方 JAR，服务器仍正常启动、执行场景和退出。该限制及两版差异使完整 L3 保持 NOT RUN。Allay 仍 BLOCKED，不再尝试；本批未引入其代码。当前本批核心阻塞：无。

### 失败日志与修复

`build-01～19.log` 原样保留，不能作为最终 PASS。先后修正 Level/ServerLevel 游戏规则 API、26.3 铜门集合 API、伤害无敌字段测试访问、原版僵尸盾不损耗、沙层支撑、入水转化等待、拆离后子体自然消失的测试前置、移动目标越出 FollowRange、正常走路后的过晚位置判断、成功清理后观察器仍读实体、不同武器计数混用，以及门 NBT 的 air 内容缺陷。缺失安全落点恢复态在自检时补齐重试。

历史“任何新僵尸 Goal 都不能存在”的 R2-A 范围断言改为检查旧 combat Goal 不存在，掠夺者原行为断言仍保留。原版自然重索敌改用明确的 80 tick 有界窗口；不改变攻击目标断言。60 场景中玩家 45 格超距移动可能进入旧 55 格间距的相邻场景索敌区，arena 尺寸 48→96，将间距隔离到 103，保留全部生命周期断言。`build-19` 的既有骷髅重索敌失败未跳过；`build-20` 隔离后 60 项全部通过，记录了该玩家健康/位置和实际目标；`build-21` 加入未知溺尸附件回退后 61 项通过，最终 `build-22` 加入真实在途的原版转化结算后 62 项全部通过。此处隔离依据距离几何，未将此前未记录的目标选择原因伪报为已证明的生产 Bug。

## 人工验收清单

使用本批 `build/libs/buildup-mob-tweaks-1.0.0.jar`，Minecraft 26.3、上述固定依赖、默认配置。建议独立备份世界、普通/困难难度，打开日志；以下定向命令只布置条件，不代表默认出生。诊断计数重载后可归零，持久数据/物品才是守恒依据。

1. **门盾观感与绕盾**：创造模式在平整遮阳场地执行下述定向门盾召唤，装铁头盔。确认真门上下半部、朝向/随动；改生存正面对准与侧背绕行分别攻击，正面磨损、侧背伤害、破门后不残留，已有剑不消失。合理等待 1–5 秒。错误：不可见/错位/重复门、侧面也磨门、门破后仍无敌。再开启/关闭 doorGuard、保存重进及杀死，检查展示和属性清理。
2. **已有盾**：召唤普通 Zombie/Husk，`/item replace entity <选择器> weapon.offhand with minecraft:shield`，生存或放置无 AI 铁傀儡作目标，接近到 3 格。等待一次 15 tick 格挡与 80 tick 冷却，盾有损耗，随后继续攻击；关闭 shieldUse 后不再持续举盾。没有盾时不能凭空得盾。
3. **尸壳**：制作有支撑的至少三层沙、平整同高度、上方足够空地。尸壳与无 AI 铁傀儡相距 15 格，普通/困难，mobGriefing true，等待约 3–8 秒，观察下沉→换位→上浮。移动目标检查快照；在沙中切创造/旁观或关闭功能/保存退出，重进必须有碰撞/重力且在安全地表。薄沙、障碍、mobGriefing false 时应步行攻击，不改方块。错误：卡地、穿障碍、出沙后飘浮、冷却被重载清零。
4. **出生乘客**：默认 10/1000，需要足够出生样本，不能以几只未中奖判故障。另短暂将 riderChance=1000、doorChance=0，用刷怪蛋产生成年普通僵尸/尸壳；每只最多一个同族幼体，无递归鸡/骷髅链。恢复默认，保存重进保持原乘客；禁用只拆离，不补新的。注意拆离后仍可能原版自然消失。
5. **溺尸岸/水战斗**：遮阳平地召唤溺尸，给主手一支带名字/耐久的原版三叉戟，放无 AI 铁傀儡于 8–9 格；水池重复。等待约 2–10 秒，真投出时手空、命中伤害、寻回后原组件仍在。观察多轮只有一支；回收不可达时默认 200 tick 超时释放，不补发；玩家拾取遵原掉率，默认不保证掉落。重启在途、远离卸载区块再返回以及死亡时核对原实体/物品。关闭守恒应恢复原版未来投射，不凭空归还在途武器。
6. **兼容/多人**：另一玩家观察门位置/上浮、出生乘客、投射物和手中物品，切模式/跨区块/重连；测试独立开关及 vanilla_ai 标签。此次自动客户端进世界不能替代这些观察。未批准变种、其他生物和 S2 特性不能因此启用。

专服实测的定向门盾命令：

```mcfunction
/summon minecraft:zombie 16 64 8 {PersistenceRequired:1b,Tags:["r2c_door"],"fabric:attachments":{"buildupmobtweaks:zombie_rebuild":{version:1,birth:"door",door_health:40.0f,material:"minecraft:oak_door"}}}
/item replace entity @e[tag=r2c_door,limit=1] armor.head with minecraft:iron_helmet
/item replace entity @e[tag=r2c_door,limit=1] weapon.mainhand with minecraft:iron_sword
/buildupmobtweaks zombie @e[tag=r2c_door,limit=1]
```

默认自然门盾另外需要成年、自然出生、局部难度 >2.5、Traits/doorGuard 开启；不能用普通 `/summon` 未中奖来代替自然出生验收。

## 自检与更新说明

Code Review：**PASS WITH RISKS**。本批源码/资源/构建/测试完整差异、目标源码签名、客户端边界、注册白名单、持久物品归属、许可证、旧工程只读及实际日志已审查；BLOCKER/MAJOR：无。保留风险为上表 NOT RUN 项和已列主动差异。完整机器收据在外层 `verification.json`；实际执行脚本与日志不在本 Git 根。

验证和审查通过后，UPDATE_NOTES 追加 `✨ feat(zombie)` 与 `✨ feat(drowned)` 两条 Release 说明。按 AGENTS 持续授权做一次本地 commit，包含本批目标目录文件；外层报告/矩阵/证据不由该提交覆盖。等待本批人工确认，不进入下一批。
