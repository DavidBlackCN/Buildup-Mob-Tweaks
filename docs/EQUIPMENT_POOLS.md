# 装备池与 Data Pack（S1-C）

服务端对新僵尸、尸壳、溺尸执行一次装备分配尝试，独立于 Traits。只填空槽、每次最多一件、数量固定 1；不覆盖已有装备，不创建弹药、不注册战斗 Goal、不修改掉率。Boss 与其他实体不在本阶段支持范围。

## 开关和默认行为

- `general.enabled` 与 `equipment.poolAssignment`（默认 true）控制新实体分配；不是通过 Traits 开关控制。
- `equipment.assignmentChance` 范围 0–1000，默认 100（10%）。这是一次分配的总概率；JSON 的权重决定抽中哪件装备。
- 内置规则 `buildupmobtweaks:zombie_demo`：主世界、非和平难度的成年僵尸/尸壳/溺尸，主手为空时以配置概率获得一把木剑。
- GUI 应用只影响以后出生的实体。关闭不会移除现有装备，重开不补发。手改 TOML 需要停服再启动；`/reload` 用于数据包，不负责配置文件。
- 配置版本 3 在 v2 上增加 equipment 字段；装备分配存档版本 1 与 Traits 数据版本 1 互不依赖。

## 文件路径和最小格式

`data/<namespace>/buildupmobtweaks/equipment_pool/<path>.json`，规则 ID 为 `<namespace>:<path>`。相同 ID 由优先级最高的数据包整体覆盖，不合并 entries，也不会在高优先级规则损坏时偷偷启用低优先级版本。

```json
{
  "version": 1,
  "entities": ["minecraft:zombie", "#example:zombies"],
  "slot": "mainhand",
  "adult_only": true,
  "dimensions": ["minecraft:overworld"],
  "difficulties": ["easy", "normal", "hard"],
  "entries": [
    { "item": "minecraft:wooden_sword", "weight": 2 },
    { "item": "#example:weapons", "weight": 1 }
  ]
}
```

| 字段 | 规则 |
|---|---|
| version | 必须为整数 1；未来版本拒绝 |
| entities | 1–32 个带命名空间的实体 ID 或 #Tag，按并集匹配；目前所有成员都必须为 zombie/husk/drowned |
| slot | mainhand、offhand、head、chest、legs、feet；只允许空槽，护甲还须匹配实体判定的装备槽 |
| adult_only | 默认 true；false 允许幼年实体 |
| dimensions | 可省略或空列表（不限），最多 16 个维度 ID；未知维度不匹配 |
| difficulties | 可省略或空列表（不限）；peaceful/easy/normal/hard，最多 4 项 |
| entries | 1–128 项；每项只有 item 与 weight |
| item | 带命名空间的物品 ID 或 #Tag；未知/空 Tag、未知物品、空气或未启用的物品使整条规则失效 |
| weight | 整数 1–10000；先按 entry 权重抽取，再从该 entry 的 Tag 成员中等概率选一件 |

不接受未知字段，包括 count、components、replace、脚本或嵌套条件。实体与物品 Tag 使用 26.3 的 `tags/entity_type/`、`tags/item/` 路径。规则按 ID 字典序，取第一条满足实体/环境条件的规则；该规则的槽被占用或概率未中时直接结束，不继续尝试后面的规则。Tag 成员排序后抽取，数据包文件枚举顺序不改变结果。

## 可复制的示例

把仓库 [examples/equipment-demo](../examples/equipment-demo/) 整个目录复制到世界的 `datapacks/` 中。该包使用 data format **121.0**（来自目标 26.3 version.json），覆盖内置 zombie_demo，为空副手分配盾牌。示例不自动安装到用户世界，也不随 Mod 自动启用。

```text
/datapack list
/reload
/buildupmobtweaks equipment_pools
/buildupmobtweaks equipment @e[type=minecraft:zombie,sort=nearest,limit=1]
```

