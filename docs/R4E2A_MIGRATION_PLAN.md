# R4-E2-a 装备闭环实施方案

用户已授权方案核验后直接实施，无需第二次批准。本批仅接通装备池及两类出生武器；不进入E2-b/R5/Phase2/3，批末验证、自检、一次本地提交后停止。

环境：HEAD 48165ac、分支26.3、起始工作树干净；MC26.3/JDK25.0.3/Wrapper9.7.1/Loom1.18.3/官方未混淆命名；Loader0.19.5/API0.162.0+26.3/Fzzy0.7.7+fix3+26.3/Kotlin1.14.1+kotlin.2.4.20。依赖不变，旧26.3-tests只读。

| 项目 | 方案及依据 | 验证 |
| --- | --- | --- |
| equipment_assignment | ADAPT：重审现有严格v1池加载器和单次出生状态后注册；仅支持僵尸/尸壳/溺尸/凋灵骷髅/僵尸猪灵。稳定池ID顺序，只取第一个匹配规则、一个槽、一个物品；已有组件及掉率不改 | 真正资源加载→实体加入→Tick/Goal使用→保存/死亡，禁用/空规则/占槽/重载不补发 |
| wither_skeleton_bow 出生部分 | ADAPT：在原版 populateDefaultEquipmentSlots 唯一 setItemSlot 调用处窄包装；仅原生出生、空主手且首匹配规则主手实际原版弓时选择；其余保留石剑。原版后续 reassessWeaponGoal 及既有射箭/耐久逻辑保留 | 原生finalizeSpawn和GoalSelector射箭/受伤，原装备保护，原掉率 |
| zombified_piglin_spawn_crossbow | ADAPT：同一原版出生调用处窄包装，池给实际弩；关闭/未命中保留原金剑或金矛；本批不新增弩AI | 实际出生、保存、退出、掉落；远程AI明确仍E2-b |
| traits | REPLACE：保留各模块出生附件及现有traits.enabled门，不注册旧Marker抽签/服务 | 原模块回归，旧ID仍未生效 |

数据池v1格式不变（ID/Tag/权重，无count/组件覆写/replace字段）。增加两份默认原版武器池；移除数据规则便无特殊出生分配。特殊出生仍须equipment_assignment总门及对应单项门；Fzzy两个新概率为千分制，默认10（1%，沿用Buildup既有声明），一般空槽池仍默认100（10%）。配置版本升10，只增加默认字段，不重抽存档。上游10%/20%和局部难度>2.5是来源证据，不宣称本项目1%等同上游；不恢复bows专属硬编码或上游附魔链。

来源：已核验CExkBTN8/bFb56Zw2的WitherSkeletonEntityMixin、ZombifiedPiglinEntityMixin；目标javap描述符/调用顺序见外层docs/rebuild/evidence/r4e2a。原版ZP有1/20金矛分支，必须保留。新出生账本沿用equipment_assignment version1，所有成功/失败/关闭尝试终结；LOAD/转换/跨维度、未知版本原样保留，不恢复旧S2 AI/Mixin。不额外死亡掉落，遵守原版掉率及doMobLoot。

风险：资源标签提交时序、初始化前后覆盖、重复finalizeSpawn、转换和旧存档补发、第三方装备/掉率、稀有概率难验收。先原生调用链，补真实调度测试与独立QA，保存完整失败证据；运行上游仅限定来源/出生对照，不外推完整L3。人工/多人/整合包/性能另标NOT RUN。
