# R4-E1-c 骷髅差异重建报告

用户授权方案核验后直接实施；具体范围及环境见[实施方案](R4E1C_MIGRATION_PLAN.md)。本批两项接入，两个旧 ID 按已有链 REPLACE，一个旧 ID 纠正来源后 DEFER。当前 **66 支持 / 58 未生效 / 124 总 ID**，配置 v9，不把支持数量解释为完整 Phase 1 验收。只执行 E1-c，不进入 E2/R5/Phase 2/3。

## 逐项结果

| ID | 取舍及行为 | 证据状态 |
| --- | --- | --- |
| `skeleton_offhand_visual` | ADAPT：实际新拉弓只挥真实用弓手的另一只手，支持左/右主臂与主/副手；不发箭、不换装备；单独开启也支持原版 Bow Goal | L2 / 实际客户端模型 PASS，人眼 NOT RUN |
| `bogged_omen_mushrooms` | ADAPT：已有沼骸 DODGE 内对 BAD_OMEN/TRIAL_OMEN 目标投红/棕两枚原版雪球；真实碰撞伤害及 WARPED_SPORE HARMING 云，独立门控、预算和退出 | L2 PASS，Beta 有限专服对照 PASS，完整 L3 NOT RUN |
| `skeleton_aim_fix` | REPLACE：已有 Walk/Sniper 原版看向及射箭；实际目标横移/升高后命中。旧重复 ID 保持未生效 | 限定 L2 PASS，不声明第三方所有武器瞄准 |
| `stray_snow_barrage` | REPLACE：已有 STRAY_JUMP_SHOT 一次跃射三雪球；普通/不祥目标同一链。旧稀有 Goal/ID 不恢复 | L2 PASS |
| `stray_omen_snow` | **LEGACY_ONLY / DEFER**：两版上游没有流浪者不祥雪球分支；旧 S2 的 snow_omen 渐加速为自创。上游流浪者缓慢分支属 OP，不能当不祥恢复 | 来源纠正 PASS；新玩法 NOT RUN，留 Phase 2 |

原始发行 JAR / 恢复单元哈希继续复核，两版源码只读。Beta `SkeletonSpecificGoal` 唯一 `if(this.ominous)` 在 Bogged 行480，正式版在行502；`MobAITweaks.isOminous` 为 BAD_OMEN/TRIAL_OMEN。E0 的错误 ADAPT 来源已纠正，E0 JSON/日志作为历史原始审计保留。Allay 仍历史 BLOCKED，本批不重试。

## 实现与兼容决策

正式入口仍 `BuildupMobTweaks` → `SkeletonBehavior` 原 Goal 链；新 `SkeletonDifferences` 只提供 draw/DODGE 差异及生命周期。`LivingEntity.startUsingItem(InteractionHand)` HEAD/RETURN 识别一次新使用；实际 `getUsedItemHand()` 避免上游旧 useHand 快照问题。原版 26.3 HumanoidModel 已按 `useItemHand.asArm(mainArm)` 绘制，原生 SwingDescription 驱动模型，不改 mainArm、不重写 SkeletonModel。客户端由提前注册的服务器同步布尔门控制，过滤配置、实体及实际武器排除状态，不再依赖客户端本地配置猜服务器门。

蘑菇是两枚原版 Snowball 视觉，没有背包/备用物品；新 CompoundTag 附件 version=1 保存 owner/target/expires，live 为运行态。每个沼骸最多2枚在途弹体、2朵新增云；发射时重查不祥效果而非 Goal.start 旧快照；100 tick 弹体期限，原120 tick阶段冷却不变。窄重力钩子恢复上游蘑菇 .05，普通雪球仍 .03；真实一 Tick 下落速度分别 -.0495 / -.0297（含原版阻力）。撞实体时 difficulty+1，撞实体/方块共用一次 burst；云持续20、等待10、半径 .4、增长 .1、使用增长 .1。保留原版药水对亡灵敌人的语义，模块云在 `AreaEffectCloud.serverTick(ServerLevel)` 原版药水应用前过滤盟友、创造/旁观和失效状态，既防伤害也防对亡灵盟友治疗。不恢复 OP/crazy 分支。

目标更换/死亡、独立关闭、总门、NoAI、owner死亡/卸载、过期及真实序列化重载均退役；卸载/加载回调只排队，END_SERVER_TICK 清理，避免在实体区段迭代中删除。重载不恢复已知版本的在途雪球/云；未知生物 DATA 仍原样保留且不启用。孤儿清理只处理 Snowball/AreaEffectCloud 的已知version=1，未知版本附件在任何载体均原样保留、模块不接管，雪球/云回到原版行为；不会误删生物或猜测未来格式。精确支持类型仍普通骷髅、流浪者、沼骸；不扩大 ExtendedSkeleton 或恢复旧 S2 服务。

保持所有装备组件、真实耐久及生成备用木剑死亡策略。依赖、Wrapper、Access Widener、配置版本、语言键未变，许可证仍 MIT 与既有 NOTICE/第三方许可。

## 实际验证

收尾测试与自检以[验证收据](../../docs/rebuild/evidence/r4e1c/verification.json)为准；最终 `build-15.log` 的 **207项必需真实调度测试全部 PASS（新增29 + 原178）**，BUILD SUCCESSFUL；所有旧测试Java未修改。

