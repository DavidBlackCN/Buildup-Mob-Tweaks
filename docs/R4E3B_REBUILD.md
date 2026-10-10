# R4-E3-b 完整工作报告

本轮按用户授权连续完成 b-1/2/3/4 九项开发，停在整 b 组人工验收。唯一工程为 26.3 分支，起点 d3af067；未恢复旧 S2 AI/Mixin，未进入 E3-c/E4/Boss/R5/Phase2/3，不推送发布。实施方案见 [R4E3B_MIGRATION_PLAN](R4E3B_MIGRATION_PLAN.md)，人工验收见 [统一清单](R4E3B_ACCEPTANCE_CHECKLIST.md)。

当前 **133 声明、96 支持、37 未生效；配置 v11；独立 QA 44 场景**。这是新增九个 G 候选，不改变旧 68 项的取舍总数，不把人工或完整 L3 视为通过。

| 单元 | 功能/来源 | 实际结果 |
|---|---|---|
| b-1 | G02 squid_ink_defense / ADAPT | **限定 PASS**：原生受伤喷墨，近身直接攻击者优先100刻效果；8格内其他候选递减失明/发光，候选及受益者合计最多16，冷却100刻；关门仍保留原版受伤和墨汁，重载不刷新冷却 |
| b-1 | G03 glow_squid_gaze / ADAPT | **限定 PASS**：16格/LOS/角度/60刻检查；实际玩家夜视，背向、阻挡、远距、创造和旁观排除；实际客户端收到夜视效果 |
| b-1 | G04 chicken_threat_escape / ADAPT | **限定 PASS**：原生 Goal 调度真实逃跑，玩家8格/怪物6格，5/6优先级、.8/1.2速度；持食物玩家、创造模式及禁用不触发；采用有候选预算的重写 Goal，取舍由原 PORT 精化为 ADAPT |
| b-2 | G06 animal_group_panic / ADAPT | **限定 PASS**：原生 PanicGoal.start 真实受伤事件传播；鸡/牛/羊/猪/兔，同种、16格、最多8个原始候选，200刻冷却；接收者独立Goal真实移动，无递归传播 |
| b-2 | G08 tameable_recovery / ADAPT | **限定 PASS**：猫/狼/鹦鹉所属 TamableAnimal 及已驯服有主马系；活跃同维度主人、伤后200刻、每100刻1HP；战斗/着火/乘坐或被骑乘暂停；真实有主狼、马、重载与骑乘测试。原版马本来可能自然回血，禁用不屏蔽原版回血 |
| b-2 | G11 cat_projectile_dodge / ADAPT | **限定 PASS**：真实移动投射物预测、实际横跳；两侧各最多8步加载/地面/碰撞检查，40刻冷却；坐下/关门仍实际中箭；闪避中再次真实命中仍扣血，不销毁弹/生成弹药 |
| b-3 | G14 goat_horn_aggro / ADAPT | **限定 PASS**：真正开始原生乐器使用，24格/LOS/16个原始候选，100刻请求冷却；不抢已有非玩家目标或 Brain 非玩家目标；60刻/使用结束/禁用/其他AI改换目标退出，只清自己的目标 |
| b-3 | G23 trader_self_defense / ADAPT | **限定 PASS**：真实受伤后原生Goal 20刻前摇，8格LOS有效攻击者失明100刻、冷却200刻；禁用或中途退出不重放旧伤害。直接有限反馈，不制造药水弹或覆盖真实装备，原版隐身/交易保留 |
| b-4 | G01 sniffer_search / ADAPT | **限定 PASS**：原生潜行持物交互、矿石与生物群系；只通过 getChunkNow 查询已加载区块；近处优先采样、全服64查询/刻、最多8请求、100刻超时；真实取消、重载、队列拒绝/全服预算/超时/未加载区块检查。不是上游6400格全世界定位的等价功能 |

范围内无开发 BLOCKED；人工验收、完整两版量化 L3、默认自然分布、真实多人/整合包、长期性能及地图保护联动均 **NOT RUN**。九项具体参数及取消语义见方案；“限定 PASS”只代表下述实际自动证据。

