# R4-E2-c：幻术师与卫道士支援

本轮完整实现 `illusioner_swap`、`illusioner_shoot_pause`、`illusioner_clone_arrows`、`vindicator_support`。仅目标26.3/分支26.3，起始HEAD `44260ac1a79ac8d9b72e4957679d707e90280ccf`干净。用户已批准方案后直接实施；[方案](R4E2C_MIGRATION_PLAN.md)与[人工清单](R4E2C_ACCEPTANCE_CHECKLIST.md)配套。保留现有R4/装备池，未恢复S2冲突代码，未启动E3/E4/Boss/R5/其他Phase，不推送发布。

当前124个声明ID：**77支持/47未生效**；配置v10、独立QA25场景。支持计数不是完整功能验收。68项取舍分类仍1 PORT/49 ADAPT/5 REPLACE/9 DEFER/4 EXCLUDE；没有新增上游候选编号。

## 环境与来源

MC26.3 / JDK25.0.3 / Wrapper9.7.1 / Loom1.18.3 / 官方未混淆名称（不使用Yarn）/ Loader0.19.5 / Fabric API0.162.0+26.3 / Fzzy0.7.7+fix3+26.3 / Kotlin1.14.1+kotlin.2.4.20；可选ModMenu21.0.0。依赖、Wrapper、正式fabric.mod.json、许可证/NOTICE保持原样。build.gradle仅增加新GameTest类白名单，测试元数据单独增加测试入口。

三个幻术师ID为UPSTREAM ADAPT，核心参考为两版已核验官方发行源码：

- [26.2 Beta IllusionerEntityMixin](../../reverse-engineering/CExkBTN8/sources/com/notunanancyowen/mait/mixin/IllusionerEntityMixin.java)。
- [1.21.11 IllusionerEntityMixin](../../reverse-engineering/bFb56Zw2/sources/com/notunanancyowen/mait/mixin/IllusionerEntityMixin.java)。
- `vindicator_support`仍是 **LEGACY_ONLY / Buildup**，未发现对应官方技能，不改写来源。

原始JAR、Vineflower1.12.0及E0源码索引校验值复核见[after.json](../../docs/rebuild/evidence/r4e2c/after.json)。旧169个Git跟踪文件/HEAD/分支/工作区状态按[before.json](../../docs/rebuild/evidence/r4e2c/before.json)复核；26.3-tests只读。26.3原版common/client JAR校验值、14类javap/新反编译证据见[reference.json](../../docs/rebuild/evidence/r4e2c/reference.json)，另核验IllagerModel真实持弓姿势。没有修改反编译输出/缓存。

## 行为与兼容决策

| ID | 最终行为 | 明确安全边界 |
| --- | --- | --- |
| illusioner_swap | 原生隐身施法保留；首次隐身、受击结束或1200tick更新四个有限安全位置，原身体换位 | 200tick世界冷却；已加载区块/边界/实心地面/无流体/火/碰撞；无安全点回退原射；乘骑/NoAI/无效目标不换位 |
| illusioner_shoot_pause | 保留原生priority6弓Goal/MOVE/LOOK、原生施法互斥；真实拉弓停止走位输入，发箭后20tick、换位后10tick暂停 | 不直接调用或替换原Goal；20tick暂停是原生计时外的恢复阶段，实测默认射间隔约59、关闭后40，不称精确20tick射频 |
| illusioner_clone_arrows | 原生新出生一次70/1000选择；符合条件时每120tick至多双箭从真实幻影位置发射，**替代本次原箭** | 不是双箭加原箭；同一owner/实际bow和ammo组件、原生Power/Flame回调、原射箭声；安全/LOS/最大6格、不可拾取、200tick期限、同盟穿过；120/200重叠至多4枚自有在途箭 |
| vindicator_support | 没有既有有效敌人时，原targetSelector新增priority5/TARGET，查询8格内最多8名同盟唤魔者/女巫100tick内真实攻击者 | 每20tick扫描；攻击者16格/LOS/阵营/创造旁观过滤；不抢原priority1～4目标，不改全局伤害；失去同盟/门/NoAI只清自己拥有的目标 |