| 检查 | 结果与限制 |
| --- | --- |
| Wrapper `build qaJar -PtestEula=true --console=plain` | 真实 GameTestServer / GoalSelector，不直接调用 Goal、entity.tick 或模块 release/impact |
| 实际四种手臂/用弓手 | 原版拉弓、另一只手挥动、真实箭命中和真实弓名称/耐久；独立关仍射箭；仅 visual 开仍用原版 Bow Goal PASS |
| 沼骸碰撞/云/仲裁 | 一次旧DODGE释放两种蘑菇；实体及地面真实碰撞、一次爆云、云到期；普通目标/单项关仍保留孢子，底层孢子关无蘑菇 PASS |
| 新云真实伤害与保护 | AFTER_DAMAGE 确认直接来源为新增云；村民敌人1000→997、村民盟友1000、亡灵盟友900均符合预期，防止只测原孢子毒伤害 PASS |
| 生命周期与保存 | 目标退出/替换、效果先过期、NoAI、死亡、弹体100期限、实际雪球/云/owner序列化、已赚冷却及未知版本 PASS；真实活跃技能区块卸载瞬间 NOT RUN |
| 幼体守卫 | 原版 Bogged 无幼体状态；测试专用子类暴露 baby 标志并经真实调度验证，未新增幼体功能；原版幼体特性 NOT RUN |
| 独立 QA | 新 `skeleton_draw`、`bogged_omen`、`stray_flip`，15场景；实际专服 `qa-v5` 三场景 inspect/check/off/cleanup、地形恢复、夹具退役 PASS；原12场 GameTest继续回归 |
| 实际客户端 | 最终 `client-e1c-v5` 四组 draws=[62,60,59,60]、swings=[18,18,18,18]、模型改变=[15,15,15,15]，同步996/禁用门176样本，两色蘑菇；门盾上下半部16真实坐标样本 PASS，肉眼 NOT RUN |
| 原门盾/木剑 | 原178项源码不改，门盾/备用剑服务端回归 PASS，门盾上述客户端坐标 PASS；用户发行实例手动复验 NOT RUN |
| 上游与冷重启 | `upstream-v2` pinned Beta 26.2 原 Goal：普通无蘑菇，不祥两色及20/10 HARMING云；`target-v5` 同输入实际弹体/云及静止NoAI标记 UUID/999999冷却/副手弓17耐久冷重启一致 PASS。Beta默认120与目标ready=0输入差异公开，不作等时量化；正式版仅源码对照 |
| 包边界/旧工程 | 正式及源码包无QA、GameTest、探针和MC类，QA单独Mod；旧工程169个跟踪文件 SHA-256复核；全部证据见收据 |

最终代码由 build-15 的207项测试、QA v5、客户端 v5和专服/冷重启v5覆盖。新增未知版本雪球/云实际序列化重载及25 tick存活检查，确认载体和payload原样保留、模块不接管，按原版行为运行。隔离 flat 世界缺少 layers 的环境日志和 Beta 旧 advancement 解析错误保留，测试战场显式铺地；不说日志零错误，不作为完整上游兼容 PASS。

重现脚本：[服务器](../../docs/rebuild/scripts/r4e1c_servers.py)、[客户端](../../docs/rebuild/scripts/r4e1c_client.py)、[外部探针](../../docs/rebuild/scripts/R4E1cClientProbe.java)、[审计](../../docs/rebuild/scripts/r4e1c_verify.py)。证据/脚本位于外层，不在26.3 Git提交内。

## 失败记录与剩余边界

失败日志均保留。build-1 为测试旧API（setMainArm/getPotionContents）；build-2 高位目标碰屋顶及原索敌时刻失败；build-3 QA 的 Mob→AbstractSkeleton 类型问题；build-4 的200项PASS后增强移动后必须再次命中断言，去除屋顶弹道遮挡并保留高度；build-5 的 setBaby 在原版沼骸无效，改为明确测试专用子类，另有原 Pillager/Blaze 失败；build-6 原版铁傀儡药水夹具不适合云伤害验证及原 Pillager/Zombie失败，换成村民并以真实云 AFTER_DAMAGE 区分孢子毒；build-7/8均204PASS；build-9 注入错误落在 tick，而实际药水入口是 serverTick，按字节码修正后 build-10 为204PASS、build-11为205PASS；build-12新增载体守卫通过但旧恼鬼固定时刻退出失败，原断言不改；build-13为206PASS；build-14补齐真实重力对照后仍206PASS；build-15新增未知技能载体保护后207PASS。没有删改旧断言，最后PASS不能证明原固定索敌时刻确定性。

**NOT RUN**：人眼、干净发行实例、完整 L3、自然刷怪器、实际双人、整合包、长期性能、任意第三方武器/实体与标签数据包组合、活跃技能卸载瞬间、RAID_OMEN额外玩法。**BLOCKED**：历史 Allay，不重试。新生物幼体属于后续候选，不在本批。

自检见[CODE_REVIEW](R4E1C_CODE_REVIEW.md)，人工见[验收清单](R4E1C_ACCEPTANCE_CHECKLIST.md)。批末一次本地 commit 后停止，不推送/发布。下一批仅建议 E2-a 的实际装备来源/空槽/组件/死亡闭环、凋灵骷髅出生发弓、僵尸猪灵出生发弩；traits按现有出生附件REPLACE。须逐批核验具体方案，不在本轮实施。