## 源码、兼容性与修改范围

环境保持 MC26.3、Java25.0.3、Wrapper9.7.1、Loom1.18.3、官方未混淆命名、Loader0.19.5、Fabric API0.162.0+26.3、Fzzy0.7.7+fix3+26.3、Kotlin1.14.1+kotlin.2.4.20。无新依赖、映射或 Access Widener。只增加生态 common 模块及五个窄 Mixin；没有新客户端生产代码。

- `rebuild/ecology/{EcologyBehavior,EcologyGoals,EcologyState,SnifferSearch}`：事件、Goal、持久冷却、临时动作及主线程有限请求；保留原版 Brain，不手工 tick 生物或调用 Goal 作验收。
- `mixin/rebuild/Ecology*`：Squid.spawnInk/aiStep/hurtServer 中唯一 spawnInk 调用点、PanicGoal.start、Mob.tick、InstrumentItem.use RETURN、Sniffer.mobInteract HEAD。完整描述符和原生字节码在外层证据；正式专服和实际客户端均启动过。
- `FeatureId/FeatureRegistry/BuildupConfig`：九独立默认开启开关，配置 v10→v11；旧值保留，未知 ecology_rebuild 版本原样保留且不运行。附件 v1只存冷却和伤后等待；传播、临时目标及查询不跨载入恢复。
- 31个数据标签：生态AI/恢复排除、21个嗅探物品组及8矿石目标组；中英文界面/消息同键集。标签可扩展项见配置说明，未声称通用第三方AI适配。
- `src/qa/.../E3bQa`：九独立场景、服务器共享单会话/权限4/1200刻到期、输入提示/实际HP和移动/药效/冷却/原生Goal查询/关闭与恢复；旧两个工具互斥仅做必要扩展。
- `EcologyBehaviorTests`：新增47项；旧骑乘测试仅增加失败诊断位置/阶段，未改验收条件或生产骑乘代码。测试入口/环境与构建白名单同步。
- `NOTICE.md`保留两版上游 MIT 署名，不编辑恢复源码、不复制美术/奖励/OP/附魔。当前旧工程169个跟踪文件及16个本次读取的恢复文件 SHA256 均不变；另校验既有官方JAR来源收据。

## 实际验证与证据

证据目录为工作区外层 [docs/rebuild/evidence/r4e3b](../../docs/rebuild/evidence/r4e3b)，脚本在 [docs/rebuild/scripts](../../docs/rebuild/scripts)。未进入26.3 Git提交；不是伪造的源文件测试记录。

| 命令/检查 | 结果与边界 |
|---|---|
| `JAVA_HOME=...jdk-25.0.3; .\gradlew.bat build qaJar -PtestEula=true --console=plain --no-configuration-cache` | **PASS**：最终 build-11，全部397项必需真实调度测试通过，含47新生态及350旧回归；build-10 的397项也通过，后续仅修正双语分类标题重复键并重建；更早 build-6/8 的393/396项均通过，但尚未包含最后新增测试 |
| `python docs/rebuild/scripts/r4e3b_servers.py target 2` | **限定 PASS**：普通专服/冷重启收据见 target-2、target-restart-2；v10→v11保留旧 weaponSwitching=false、九新开关默认true；实际喷墨、商人失明反馈，墨汁/商人同一实体冷却截止值保存不变。群体接收者仅记录暖启动位置及冷启动附件，未逐值对照其暖/冷冷却；真实逃跑另由GameTest验收。该运行早于最后的猫热地面补修，未验证该补修；其真实伤害/避让验证在最终 GameTest 中通过 |
| `python docs/rebuild/scripts/r4e3b_servers.py qa 1` | **限定 PASS**：九场景真正加载；墨汁、鸡逃跑、群体惊慌、猫避弹、商人事件实际触发；水槽/地形恢复及自身实体退场。控制台无玩家，夜视/吹角/嗅探交互和有主恢复不在此证据中通过，分别由GameTest/客户端覆盖 |
| `python docs/rebuild/scripts/r4e3b_client.py 2` | **限定 PASS**：夜视110个客户端样本，猫实体169样本，墨汁导致的原生发光标志60样本。猫客户端闪避动作、墨汁粒子/嗅探指向肉眼均NOT RUN |
| `python docs/rebuild/scripts/r4e3b_regression_client.py 1` | **限定 PASS**：门盾16次原生渲染状态检查、四种骷髅持弓组合真实模型改变、16职业持弩和8鱼竿线实际submit/vertices；不能代替人工观感 |
| `python docs/rebuild/scripts/r4e3b_servers.py upstream 1` | **有限对照 PASS**：官方Beta26.2新隔离世界，墨汁药效与受伤惊慌真实观察。商人只观察到短窗口内原生隐身用物，不能据此宣布其失明已对照通过；完整九项/两版等价L3 NOT RUN。保留上游启动错误日志 |
| 文件、JSON、双语键、正式/源码/QA JAR隔离、Git差异、自检 | 见最终 verify.json 与 [CODE_REVIEW](R4E3B_CODE_REVIEW.md)；正式包/源码包无 QA、GameTest 或诊断探针 |

