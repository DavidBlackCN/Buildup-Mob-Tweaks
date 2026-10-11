# 服务器与兼容说明

本版本面向 Minecraft Java Edition **26.3 / Fabric / Java25**。当前验证的依赖版本见[安装说明](../README.md#安装)。其他Minecraft版本不在本分支适配范围内。

## 多人游戏

服务器负责生物行为，客户端负责相应表现。请在服务器和客户端安装相同版本的本Mod及必需依赖，修改Mod或依赖后完全重启。不要只在客户端改设置来改变服务器的战斗规则。

原版依赖组合已进行专服、两个真实客户端同步，以及代表性生物的存档重启与真实区块重载验证。此范围不等同所有第三方整合包都已兼容，也不代表长期生产世界或多人容量保证。

## 整合包与排除

其他AI/战斗Mod可能同时控制目标、武器、运动或渲染。先检查相关功能是否重复，再按需要关闭对应设置。标准弓、弩与部分原生物品类型有明确适配路径，但特殊枪械、任意自定义投射物或领地保护不能仅凭物品Tag就获得完整支持。

管理员可给指定生物添加实体标签，使已接入且遵循这些过滤的行为退出：

```mcfunction
/tag @e[type=minecraft:skeleton,sort=nearest,limit=1] add buildupmobtweaks:vanilla_ai
```

单项过滤使用 `buildupmobtweaks:disable_<feature_id>`，例如：

```mcfunction
/tag @e[type=minecraft:skeleton,sort=nearest,limit=1] add buildupmobtweaks:disable_skeleton_staggered_draw
```

使用 `remove` 可撤销标签。过滤不会撤销原版伤害、药效或已经完成的装备分配，也不删除第三方Mod的AI；已有弹体和附件仍有各自安全清理规则。实体标签和数据包的实体类型Tag是两种机制。

Mod随包提供选定的实体类型排除Tag，位于 `data/buildupmobtweaks/tags/entity_type/`；装备相关Tag和示例见[装备池说明](EQUIPMENT_POOLS.md)。新增排除对象前核对实际Tag文件，不把一个生物组的过滤当作全部生物通用规则。

## 当前限制

未提供Boss AI接管或第三方Boss控制者的自动让位保证；没有声明Stellarity专属适配。实际第三方整合包、特殊武器与领地组合、长期自然分布、连续袭击及农场压力需要独立验证。稀有能力不会因旧实体加载而重新抽取。

反馈兼容问题时附Mod及依赖完整版本、相关配置、实体来源与装备、日志和最短复现步骤。请先备份存档，在副本中排查。[返回首页](../README.md)
