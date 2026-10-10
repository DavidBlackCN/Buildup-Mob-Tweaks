# R4-E1-b 猪灵基础重建报告

范围：用户在只读方案后明确批准的四项猪灵基础功能；起点为 `26.3 / cef3aa0b4dfd1f0da2764ca783b8512afd61376e`，工作树干净。没有进入 E1-c、E2、Boss、R5 或 Phase 2/3。当前 124 个 ID 中支持 64、未生效 60；支持不等于所有专项人工验收完成。配置 v9、旧 R4 实现、历史报告和原版权保留。

实施依据：[批准方案](R4E1B_MIGRATION_PLAN.md)、[逐项取舍](PHASE1_FEATURE_DISPOSITION.md)、[人工清单](R4E1B_ACCEPTANCE_CHECKLIST.md)、[自检](R4E1B_CODE_REVIEW.md)。原始日志、脚本与验证收据位于外层 [r4e1b](../../docs/rebuild/evidence/r4e1b/)；它们以及共享计划不在 `26.3/.git` 内，不随本地 commit 提交。`26.3-tests` 的 169 个跟踪文件按开工快照逐一复核，不修改旧工程。

## 结果与实现取舍

| ID | 真实执行路径与决定 | 本批结论 |
| --- | --- | --- |
| `piglin_crossbow_compatibility` | 使用原版 Piglin Brain / CrossbowAttack；补 CrossbowItem 继承识别、实际主副手、射程和蓄力姿势；保留原版矛的 KINETIC_WEAPON 判断 | 限定 L2 PASS；不承诺任意枪械或自定义实体 |
| `piglin_shot_swing` | 原版实际发射前后检查真实弩的装填组件，成功消耗装填后按实际手 `swingForAttack`；空射不计，关掉此项仍可射击 | L2 及实际客户端状态 PASS；肉眼 NOT RUN |
| `piglin_item_dodge` | 更正旧语义为近距蓄弩后跳；成人首次出生 70/1000；200 tick 初始和再次冷却，10 tick 动作窗口，一次冲量 | 真实 Brain 蓄弩、物理位移和安全回退 L2 PASS；自然刷怪器 NOT RUN |
| `zombified_piglin_anger_fix` | 先复现纯原版同类报复，再阻止同类目标建立/继续、清理已加载同类仇恨；保留受伤、玩家群体仇恨及普通原版 Goal | 实际 HurtByTargetGoal/TargetGoal 调度 L2 PASS；运行时重新启用 PASS |

没有增加攻击 Goal、替换 Brain 或直接驱动攻击方法。CrossbowAttack 的装填、延迟和箭发射仍由原版仲裁。主手优先；副手测试用同一个实际 ItemStack 换手，无备用复制、发弩或正式新物品。弩耐久由原版射击扣除，名称、耐久、装填组件、正常掉落与库存转移保留。

26.3 原版 `PiglinAi.stopHoldingOffHandItem` 会在受伤及普通清理时把非赏金副手物移到主手。最初的失败证明“从副手输入”不能代表真实副手射击。现仅在兼容门开启且副手为可识别弩时保留该实际弩；原版 `holdInOffhand` 的黄金替换另走真实转移路径，依然完成交易，手/库存/地面合计一把弩。禁用兼容后原版可以搬回主手，报告不把固定槽位作为原版保证。

后跳方向取目标反方向，水平为移动速度属性×2、上限 .65，竖直 .2；先查支撑、碰撞、头部空间、流体和常见危险方块，幼体/骑乘/水中不启动。没有覆盖原版疾跑状态，也不宣称与上游任意蓄弩者 10 tick 连续后跳严格等价。200 tick 冷却持久化；取消不返还冷却、不恢复在途动作、不回滚外部速度。

独立 `piglin_rebuild` 附件 version=1 保存出生结果与冷却；运行态只保存动作引用和诊断计数。加载/转换不重抽或补装备，未知 version=99 保留不启用。目标退出、换手、禁用、NoAI、死亡/卸载取消模块追踪的用弩动作；不通过空 `getUseItem` 写组件，不清空实际已装填弩。同步布尔附件供原版姿势读取，跳舞/赏金优先。