## 失败记录与诚实边界

全部失败日志保留：compile-1 中不存在的 hasImpulse 字段已移除；build-1 的 QA 泛型推导、原生三参数交互和狼类包错误已修正。build-2 注视被夹具玻璃挡住、默认测试群系非目标；build-3 原生 setBiome 因边界区块不可用失败及夹具漏加禁用标签；改为明确的已加载群系夹具并保留禁用断言。吹角测试原版先自行锁定同一玩家时不会重复计数，改为在原生使用前解除旁观模式，验收仍要求真正本功能取目标。

build-4 全服区块总数随真实玩家视距加载增长，不能当搜索加载证据；改用原生无视距票据 mock 玩家，只检查实际搜索覆盖范围中先前不可用区块。build-5已证实168个候选区块未变为加载状态，正式搜索仍仅调用 getChunkNow。

旧掠夺者近战、骷髅玩家目标、马冲刺碰撞及骷髅配置场景在前序整套运行中各出现时序失败；未修改这些生产模块、期限或断言，最终397项全通过。具体根因未被完全证明，不能以再跑通过宣称治好了旧潜在问题；列入人工回归与剩余风险。只给旧马碰撞测试补位置/距离/冷却诊断。

client-1 已收到夜视，但错把普通生物药效列表当成客户端发光协议，断言失败；查官方 Entity.isCurrentlyGlowing 并以原生同步标志复测后client-2通过。没把首轮失败写成PASS。原版/第三方的已有药效、伤害、仇恨不在关闭时抹除；关闭只停止本功能后续动作。

build-9 的新商人物品槽断言误把原版合法饮用品当成问题；修正测试为允许原版 POTION/MILK_BUCKET，且检查本功能未生成喷溅药水实体，保留真实伤害和失明验收。build-10 最终397项全部通过。猫热地面补修由真实箭碰撞验证，并未因闪避添加免伤。

收尾严格 JSON 审计发现两个语言文件各有一个旧 passive 分类标题重复键，已只删除旧标题，build-11 重新通过全397项。实施表中尚有旧草案范围，已对齐最终实现：有主马系、矿石水平8/地下8及2601采样、群系289采样；不是再次扩大实现范围。收据见 audit-fixes.json 和 verify.json。

## 下一步与停止点

先执行整 b 组人工清单。按既定总方案下一批建议为 **E3-c-1**：G16雪傀儡南瓜归还与南瓜防御；先核对原生剪切/归还的消费和掉落守恒、条件护甲、独立开关与保存语义，冻结数值后补原生交互与实际伤害测试。后续 c-2 做投球及视觉；G33幼体骷髅在 c-3 核对出生/成长、装备保存、客户端尺寸及原生调度，G34旋风人OP来源的Vanilla+预算仍需明确裁决。本轮不实施这些功能。

验证与自检通过后按持续授权做一次本地 Gitmoji + Conventional Commit，包含 UPDATE_NOTES。父目录共享计划、脚本、日志和收据在Git根外，不被该提交覆盖。停止等待整 b 组人工验收。
