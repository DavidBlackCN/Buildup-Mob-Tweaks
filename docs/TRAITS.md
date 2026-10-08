# Traits 诊断骨架（S1-B）

> 本页只描述 Traits 子模块；S1-B 已获用户验收。S1-C 的独立装备分配另见 [装备池说明](EQUIPMENT_POOLS.md)，不改变 Traits 的诊断性质。

仅服务端计算。当前只给 `minecraft:cow`、`minecraft:zombie`、`minecraft:husk`、`minecraft:drowned` 保存诊断标记；没有增加 Goal、修改属性、分配装备、产生弹药或改变掉落。其他实体不进入池，包含凋灵、龙、远古守卫者、劫掠兽、监守者和全部未知 Mod 实体。

## 定义与抽取

| 特性 ID | 等级 | 互斥组 | 装备依赖 / 冷却 |
|---|---|---|---|
| buildupmobtweaks:demo_common | 1 | diagnostic_marker | 无 / 0 |
| buildupmobtweaks:demo_advanced | 2 | diagnostic_marker | 无 / 0 |
| buildupmobtweaks:demo_rare | 3 | diagnostic_marker | 无 / 0 |

等级仅是存档字段，不代表属性倍率。三项均有独立开关、默认开启，受 `general.enabled` 和 `traits.enabled` 约束。当前只有一个互斥组，每个实体最多一项诊断特性。

权重按实体池独立配置，单位为千分之一：

| 池 | common | advanced | rare | 无特性 |
|---|---|---|---|---|
| cow | 200（20%） | 70（7%） | 10（1%） | 72% |
| zombieFamily（僵尸/尸壳/溺尸） | 100（10%） | 30（3%） | 5（0.5%） | 86.5% |

每个权重限制 0–1000。关闭项的权重先置零，按 common → advanced → rare 的固定区间顺序，用服务端实体随机源取得一个 `[0,1)` 数。分母为 `max(1000, 权重总和)`：总和不足 1000 时保留未抽中区间；超过 1000 时按相对权重归一化，取消空区间。不是三个独立概率连续叠加。

## 生命周期策略

| 来源/事件 | 处理 |
|---|---|
| 白名单实体自然生成、命令生成、生成物品、刷怪笼、试炼刷怪笼等已知来源 | 首次加入服务端世界时抽取一次 |
| 繁殖 | 后代独立抽取，不继承父母结果 |
| 原版转换 API（尸壳→僵尸→溺尸等） | 新实例加入世界前复制源记录；保留空记录、禁用记录和未知版本，不重抽 |
| 转换目标在白名单内，但源实体没有记录 | 保存 conversion_without_source 空记录，跳过 |
| 旧存档没有本项目记录 | 保存 legacy_skipped 空记录，不给旧实体补抽 |
| 来源未知、未携带记录的跨维度加载或转换 | 保存 source_unknown_skipped 空记录，保守跳过 |
| 配置关闭时出生 | 保存 disabled 空记录；重新启用不补抽 |
| 区块重载、服务器重启、概率修改 | 使用已有记录，不抽签 |
| 其他 Mod | 走正常生成/转换 API 且为白名单类型时沿用上表；绕过来源接口则跳过。不声称具体 Mod 兼容 |

使用 Fabric `ServerEntityEvents.ENTITY_LOAD` 与 `ServerLivingEntityEvents.MOB_CONVERSION`。没有本项目新增 Mixin、全服扫描或 Tick 回调。

## 存档与启停

数据位于实体 NBT 的 `fabric:attachments` → `buildupmobtweaks:traits`，记录 `version=1`、`outcome`、原始生成原因 `origin` 和不可变的 ID/等级列表 `entries`。**空列表同样算已初始化**。

附件持久化保存完整 CompoundTag，领域 Codec 单独验证版本、已知 ID/等级、互斥数量及状态。未知版本或损坏记录保留原数据，不激活、不修复重掷；查询显示 `unsupported_or_invalid (preserved)`。当前没有重掷/未知存档自动升级命令。未来改格式必须实现明确迁移，不能通过删除记录重新抽签。

关闭某特性会立即从 active 查询结果中排除，saved 数据保留；恢复开关后恢复同一标记。修改权重只影响新实体。运行中通过 Fzzy GUI 应用；手改 TOML 应停服再启动，原版 `/reload` 不保证读磁盘配置。

等级 4 / 服务端控制台可执行只读查询：

```text
/buildupmobtweaks traits @e[type=minecraft:cow,sort=nearest,limit=1]
```

输出 UUID、saved（存档）和 active（当前启用的特性）；不广播、不改实体。无记录时显示 unassigned。

## 自动验证

使用已有 Fabric API 的 GameTest，不新增第三方测试框架。`src/gametest/` 不进入正式 JAR；build/check 包含服务端 GameTest。

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.3'
.\gradlew.bat build -PtestEula=true
# 阅读并同意 Minecraft EULA 后执行
.\scripts\Test-TraitPersistence.ps1 -AcceptEula
```

脚本适用于 Windows，要求回环端口 25586 可用。每次创建唯一 `run/trait-test-*` 世界和 `.gradle/trait-test-*` 证据目录，保留供检查，不删除旧世界。依次验证 v1→v2 配置升级、确认区块卸载再加载、停服将概率置零后重启比对 UUID 和完整结果。正常 stop 保存；超时只终止脚本自身的测试进程树并报告失败。

已测刷怪笼/试炼刷怪笼的生成原因入口，未构造带玩家的完整刷怪笼方块场景；自然群体生成、第三方转换、跨维度旅行及新增 GUI 字段仍需人工回归。详见 [S1-B 报告](S1B_REPORT.md)。

依据：本地 Fabric API 0.162.0+26.3 源码和 Minecraft 26.3 类签名；[Fabric 附件说明](https://docs.fabricmc.net/develop/serialization/data-attachments)、[GameTest 说明](https://docs.fabricmc.net/develop/automatic-testing) 作辅助。文档站当前标注 26.2，接口以本地 26.3 实物为准。
