# R4-E2 验收反馈修复 1

2026-10-10。限定26.3分支，基线6f78edf；开始时Git工作区干净。本轮只处理用户的鱼竿渲染、5格停抛与幻术师本体单箭反馈，不进入E3/R5/其他Phase。77支持/47未生效、配置v10、独立QA25场景不变。

## 判断与改动

| 反馈 | 判断 | 处理/证据 |
| --- | --- | --- |
| 所有持竿生物抛竿没有钓线 | 真实缺陷，已修复 | 原版FishingHookRenderer.shouldRender在父类距离/裁剪检查后要求getPlayerOwner非空，过滤掉本Mod的生物浮漂；旧测试只手动提取状态，漏验绘制入口。现在只为ProfessionState.HOOK标记、确切渔夫/溺尸主人补用父类可见性检查，不伪造玩家主人或强制无限距离可见 |
| QA距离5格后停止抛竿 | 目标现象，保持 | ProfessionGoals.Fishing启动条件严格6<d<13；同高度5格被排除，牵引后进入近距离自然停抛。不是连续永久抛竿场景，也不关闭原生近战。两类真实Goal在5格时零抛/零磨损，移至7格后实际抛竿、原生碰撞牵引、损耗耐久均PASS |
| illusion_arrows本体单次一箭 | 目标现象，保持 | clone_trait只允许有限特殊齐射，120世界tick为最短特殊齐射间隔，非固定每6秒或每次都双箭。冷却期间继续原版本体单箭；未隐身、施法中或无安全幻影发箭位置也回退原生行为。同一轮真实Goal观察到2次双箭/4支分身箭与4次本体单箭、真实扣血 |

两版上游官方恢复源码仍只读。bFb56Zw2的ZombieVillagerEntityMixin也使用距离平方>36且<169；FishingBobberEntityRendererMixin具有生物主人可见性补丁。此次实现保持Buildup自己的标记和确切类型范围，没有整体恢复旧S2代码。

修改文件：

- `src/client/.../MobFishingRenderMixin.java`：补shouldRender完整描述符的RETURN注入，继续调用原版EntityRenderer父类检查；原持竿坐标提取不变。仅客户端Mixin，common/服务端代码、配置、注册、附件格式、依赖和元数据均未改动。
- `src/gametest/.../ProfessionBehaviorTests.java`：新增两类5→7格实际调度验收，保留全部旧断言。
- `src/gametest/.../IllusionBehaviorTests.java`：新增原版本体单箭与有限双箭交替验收，观察真实实体、箭源、世界冷却和扣血，不直接调用Goal或performRangedAttack。
- `docs/PHASE1_QA_GUIDE.md`、`R4E2_ACCEPTANCE_CHECKLIST.md`、`R4E2B_ACCEPTANCE_CHECKLIST.md`、`PHASE1_FEATURE_DISPOSITION.md`：明确边界、纠正历史渲染验证的覆盖范围。
- `UPDATE_NOTES.md`：追加已验证钓线缺陷的修复说明。

## 实际验证

版本不变：Minecraft26.3、Java25.0.3、Wrapper Gradle9.7.1、Loom1.18.3、官方未混淆命名、Loader0.19.5、Fabric API0.162.0+26.3。Fzzy/Kotlin与既有配置不变。

