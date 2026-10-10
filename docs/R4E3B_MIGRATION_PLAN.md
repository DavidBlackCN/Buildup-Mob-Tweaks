# R4-E3-b 实施方案

用户已授权整 b 组按上次要求直接实施。唯一目标 26.3，基线 d3af067；不进入 E3-c/E4/Boss/R5/后续 Phase，不推送发布。

环境保持 Minecraft 26.3 / Java 25.0.3 / Wrapper 9.7.1 / Loom 1.18.3 / 官方未混淆命名 / Loader 0.19.5 / Fabric API 0.162.0+26.3。配置 v10→v11：只在既有空 passive section 添加九个独立默认开启字段；保留旧字段和值，不迁移实体旧附件。

| 单元 | 新 ID | 确定的改造边界 |
|---|---|---|
| b-1 | squid_ink_defense | 原生 spawnInk 触发；8格球形，最多16个非鱿鱼有效生物，100tick冷却；普通失明/发光鱿鱼发光，距离递减；创造/旁观排除；两路径共用开关 |
| b-1 | glow_squid_gaze | 原生 aiStep 每60tick，16格、最多8玩家，视线和注视角，夜视600tick；不全维度扫描 |
| b-1 | chicken_threat_escape | 原版 Goal 调度，玩家8格/怪物6格；优先级5/6，速度.8/1.2；手持鸡食物玩家不触发，保留优先级更高的喂食/繁殖/惊慌 |
| b-2 | animal_group_panic | 仅鸡/牛/羊/猪/兔原生 PanicGoal.start 的真实伤害源传播；16格、同种最多8、200tick冷却；短60tick一次信号，由独立 Goal 逃离，不递归调用原 PanicGoal |
| b-2 | tameable_recovery | 原生 TamableAnimal 及已驯服有主 AbstractHorse，有活跃同维度主人，伤后等待200tick，再每100tick恢复1；满血/战斗/火/乘坐或被骑乘/未知附件/排除Tag退出；不复活、不改主人，不屏蔽原版马回血 |
| b-2 | cat_projectile_dodge | 原生 Goal，8格最多8真实移动投射物，预测临近轨迹；8tick横向小跳、40tick冷却、安全加载落点；坐下/坐骑退出，不免伤、不销毁弹、不生成物品 |
| b-3 | goat_horn_aggro | 成功开始原生 InstrumentItem.use 后，24格、LOS、最多16原生Monster，只替换空目标/其他玩家目标；当前仇恨60tick到期/使用结束/禁用退出；不抢NPC目标 |
| b-3 | trader_self_defense | 受真实伤害后原生 Goal 20tick前摇，对8格LOS有效攻击者失明100tick，200tick冷却；不制造药水，不改商人装备/普通交易/完整生活Brain |
| b-4 | sniffer_search | 潜行生存玩家持白名单物交互成年嗅探兽；只查询已加载区块；地下方块水平8/深度8、每请求32查询/tick、最多2601采样；生物群系每水平轴128/16步长、最多289采样、每请求8/tick，近处优先首个匹配而非严格最近；全服总64查询/tick、最多8请求、100tick超时、100tick请求冷却。停潜行/换物/离开8格/退出/死亡/卸载/禁用或嗅探兽离原点4格取消；无同步findClosestBiome3d、无异步世界访问；原Brain保留，结果只指向60刻/粒子/消息，无消费或掉落 |

来源：两版官方恢复 Squid/Chicken/EscapeDangerGoal/Cat/WanderingTrader/Sniffer/LivingEntityMixin 和 MobAITweaks；MIT署名保留。源与旧工程文件哈希、26.3官方完整描述符/字节码见外层 docs/rebuild/evidence/r4e3b。实现前已核对原生注入方法和事件链。初次 javap 缓存访问拒绝属于环境问题，随后只读授权运行通过，失败尾记录保留于 native-signatures.txt。

新生态附件 v1 只存伤后等待和冷却；未知版本原样保留且不运行。临时传播/请求/目标均不跨载入恢复。独立 QA 新场景不得进入正式包或源包；实际 Server Tick/Goal/Brain、负向关门、序列化重载、专服以及可能的上游对照分别记录结果。编译不能替代行为验收，人工/客户端肉眼/完整 L3/自然分布/多人性能另列 NOT RUN。