修改示例的 `tags/item/demo_equipment.json`，例如把 shield 改为 bow，执行 `/reload` 后只影响新实体。需要确定性检查时，将 GUI 的 assignmentChance 暂设为 1000；生产默认仍为 100。持弓/盾并不等于本阶段已实现射箭/格挡 AI。

两个查询命令均只读，限等级 4/服务端控制台。equipment_pools 输出当前有效规则 ID 和拒绝数量；equipment 输出实体 UUID、已保存分配记录及当前主/副手能力。

## 安全回退与加载时序

使用 Fabric Resource Loader v1 和 DataResourceStore，资源快照随当前服务端的资源管理器保存。JSON 先异步读取并校验；26.3 的物品组件和 Tag 成员在 reload listener 的 apply 阶段尚未正式绑定，因此不在那里构造 ItemStack 或枚举 Tag。服务器启动/成功重载后用已绑定的注册表解析，并缓存该资源快照的不可变结果。失败的整次 Minecraft 重载不会发布新资源存储；该分支依赖 Fabric/原版资源切换契约，未通过故意破坏其他 Mod 的重载器做实测。

单个错误规则记录具体文件、来源包和原因，然后从本次装备池排除；其他有效规则继续使用。全部无效时不分配装备，原版世界照常运行。修复后再次 `/reload` 可恢复；删除规则或停用数据包会移除对应快照内容，不保留全局旧规则。

## 生命周期与物品所有权

实体附件 `fabric:attachments` → `buildupmobtweaks:equipment_assignment` 保存 version、outcome，以及适用时的 origin/pool/slot/item。记录只说明过去的分配尝试；实际物品仍由原版装备存档管理，不从记录重新创建物品。

- 出生、命令、刷怪笼等已知新生成来源：首次加入服务端时尝试；未命中、没有规则、槽被占用、开关关闭也保存最终记录。
- 旧存档、未知来源、没有记录的跨维度加载：跳过并保存跳过记录。
- 原版转换：继承源分配记录；源无记录时标记 conversion_skipped。实际装备转移完全交给原版转换参数，不额外复制 ItemStack。
- 重载、重启、改概率、死亡或物品被取走：不重新分配。未知版本/损坏附件保留且不重试。
- 掉率保持原值；新增装备只按原版流程参与掉落，没有自定义死亡发放或掉落补偿。其他 Mod 后续主动替换装备的行为不由本模块撤销。

## 能力识别与未来适配边界

只按当前实际物品识别原版弓、弩、三叉戟、盾牌，以及七种原版剑（含铜剑）。识别不读取 Traits，按需查询，不设置 Tick 扫描或装备能力缓存，因此换装后不会读到旧缓存。未知物品返回无已知能力，保持原版 AI。数据包能选择已注册的 Mod 物品，但不会因为 Tag 而获得特殊武器使用能力。

未来若加入特定 Mod 武器，需在初始化阶段注册有确定物品匹配、前置条件、使用/回收与失败回退契约的 Java Adapter，并针对明确版本测试；S1-C 不提供空的 Adapter 注册框架，也不声明任何第三方武器/AI/保护插件兼容。拾取、切换、投射物回收、Boss 接管属于后续阶段。

## 自动验证

Windows PowerShell **7**、JDK 25；阅读并接受 Minecraft EULA 后：

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.3'
.\gradlew.bat build -PtestEula=true --console=plain
.\scripts\Test-EquipmentPools.ps1 -AcceptEula
.\scripts\Test-TraitPersistence.ps1 -AcceptEula
```

装备脚本每次创建唯一 `run/equipment-test-*` 世界与 `.gradle/equipment-test-*` 日志目录，回环端口 25587；Traits 脚本使用 25586。二者均正常 stop 并保留证据，不删除旧世界。完整结果、失败修正和人工项见 [S1-C 报告](S1C_REPORT.md)。

接口证据：本地 Fabric resource-loader-v1 **3.0.4+fcdff87f5d** 源码，以及目标 Minecraft 26.3 的 WorldLoader、MinecraftServer、MappedRegistry 和 ItemStack 字节码；[Fabric 资源接口迁移说明](https://docs.fabricmc.net/develop/porting/fabric-api) 仅作辅助。