| 验证 | 结果 | 证据/范围 |
| --- | --- | --- |
| `JAVA_HOME=...jdk-25.0.3; .\gradlew.bat build qaJar --console=plain -PtestEula=true` | PASS | 外层`docs/rebuild/evidence/r4e2-feedback-1/build-2.log`，All 301 required tests passed、BUILD SUCCESSFUL，原生Server Tick/GoalSelector及原有死亡掉落测试一起运行 |
| `python docs/rebuild/scripts/r4e2_feedback_client.py v6`（内部Wrapper runClient+隔离初始化脚本） | PASS | 同目录`client-v6.log`，真实集成服创建、网络跟踪、客户端原生渲染。新Mixin实际加载；临时观察器未替换/直接调用submit或stringVertex |
| 生物浮漂实际提交/钓线顶点 | PASS | 渔夫/溺尸×成年/幼体×真实主副手及左右惯用手，共8个配对组合；提交计数[175,175,175,175,1981,1981,1981,1981]，顶点[5600,5600,5600,5600,63392,63392,63392,63392]，每次原版提交32顶点；不是手动状态提取替代绘制 |
| 标记边界、原版远距/玩家浮漂 | PASS，限定自动检查 | 标记生物钩可见；10万格外不可见；相同生物主人的未标记钩及无主人钩仍不可见；原生玩家主人钩可见。没有实玩普通钓鱼全周期 |
| 原有客户端模型/门盾回归 | PASS，限定 | 16种持弩配对状态、4种真实持弓手组合、同步禁用、门盾16次状态样本通过；不等同全整合包肉眼验收 |
| 5→7格实际行为 | PASS | build-2的两个five_to_seven_blocks：移至7格后各casts=2、pulls=1，原生钩碰撞/拉拽与耐久断言通过；5格阶段均未抛竿 |
| 幻术师单箭回退 | PASS | native_between_volleys：shots=6、volleys=2、arrows=4、singleArrows=4；事件顺序clone/native/native/clone/native/native，齐射间隔177tick≥120，目标HP977 |
| 只读对照/发行隔离/完整差异 | PASS | `after.json`校验26.3-tests状态/HEAD/169个跟踪文件哈希不变；正式主包和源码包没有QA、GameTest、临时观察器；完整差异自检及git diff --check |
| 用户完整E2复验、实际多人/整合包、完整上游L3 | NOT RUN | 本轮未扩展覆盖，也未把用户问题反馈记为整套验收通过 |

临时探针脚本/证据均在外层`docs/rebuild/`，不进入正式JAR/源码JAR和26.3本地提交。隔离测试世界在`26.3/run/r4e2-feedback-client-v6/`，截图为原生Screenshot API保存；已查看截图，浮漂与黑色钓线可见。证据目录保存一份`fishing-lines-v6.png`，没有编辑图像。

失败记录保留：build-1为新增三个用例前的298项运行，两个既有用例失败：骷髅第40tick自然锁敌、溺尸第230tick游泳回收；build-2包括新用例的301项全部通过。正式服务端逻辑在本轮没有改动，但这不能证明首轮失败的唯一根因，未消除的测试稳定性风险保留。未删除/放宽旧断言来获得通过。

临时客户端v1截图API调用编译失败；v2漏写Mixin包名，观察器未加载；v3记录器放入Mixin保留包导致类加载失败；v4/v5使用TAIL漏掉原版非玩家主人分支的提前return，计数缺失。已按真实26.3签名及字节码纠正，v6完整通过，失败日志不覆写。正式修复始终使用RETURN。

## 人工复验清单

1. 更新正式`build/libs/buildup-mob-tweaks-1.0.0.jar`，独立QA包仍为`buildup-mob-tweaks-1.0.0-qa.jar`，重启客户端；服务器也使用配套版本。
2. `/buildupqa spawn fisherman`，约9格初始目标：抛竿期间浮漂及钓线可见，线连持竿手；看目标牵引后接近5格、后续停抛。`cleanup`后用`drowned_fishing`复验。需要再抛时把目标移回7～10格且保持视线，等待冷却。
3. `cleanup`后`/buildupqa spawn illusion_arrows`，观察15～20秒并`check`/`inspect`：有分身箭齐射和真实伤害；期间本体单箭属于正常。120tick只是下限，施法/拉弓调度会使间隔更长。若从始至终只有本体单箭且volleys始终0，记录UUID、门开关、目标有效性/位置和诊断输出，该情形需要另外排查。
4. 普通玩家钓鱼/收竿线与cast、`door`、`skeleton`人工复验；最后`cleanup`。整体E2按原统一清单继续，不把本次三项反馈替代其他检查。

## 自检与交付边界

Code Review: **PASS WITH RISKS**。BLOCKER/MAJOR：无本次未解决缺陷；范围、客户端隔离、准确签名、保存兼容、完整差异、旧断言保留、发行隔离与Update Notes检查通过。风险：首轮两个既有测试失败唯一根因未证实；第三方渲染器兼容、用户肉眼复验及普通玩家钓鱼全周期NOT RUN。

UPDATE_NOTES已追加`🐛 fix(fishing)`。按持续授权仅做本批本地commit，不推送/发布/切换分支；外层证据及脚本不被该commit覆盖。停止在本轮验收修复，等待用户统一E2复验反馈。
