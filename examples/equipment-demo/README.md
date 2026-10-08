# 装备池示例（Minecraft 26.3）

复制整个 equipment-demo 目录到世界 datapacks/，然后执行 /reload。此包覆盖内置 zombie_demo：为成年僵尸/尸壳/溺尸的空副手分配盾牌，总概率仍由 equipment.assignmentChance 控制，默认 10%。装备识别不会授予射击或格挡 AI。

修改 data/buildupmobtweaks/tags/item/demo_equipment.json 可换物品；支持同包实体 Tag 示例。min_format/max_format 均为 121.0。停用该包后恢复 Mod 内置的主手木剑规则；旧实体不会重新分配装备。

字段、安全回退及命令见仓库 docs/EQUIPMENT_POOLS.md。所有 JSON 属于本项目 MIT 示例。