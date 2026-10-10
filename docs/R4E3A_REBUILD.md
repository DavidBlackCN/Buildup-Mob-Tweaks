# R4-E3-a 实施报告

2026-10-10；目标仅 `26.3/`、分支 `26.3`，起始干净 HEAD `f8602e36a2d913749415ade8d70a2b44e2fc280d`。用户授权整个 a 组连续执行。没有恢复 S2 服务，没有进入 E3-b/c、E4、Boss、R5 或后续 Phase，没有推送/发布。

**状态：九项实现已提供；整个 E3-a 尚不能宣称完成。** `rider_friendly_fire` 补审发现为 LEGACY_ONLY，保留为 Buildup 安全功能还是 Phase2 DEFER 的明确裁决仍待回复，当前 BLOCKED、未实现。亡灵马日烧已获用户明确裁决 Phase2 DEFER。尸壳矛由 26.3 原生能力 REPLACE，旧配置仍不生效，不虚构额外实现。

环境未升级：MC26.3 / 官方名称 / Java25.0.3 / Gradle9.7.1 / Loom1.18.3 / Loader0.19.5 / Fabric API0.162.0+26.3 / Fzzy0.7.7+fix3+26.3（非传递）/ Kotlin1.14.1+kotlin.2.4.20 / 可选Mod Menu21。配置仍 v10，124个声明 ID，**86支持 / 38未生效**（77+9）。独立 QA 新增9个场景，原25个保留，共34个；支持数与测试数均不等于人工验收通过。

## 行为与关键决策

| 单元 | 当前结果及偏差 | 状态 |
| --- | --- | --- |
| 两种灾厄关系 | 精确 Pillager/Vindicator/Evoker/Illusioner ↔ Zombie 或 ZombieVillager；两个门独立，不采用上游 else-if 耦合及 Zombie 继承扩大范围。原 targetSelector 6/TARGET，玩家/村民/Johnny/支援优先；只退出自有目标，治疗中不敌对 | L2 PASS，人工 NOT RUN |
| 傀儡误击 | 原生雪球误击铁傀儡触发报复已实际复现。只过滤精确 IronGolem 原 HurtBy 的铁/雪傀儡攻击者，不取消伤害、不全局重写 setTarget、不扩大到第三方 AbstractGolem | L2 PASS，人工 NOT RUN |
| 鸡骑士扑击 | 仅已存在的幼年 Zombie 鸡骑士，出生单次10/1000，100tick世界冷却，安全方向一次 .45水平/.35竖直冲量；仍由原版近战攻击，不生成鸡、不加速属性累积 | L2 PASS；默认自然分布 NOT RUN |
| 敌对脱船 | 仅原生 Monster 的 AbstractBoat，真实受伤→20tick待处理→伤害/最大HP概率；100tick冷却，最多12个已加载安全落点；不泛化玩家座椅/第三方坐骑 | L2 PASS，人工 NOT RUN |
| 尸壳矛 | 目标原版 SpearUseGoal 使用 root vehicle 的速度和真实 KINETIC_WEAPON；实测已有 CamelHusk 骑乘使用、位移和矛伤。不安装第二套矛 Goal，不启用旧 husk_spear_charge ID | REPLACE PASS；其开关不能禁用原版矛 |
| 尸壳环绕 | 已存在 CamelHusk 的主骑手精确 Husk，在未实际用物品时安全 .16切向冲量；不抢原版矛使用，不恢复上游治疗/属性叠加/目标共享 | L2 PASS，人工 NOT RUN |
| 骷髅马冲刺 | 既有骷髅马主骑手，出生单次10/1000；20tick抬身预告、至多20tick冲刺、200tick世界冷却，单次接触伤害上限4；不额外造马、不添加长期速度修饰 | L2 PASS，人工 NOT RUN；并非保证每次冲刺命中 |
| 骷髅陷阱变体 | 保留真正 SkeletonTrapGoal 与4马/4骑手；第一骑手Skeleton，另三名Wither/Stray/Bogged，移交原生附魔装备；关闭则原4Skeleton。事件局部，不全局组队或生成第二份装备 | L2 PASS，人工 NOT RUN |
| 致死转凋灵 | 仅精确Skeleton在凋零效果或凋灵系致死时，native convertTo 转实际装备，保留名称/NoAI/乘客与已赚取骑乘选择冷却；实际外部备用品移交，生成木剑退役。转换不结算死亡奖励；之后真实死亡备用品只结算一次，不循环转换 | L2 PASS，人工 NOT RUN |
| 骑乘友伤过滤 | 两版恢复 PersistentProjectileEntityMixin 未找到此过滤；原版 leftOwner=true 后允许再命中自己的当前骑乘树。已准备仅当前同树箭/三叉戟安全修正的方案，但不把来源误写上游 | BLOCKED：待用户保留/暂缓裁决，NOT RUN |
| 亡灵马日烧 | 两版所谓 burnInDay 只是自然骑乘马移除。用户明确选择 Phase2 DEFER；保留未生效字段，不添加日烧或自然马移除 | DEFER，不计 Phase1实现 |

新 `relations_ai_excluded`、`riding_ai_excluded` 类型标签继承已有相关模块排除标签；总门、独立门、`vanilla_ai`、`disable_<id>` 仍可回退。关系检索每20tick、半径≤16、最多16候选；安全检查最多4×4落脚格。骑乘瞬时动作不保存，已赚取世界冷却/出生选择保存，未知附件版本不接管。转换后的外部备用品死亡结算独立于技能关闭，避免物品所有权消失。