幻影为同步坐标，**没有分身实体/装备/额外掉落**。世界位置随普通移动平移，真实换位则保留原刷新位置；实际箭源与客户端原生getIllusionOffsets一致。同步附件不持久化；客户端仅该模块同步幻影存在时修正实际持弓臂、原版固定右臂的左弓姿势和最大6格裁剪范围，其他灾厄村民不接管。

附件v1只持久化出生选择和世界冷却；已存旧实体标legacy_skipped，不重抽、不补装备。未知附件版本保留/让位。目标/幻影/运行计数不保存；卸载/死亡/禁用/NoAI退役自有箭，加载已标记在途箭不继续恢复。实际主副手弓、外部命名/损伤物品、外部箭保留。采用现有FeatureRegistry独立门、general/traits（稀有分身箭）、vanilla_ai、disable_ID及空`illusion_ai_excluded`实体类型标签；不冒充通用第三方武器/领地兼容。

不复制上游不安全落点、ZERO引用比较、OP分支坐标错误、无限齐射/可拾取复制箭，也不增加动物→幻术师或袭击幻术师出生。原版正常袭击不含幻术师，袭击测试验证卫道士及已有生物组合；幻术师与其他施法者另在独立真实调度场测试。

## 验证结果与证据范围

| 检查 | 结果 | 实际证据/限度 |
| --- | --- | --- |
| Wrapper build + qaJar | PASS | 目标目录执行`.\gradlew.bat build qaJar -PtestEula=true --console=plain`，[最终构建](../../docs/rebuild/evidence/r4e2c/build-14.log)；298required，新增26项 |
| 真正Server Tick/原Goal调度 | PASS | 原生首次施法/隐身/四持弓手/箭源与HP/阶段互斥、走位、受伤冷却、安全失败、退出/实际保存/守恒；[after.json](../../docs/rebuild/evidence/r4e2c/after.json)逐scenario原始proof |
| 分身箭实际伤害 | PASS | clone-only首次齐射目标HP996；同盟Evoker完整生命；Power V+Flame实体组件/着火及HP≤990，不用普通原箭伤害掩盖分身箭未命中 |
| 真实行走/拉弓输入 | PASS | 行走约1.44格、同步offset误差0；拉弓样本>10，横向速度<.025；箭伤仍正常，不把静止QA当移动验收 |
| 原生普通难度五波袭击生命周期 | 限定PASS | 原Raids管理器创建/出生5波（5/6/7/9/14原生成员）、真实支援/近战、原生VICTORY；受控真实击杀推进波次，见RAID_PASS；**不是连续玩家战斗** |
| 真实客户端包/原生模型 | 限定PASS | [client-v3.log](../../docs/rebuild/evidence/r4e2c/client-v3.log)：4主副手/惯用手、实际拉弓臂/幻影坐标、owner箭、退出；门盾16、骷髅弓4、蘑菇2旧探针回归。自动提取与模型观察，肉眼NOT RUN；末次自检仅补服务端射箭声，客户端文件未改 |
| 独立QA专服 | PASS | [qa-v2.log](../../docs/rebuild/evidence/r4e2c/qa-v2.log)：3新场+door/skeleton，真实计数与targetHP、off、cleanup、地形与自有实体清退 |
| 正式包独立专服/冷重启 | 限定PASS | [target-v2.log](../../docs/rebuild/evidence/r4e2c/target-v2.log)/[restart](../../docs/rebuild/evidence/r4e2c/target-restart-v2.log)：已有17损伤命名弓、原生施法换位/箭伤；冻结/NoAI后静态冷保存，UUID/装备/出生状态/已赚取冷却相等 |
| clone_trait=true真实保存/退出 | PASS | 原生serialize GameTest保留true及777/888未来预算，不恢复瞬时幻影/箭；专服冷例是legacy/clone_trait=false，不能冒充true在途冷恢复 |
| 官方上游运行 | LIMITED | [upstream-v1.log](../../docs/rebuild/evidence/r4e2c/upstream-v1.log)，独立Beta26.2：真实隐身、受伤刷新/多个箭源、实际目标HP984、17损伤命名弓；跨版本且非逐项量化对照，不是完整L3 |
| 原门盾/备用木剑反馈回归 | PASS | 完整旧服务端断言保留；客户端门盾16姿势、旧弓4手组合；死亡生成备用剑抑制仍通过；本批没有重改其正式实现 |
| 包隔离/旧工程/源证据 | PASS | verify脚本检验正式/sources无QA/GameTest/probe，独立qa.jar含QA；旧169文件与原始源码/JAR校验值相等 |
| 人工连续袭击、全量两版L3、自然默认概率分布、多人/整合包、性能 | NOT RUN | 不把编译、测试数、强制QA出生选择、受控五波替代这些验收 |
| Allay | 历史BLOCKED | 未在本批重试，不是新增阻塞 |

