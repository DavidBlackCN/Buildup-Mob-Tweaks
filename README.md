# Buildup Mob Tweaks — R2-A

Minecraft Fabric 26.3 的独立 MIT 项目。本阶段只启用掠夺者 P01～P05 重建：真实背包武器交换、近战与弩射击衔接、安全后撤、消耗食物回血、举盾目标接近及目标退出。

旧 S2 AI、Mixin、客户端注入和旧行为测试已隔离，不编译进当前产物；源码、历史报告和 Git 历史保留。其他生物当前保持原版行为。旧配置值保留，但非掠夺者功能暂不生效。旧 README 快照见 [docs/legacy-s2/README.md](docs/legacy-s2/README.md)。

## 环境与验证

Java 25、Gradle 9.7.1、Loom 1.18.3、Loader 0.19.5、Fabric API 0.162.0+26.3；官方未混淆命名。Fzzy Config 与 Fabric Language Kotlin 为外部依赖，不内嵌。完整固定版本见 gradle.properties。

在本目录设置 JAVA_HOME 后执行 `./gradlew.bat build -PtestEula=true`。build 包含真实服务器 tick / 原版 GoalSelector 下的 GameTest。testEula=true 表示接受测试服务器 EULA。测试操作场景与实体，不直接调用生产 Goal 或手动 tick AI。

## 配置与兼容

Fzzy Config 服务端配置 v9。总开关、hostile 分组和各项独立开关均保留；具体字段见 [docs/R2A_REBUILD.md](docs/R2A_REBUILD.md)。

- 主手弩与背包实际斧/剑交换；小于 3 格近战，大于 3 格换回弩。不会凭空补武器。
- 出生补给仅首次有效出生执行，食物按局部难度随机，不保证每只都有。读取存档不补发。
- 脱战受伤等待 60 tick，副手使用真实背包食物，完成原版消费后按 nutrition 回血；中断、禁用、死亡及重载恢复原槽位。
- 持续举盾超过 60 tick 时接近，实际近战斧命中才可能造成原版盾冷却。
- `buildupmobtweaks:vanilla_ai` 实体标签退出新 AI；`buildupmobtweaks:disable_<feature_id>` 可单项退出。实体类型标签 pillager_ai_excluded、物品标签 ranged_items_excluded 排除兼容接管，pillager_melee_weapons 扩展近战武器（默认 swords，axes 固有支持）。
- 兼容标准 CrossbowItem 子类；没有验证第三方枪械、非标准武器系统、整合包或旧 S2 存档迁移。

管理员只读诊断：`/buildupmobtweaks pillager <实体选择器>`，查看目标、阶段、真实背包及事件计数。实体背包 /item 槽位为 mob.inventory.0～mob.inventory.4。

## 阶段状态

实现与自动行为证据见 [docs/R2A_REBUILD.md](docs/R2A_REBUILD.md)。外层 docs/rebuild/REBUILD_REPORT_R2A.md 保存完整工作报告、原始日志索引及人工验收清单；这些共享文件不在本 Git 根内。自动测试通过不等于人工实机验收完成。本阶段结束后等待人工确认，不进入 R2-B。

本项目不是上游官方续作。参考恢复的官方发行源码，保留 [NOTICE.md](NOTICE.md) 与 [上游 MIT 文本](licenses/Mob-AI-Tweaks-MIT.txt)。
