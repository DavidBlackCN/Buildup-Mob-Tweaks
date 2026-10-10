# Phase 1 确定性 QA 使用说明

目标 MC Fabric **26.3** / Java25 / Loader0.19.5 / API0.162.0+26.3 / Fzzy0.7.7+fix3+26.3 / Kotlin1.14.1+kotlin.2.4.20；Buildup1.0.0/config v9。工具使用服务器当前配置，拒绝已关闭能力，**不修改全局配置、概率或出生规则**。只在废弃测试世界安装独立QA Mod。正式包没有`buildupqa`命令、QA类或GameTest入口。

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
