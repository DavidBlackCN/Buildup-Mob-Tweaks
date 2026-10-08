# R2-B 骷髅系列重建记录

2026-10-09。用户批准 R2B_MIGRATION_PLAN 后实施；仅普通骷髅、流浪者、沼骸。R2-A 掠夺者保留。未启用旧 S2 模块，未开发焦骸、凋灵骷髅、骑乘技能或下一批。

## 实现及来源

上游主参考：已核验官方 CExkBTN8（Mob AI Tweaks 1.11.0-beta / MC 26.2），bFb56Zw2 仅交叉参考。MIT 作者声明及完整许可见 ../NOTICE.md 与 ../licenses/Mob-AI-Tweaks-MIT.txt。未引入新依赖；MC 26.3、Java 25.0.3、Gradle 9.7.1、Loom 1.18.3、Loader 0.19.5、Fabric API 0.162.0+26.3，官方未混淆命名。

| 功能 | 上游方法/类依据 | 当前实现与差异 |
| --- | --- | --- |
| S01 | AbstractSkeletonEntityMixin、RangedAttackGoalMixin、RangedBowAttackGoalMixin | SkeletonGoals 的 Sniper/Walk 继承原版 Goal；昼间或无天空狙击，夜间露天走射；真实拉弓 20 tick、非幼体暴击箭，射击扣实际持手弓耐久。未复制 Throwable 吞异常、群体射击等待协调。 |
| S02 | AbstractSkeletonEntityMixin 的 BackupWeapon、持手与 reassess | SkeletonState 独立持久备用物品；原弓和备用剑交换物品实例、组件、耐久、掉率。上游默认切换阈值 0，本项目明确采用独立可关的 3 格；出生持原版弓时配一把木剑，加载不补发。 |
| S03 | SkeletonSpecificGoal.FLIP、StrayEntityMixin | SkeletonSpecificGoals：120 tick 冷却，拉弓至少 10 tick 且目标超过 6 格或无弓；实际跳跃、19 tick 阶段内投掷雪球。实际碰撞按难度造成伤害；保留原版迟缓箭。 |
| S04 | SkeletonSpecificGoal.DODGE、BoggedEntityMixin | 目标距离 ≤6 或 ≥15，120 tick 冷却、安全冲量、10 tick 动作；真实毒云初始半径 .4、每 tick +.1、命中半径 +.1、duration=40、原版等待时间、POISON 200 tick。生命周期退出会提前清理。 |
| S06 | RangedAttackGoalMixin 随机重定位；旧 SkeletonExtras 屋顶设计只读参考 | 随机重定位独立开关。Buildup 屋顶搜索另一个 MOVE Goal：8 格内、可达、距目标不超 15 格、有遮蔽余量；最多寻路 80 tick，结束冷却 100 tick，恢复攻击，不改方块。 |
| 动作 | LivingEntityRendererMixin | 新客户端状态接口与 3 个 Mixin，同步有符号阶段；按源代码末九 tick 绕 X 轴旋转，使用实体高度中点。只验证加载，视觉待人工。 |

初始化只登记 rebuild/skeleton；FeatureRegistry 启用本批九个独立开关，旧概率字段保留但不参与重建行为。配置格式仍为 v9，无需重置配置。中英文描述已同步。

## 所有权、回退及兼容

- 持久数据 skeleton_rebuild(version=1)、skeleton_reserve；未知版本不覆写、不安装新 Goal。旧 S2 skeleton_trait 不被冒充为新实现存档。
- 禁用单项武器切换会还原实际备用弓；总开关或 vanilla_ai 标签撤掉本模块攻击 Goal 并重装原版攻击 Goal，保留其他 Mod 的 Goal。
- 标准 BowItem 子类与副手按实际手使用；关闭兼容后也归还已经持有的备用子类弓。未知 ProjectileWeaponItem 或 ranged_items_excluded 物品退出攻击 Goal 接管；不保证第三方武器能由原版使用。
- skeleton_ai_excluded 实体类型标签与 vanilla_ai / disable_<feature_id> 实体标签支持退出。原版目标选择器负责索敌；死亡、同队、不可攻击、创造/旁观、超 FOLLOW_RANGE、遮挡超过 60 tick 清理目标和动作。
- 死亡前还原弓，备用品单独按保存掉率结算；细雪自然转换转移原备用实例而不复制，保存冷却。
- 在途动作不读档恢复；带标记的旧毒云加载即移除，防止存档恢复后残留。原版药水云不受影响。安全检查保留落脚和头顶空间，不保证任意复杂地形。
- 骑乘不触发本批变种技能/狙击；没有迁移上游疯狂模式、奖励、武器成长或出生幼体扩展。

## 实际验证

在 26.3 执行 `./gradlew.bat build -PtestEula=true --console=plain`，JDK 25.0.3：**PASS**。最终外层日志 `docs/rebuild/evidence/r2b/build-19.log`：38 required tests passed（本批 21、掠夺者回归 16、Fabric 内置 1）。测试以实际 Server Tick / 原版 GoalSelector 驱动；不直接调用生产 Goal 或手动 tick。数量只做完整性索引，行为证据如下。

