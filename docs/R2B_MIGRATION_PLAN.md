# R2-B Migration Plan：骷髅系列（已批准）

状态更新：用户随后明确回复“同意开始下一阶段”，已授权完整执行下述普通骷髅、流浪者、沼骸方案。2026-10-09 实现与验证见 [R2B_REBUILD.md](R2B_REBUILD.md)。以下保留批准前方案原文作为历史依据；不再表示等待实施授权。R2-B 人工验收仍待执行。

2026-10-08：用户确认 R2-A 人工验收通过，要求修复掠夺者背包食物死亡掉落，修复后可进入下一阶段。修复已完成真实死亡回归。本文件进入下一阶段的方案准备，不代表骷髅实现或验收完成。

暂按外层 PLAN 的 R4-A 顺序命名下一批为 R2-B：普通骷髅、流浪者、沼骸。若用户选择先普通骷髅，则本方案的变种步骤移至后批；不默认扩展焦骸、凋灵骷髅、骑乘技能、Boss 或其他生物。

## 环境与来源

| 项目 | Source | Target / 依据 |
| --- | --- | --- |
| Minecraft / Java | 官方 CExkBTN8：26.2 / 25；bFb56Zw2：1.21.11 交叉行为参考 | 26.3 / JDK 25.0.3；现有 gradle.properties 与构建 |
| Gradle / Loom | 发行 JAR 不提供可靠构建版本，不推测 | 9.7.1 / 1.18.3，保持现状 |
| Mapping | 26.2 官方名称；1.21.11 intermediary 恢复证据 | 官方未混淆命名，无 mappings 依赖 |
| Loader / API | 上游对照实例 0.19.3 / 0.161.0+26.2 | 0.19.5 / 0.162.0+26.3 |
| 第三方 | 只取已审计行为，不复制上游注册链 | Fzzy 0.7.7+fix3+26.3、Kotlin 1.14.1+kotlin.2.4.20 不升级、不内嵌 |

只读来源：外层 R1 报告、FEATURE_REBUILD_MATRIX、BEHAVIOR_BASELINES；恢复源码的 AbstractSkeletonEntityMixin、RangedBowAttackGoalMixin、RangedAttackGoalMixin、SkeletonSpecificGoal、StrayEntityMixin、BoggedEntityMixin。旧 SkeletonCombat / SkeletonExtras 只对照安全设计，不重新注册。

已用实际 26.3 JAR 的 javap 核对 AbstractSkeleton.registerGoals()V、reassessWeaponGoal()V、performRangedAttack(LivingEntity,float)V、canUseNonMeleeWeapon(ItemStack)Z 和两类远程 Goal 的构造器/start/stop/tick。各具体注入点、数据组件与变种动作仍须在实施时读取目标源码核验，未核实部分不得猜测。

## 迁移单元

| 顺序 | 单元 | 分类 | 关键变化 / 目标文件 | 验证 |
| --- | --- | --- | --- | --- |
| 1 | 模块入口和开关 | Adapt | BuildupMobTweaks、FeatureRegistry、BuildupConfig；仅启用本批白名单，保留已验收掠夺者，旧 S2 继续隔离 | 默认/禁用及无关生物对照 |
| 2 | S01 昼夜/天空狙击与走射 | Adapt | 新 rebuild/skeleton/SkeletonBehavior、SkeletonGoals 及必要 Skeleton* Mixin；区分两种真实远程 Goal，避免旧 Trait+30 tick 前摇替代上游模式；不复制 Throwable 吞异常 | 白天/夜间、有/无天空、实际拉弓、箭、伤害、位移和模式转换 |
| 3 | S02 武器切换和持手 | Rewrite | SkeletonState 等新模块；上游备用武器语义，单一所有权、组件/耐久/掉率、保存与死亡结算；标准 BowItem 子类按实际手处理。上游近战默认距离 0，Buildup 开启属于主动产品差异；方案采用独立可关的 3 格切换，不谎称上游默认 | 自然近远切换、箭/近战伤害、重载/死亡/禁用守恒 |
| 4 | S03 流浪者 | Adapt | SkeletonSpecificGoals 与必要同步：按上游 FLIP 阶段还原位移/动作和实际雪球，不以旧独立三连发替代；不迁移疯狂模式 | 默认自然触发与定向场景分列，雪球实体/伤害、动作结束和中断 |
| 5 | S04 沼骸 | Adapt | 独立 DODGE 阶段；近/远端条件、实际 AreaEffectCloud、持续时间/半径/毒效果；补安全落脚和生命周期清理 | 真实闪避轨迹、云实体及效果，中断/退出/重载无残留 |
| 6 | S06 掩体 | Adapt | 将上游随机重定位和 Buildup 屋顶寻掩体严格分开；旧 ShelterGoal 仅设计参考，新独立开关和可达路径，不能称上游等价 | 有/无可达屋顶、寻路成功/失败、攻击恢复、不改方块 |
| 7 | 测试、对照和报告 | Adapt | SkeletonBehaviorTests、独立结构/世界、只读诊断、配置翻译、矩阵与 REBUILD_REPORT_R2B | Wrapper build、真实 server tick/原版调度、独立专服重启、尽可能上游同场景；人工验收后再下一批 |

## 不可放宽的验收与风险

- 原版对照、上游源码/实际运行、新版默认、新版定向、独立/总开关关闭分别记录；不直接调用生产 Goal 或手动实体 tick 作为行为证据。
- 目标死亡、创造/旁观、遮挡/超距、重新索敌和装备变更必须退出完整。重载不恢复在途动作、不复制武器，不重抽出生补给。
- 骷髅装备变更会调用原版 reassessWeaponGoal，风险是重复注册、互相抢占或禁用后无攻击；须以自然调度和实际伤害回归验证。
- 同一个 AbstractSkeleton Mixin 可影响未授权变种，必须严格限定本批实体；焦骸、凋灵骷髅和其他生物保持未实现。
- 上游对照实例有已披露资源/Mixin 错误；只记实际可观察的场景，不把退出码 0 当整体 L3。无运行证据标 NOT RUN/BLOCKED。
- 客户端动作/同步需要新增实际加载与人工画面验收；上一批客户端退出受限不能沿用为本批 PASS。
- 本批结束输出源码映射、逐项 PASS/BLOCKED/NOT RUN、完整日志、更新说明和人工清单，停止。旧 26.3-tests 只读，不推送、不发布。

仓库 VERSION_MIGRATION.md 要求先输出 Migration Plan 后等待明确批准再实施；本文件是该可审查方案，尚未新增任何骷髅 AI 或启用旧 S2 骷髅代码。