工具命令：外层`python docs/rebuild/scripts/r4e2c_client.py v3`；`r4e2c_servers.py qa v2`、`target v2`（内部实际同世界冷重启）、`upstream v1`；`r4e2c_verify.py build-14.log`。各脚本参数/原生命令/退出码与临时世界路径在对应JSON/日志中。所有新测试不调用Goal.tick、实体tick或Raid.tick推进。五波夹具临时强制加载13×13区块和193×193地板，仅测试环境使用，正式实现不强制加载区块。

## 失败与复核记录

早期失败日志全部保留，不计PASS：build-1旧猪灵载入RNG/末影人随机放块污染保存测试；build-2/3误用床集合接口，查实际javap后改Blocks.BED.red()/ChunkPos.x()/z()；build-4独立分身箭瞄眼加弹道导致过射、袭击donor在tick0受伤/床碰撞，修为原生目标1/3高度、实际第5tick受伤及可走平台；build-8旧唤魔者保存回归一次未通过（保留全部原断言，追加诊断，后续完整轮次未复现）；build-9实际附魔HP990被误要求<990，修为≤990；build-10旧配置测试的近战前置未满足、build-11旧手兼容测试两箭随机未命中，分别定向合法目标和将手兼容夹具距离10→6；build-13三种备用剑死亡测试在70tick时的持剑前置未满足，向三实体提供合法目标，保留真实Goal装备切换/近战/死亡/掉落全部断言。两种持剑前置失败时的目标状态未记录，随机取敌是夹具风险解释，不能冒充已证实的唯一根因。改动只限GameTest输入/诊断，旧断言保持相等，原生攻击/散布/碰撞仍运行；独立自然取得目标/远距测试保留。

build-5（295）、6/7（297）、12（298）通过；最终构建以build-14（298）为准。自检发现分身箭替代原箭同时跳过原射箭声，已补每次齐射一次原生声音并重新完整构建。早期一次旧唤魔者保存失败没有被擅自称为已查明根因，残留随机夹具风险见[自检](R4E2C_CODE_REVIEW.md)；不能据最终通过否认历史失败。

## 产物、文档与停止点

- 正式包：`build/libs/buildup-mob-tweaks-1.0.0.jar`；源码包：同目录`-sources.jar`。
- 独立测试工具：同目录`buildup-mob-tweaks-1.0.0-qa.jar`（仅测试世界使用）。大小/SHA256见after.json，禁止把QA包当发行包。
- 主要实现：`rebuild/illusion`三类、4个窄common Mixin、3个client Mixin与1接口、FeatureRegistry/注册入口/空排除标签/中英说明；新IllusionBehaviorTests及4个测试环境、QaHarness三场。
- [自检](R4E2C_CODE_REVIEW.md)、[人工清单](R4E2C_ACCEPTANCE_CHECKLIST.md)、功能取舍/入口/剩余台账/缺口/QA/实施计划已更新；UPDATE_NOTES追加两条已验证变化。
- 外层PLAN/Phase1计划/矩阵/基线/报告、脚本和证据位于26.3 Git根之外，不在本地提交内；本地提交收据见外层evidence/r4e2c/commit.json。

下一批只建议E3-a-1的`illager_zombie_conflict`、`illager_zombie_villager`、`golem_friendly_fire_fix`，先上游/原版审计再裁决，不代表已实施或批准。完成本批本地提交后停止，等待本清单人工反馈。
