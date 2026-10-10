# R4-E2-b 完整实施方案

用户本轮明确授权一次完成整个E2-b，确认方案后直接实施；不再拆成E2-b-1/2/3或二次停顿。只修改26.3，批末验证、自检、本地提交后停止；E2-c/E3/E4/Boss/R5/Phase2/3不在本批，不推送发布。

## 环境与来源

目标起始HEAD b209c5ef230991f816b895e316ae4a21d35de13f，分支26.3、工作树干净。MC26.3、Java25.0.3、Wrapper9.7.1、Loom1.18.3、官方未混淆命名；Loader0.19.5、API0.162.0+26.3、Fzzy0.7.7+fix3+26.3、Kotlin1.14.1+kotlin.2.4.20；配置v10，依赖/元数据范围不升级。26.3-tests只读快照已保存。

源：已核验CExkBTN8/26.2与bFb56Zw2/1.21.11的ZombieVillagerEntityMixin、ZombifiedPiglinEntityMixin、DrownedAttackGoalMixin、FishingBobberEntityMixin以及四项G38客户端配套。目标原版JAR字节码/隔离Vineflower审计见外层docs/rebuild/evidence/r4e2b，不把反编译输出作为新工程源码。

## 冻结范围和取舍

| 顺序 | 项目 | ADAPT方案与边界 | 必要验证 |
| --- | --- | --- | --- |
| 1 | 职业出生与生命周期 | 仅实际ZombieVillager职业FISHERMAN/CLERIC/FLETCHER；原生finalizeSpawn后一次发到空手，已有物品/组件/掉率保留。渔夫一根竿、制箭师一把原版弩、牧师一瓶实际HARMING喷溅药水。LOAD/转换/跨维度不补发；未知附件版本不管理；实际职业/装备改变可退出，不能重发物品 | 实际出生/保存/转换/死亡、已有武器和双手占槽、幼体与普通僵尸边界 |
| 2 | zombified_piglin_crossbow / zombie_fletcher_crossbow | 主副手实际原版弩；借鉴原版弩Goal状态机，在真实GoalSelector内装填、等待、射击、耐久，保留原仇恨和近战Goal。不照搬原Goal.stop清目标/删除已装弹组件的路径；关闭或武器变化只中止自己使用中的物品，已装弹弩不清空。弹药使用原版Monster.getProjectile规则，不创建隐藏备用弩 | 真实目标/装填/箭碰撞/耐久、四手组合、创造旁观/目标死亡/NoAI/禁用原版近战回退、物品替换与重载 |
| 3 | drowned_fishing_pull / zombie_fisherman_rod | 复用原版FishingHook实体/包/碰撞/钩住ID/拉拽事件，只接管两种指定生物的Mob归属浮漂；玩家钓鱼保持原版。6～13格LOS，溺尸不钓水下目标；60tick世界冷却、同一时刻最多一个浮漂、最长60tick，真实命中后收竿拉回并损耗实际竿。溺尸沿原版自然发竿，不新发竿；在途三叉戟恢复优先。目标/手/职业变化、NoAI/退出/卸载/死亡/重载取消，不生成鱼或经验 | 原生碰撞与可测位移、水陆/错失/遮挡/三叉戟互斥、生命周期/耐久/玩家回归 |
| 4 | zombie_cleric_potions | 上游为HARMING治疗亡灵，不能解释成普通治疗药水攻击玩家。初始60tick预算、20tick投药预备、60tick冷却、最多12格LOS，支援受伤亡灵Monster；独立支援目标不覆盖原生敌对目标，不给健康生物/活体玩家投药。消耗手中实际一瓶，禁止无限复制；原生药水弹体/效果，仅本模块药水过滤无关活体和非支援对象，玩家/女巫药水不受影响 | 真实药水投出/消耗/碰撞回血、活体与满血过滤、药水组件/次数、保存/禁用与转换 |
| 5 | G38客户端与独立QA | 同步真实装填/持弩姿态、主副/左右手；僵尸模型不再用抬手动画覆盖实际弩。浮漂原版owner包允许指定Mob，线连对应持竿手，cast属性只针对实际竿/自有浮漂。五个QA场景在独立源集；实际职业和原生出生给装备，不改全局概率或正式作弊门 | 客户端真实同步/模型/浮漂线/cast探针，独立专服spawn/inspect/check/off/cleanup、正式包隔离 |

全五项使用已有FeatureRegistry布尔门及单项退出标签，新增限定类型排除Tag。配置字段和装备池v1格式保留，不启动旧S2职业/AI框架或通用共享Mixin。

## 验证与风险

实际Wrapper build+qaJar，新增真实Server Tick/GoalSelector行为/生命周期测试，保留231既有测试；必要时运行隔离专服重载/冷重启与上游限场。门盾/备用木剑回归。审查浮漂所有权、实体跟踪包、手切换与原版AI仲裁、有限药水组件和原版掉落；失败日志保留并修最早有效根因。人工肉眼/GUI、完整L3、实际自然刷怪分布、多人/整合包/长期性能未执行则明确NOT RUN，不以编译或数量代替行为。
