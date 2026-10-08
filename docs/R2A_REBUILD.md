# R2-A 掠夺者重建记录

2026-10-08。范围仅 P01～P05；当前为 **L2_BEHAVIOR_PASS，等待人工验收**，不宣布整个 R2 或 L3 完成。外层完整报告：`../docs/rebuild/REBUILD_REPORT_R2A.md`（相对本 Git 根）。

## 干净基线与来源

起点分支 `26.3`，HEAD `793f5a7aa8872c044713af0da5e42c08aebb2fac`，工作树干净。保留原有配置、FeatureRegistry、装备池加载和诊断；停止旧 combat、Trait、装备自动分配等业务注册。build.gradle 以允许列表编译新业务，旧 combat/Mixin/client 实现保留但不打包。旧入口和 README 快照在 [legacy-s2](legacy-s2/README.md)。旧 S2 测试未删除，但不作为新运行时的测试入口；历史报告不继承为本轮证据。

主参考为恢复的官方 CExkBTN8（26.2）源码，bFb56Zw2（1.21.11）交叉参考。按维护者已确认的 MIT 口径保留来源、版权和全文许可；见 [NOTICE](../NOTICE.md)。未导入原版/上游完整反编译树，未引入附魔、枪械、弓分支或其他生物玩法。

## 实现与主动差异

| 功能 | 当前实现 / 决策 |
| --- | --- |
| P01 | GoalSelector priority 1 原版 MeleeAttackGoal 子类 + priority 9 无 Flags 交换 Goal。主手与背包实际物品交换，<3 / >3，无旧 20 tick 冷却或 3/6 回差；保留组件、耐久及掉率。原版弩 Goal 的 stop 仅在有效近战交接时保留目标。上游基础攻击 5→1，以本 Goal 生命周期内 -4 临时 modifier 表达，退出即撤销。 |
| P02 | 保留原版弩装填/射击调度，范围开关为 15 格。普通/困难、LOS、非 AgeableMob、<6 格等源条件下后撤，使用物品时 -.4，否则 -.8；追加落脚支撑/障碍检查，不照搬不安全位移。 |
| P03 | 无目标且受伤等待 60 server/Goal tick；priority 4 MOVE/LOOK 防止游荡抢占，固定借用副手，与真实背包槽交换。原版 completeUsingItem 完成真实消费后才按 nutrition 回血。中断、禁用、死亡前及实体加载归还；容器余物保留。上游 priority 9 空 Flags、别名物品和动态恢复手不照搬。 |
| P04 | 目标连续举盾 >60 tick 且有背包近战武器才导航接近；依赖 P01。靠实际斧近战命中触发原版盾冷却，不远程写玩家冷却。 |
| P05 | 保留原版 targetSelector；拒绝无效/创造/旁观目标，超 follow range 或持续不可见 >60 tick 清理目标、使用、装填、导航和移动。允许重新索敌；单纯 LOOK 不当作攻击失败。 |

物品始终位于原版 equipment/Inventory，不保存备用 ItemStack。附件 `buildupmobtweaks:pillager_rebuild` version 1 仅保存出生判定、借用槽、交换状态和各槽掉率。原版 Inventory 存储会省略空槽并在加载时合并同类堆叠，因此另存 `buildupmobtweaks:inventory_slots_v1` 整数索引数组；读取同一原版 Inventory 列表恢复布局，未另存物品副本。未知附件版本不启用新行为。

出生石斧和随机食物只在首次有效出生、相应开关开启且有空槽时添加；不覆盖、不补发。死亡前恢复借用副手，之后对实际背包清空并按记录掉率结算，遵守 mob drops 和 PREVENT_EQUIPMENT_DROP。该守恒清理即使总开关关闭也执行，避免遗留借用状态/库存丢失。

## 配置及兼容边界

配置版本 v9，保留旧键和值；基本掠夺者动作不挂接旧随机 Trait。

