# 村民核心（实验功能）

适用于 Fabric 26.3。人工验收前默认关闭；在测试存档中启用 `villagers.enabled=true` 后，村民会额外检查床和工作站，并在普通移动卡住或路径出现危险时尝试恢复。原版职业、交易、补货、社交、日程和袭击响应继续运行。

## 设置

配置文件为 `config/buildupmobtweaks/main.toml`，服务器所有者可以通过 Fzzy Config 修改。全局 `general.enabled` 同样生效；村民核心不依赖 `traits.enabled` 或 `traits.unified_selection`。

| villagers 下的键 | 默认值 | 用途／范围 |
| --- | --- | --- |
| `enabled` | false | 整体启停；旧 v13 缺少本节时也是 false |
| `poi_checks` | true | 工作站、待认领工作站和床位有效性检查 |
| `navigation_safety` | true | 危险路径、无进展检测和有限重试 |
| `schedule_coordination` | true | 预留职业活动的安全工作入口；目前没有新职业活动 |
| `poi_interval` | 100 | 检查间隔，20～1200 刻；各村民错开执行 |
| `navigation_interval` | 10 | 移动进展采样间隔，5～40 刻 |
| `stuck_ticks` | 80 | 持续未移动至少半格的检测窗口，40～600 刻 |
| `retry_ticks` | 100 | 失败目标的重试等待，20～1200 刻 |
| `max_retries` | 2 | 暂缓前请求原版重算路径的次数，1～5 |

20 TPS 时，20 刻约为 1 秒。这些是 A1 的实验实现参数，正式发布默认值仍需体验验收。

## 实际处理

有效绑定继续保留。检查只访问当前维度已加载的目标；未加载、其他维度或方块与兴趣点记录尚未同步时等待复查。已确认失效的记忆按原版入口清理，随后由原版寻找可用床或工作站，容量和职业规则仍由原版决定。

移动恢复只作用于识别到的原版移动任务，核对路径与 Brain 记忆后请求重算；超过次数则暂缓同一目标。不会主动解绑仍有效的工作站。危险检查覆盖接下来最多四个路径节点，避开岩浆、火、岩浆块、仙人掌、点燃的营火、细雪、水渠和明显落差。它不能保证被推落或外部改写路径时绝不会受伤，也不保证每次重算都能找到替代路线。

交易、睡觉、受伤／可见敌人、逃跑、袭击和躲藏优先。关闭或让位后 Core 停止干预，原版照常运行；必要的重试记录保留。加载后重新观察移动，不接续一个旧 Java 路径对象。

## 兼容让位与诊断

目前没有第三方村民 Mod 自动协调。遇到重叠 Brain 或寻路改动，先关闭对应子项，或使用以下实体标签；整合包也可在实体类型标签 `buildupmobtweaks:villager_core_excluded` 中排除村民。

| 实体标签 | 范围 |
| --- | --- |
| `buildupmobtweaks:vanilla_ai` | 全部 Core |
| `buildupmobtweaks:disable_villager_core` | 全部 Core |
| `buildupmobtweaks:disable_villager_poi_intelligence` | POI 检查 |
| `buildupmobtweaks:disable_villager_navigation_safety` | 寻路恢复 |
| `buildupmobtweaks:disable_villager_schedule_coordination` | 职业入口 |
| `buildupmobtweaks:disable_villager_profession/minecraft:fletcher` | 当前箭匠职业的全部 Core；替换末尾为实际职业 ID |

例如：`/tag @e[type=minecraft:villager,limit=1,sort=nearest] add "buildupmobtweaks:disable_villager_navigation_safety"`。移除标签后重新检查条件，不补发物品或重置保存的重试记录。

服务器所有者可用 `/buildupmobtweaks villager <实体>` 查看各部分是否运行、让位原因、原版活动、检查计数与重试记录；该命令只读，服从 `general.diagnosticProbe`。未知或异常版本的数据原样保留，相关 Core 暂停。

农夫主动收获、作物扩展和其他职业工作尚待后续阶段；本功能不注册额外职业活动。
