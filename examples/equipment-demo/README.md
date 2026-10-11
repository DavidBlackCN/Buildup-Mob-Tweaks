# 装备池示例：副手盾

适用于Minecraft26.3。把整个equipment-demo目录复制到世界的datapacks中，执行`/reload`。它覆盖内置zombie_demo规则，允许成年僵尸、尸壳、溺尸向空副手分配一面盾，仍受装备分配开关和概率控制，默认10%。

修改`data/buildupmobtweaks/tags/item/demo_equipment.json`可替换物品。物品必须适配指定槽位；分配装备并不保证该生物会使用任意武器或盾牌。停用示例恢复内置规则，旧实体不会重新分配。

安装、字段、限制与查询命令见[装备池说明](../../docs/EQUIPMENT_POOLS.md)。示例JSON采用项目MIT许可。