各 ID 经 FeatureRegistry 和 general 总门，精确实体类型、`buildupmobtweaks:vanilla_ai`、`buildupmobtweaks:disable_<id>`、`piglin_ai_excluded`；弩还受现有 `ranged_items_excluded` 过滤。未知实体/物品回到原版路径，不恢复旧 S2 全局服务。

## 原版及上游证据

核对本机 26.3 common/client JAR 的完整方法描述符，9 个 javap 文件见外层证据目录。环境保持 MC26.3 / Java25.0.3 / Wrapper9.7.1 / Loom1.18.3 / 官方未混淆名称（无 Yarn）/ Loader0.19.5 / Fabric API0.162.0+26.3 / Fzzy0.7.7+fix3+26.3 / Kotlin1.14.1+kotlin.2.4.20。依赖、Wrapper、Access Widener 未改。

两版已核验官方 `CExkBTN8` / `bFb56Zw2` 的 PiglinEntityMixin、PiglinBrainMixin、RangedCrossbowAttackTaskMixin、ZombifiedPiglinEntityMixin 是源码起点，完整发行 JAR 和恢复文件哈希复核见 [verification.json](../../docs/rebuild/evidence/r4e1b/verification.json)。MIT 与版权说明保留。

E0 将僵尸猪灵仇恨修复列为“上游只有弩行为、LEGACY_ONLY”不完整：上游同一弩模块也替换了 HurtByTargetGoal，忽略 ZombifiedPiglin。现修正为 UPSTREAM + LEGACY_ONLY 意图证据；Buildup 用独立门窄修，并保留原 Goal，不把该历史误分类悄悄删除。

纯原版专服 `vanilla-v2`：实际同类 `/damage` 后，受害者与同伴 `angry_at` 都指向源实体 UUID `[-751308583,-1179694922,-2057920830,1921542686]`，源实体 HP1000→950.8002。新实现 `target-v1` 与 Beta `upstream-v2` 同类场景均保留受害者 HP19.06、同伴 HP20、源 HP1000，没有同类仇恨引用。关闭修复的 GameTest 仍观察到真实群体目标与源 HP下降，玩家报复/传播、创造/旁观退出另有原生调度测试。

有限专服用弩对照：新实现主/副手目标 HP1000→974/976，Beta 为974/960；两者都真实发箭扣血。Beta 副手输入最终搬进主手，新实现副手实际保留并发射。此结果只证明受控场景路径，不比较命中率、时间一致性或完整自然战斗，也不能将不同运行时随机数下的伤害差值当成强弱结论。成熟 1.21.11 本批只读源码；完整两版 L3 **NOT RUN**。

## 实际验证

最终命令：在 `26.3/` 使用 JDK25.0.3 执行 `./gradlew.bat build qaJar -PtestEula=true --console=plain`。最终日志 `build-14.log` 及逐场景证明由 verification.json 指向；24 个新测试加原 154 个回归，共 178 项必需测试。测试只安排受伤、实际物品、移动目标、保存等夹具输入并观察，不直接调用 Goal、Brain.tick、实体 tick 或 performRangedAttack。