| 字段（省略根 main） | 用途 |
| --- | --- |
| general.enabled | 总开关 |
| hostile.raid.pillagerWeaponSwitch / pillagerRetreat | P01 / P02 |
| hostile.raid.pillagerTargetLifecycle / pillagerSpawnSupplies | P05 / 一次出生补给；新增独立字段 |
| hostile.extended.pillager_food_heal / pillager_shield_break | P03 / P04 |
| hostile.extended.pillager_crossbow_compatibility / pillager_range | 标准 CrossbowItem 子类实际持手 / 15 格范围 |

实体退出标签 `buildupmobtweaks:vanilla_ai`；单项 `buildupmobtweaks:disable_<feature_id>`；实体类型 tag `pillager_ai_excluded`；物品 tag `ranged_items_excluded`、`pillager_melee_weapons`。禁止抢占副手远程武器。装备池基础设施保留，但旧自动装备分配当前不运行。

没有声明第三方枪械、其他 AI Mod、数据包并发改写背包、未知旧 S2 附件迁移兼容。版本仍是开发用 1.0.0，不表示新发行。

## 实际验证

目标目录执行 `./gradlew.bat clean build -PtestEula=true --console=plain`，最终 `build-12.log` PASS。15 个自定义行为场景 + Fabric 结构校验共 16 required tests；数量仅用于识别该轮日志，验收依据是以下观察：

- 自然索敌→换斧→铁傀儡受伤→目标拉远→换回原弩→真实箭实体；名称、耐久、数量和掉率守恒。
- 原版装填期间真实后撤，从 5 格到约 6.15 格，并实际射箭。
- 原版食用计时与面包减少、回血、副手盾恢复；目标伤害事件打断、满背包碗返还、进食死亡、真实实体保存/加载、稀疏槽位与相同物品分堆。
- 真实 ServerPlayer 连接进入服务器网络 tick：生存/创造/旁观退出与重新索敌；持续举盾后接近换斧，实际命中后盾冷却可观测。
- 遮挡退出/恢复、超距退出；总开关和单项开关在运行中关闭，归还借用物品与原弩；实体退出后仍实际原版射击；出生物品重载不补发。
- 标准测试 CrossbowItem 子类实际装填射箭；原版僵尸没有注册旧业务 Goal。

专服两次 `runServer` 实际 save-all/stop/restart PASS：同 UUID 稀疏背包与副手恢复，随后自然消费面包 3→2、健康 20→24。日志在外层 `docs/rebuild/evidence/r2a/*restart-v4.log`。

上游官方 JAR 独立 26.2 启动，观察到弩→斧→弩及实际伤害；独立进食从 15→20→24，面包保持 3，与本版真实扣除形成明确主动差异。上游启动有 MagmaCube Mixin 和进度资源错误，不以“进程退出 0”认定整体上游兼容通过。P02/P04/P05 的完整上游运行对照 NOT RUN。

客户端观察到窗口、声音及纹理加载，未见本项目 Mixin 加载错误；自动窗口退出未完成，最小化后 SurfaceException，测试进程终止退出 1。完整客户端会话检查 BLOCKED（自动退出受限），人工视觉、Fzzy GUI、联机、完整袭击 NOT RUN。不能由 GameTest 或窗口加载宣布自然游戏验收通过。

## 自检与停止点

Code Review: **PASS WITH RISKS**。无已知代码 BLOCKER/MAJOR；未验证项如上。目标 JAR 仅 6 个 rebuild Mixin，旧 combat/Mixin 类及上游/原版类没有打包。旧 `26.3-tests` 的 Git 状态、HEAD 和 169 个跟踪文件哈希与开始一致。MIT 全文已进入 JAR，JSON 和 git diff --check 通过。外层 `verification.json` 记录产物和证据哈希。

人工验收完成前不进入下一阶段。完整检查表及失败迭代记录见外层 R2-A 报告；本 Git 提交不包含外层报告、日志、下载、缓存、构建产物或测试世界。
