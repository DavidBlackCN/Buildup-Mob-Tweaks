# R4-E1-c 人工验收清单

自动结果及其边界见[报告](R4E1C_REBUILD.md)。以下肉眼/发行实例项目前 **NOT RUN**，不能由自动计数替代。只在废弃测试世界安装正式包、独立 `-qa.jar` 与已有运行依赖；生产世界不要装QA。工具说明见[QA指南](PHASE1_QA_GUIDE.md)。

1. `/buildupqa spawn skeleton_draw`：约27秒内自动将同一把真实弓移到副手、切左主臂、移回主手（140/280/420tick）。观察每次开始拉弓时另一只手挥动，实际持弓手、箭伤和左右模型一致，无假箭物品/装备复制。`inspect`看swings/shots/visual；`off`只关闭新增挥手，弓仍正常射击。清理。
2. `/buildupqa spawn bogged_omen`：40tick后进入现有孢子后撤，看到红/棕两枚蘑菇与紫色伤害云，没有第二套后撤；inspect查看dodges/mushrooms/mushroomHits/mushroomClouds。第一阶段至下一120冷却前 `off`，后撤孢子仍可发生，新增蘑菇不再产生。对同一QA目标执行 `/effect clear @e[tag=buildupmobtweaks_qa_fixture,type=minecraft:iron_golem,limit=1] minecraft:trial_omen` 可验普通目标无蘑菇；独立关闭底层 bogged_spore_retreat 时两者均不发生。清理。
3. `/buildupqa spawn stray_flip`：40tick后一次跃射三雪球；默认场使用实际石剑以稳定进入旧阶段。给目标trial_omen后仍同一链，不出现 S2 的渐加速或第二个稀有连发 Goal。`off`关闭STRAY_JUMP_SHOT后停止该阶段，普通攻击保留。清理。
4. 选择已有空闲骷髅/沼骸/流浪者可用 `force <scene> <entity>` 接入检查，再 `target <living_entity>`；新三场不改birth、不退款冷却、不发装备、不加不祥效果。已有实际弓、原技能门、距离与当前不祥目标由操作者设置。`check`说明阻断条件，`cleanup`恢复原标签/选择字段并保留实际耐久、已赚冷却与血量。
5. 战斗中切创造/旁观、移出有效距离、移除不祥、NoAI、杀死目标/生物以及保存重启；确认在途新增效果退役、装备不复发、无蘑菇掉落物。不祥移除只阻止后续发射，已合法发射弹体仍可落地；目标退出/禁用则退役。
6. `/buildupqa spawn door`：确认门盾上下半部正确，不重叠在头上；转身、重载及禁用后无残留。用原 `skeleton` 场景近战后击杀，确认模块生成木剑不掉，原弓/外部武器按正常规则掉落。
7. 每场 `/buildupqa check`、`inspect @e[tag=buildupmobtweaks_qa_fixture,type=!minecraft:iron_golem,limit=1]`、`off`、`cleanup`。生成场景的地板/屋顶应恢复，夹具、云、弹体无残留；不要同时开两个QA场。移除QA包后验证正式包没有 `buildupqa` 命令。

记录实例依赖/Mod列表、场景、普通或不祥、左右主臂/实际用弓手、开关、退出方式、现象和日志。任意未运行项保持 NOT RUN。完整上游等时对照、自然刷怪器、多人和性能需另列验收，不从本批L2推断。

本批到此停止。下一建议 E2-a（装备闭环及凋灵骷髅/僵尸猪灵出生装备），本轮未实施。
