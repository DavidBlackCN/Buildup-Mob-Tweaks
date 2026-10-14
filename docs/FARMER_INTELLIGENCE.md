# 农夫智能（实验功能）

Fabric 26.3 · 默认关闭。在测试存档中同时启用 `villagers.enabled=true`、`villagers.schedule_coordination=true` 与 `villagers.farmer.enabled=true` 后，成年农夫会在原版工作时段收获工作站附近的成熟作物，并用自己库存里的同种材料补种。

首批支持小麦、胡萝卜、马铃薯和甜菜根。农夫先确认路径、农田、库存余量及补种材料，再收获；补种成功才扣除材料。无种子或库存没有空格时等待，未成熟作物保持原样。田里的空地只有来自已保存的待补种任务才会补种。

## 与原版工作的关系

收获、补种由本模块负责，启用期间暂停原版施骨粉行为。工作站使用、交易补货、物品拾取、分享、休息与袭击响应仍由原版负责；本阶段没有新的施肥活动。关闭模块会恢复原版农业行为。原版或玩家后来种下的作物会保留，自有待补种记录随后清理。

## 配置

文件为 `config/buildupmobtweaks/main.toml`，可通过 Fzzy Config 修改；全局 `general.enabled` 同样生效。旧 v13 文件没有新节时，村民核心与农夫模块均保持关闭，原有数值保留。下面是实现的实验参数，发布默认值仍待体验审核。

| `villagers.farmer` 下的键 | 默认值 | 用途／范围 |
| --- | --- | --- |
| `enabled` | false | 开启自有收获和同种补种 |
| `external_control` | false | 显式把农业交给外部控制器；暂停本模块及原版收获、施骨粉选择 |
| `protection_required` | false | true 时要求已注册的保护适配器明确允许；没有适配器就暂停 |
| `radius` | 8 | 工作站附近扫描范围，2～16 格；扫描高度为上下各一格 |
| `scan_interval` | 60 | 扫完一轮或失败后的重试间隔，20～1200 刻 |
| `scan_budget` | 64 | 每次工作选择最多检查的方块数，16～256 |
| `world_scan_budget` | 512 | 同一世界每刻总扫描上限，64～4096 |
| `action_interval` | 40 | 完成／清理任务后的冷却，20～600 刻 |
| `target_timeout` | 180 | 单个目标占用和工作超时，60～600 刻 |

20 TPS 时，20 刻约为一秒。扫描分批进行，每次选择最多请求四条路径；只访问已加载区块，优先处理已有待补种任务。首版路线保守：目标必须与脚部同层，遇到水渠、危险路径或无法到达的作物会跳过。

## 保护、让位和诊断

收获与补种分别检查 `mobGriefing`，都需要为 true。领地保护 Mod 没有通用自动适配；保护服务器在测试中启用农夫时，应设置 `protection_required=true` 并确认安装了对应 Java 适配器。数据包不能凭空提供领地权限检查。

显式让位可用实体标签 `buildupmobtweaks:farmer_external`；关闭自有农作并回到原版用 `buildupmobtweaks:disable_farmer_intelligence`。村民核心的排除标签和实体类型排除同样适用，见[村民核心](VILLAGER_CORE.md)。全局关闭／明确 Core 排除优先于农作负责方选择。外部模式仅让出农业选择，不会自动安装第三方 AI。

管理员使用 `/buildupmobtweaks villager <实体>` 可查看负责方、扫描／寻路／收获／补种计数、暂停原因及保存的待补种记录；该命令只读，受 `general.diagnosticProbe` 控制。计数是当前加载期间的观察值，重载实体后重新计数。

## 数据包作物描述

路径为 `data/<命名空间>/buildupmobtweaks/crop_profile/<名称>.json`，文件名决定 Profile ID。例如覆盖内置小麦时使用 `data/buildupmobtweaks/buildupmobtweaks/crop_profile/wheat.json`：

```json
{
  "version": 1,
  "crop": "minecraft:wheat",
  "maturity": {"type": "age", "property": "age", "value": 7},
  "harvest_handler": "buildupmobtweaks:break_and_replant",
  "replant": {
    "item": "minecraft:wheat_seeds",
    "count": 1,
    "block": "minecraft:wheat",
    "state": {"age": "0"},
    "substrate": "minecraft:farmland"
  }
}
```

当前格式支持 `CropBlock` 的整数年龄属性、最大年龄成熟判断、原版耕地、真正能种下该作物的 `BlockItem` 与未成熟补种状态；材料数为 1～16 的整数。甜菜根成熟年龄为 3，其余内置作物为 7。原版方块掉落负责产物。

同一 ID 按数据包优先级覆盖；不同 ID 同时描述同一作物会暂停这些描述并记录错误。未知版本、无效字段、物品或处理器会拒绝。使用 `/reload` 后重新读取；正在执行的自有活动停止，待补种记录保留。保存的规则与新规则不同，任务暂停；恢复同一规则后再尝试补种。整次资源重载失败沿用旧资源。

简单 Mod 作物可以提供同格式描述，但仍须逐项实测。特殊右键收获、多阶段、非年龄作物需要额外实现，当前没有现成支持。

## 保存与限制

待补种记录与村民真实库存分开保存：库存只有原版那一份，本模块没有额外种子箱。卸载或正常重启后只恢复补种，收获不会重放。缺少材料、规则或权限时等待；玩家已改种、耕地消失、目标超出新的有效工作区域时结束该记录。转化后原版丢失的库存不会补发，材料不足时继续等待。

未知存档版本原样保留并暂停自有农业。关闭期间原版可继续改变田地，因此再次开启会重新检查现场。本机制不提供进程在单次世界操作中途崩溃时的跨世界／库存原子事务保证。

真实第三方村民 AI／保护 Mod 和大型整合包尚未联合验收；实验开关保持关闭，先用独立测试存档确认控制权、权限、补货及农田布局。
