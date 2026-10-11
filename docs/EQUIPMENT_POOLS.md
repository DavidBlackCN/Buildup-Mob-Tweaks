# 装备池数据包

服务器或整合包作者可用数据包指定适用生物出生时得到的装备。分配只尝试一次、只填空槽，不替换已有装备，不因读档、转换或重新加载数据包而补发物品。

普通装备分配由 `general.enabled`、`equipment.poolAssignment`和`equipment.assignmentChance`控制；默认概率100/1000，即10%。凋灵骷髅出生弓和僵尸猪灵出生弩有各自独立开关与概率，不套用普通10%值。支持范围限已实现的生物和武器，不让任意生物自动学会使用任意Mod武器。

当前规则只支持僵尸、尸壳、溺尸、凋灵骷髅和僵尸猪灵；实体Tag也必须解析为这些支持类型。

## 示例

将仓库的[示例目录](../examples/equipment-demo/)整个复制到世界的 `datapacks/` 后执行 `/reload`。示例把成年僵尸/尸壳/溺尸的规则改为向空副手分配盾，仍需通过分配概率。停用示例会恢复内置规则，不改变旧实体已持有的物品。

规则路径为 `data/<namespace>/buildupmobtweaks/equipment_pool/<path>.json`，ID为`<namespace>:<path>`。高优先级数据包覆盖同ID规则，entries不合并。

```json
{
  "version": 1,
  "entities": ["minecraft:zombie", "minecraft:husk", "minecraft:drowned"],
  "slot": "offhand",
  "adult_only": true,
  "dimensions": ["minecraft:overworld"],
  "difficulties": ["easy", "normal", "hard"],
  "entries": [{"item": "minecraft:shield", "weight": 1}]
}
```

| 字段 | 规则 |
| --- | --- |
| `version` | 当前为1，不是Mod配置版本 |
| `entities` | 实体ID或`#实体类型Tag`，仅支持已接入的实体类型 |
| `slot` | `mainhand`、`offhand`、`head`、`chest`、`legs`、`feet`；物品还需兼容该槽位 |
| `adult_only` | 默认true |
| `dimensions` / `difficulties` | 可省略或空列表；填写时限制维度与难度 |
| `entries` | 物品ID或`#物品Tag`及正整数weight；先按权重选条目，再从其Tag成员选择 |

每次只分配一件；不支持通过额外count/components/replace字段复制或覆盖装备。未知物品、空Tag、非法字段或不支持实体会使相应规则无效；其他有效规则仍可用。规则按ID稳定排序，采用第一个匹配规则；空槽不符或概率未中时不会继续向后反复抽取。

```mcfunction
/datapack list
/reload
/buildupmobtweaks equipment_pools
/buildupmobtweaks equipment @e[type=minecraft:zombie,sort=nearest,limit=1]
```

后两个查询需要管理员权限等级4或服务器控制台，只读查看有效规则和实际分配记录。`/reload`不重读磁盘TOML；概率修改按[配置说明](CONFIGURATION.md)操作。已分配的真实装备按实际槽位保存，没有额外死亡补偿或补发。[返回首页](../README.md)
