# E3-a 骑乘弹体安全补充报告

2026-10-10；用户明确选择保留为Buildup安全修正，原产品裁决阻塞解除。目标仅26.3/分支26.3，起点干净 `d860821236dc424ef82edb74a64738e4f464e33a`；不进入下一组、不恢复旧S2、不推送/发布。亡灵马日烧继续Phase2 DEFER，原生尸壳矛继续REPLACE。

## 实现与范围

新增 `RiderProjectileMixin`，在实际 `Projectile.canHitEntity(Entity)Z` HEAD 返回false，**仅**服务器AbstractArrow（包括原生箭/三叉戟）和存活、非NoAI的Mob射手；射手必须当前处于骑乘树。每次使用原生 `isPassengerOfSameVehicle`，不缓存旧坐骑，不取消hurt、不设置实体无敌，不改其他弹体/玩家射手。主模块门、射手排除类型标签、vanilla_ai、disable_rider_friendly_fire、已知附件版本均检查；未知已存状态不接管。

原版 `leftOwner=true` 后放开同树候选；本项安全门在这个阶段仍限制**当前**同树。下马、换乘、死亡或NoAI即时取消新增保护，敌方树/原坐骑仍由原版碰撞处理。不同树上的其他骷髅骑士不因为同类而免伤；其他已独立实现的防护功能仍有自己的门。本项独立于traits门和骑士抽签，不需要特性入选，也不生成坐骑/装备。现124声明、**87支持/37未生效**，配置v10不变。

来源明确为 **LEGACY_ONLY → Buildup ADAPT**，不是上游功能恢复。两版PersistentProjectileEntityMixin来源哈希见前批 `rider-source-audit.json`；原Projectile/Entity完整字节码沿前批native.txt，补读真实 Arrow/arrow.ThrownTrident 构造和命中签名见新证据目录 `../../docs/rebuild/evidence/r4e3a-rider/`。环境仍MC26.3/official names、Java25.0.3、Wrapper9.7.1、Loom1.18.3、Loader0.19.5、API0.162.0+26.3，依赖与AW未升级。

## 真实行为验证

新增14个GameTest，不调用Goal、实体tick、canHitEntity或onHit：原生箭/三叉戟先从射手眼部实际飞离，保存读取原生LeftOwner=true，再将**同一个实际弹体**摆到明确的坐骑碰撞路径；由正常世界弹体Tick结算。正例保持马1000HP，负例箭降至998.08/999.08HP、三叉戟992.128HP；关闭、vanilla_ai、NoAI、无主、下马、换乘、敌树及玩家射手均保留实际命中。独立原版弓Goal自然索敌并真正发出一箭，再以其真实LeftOwner状态定向返回，确认当前坐骑安全。上述都是定向输入，不代表自然战斗中每箭轨迹/概率通过。

第一版完整构建失败6项，全部保留：四项低高度箭候选夹具无法证实马伤害；将路径明确置于马下身、避开骑手重叠后严格HP断言通过，不改碰撞实现或伤害。自然Goal夹具过早要求2tick必定LeftOwner，改成观察真正LeftOwner再返回。既有马接触测试同时可能转近战/箭伤，在专项夹具独立关闭切剑/狙击并设攻击4以隔离冲刺，不修改正式动作和成功命中要求，仍要求charges=1/hits=1。精确历史失败原因不通过这些调整冒充已证明，前批猪灵和客户端记录也保留。

`build-2.log` 全350必需测试PASS（前批336+14），全10个E3 QA场景正常Tick；最后补强 `returnedArrows>0` 的严格QA断言，最终命令/结果见 `verification.json` 和build-3.log，不使用之前PASS替代最终。

**最终build-3：350 required PASS、BUILD SUCCESSFUL、qaJar成功。** 独立QA专服qa-1也限定PASS：rider在320tick返回6发、skips=73、马HP1000；off后370tick返回7发、skips仍73、马HP997.06。地板/屋顶及夹具retired标记通过。console跑9场景，GameTest跑完整10场景并以实际生存玩家触发trap。专服flat生成设置的No key layers提示沿用前批且保留，不算无错世界生成审计。

## QA与人工

独立QA现在原25+E3十场景=**35场景**。新增 `/buildupqa e3 spawn rider`：已存在Skeleton骑马、马NoAI只固定路径，射手原版Goal实际射箭；工具等实际LeftOwner后返回**原箭**至马下身，不复制箭。check显示returnedArrows、projectileSkips、vehicleHP及原Goal/UUID；projectileSkips是候选过滤查询数，**不是阻挡箭数量**，必须同时看到returnedArrows>0和马1000HP。场地/会话/清理/权限规则不变，正式与源码包无QA。

统一清单 [R4E3A_ACCEPTANCE_CHECKLIST](R4E3A_ACCEPTANCE_CHECKLIST.md) 增加rider路径。人工关闭后需让一发实际离开发射者的箭返回才可见原版伤害；正常向前射击本来不碰马，不能以“关门仍没伤马”判失败。下马/换乘、敌方骑士、玩家射手、普通近战仍有伤害。现在产品决定全部已落实；完整E3-a **开发范围已覆盖**，停止等待统一人工验收，不自行开始E3-b/c。

自检沿CODE_REVIEW：窄精确注入、server-only判定、不依赖客户端类、无新配置版本/依赖、真实运行注入验证；结论和最终包哈希/旧169文件/许可来源校验见verification.json。UPDATE_NOTES追加本次窄安全变化。人工/完整L3/自然分布/全部出生与卸载冷重启组合/多人整合包/长期性能仍NOT RUN；本项无已找到上游对应，不能编造1:1运行对照。
