# R4-C 女巫、唤魔者、恼鬼重建报告

2026-10-09。本批仅 R4-C，按已单独批准的 [Migration Plan](R4C_MIGRATION_PLAN.md) 实施。限定场景 L2 行为通过；等待人工验收，不进入 R4-D/R5/S3，不推送、不发布。

## 环境与范围

目标 `26.3/`、分支 `26.3`，起始 HEAD `d67d3939fe363b0d0037f836221b700be6814aaf`，起始工作树干净。旧 `26.3-tests` 严格只读，最终逐文件哈希与 HEAD 复核见外层 verification.json。Java 25.0.3、Wrapper 9.7.1、Loom 1.18.3、Loader 0.19.5、API 0.162.0+26.3、Fzzy 0.7.7+fix3+26.3、Kotlin 1.14.1+kotlin.2.4.20；官方未混淆命名，无 Yarn。依赖、配置版本 v9、旧历史不变。

参考官方发行 CExkBTN8（26.2）与 bFb56Zw2（1.21.11）的恢复源码；额外恢复真实 26.3 Witch/Evoker/SpellcasterIllager/Vex/Projectile/DeathProtection 类并导出 javap 描述符。没有编辑反编译输出或将 Minecraft 代码并入 Mod。MIT 及 Copyright (c) 2024 N0t_UN_Owen 保留于源码、NOTICE 和 licenses。

## 实现和主动差异

| 功能 | 本批实现及边界 |
| --- | --- |
| W01 | 拦截原版已经选定的真实药水，空主手展示后由实际 Goal 默认等待 20 world ticks 投出；独立默认 100 ticks 冷却。目标变更、创造、死亡、禁用或重载清理本模块标记物品。已有外部主手不被预告替换。上游是两次原版攻击调用间缓存，没有固定 20 ticks；Buildup 主动采用独立预算。袭击同伴治疗走原版，无敌对预告/冷却阻塞。 |
| W02 | 高目标、在地面、无跳跃效果时以 5% 检查加入 LEAPING 候选，后续原版水、火、治疗、速度选择可覆盖它，原版饮用时长和药效生效链保留。有跳跃效果时调用原版 JumpControl 起跳投掷，开关独立；不加入 OP 药水。 |
| E01 | 首次 NATURAL / SPAWN_ITEM_USE 出生互斥抽签，默认火球 70/1000、图腾 10/1000。火球是三个真实 SmallFireball 乘客，阶段第 10/30/50 ticks 生成、第 70 ticks 径向释放、第 80 ticks 结束；默认 400 ticks 世界冷却在开始时预留，中断不返还。固定序号环绕，不计无关乘客，不覆盖原版施法/避让 Goal。环绕接触和释放后的命中仍有真实伤害；独立 no-fire 默认同时防止目标及方块点火，关闭恢复原版点火/mobGriefing。上游负冷却循环、默认 20 和无限续环不照搬；不复制键名拼写错误、WindCharge 爆炸与吞异常。 |
| E02 | 图腾特性要求生命严格低于 20%、副手为空、有效敌人、当前未原版施法。一生一次，真实副手图腾 40 world ticks 窗口，原版 DeathProtection 自行消耗和复活。进入即保存 spent，禁用/重载/过期不返还。外部副手不替换，期间被外部换装也不删除外部物品。死亡在原版 die 入口清理自己的临时物品，不增加装备战利品；原版唤魔者战利品表中的图腾仍正常掉落。与上游临时主手、重复窗口有意不同。 |
| E03 | 原版 EvokerSummonSpellGoal 保留；默认总名额 6、世界冷却 680。简单/普通/困难每批 2/3/4，必须容纳完整一批；原版附近恼鬼的随机限制仍可进一步延迟。保留 v1 `vex_owners` 世界 UUID 索引、native owner 引用；卸载占额，实际销毁释放，重启不清索引，不强制加载。上游瞬态列表非空即阻止再召唤不同于本总预算。 |
| V01 | 原版 VexChargeAttackGoal 自行取得目标、启动及碰撞伤害。方向快照为双方眼部位置之差，持续 floor(length*1.5)、上限 40；原版控制器每 Tick 执行固定方向，使用绝对终点保留 native hasWanted；修正上游方向向量当世界坐标的问题。默认恢复 20 ticks、近距保护 3 格，可独立关闭；恢复预算持久化，在途方向不恢复。未添加第二次手工攻击。 |

