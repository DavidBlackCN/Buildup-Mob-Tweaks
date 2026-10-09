# Buildup Mob Tweaks — R3 / R4-A / R4-B

正式阶段遵循 PLAN.md：历史 R2-A/R2-B/R2-C 对应 R3/R4-A/R4-B，见 [编号对照](docs/STAGE_INDEX.md)。生成备用木剑死亡掉落的反馈修复见 [R4-A 修复](docs/R4A_SWORD_FIX.md)。

Minecraft Fabric 26.3 的独立 MIT 项目。当前启用掠夺者 P01～P05 重建：真实背包武器交换、近战与弩射击衔接、安全后撤、消耗食物回血、举盾目标接近及目标退出。

旧 S2 AI、Mixin、客户端注入和旧行为测试已隔离，不编译进当前产物；源码、历史报告和 Git 历史保留。现已加入普通骷髅、流浪者、沼骸的 S01～S04/S06，以及普通僵尸、尸壳、溺尸的 Z01～Z04/D01；其余生物保持原版。旧配置值保留，未在本批白名单的功能暂不生效。旧 README 快照见 [docs/legacy-s2/README.md](docs/legacy-s2/README.md)。

## 环境与验证

Java 25、Gradle 9.7.1、Loom 1.18.3、Loader 0.19.5、Fabric API 0.162.0+26.3；官方未混淆命名。Fzzy Config 与 Fabric Language Kotlin 为外部依赖，不内嵌。完整固定版本见 gradle.properties。

在本目录设置 JAVA_HOME 后执行 `./gradlew.bat build -PtestEula=true`。build 包含真实服务器 tick / 原版 GoalSelector 下的 GameTest。testEula=true 表示接受测试服务器 EULA。测试操作场景与实体，不直接调用生产 Goal 或手动 tick AI。

## 配置与兼容

Fzzy Config 服务端配置 v9。总开关、hostile 分组和各项独立开关均保留；具体字段见 [docs/R2A_REBUILD.md](docs/R2A_REBUILD.md)。

- 主手弩与背包实际斧/剑交换；小于 3 格近战，大于 3 格换回弩。不会凭空补武器。
- 出生补给仅首次有效出生执行，食物按局部难度随机，不保证每只都有。读取存档不补发。
- 脱战受伤等待 60 tick，副手使用真实背包食物，完成原版消费后按 nutrition 回血；中断、禁用、死亡及重载恢复原槽位；死亡结算时背包食物不掉落，非食物装备保留原掉落规则。
- 持续举盾超过 60 tick 时接近，实际近战斧命中才可能造成原版盾冷却。
- `buildupmobtweaks:vanilla_ai` 实体标签退出新 AI；`buildupmobtweaks:disable_<feature_id>` 可单项退出。实体类型标签 pillager_ai_excluded、物品标签 ranged_items_excluded 排除兼容接管，pillager_melee_weapons 扩展近战武器（默认 swords，axes 固有支持）。
- 兼容标准 CrossbowItem 子类；没有验证第三方枪械、非标准武器系统、整合包或旧 S2 存档迁移。

骷髅系列使用真实备用武器、昼夜狙击/走射、流浪者翻转雪球、沼骸闪避毒云与独立屋顶寻路。旧 Traits 概率不控制这些行为，开关和过滤仍独立。标准 BowItem 子类按实际持手使用，未知弹射武器退出接管；配置与兼容边界见 R2-B 文档。

僵尸系列使用原版门方块上下半部展示、10° 正面材质耐久格挡、已有副手盾的实际格挡与损耗；尸壳在三层沙及 mobGriefing 条件下下沉、按目标位置快照换位并上浮。门盾/最多一个同族幼体在首次合格出生互斥抽取；不删除已有装备，读档不重抽。溺尸将手中实际三叉戟投出、寻回与归还；在途保存 UUID 引用，没有备用物品副本。配置 v9 不变，旧高级主动盾和相关未使用字段暂不生效。详见 [R2-C 范围、配置与验收](docs/R2C_REBUILD.md)。

管理员另可用 `/buildupmobtweaks skeleton <实体>` 查看备用物品和模式计数。管理员只读诊断：`/buildupmobtweaks pillager <实体选择器>`，查看目标、阶段、真实背包及事件计数。实体背包 /item 槽位为 mob.inventory.0～mob.inventory.4。

## 阶段状态

R3 掠夺者已获用户人工验收，死亡背包食物掉落反馈已修正。历史证据见 [R2A_REBUILD](docs/R2A_REBUILD.md)。R4-A 骷髅与 R4-B 僵尸已经实现，用户已进行了实机验收并反馈生成木剑掉落；本次反馈修复通过 66 项实际行为回归和隔离专服重启/死亡测试，见 [修复报告](docs/R4A_SWORD_FIX.md)。未提供的专项验收结果不推定为 PASS。外层报告和原始证据不在本 Git 根内。

R4-B 的历史自动证据见 [R2C_REBUILD](docs/R2C_REBUILD.md)。管理员只读诊断另有 `/buildupmobtweaks zombie <实体>`、`/buildupmobtweaks drowned <实体>`；实体类型标签 `buildupmobtweaks:zombie_ai_excluded` 排除接管。用户已授权进入 R4-C 女巫/唤魔者/恼鬼，但具体 Migration Plan 输出后仍须按版本迁移规范单独批准；新 AI 尚未实施。多人、完整 L3 和旧 S2 附件迁移保持各报告的实际状态。

本项目不是上游官方续作。参考恢复的官方发行源码，保留 [NOTICE.md](NOTICE.md) 与 [上游 MIT 文本](licenses/Mob-AI-Tweaks-MIT.txt)。
