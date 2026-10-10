# R4-E2-b 完整职业武器重建报告

本轮按用户明确授权一次完成五个ID及G38客户端配套；[实施方案](R4E2B_MIGRATION_PLAN.md)先核验后直接执行。当前124个ID中 **73支持 / 51未生效**，配置仍v10，独立QA **22场景**。本批结束，不进入E2-c/E3/E4/Boss/R5/Phase2/3，不推送或发布。

| 项目 | 状态 | 实际结果与取舍 |
| --- | --- | --- |
| zombified_piglin_crossbow | ADAPT / L2 PASS | 实际主副手弩装填、原版箭命中和耐久；保留原生受击仇恨及近战。E2-a出生发弩仍由另一门控制 |
| zombie_fletcher_crossbow | ADAPT / L2 PASS | 仅制箭师职业僵尸村民一次出生空手发弩；实际主副手射击、已装填后追赶离开射程目标、组件与装弹保存 |
| zombie_fisherman_rod | ADAPT / L2 PASS | 仅渔夫职业僵尸村民一次出生发真实竿；原生浮漂碰撞、钩住同步、拉拽和实际位移、收竿耐久 |
| drowned_fishing_pull | ADAPT / L2 PASS | 原版溺尸本来就能自然持竿，本模块不再发竿；水陆射手、非水下目标、三叉戟在途优先及钓竿闭环 |
| zombie_cleric_potions | ADAPT / L2 PASS | 一瓶真实HARMING喷溅药水，消耗后原生碰撞给受伤亡灵回血；不覆盖原生敌对目标、不无限复制、不伤害无关活体 |
| G38 | 限定客户端 PASS | 真实归属包、装填/持弩模型、钓线、cast与禁用同步；成年/幼体及手组合探针通过。肉眼视觉 NOT RUN |

## 实现与兼容决策

入口为ProfessionState.bootstrap、ProfessionBehavior.register，代码位于rebuild/profession；四个common Mixin处理出生、Zombie.tick退出、FishingHook原生链及自有药水过滤，五个client Mixin处理两类模型、持弩姿势、线与cast。没有启用旧S2服务或通用LivingEntity AI，26.3-tests严格只读。

API以当前26.3原版JAR的javap与隔离Vineflower审计为准。Java25.0.3、Gradle9.7.1、Loom1.18.3、官方未混淆命名、Loader0.19.5、API0.162.0+26.3、Fzzy0.7.7+fix3+26.3、Kotlin1.14.1+kotlin.2.4.20均保持；没有依赖、映射、版本范围或配置字段升级。MIT与上游版权/NOTICE保留，原始CC0残留仍是历史审计事实。

出生账本profession_rebuild v1只在原生finalizeSpawn后给对应职业的空手一个物品，主手占用则尝试空副手。保护预先存在的实际物品、耐久、名称、附魔和掉率；16个高难度原生出生种子验证了外部手持物品组件不变。关闭、占槽、无职业都是最终出生结果；LOAD、转换、跨维度、职业改变、丢失装备均不补发。未知版本保留原数据、不给新Goal。成年和幼体职业边界都有原生出生证据。死亡走原版实际装备掉落，没有隐藏副本。

实际GoalSelector添加窄MOVE/LOOK Goal，保留原近战/目标链。弩使用原版Monster弹药规则和CrossbowItem射击，不制造备用弩；装填阶段和真实手中实例绑定，退出不清空已装弹药或有效敌对目标。8格射程、视线预算和20～39tick射前等待；已装填弩在目标离开射程时继续原生寻路。相对上游的全局Goal覆盖与后撤分支，本批采用限定模块状态机，不宣称全部走位分支等价。

钓竿6～13格LOS、初始与再次60tick世界冷却，真实浮漂最长约60tick，收竿消耗实际竿3耐久，命中才拉拽。自有浮漂有同步来源标记，不让另一个同owner的未标记浮漂夺取运行状态；玩家钓鱼走原版。客户端归属与标记分包到达时只留最多3tick等待窗口。职业/手中实例/目标/NoAI/禁用/死亡/卸载退出，跨保存不恢复在途动作；溺尸不钓水下目标，在途三叉戟优先。不生成鱼、经验或奖励副本。

上游牧师实际用HARMING治疗亡灵。本批保留这个意图，改为有限真实消耗：初始60tick、预备20tick、冷却60tick、12格LOS，最多检查32个附近Monster候选，选受伤亡灵。真实药水组件参与原生效果；独立支援目标不覆盖敌对目标。自有药水记录owner/recipient/60tick有效期，只作用于仍有效的指定受伤亡灵；未标记的玩家/女巫药水及未知版本不走该过滤。

## 实际验证

