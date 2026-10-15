# Buildup Mob Tweaks

Buildup Mob Tweaks 改善 Minecraft 生物的战斗、装备使用和日常行为。掠夺者会切换真正持有的武器，骷髅会寻找更安全的射击位置，部分僵尸会拿门格挡；动物、宠物和雪傀儡也有各自的互动变化。

本分支适用于 **Minecraft Java Edition 26.3 / Fabric**。各项行为可独立开关；特殊能力有触发条件或出生概率，不会出现在每一只生物身上。

实验性的[村民核心](docs/VILLAGER_CORE.md)和[农夫智能](docs/FARMER_INTELLIGENCE.md)已提供，均默认关闭；测试存档可启用床／工作站检查、寻路恢复，以及成熟作物收获与真实库存同种补种。农夫会优先选择安全农业路线，支持直楼梯并保护耕地；原版交易补货与日程保留。

## 安装

1. 使用 Minecraft **26.3**、**Java 25** 和 Fabric Loader **0.19.5 或以上**。目前验证的 Loader 版本为 0.19.5。
2. 将正式 Mod JAR 放入游戏的 `mods` 文件夹，并安装下列 Fabric 版依赖。不要把 `-sources.jar` 或开发用 `-qa.jar` 当作游戏 Mod 安装。
3. 多人游戏时，在服务器和各客户端安装本 Mod 及相同的必需依赖；独立服务器也支持。

| 必需依赖 | 当前验证版本 |
| --- | --- |
| Fabric API | `0.162.0+26.3` |
| Fzzy Config | `0.7.7+fix3+26.3` |
| Fabric Language Kotlin | `1.14.1+kotlin.2.4.20` |

这些依赖需要单独安装。Mod Menu 不是必需依赖。更新 Mod 或依赖后，请完全重启游戏或服务器；更新现有存档前先备份。

## 游戏中有什么变化

| 生物或场景 | 主要变化 |
| --- | --- |
| 掠夺者 | 近战与弩射击衔接、持有武器交换、后撤、消耗食物恢复及目标退出 |
| 骷髅家族 | 弓与备用近战武器切换、安全横移、寻荫、错峰拉弓及部分幼体变种 |
| 僵尸、尸壳、溺尸 | 门盾或已有盾牌格挡、条件式沙地下潜、幼体骑乘、真实三叉戟投出与回收、部分职业武器 |
| 灾厄村民与恼鬼 | 药水和施法预告、有界支援、幻术师分身箭与换位、恼鬼冲锋与恢复 |
| 其他敌对生物 | 选定的火球、闪避、连击、骑乘、散弹和视觉行为 |
| 动物、宠物和雪傀儡 | 惊慌与避让、墨汁与注视、宠物缓慢恢复、猫避弹、雪傀儡南瓜互动和投球表现 |

详细触发条件与限制见[功能介绍](docs/FEATURES.md)。需要调整难度或关闭某项行为，请看[配置说明](docs/CONFIGURATION.md)；服务器与整合包使用者可查看[兼容说明](docs/COMPATIBILITY.md)和[装备池数据包](docs/EQUIPMENT_POOLS.md)。

本版本没有新增附魔、独立装备成长或 Boss AI 接管。配置中标为“未生效”的历史选项不会启用相应玩法。

## 遇到问题

反馈时请附 Minecraft、Loader、本 Mod 和依赖版本、其他 Mod 清单，以及复现步骤。特殊能力未触发时，请说明生物来源、装备、目标、距离、难度和相关设置；这有助于区分出生概率、触发条件和实际故障。崩溃或报错可附 `logs/latest.log` 的相关部分。

第三方 AI、特殊武器、领地保护和大型整合包需要分别验证，不能仅凭本 Mod 能启动就认定兼容。

## 来源与许可

项目采用 [MIT License](LICENSE)，参考和改编 Mob AI Tweaks 的部分生物行为，是独立项目。作者署名、第三方许可与来源见 [NOTICE](NOTICE.md) 和 [licenses](licenses/)。

## Phase 2：可选统一特性模式

默认继续采用 Phase 1 行为。管理员可以在新个体上体验首批七项的统一抽取；模式与结果保存，旧个体不会因开关变化而重抽。当前尸壳／沼骸的新池需显式配置，其他候选暂不纳入统一模式。用法见[配置说明](docs/CONFIGURATION.md#可选统一特性抽取)。

Phase 2 的特性资格、生成来源、装备与状态边界及生命周期验证已完成，统一注册范围为首批七项。当前默认配置与数值保持不变；Phase 3-A1 Villager Core、A2 Farmer 与 A2.5 环境适应已完成工程和限定自动验证，默认关闭，等待人工审核；其他职业、Boss Profile 和外部大型内容联动尚未实施。

开发与整合包维护入口为外层[默认配置](../development-records/phase2-c/DEFAULT_CONFIGURATION_SPEC.md)、[平衡记录](../development-records/phase2-c/DEFAULT_BALANCE_GUIDE.md)、[兼容路线](../development-records/phase2-c/COMPATIBILITY_ROADMAP.md)和[发布收口报告](../development-records/phase2-c/COMPLETION_REPORT.md)，等待人工最终验收。报告和开发探针位于版本 Git 之外。