| 验证组 | 证据与范围 | 状态 |
| --- | --- | --- |
| 真正主副手/继承弩/箭命中 | 原版 Brain 蓄力与装填、真实箭伤害、耐久/名称、换手后 offSwing 和 chargePose | PASS |
| 独立关闭/总门/traits/NoAI | 关闭挥手仍发箭；兼容关后继承弩拒绝、原版弩仍攻击；总门/traits实际回退；蓄力中退出无新增空射 | PASS |
| 安全后跳 | 原版正在蓄弩时移近目标；一次冲量、真实位移、200 冷却；危险地面/独立关闭不消耗冷却 | PASS |
| 目标与仇恨 | 真实同类受伤、群体传播基准、窄修、玩家、创造/旁观、旧缓存目标重新启用；丢弃目标取消充能 | PASS |
| 物品/出生/生命周期 | 1000 次 native finalizeSpawn+ENTITY_LOAD 包含原版幼体，仅成人以70/1000抽签，最终46个dodge；幼体none、装备不变，不等于自然刷怪器或全体7%计数；拾弩一次、交易输出、副手黄金替换、实际死亡、未知附件 | PASS |
| 装填保存与转换 | 真实原版装填组件保存一枚，重载后 native Brain 再开火；真实约300 tick 主世界转换，弩组件一份、5金粒在转换后手/地面守恒，无 E2 AI | PASS |
| 专服卸载/冷重启 | 真实区块卸载期间查询无实体，重新加载及冷重启 UUID/birth/999999冷却/副手17耐久相同 | PASS（静止 NoAI 标记，非活跃蓄力卸载瞬间证明） |
| 独立 QA 包 | 三场景真实调度、off/check/cleanup；地板屋顶恢复、所有夹具退休；原9场景继续回归 | PASS |
| 客户端 | 隔离原版客户端探针实际收到同步状态，主/副挥手28/28样本，副手 charge RenderState120样本，同步510样本，正常保存退出0 | PASS（人眼 NOT RUN） |
| 原 R4/门盾/备用剑 | 原154项全部回归，原测试 Java 文件未改；门盾同步/清理、生成备用剑来源掉落断言仍通过 | PASS（本批未重做人眼门盾/剑验收） |
| 正式与 QA 包边界 | 正式主/源码包无 QA、GameTest、探针、Minecraft 类；QA独立 Mod，仅含QA源集；语言键保留，124 ID核对 | PASS |

专服/客户端重现脚本：[r4e1b_servers.py](../../docs/rebuild/scripts/r4e1b_servers.py)、[r4e1b_client.py](../../docs/rebuild/scripts/r4e1b_client.py)、[客户端探针](../../docs/rebuild/scripts/R4E1bClientProbe.java)、[审核脚本](../../docs/rebuild/scripts/r4e1b_verify.py)。需要目标 JDK、已解析依赖及隔离开发实例。研究上游的已知日志警告不等于本项目运行失败，也不支持完整兼容结论。

失败记录完整保留：compile-1/build-1 暴露旧 swing/ChargedProjectiles API，按本版本字节码修正；build-2～5 的真实副手问题修复，其他失败为 tick0 受伤时间戳、击退后超8格、错误固定槽位/装填观察等夹具问题。build-4 另有原骷髅 melee-config 失败，未删改原测试。build-6 为176、build-7 为177全通过；build-8 转换测试错误只数地面金粒，build-9确认金粒在转换者主手、另有蓄力末端近距输入失败。后者改为落地、蓄力5～12tick中段输入，不降低实际一次位移要求或延长测试时限。build-10全178通过；自检补上出生抽签的成人限制及幼体断言后，build-11原骷髅“105tick生存重新索敌”断言失败，未改旧测试；build-12本批猪灵“15tick必已蓄力”断言失败。原版可见性传感器不能保证这个固定时刻，改为观察真实蓄力后切创造、重新受伤并观察真实蓄力后切旁观，仍120tick总时限，两次充能退出必须全部完成；不是只测试目标或跳过蓄力。原索敌夹具有间歇时机失败，最终PASS不证明其精确时间确定性。`upstream-v1` 端口占用，未启动，记录 FAIL；修正隔离端口后 v2通过。不得以历史失败构建作交付包。

## 交付边界与下一批

收尾 `build-13` 与 `build-14` 均全178通过；最后一处QA调整与后跳测试保持一致，在落地、蓄弩5～12tick中段移近目标。最终独立包专服 `qa-v4` 三场景、关闭/清理正常退出0。客户端实测之后只变更服务端安全/生命周期/成人出生守卫及QA/测试输入，主副手及渲染钩子未改，不把客户端计数说成最终用户实例肉眼通过。

没有未解决代码阻断项。**NOT RUN**：本批肉眼视觉、用户整合环境、完整L3、自然刷怪器、实际多人、长期性能、任意第三方枪械/实体、排除标签数据包组合穷举、正式两JAR在用户干净发行实例的手动验收。Allay 仍历史 **BLOCKED**，本批不重试。

本批完成自动证据、自检及一次本地 commit 后停止。下一批仅建议 R4-E1-c：`skeleton_offhand_visual`、`stray_omen_snow`、`bogged_omen_mushrooms`，另核验 `skeleton_aim_fix` / `stray_snow_barrage` 的 REPLACE；先写具体方案和26.3原版差异证据，等待用户批准，禁止第二套跃射/孢子攻击链。不会自动开始。