`FeatureRegistry` 只新增上述已实现 ID。实体 `buildupmobtweaks:vanilla_ai`、`buildupmobtweaks:disable_<feature_id>` 和类型标签 `raid_ai_excluded` / `vex_ai_excluded` 为兼容退出；仅精确原版 WITCH/EVOKER/VEX 接入，第三方子类不接管。未知附件版本保留，不安装新 Goal。管理员只读 `/buildupmobtweaks raid <实体>` 显示状态、目标、冷却和 owner 名额，不直接调用 AI。

## 修改位置与原因

- `rebuild/witch`、`rebuild/evoker`、`rebuild/vex`：独立行为、真实世界索引和生命周期；`rebuild/raid`：附件、门控、临时物品标记及只读诊断。
- `mixin/rebuild/{Witch,Evoker,Vex,Raid}*` 与 common Mixin JSON：验证过的原版选药/施法/运动/投射物/死亡入口，client 不加载客户端专用类。
- `BuildupMobTweaks` / `FeatureRegistry`：注册已实现模块，并在读取世界前注册持久附件；旧 S2 combat/Mixin 未启用。
- 两个排除类型标签：数据包兼容退出；未提供通用领地/第三方装备协议。
- `RaidBehaviorTests`、GameTest metadata、独立 test_environment、build allowlist：真实 Tick / 原版 Goal 的 29 个 R4-C 场景。`CombatTestWorld` 将已有递归实体存档夹具泛化以保存实际火球乘客。
- `SkeletonBehaviorTests`：已有走射断言改观察全程最大位移，避免往返到起点被误判为没移动；不改骷髅生产代码或放宽阈值。
- 本报告、Migration Plan、README、STAGE_INDEX、AGENTS、NOTICE、配置说明、UPDATE_NOTES 与外层矩阵/基准/证据：正式阶段、授权、来源及结果一致。

## 实际验证

原始证据在 Git 根外 `../docs/rebuild/evidence/r4c/`；[外层报告](../../docs/rebuild/REBUILD_REPORT_R4C.md) 提供链接。

| 项目 | 状态 | 实际证据 / 限定 |
| --- | --- | --- |
| Wrapper build + 全量原版调度回归 | PASS | `./gradlew.bat build -PtestEula=true --console=plain`，最终 `build-15.log`；95 项必需测试（R4-C 29 + 原有 66），实际服务器 Tick，不直接调用 Goal/生产 tick。 |
| W01/W02 | PASS | 实际药水实体、默认前摇及冷却；高位目标真实喝药→Jump Boost→位移→投掷；关闭回退、创造、重载、外部物品；限定 native Raid membership 夹具真实治疗同伴，不代表完整村庄袭击。 |
| E01/E02 | PASS | 三个真实移动乘客及三次释放；实体与方块命中、no-fire 关闭点火；原版图腾消耗、health=1、原版效果；40 ticks 窗口、终身一次、外部副手、禁用/重载/死亡及创造/NoAI退出。死亡仅 1 个原版战利品图腾。 |
| E03/V01 | PASS | 实际第二批召唤相隔至少 680 ticks，峰值 <=6；满名额阻止启动，销毁释放。方向快照、横移目标不弯轨、原版伤害、恢复期真实停止、原版回退及旁观/重载退出。 |
| 专服正常 / 简单 / 困难 | PASS | `python ../docs/rebuild/scripts/r4c_servers.py target v2` 与 easy/hard；隔离世界/端口，原版 Goal 实际生成 3/2/4，进程正常退出 0。 |
| 冷启动与真实远区块卸载 | PASS | 同 UUID `aec2f3ce-9d9b-4fc8-a612-a95611bf25a6` 保存 `summon_ready:777L`，重启保留；totem_spent 与特性保留。实际 owner-linked 恼鬼定向移至 708/708，取消 force-load、迁走 spawn，查询确实无实体；只加载主人原区块仍占 3，远区块加载并实际销毁后 0。定向 NoAI/长寿命仅隔离索引夹具，不代替恼鬼 AI 验收。 |
| 上游官方 26.2 / 原版 26.3 | PASS（有限观察） | 独立服务器实际启动、默认普通难度召唤各 3；上游手持预告与真实火球乘客有记录，原版无这类预告环绕。采样、目标剩余血量不同，不是等效量化对照。 |
| 客户端集成世界加载 | PASS（L1） | `r4c_client.py` 的 v3 隔离世界；实际玩家登录，唯一 VM 标记关闭本轮客户端，正常退出 0。未验收 GUI/环绕视觉/多人。 |
| 自然刷怪器完整分布、完整袭击、多人、第三方整合包、完整 L3 | NOT RUN | Native finalizeSpawn 1000 个受控出生样本只验证独立抽签/不重抽，不证明自然分布或平衡。 |
| Allay | BLOCKED | 历史恢复问题；按用户指示不再尝试，本批不包含。 |