`SupportGoal` 只增加对**自有关系目标**的高优先级支援接管，保留其他已有有效目标；原支援断言未弱化。正式执行链：ENTITY_LOAD/UNLOAD → 原版 GoalSelector/TargetSelector；Mob.tick TAIL 只做退出边界；AFTER_DAMAGE 输入脱船窗口；SkeletonTrapGoal 添加实体处替换骑手；LivingEntity.die HEAD 精确转换。无新增客户端 Mixin、依赖或 Access Widener。

## 验证与证据

所有本批 GameTest 通过真实服务器 Tick/原版 Goal 调度推进；不直接调用 Goal 或实体 tick，不以 setTarget 模拟自然索敌。定向 QA 只固定已有骑乘特性选择和出生夹具，不改服务器全局概率。碰撞专项把目标移到正在运动的冲刺路径上，之后由真实物理/伤害调度结算；这是定向输入，不能代表默认自然命中率。

证据目录在 Git 根之外：`../../docs/rebuild/evidence/r4e3a/`。`before.json` 保存起始状态/旧工程169个跟踪文件哈希；`reference.json`/`native.txt` 保存目标原版13类实际完整描述符/字节码。上游两版来源仍沿 R0、MIT/NOTICE，不编辑恢复源码/缓存。

最终命令、计数与包哈希见同目录 `verification.json`。构建命令为 `gradlew.bat build qaJar -PtestEula=true --console=plain --no-configuration-cache`。旧门盾、备用生成木剑、职业钓线、猪灵 Brain、幻术双箭、支援与受控五波原袭击回归包含在完整套件中；受控袭击不能替代人工连续战斗。

早期失败全部保留：build-1 为新夹具泛型；build-2 暴露未知状态安装及关系阻挡支援，并修复根因；build-3 为 GoalSelector API；build-4 暴露 Brigadier 合并后的非操作员权限回归，已修根命令权限。build-5 一项既有猪灵首次箭未命中失败，根因**未证实**；build-6 为新 QA 转化观察半径和失败清理泄漏，已修夹具和 finally 清理；build-7、8全335必需项通过。build-9新增真实碰撞专项通过，但猪灵同项再次失败，185tick已发1箭而未伤目标。本次唯一既有测试修改因此将185tick发射期限保留为独立断言，225tick另断言实际命中/伤害，新增首发与首命中记录，最大时长240；没有修改猪灵生产AI、关闭随机散布或伪造命中。build-10全336必需项通过，猪灵记录首发165/首命中169tick。两次历史未命中到底为飞行期限还是散布，**仍未证明**，不得宣称根因修复；交付保留此风险。其余既有断言未弱化。

最终 `build-10.log`：**BUILD SUCCESSFUL，336 required PASS**（旧301+新增35），`qaJar` 成功。新骷髅马定向物理接触为charges=1、hits=1；真实原矛HP980.32，误击基准revenge=true/修正revenge=false且lastHurtSnow=true；转化及后续死亡证明外部备用品一次结算。总测试数只用于复核，不代替范围表或人工。

| 验证范围 | 口径 |
| --- | --- |
| Wrapper / GameTest / qaJar | 最终结果见 verification.json；测试失败日志不删除 |
| 独立正式专服 / 冷重启 | 实际关系互击、实际凋灵系致死、弓damage17/名称/NoAI保存和无原Skeleton复制；限定结果，不替代所有骑乘冷重启组合 |
| 独立 QA 专服 | console 可执行8场景；陷阱需真玩家，另由 GameTest实际生存玩家触发9场景全流程 |
| 原客户端回归 | 专门隔离客户端探针检查真实门盾/持弓模型及8种钓线提交顶点；限定自动渲染回归，不冒充肉眼骑乘动画验收 |
| 上游 Beta26.2 | 专服能启动，实际关系互击/死亡转化有限对照。启动有上游 advancement 解析ERROR，不能称完整无错L3；两版源码交叉核验不代替运行对照 |
| 1.21.11逐项上游运行、全功能L3 | NOT RUN |
| 人工、默认自然概率、刷怪蛋/刷怪笼全部输入组合、所有冷重启/卸载组合、多人整合包、长期性能 | NOT RUN |

最终专服 `qa-1` 的8个console场景、关闭及terrain/entities retired标记通过；`target-2`/`target-restart-2`正式专服与冷重启通过。测试flat世界启动有 `No key layers in MapLike[{}]` 生成设置日志，夹具在已加载高空工作；不把这些日志包装成无错世界生成审计。上游自身 advancement ERROR 也保留，均限制对应L3结论。

客户端 `client-1` 先在浮漂3可见性断言失败；没有修改正式渲染代码。外部探针改为仅在真实同步mobHook标记已到达时检查可见性，最终仍要求8组全部实际提取/提交/顶点/命中，避免用未同步窗口判定绘制。`client-2` PASS：8组submit为168×4、1922×4，分别写入5376×4、61504×4个顶点；16组弩模型均253个变化样本，门盾16样本。首轮失败的具体网络窗口未被诊断数据独立证实，保留记录；本次只能确认标记有效时的真实渲染，不能宣称所有网络时序问题已修复。

## 自检与交付边界

按外层 CODE_REVIEW 审查本次完整 diff，具体收据见 verification.json：代码自检 PASS WITH RISKS，保留上面的运行覆盖边界与既有首次射击偶发失败记录。整个批次因骑乘过滤产品裁决 **BLOCKED**，不能将本地提交当作人工通过或 E3-a 全部完成。实施、矩阵、QA使用及人工清单同步，UPDATE_NOTES仅追加已验证行为。没有推送发布；共享计划与原始证据不在 `26.3` 本地提交内，须另行保管。

下一步只处理骑乘过滤裁决和本清单人工验收；E3-b/c 未启动。