| 验证 | 结果 | 观察证据 |
| --- | --- | --- |
| S01 昼夜/屋顶、走射/狙击 | PASS | 实际蓄弓、箭实体与暴击标记，夜间走射转白天狙击，目标真实失血 |
| S02 近战换剑再射箭 | PASS | 铁傀儡近战失血，原弓实例/名字/耐久/掉率保留，真实箭；副手 BowItem 子类射击 |
| S02 保存/死亡/出生/转换 | PASS | 保存近战状态后恢复，真实死亡一弓一剑，丢失备用不补发；440 tick 左右细雪自然变种转换保留单一所有权 |
| S03 默认持弓与无弓场景 | PASS | 默认 FLIP 位移约 2.01 格，实际雪球；无弓定向场景实际雪球碰撞伤害 |
| S04 条件逼近 | PASS | 定向关闭随机重定位、目标逼近；在途位移 >.5 格，云扩张，猪获得中毒；不等于默认随机场景触发率验收 |
| S04 中断和保存 | PASS | 禁用清理云/阶段，实际云保存读回即移除，主体冷却保留 |
| S06 | PASS | 到达可达屋顶后恢复实际射箭；不可达屋顶不阻塞射击、不改方块 |
| 安全走射 | PASS | 修正左右坐标检查；三格宽平台上实际发射 5 箭，245 tick 未跌落 |
| 目标生命周期与回退 | PASS | 实际 ServerPlayer 生存/创造/旁观切换、超距、遮挡/重索敌；Fzzy 单项及总开关关闭后原版 Goal 实际射箭 |
| 范围/未来数据 | PASS | 焦骸与凋灵骷髅未装新 Goal；未知附件版本保留 |
| 独立专服停止/重启 | PASS | target-server-v2.json 两次 exit=0；同 UUID 从剑主手存档恢复弓主手/一把备用剑，随后 shots=1 |
| 客户端实际进世界/正常退出 | PASS | client-world-v2.json：joined=true、exit=0、未强杀；新渲染 Mixin 加载无异常 |
| 原版/上游专服对照 | 部分 PASS | vanilla/upstream-server-v2.json；上游配置阈值明确改 0→3，观察 BackupWeapon 弓↔剑和弓耐久消耗；原版无备用剑。不是完整 L3。 |
| 所有动画观感/多人同步/整合包/大量群体战斗 | NOT RUN | 交人工验收，不由构建或窗口加载替代 |
| 完整上游 L3 | NOT RUN / 部分 BLOCKED | S03/S04 未做上游逐帧同场景量化；上游自身日志有旧 Mixin/数据解析问题，不能整体作为黄金基准 |

独立专服与原版对照在实际 26.3 跑，官方上游 JAR 在其原生 26.2 跑。上游默认近战阈值仍为 0；调为 3 的结果不能写为默认行为。随机种子、时序与平台版本不同，不比较绝对 DPS。详细外层报告保存失败及修复过程。

## 自检

Code Review: **PASS WITH RISKS**。审查全部本轮 diff、新文件、Mixin 目标/描述符、client/common 隔离、源集合白名单、数据/掉率所有权、资源、许可证、测试与更新说明。已修复避光取模饥饿/边缘寻路/抢占、子类弓禁用归还、转换所有权、毒云重载、客户端接口包隔离问题。BLOCKER/MAJOR：无已知未解决项。残余风险：复杂地形、第三方 Mod、多人和视觉未实测；完整上游等价未验收。

未推送、未发布、不进入下一批。外层报告、证据、反编译资料不属于本 Git 根；旧 26.3-tests 只读哈希检查见外层 verification.json。

## 人工验收清单

使用新 build/libs 中主 JAR、Fabric 26.3 及上述依赖，在备份后的测试世界、普通难度、平地 28×28、足够头顶空间测试。默认配置；先排除附近额外目标。可 `/summon minecraft:skeleton ~ ~ ~`，变种换成 stray / bogged；铁傀儡目标用 `/summon minecraft:iron_golem ~ ~ ~10 {NoAI:1b}`。用标签/选择器移动目标，不需要直接修改 AI。

- 普通骷髅：目标 2 格，观察剑和实际近战；将目标移到 10 格，5–10 秒内原弓与实际箭恢复。命名弓/耐久应保留；保存退出再进入、击杀不应复制武器。
- `/time set midnight` 露天走射；`/time set noon` 或加屋顶，观察真实蓄弓狙击，切换没有卡弓。夜间可戴头盔避免日晒干扰。
- 流浪者：目标 8–12 格，等待至少 12 秒，检查跳跃后末段翻转、雪球与原迟缓箭，动作无永久倾斜；无弓场景检验实际雪球伤害。
- 沼骸：目标保持 4–5 格，等待至少 8–12 秒，观察安全闪避、短暂扩张毒云；附近可中毒实体应有真实效果。退出目标、禁用、存档重载后不能留下在途动作或旧云。
- 白天在 4–8 格处做有一格内侧余量的屋顶，观察走到遮蔽处并恢复射击；封死入口时仍射击、不破坏屋顶。坑边/水边/低顶检查安全降级。
- 对照独立开关和总开关，或 `/tag <实体> add buildupmobtweaks:vanilla_ai`；恢复原版攻击、物品不丢不复制。生存→创造→旁观→生存、墙体遮挡后拆除、超距再接近，应退出并可重新索敌。
- 客户端/专服双人查看两种动作方向、同步、持手姿势；此项自动测试未覆盖。诊断：`/buildupmobtweaks skeleton <实体>`，计数为本次加载累计，mode 为最近启动的攻击模式。

R2-B 人工确认前停止，不自动进入 R2-C 或 S3。