失败记录保留：首轮编译的 Raider 包名/DropChances API、测试递归夹具签名已据实际源码修正；早期测试揭示 Fabric ALLOW_DEATH 位于原版图腾判断之前，清理已移至 die。专服 v1 暴露附件延迟注册导致冷启动跳过类型，已提前注册并在 v2 复测。并行长场景与全局配置测试用独立 Test Environment 隔离；恼鬼平台下方空洞、观察窗口和跨不同冲锋比较的夹具问题已修正。既有随机目标/回收的失败尝试仍保留，不以重试数量作验收。客户端 v1/v2 曾加载成功但关闭追踪失败而强制结束，仅 v3 标正常退出；不隐去它们。

最终日志仍包含测试玩家断连后的 `StacklessClosedChannelException` 发包警告和服务器落后警告，未过滤原始日志；95 项必需断言通过，未出现 Mixin 注入失败。最终 NOTICE 更新后执行 `./gradlew.bat jar sourcesJar --console=plain`，PASS；common/client 编译为 UP-TO-DATE，刷新许可与来源打包，不重复以文档修改触发行为测试。

发行产物 `build/libs/buildup-mob-tweaks-1.0.0.jar`，299449 字节，SHA-256 `0eb3318000d047b0534aa571c311469e9a7d44f56ef22d8dba01a2b61e7074f9`。源码包 `build/libs/buildup-mob-tweaks-1.0.0-sources.jar`，141265 字节，SHA-256 `5585c838b4cd659e144bfbd71b154e3714bf0fb1d8c5d3b692ebc39d2a33a268`。外层 `r4c_verify.py` 核验发行/源码包、30 个 common Mixin、真实运行回执、来源哈希和旧工程 169 个跟踪文件，结果见 verification.json。

## 自检与剩余风险

Code Review: **PASS WITH RISKS**。审查完整 Git 差异、注册调用链、目标签名/静态语义、local/ordinal、common/client 边界、未知数据、装备数量、native death protection、真实卸载、资源白名单、MIT、日志和 UPDATE_NOTES；无未解决 BLOCKER/MAJOR。没有依赖升级、AW、旧工程修改、数据格式破坏、关闭验证或恢复旧 S2 AI。

风险：旧 S2 升级前未加载、未索引的恼鬼无法追溯；被其他 Mod 绕过生命周期直接移除/改 owner 的索引兼容未验证；第三方战斗/掉落/领地与联机同步未验证。原版附近数量随机检查与其他施法可推迟技能启动，400/680 是预算下限，不保证每到时立即攻击。没有将 L1、测试数量或受控出生替代视觉/完整袭击/自然刷怪器验收。

UPDATE_NOTES 已追加本批经过验证的 Release 说明。本地提交按持续授权收尾，父目录报告、原始 JAR、反编译和运行证据不在提交内。没有 push、发布或下一阶段工作。

## 人工验收清单（完成后暂停）

- [ ] 默认配置女巫 8 格平地：实际手持药水、约 1 秒预告后投掷；高台目标：普通喝跳跃药水后起跳投掷。展示中切创造、移走或关闭功能，应清预告并恢复原版路径。
- [ ] 在真实村庄袭击中检查女巫对受伤同伴的治疗、敌人切换、袭击结束；本批自动证据只是限定同伴场景。
- [ ] 新出生唤魔者观察默认稀有火球特性；需要定向视觉验收时暂调火球概率为 1000、图腾为 0，新生成后检查三个真实环绕、径向释放、原版其他施法恢复，再还原配置。
- [ ] 图腾场景反向调出生概率，生成新的特性个体；血量 <20% 时副手短暂图腾可真实复活一次；已有副手不替换；死亡不会额外掉临时图腾，原版战利品图腾仍允许。
- [ ] 简单/普通/困难各观察恼鬼 2/3/4；留存、卸载、杀死及重启后，主人不能突破 6 的预算。只读 `/buildupmobtweaks raid <实体>` 辅助核对，不能用诊断计数代替战斗录像。
- [ ] 恼鬼 8 格起冲后横移，观察固定轨迹与恢复；3 格内近距保护；切创造/旁观、重载和各独立开关后无持续冲锋残留。
- [ ] 联机客户端检查环绕位置、姿态、临时装备同步；第三方整合包与旧 S2 升级先备份另测。

等待本批人工确认，不进入下一阶段。