最终命令 `./gradlew.bat build qaJar -PtestEula=true --console=plain`，build-11 **272项必需GameTest全部PASS（原231 + 新41）**。使用实际Server Tick、原版GoalSelector、实际物品/伤害/碰撞/寻路/死亡/转换与原生序列化；没有直接调用Goal或entity.tick。数量不是行为验收依据，逐场景证据在[after.json](../../docs/rebuild/evidence/r4e2b/after.json)。

- 弩四种手组合观察真实使用tick、箭和1000HP目标扣血；装填后追赶目标产生位移与实际命中。创造/旁观、目标死亡、NoAI、替换物品、禁用原近战回退及已装弹保留通过。
- 渔夫/陆地溺尸/水中溺尸的原生碰撞、钩住和拉拽产生超过0.2格实测位移，真实竿磨损；遮挡/近远距离/水下目标/三叉戟互斥、退出/重载/死亡守恒通过。未标记的外部浮漂不能抢占自有浮漂指针。
- 牧师真实瓶子消耗、900HP亡灵回血、外部强伤害药水组件、活体/满血过滤、敌对目标不改、关闭前摇保瓶及消费后重载无补发通过。
- 独立QA专服qa-v2：五个新场景及旧door/skeleton，spawn/inspect/check/off/cleanup和地形/实体退场PASS。目标有必要的运动物理，NoAI不再被误用来验证拉拽位移。QA包未打入正式包或源码包。
- 实际客户端client-v5：16个两类成年/幼体持弩夹具、8个持竿夹具，四种手组合的原生模型差异、真实owner/钩住包、线/cast、关门同步PASS；旧弓动画/蘑菇颜色与门盾16个渲染坐标样本PASS。探针独立于发行包。
- 独立专服target-v2与v3正常保存、退出、冷启动，五个实体UUID/实际装备/冷却前后相同；v2覆盖已磨损竿，v3在原生Goal真实装弹后冻结，覆盖已装填弩；已消费药水不补发。NoAI冻结保存输入用于核对状态，不冒充冷启动后继续战斗验收。
- 旧门盾、备用木剑不额外掉落/外部物品保留掉落、实际转换和其他R4行为随最终全套回归PASS。旧工程169个追踪文件哈希、HEAD、分支、工作树状态均与开始一致；原上游JAR与恢复源码哈希未变。

失败日志全部保留。compile-1使用过时swing签名，后按26.3改为swingForAttack；build-1钓竿目标NoAI不运动、猪灵tick0受击时间戳；build-2访问了不存在的目标选择器getter，改用已有已验证Accessor；build-4序列化夹具不等同真实磁盘fromDisk标记，改核实际无补发，另由专服冷启动补证。client-v1探针不合理排除近手短线，v2发现使用包先到造成一帧姿势错误，v3发现非玩家返回分支跳过TAIL，修后v5通过。build-6/7/9暴露旧测试随机输入：白天狙击验收混入随机走位、空手夹具被真实装备池发武器、未固定掠夺者索敌及恼鬼游走RNG。只固定相应前置，不删除旧断言、不开正式概率作弊门；build-11全套通过。

## 上游对照、边界和交付

两版官方JAR恢复源码核验PASS。Pinned Beta26.2 upstream-v2的限定专服实测：职业制箭师与受击猪灵真实弩伤害、两个持竿生物真实浮漂、牧师对受伤亡灵回血；上游牧师投后仍保留原瓶，本项目消耗一次为明确安全ADAPT。对照使用显式既有真实装备/职业、不同Minecraft版本，不是自然出生分布或完整L3量化等价。完整L3、人工肉眼/GUI、自然刷怪分布、多人/整合包/第三方AI武器/长期性能 **NOT RUN**。Allay历史BLOCKED未重试，本批无新增BLOCKED。

产物在build/libs：buildup-mob-tweaks-1.0.0.jar、-sources.jar、独立-qa.jar；字节数/完整SHA256见[after.json](../../docs/rebuild/evidence/r4e2b/after.json)。证据与脚本位于外层docs/rebuild/evidence/r4e2b及docs/rebuild/scripts/r4e2b_*，不属于26.3 Git根，本地提交不包含外层计划/矩阵/日志。

[自检](R4E2B_CODE_REVIEW.md)：PASS WITH RISKS，未发现BLOCKER/MAJOR；UPDATE_NOTES追加职业与独立QA两条已验证记录。本地提交依AGENTS§8授权，不代表人工阶段验收。[人工清单](R4E2B_ACCEPTANCE_CHECKLIST.md)待用户实机确认。

下一建议仅E2-c四项：illusioner_swap、illusioner_shoot_pause、illusioner_clone_arrows、vindicator_support。须先核定真实分身/箭预算、阶段互斥、同队支援与完整袭击场景，写具体方案；本轮未实施。
