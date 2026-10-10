# R4-E2-a 装备闭环重建报告

用户授权核验方案后直接实施；[方案](R4E2A_MIGRATION_PLAN.md)限定本批。当前配置v10、124个ID中68支持/56未生效、独立QA17场景。只交付E2-a；不进入E2-b/R5/Phase2/3，不推送或发布。Allay维持历史BLOCKED，不重试。

| 项目 | 结果 | 边界 |
| --- | --- | --- |
| equipment_assignment | ADAPT / L2 PASS：真实资源存储→标签与权重→单次出生→实际空槽装备→原版Goal使用→转换/保存/死亡 | 精确支持僵尸、尸壳、溺尸、凋灵骷髅、僵尸猪灵；不保证所有第三方武器AI |
| wither_skeleton_bow 出生 | ADAPT / L2 PASS：原版出生武器setter前按池选真实弓；原版Bow Goal命中并消耗真实耐久 | 已有弓行为保持；不生成备用石剑/木剑，不给所有实体发弓 |
| zombified_piglin_spawn_crossbow | ADAPT / L2 PASS：空出生主手的真实稀有弩；未命中保留原金剑/金矛 | **远程AI仍NOT RUN/待E2-b**，本批不新增弩射击 |
| traits | REPLACE / 限定PASS：保留模块出生附件及既有traits.enabled门；装备独立于Traits | 旧Marker抽签、旧traits ID/服务不启用；不把REPLACE当新增支持ID |

## 来源与行为取舍

已核验CExkBTN8/Beta26.2和bFb56Zw2/1.21.11的WitherSkeletonEntityMixin、ZombifiedPiglinEntityMixin：上游出生弓10%、弩20%，局部有效难度>2.5，可用bows特定装备并显式附魔。Buildup沿用原已声明1%稀有意图，增加两个独立千分制概率（默认10），普通空槽池默认100；不保留该局部难度阈值、bows硬编码、附魔奖励链。属于明确ADAPT，不能宣称概率/难度/掉落与上游等价。

原版26.3 ZP有1/20金矛初始化分支。窄WrapOperation只处理两类populateDefaultEquipmentSlots中唯一的setItemSlot；不覆盖已有装备、默认原版武器路径保留，后续原版武器Goal重评保留。实际描述符及字节码见外层证据reference.json。没有新Goal或旧S2 AI/Mixin启用。

EquipmentPools v1格式不变：严格ID/Tag、权重、一个槽、一个默认物品，不接受数量、组件覆写或replace字段。增加两份默认出生规则；ZP规则允许原生幼体（上游无成年限制），普通僵尸示例仍仅成年。规则按稳定ID顺序，第一个匹配规则是最终选择，占槽或抽不中不尝试下一个规则。主手凋灵骷髅只接受实际原版弓、ZP只接受实际原版弩；其他槽遵守canUseSlot和原版装备槽。新装备排除类型Tag为equipment_assignment_excluded，并尊重vanilla_ai、disable_equipment_assignment及对应出生单项标签。

新实体只有一次尝试，成功、关闭、无规则、占槽、未命中都记录终结结果。LOAD/转换/跨维度来源不补发；未知附件版本原样保留。既有真实装备组件、耐久和掉率不改变，死亡只走原版装备掉落/游戏规则，没有第二份隐藏物品。配置v9→v10增加默认字段，旧布尔门及参数保留；配置变化和数据包重载不重抽旧实体。移除池规则/禁用总门不会删除已获得物品。

## 验证及证据等级

最终Wrapper命令：`./gradlew.bat build qaJar -PtestEula=true --console=plain`，build-8 **231项必需测试PASS（原207 + 新24）**。测试使用实际Server Tick/GoalSelector、原生spawn/finalize、碰撞/死亡/转换/序列化，不直接调用Goal或entity.tick。新池武器的原版近战目标1000→970；原生出生弓真实箭伤和耐久通过。

| 验证 | 结果 |
| --- | --- |
| 出生/独立门/空槽/组件/无规则/未知99/不补发 | L2 PASS；真实出生原因NATURAL/SPAWNER/SPAWN_ITEM_USE输入，不能替代实际自然刷怪器/刷怪笼分布 |
| 幼体ZP | 原生ZombieGroupData(true,false)、真实幼体弩及Tick PASS；不新增幼体模型或额外骑乘 |
| 默认概率样本 | build-8每类1000个原生成年NATURAL夹具，弓9、弩9、原金矛46；金矛使用原版随机源，各轮数量不同。不是自然刷怪统计，也不是精确概率证明 |
| 转换/死亡 | 原生convertTo保留账本、实际组件和.42掉率；实际死亡一件17耐久命名武器，零掉率及MOB_DROPS=false均无额外装备 PASS |
| 实际资源重载 | target-v2：原生标签池副手盾→改标签后新生副手弓；旧盾不变；saddle错误规则被拒绝，其他规则继续运行 PASS |
| 冷重启 | target-restart-v2：相同UUID、已获得弓damage17、旧盾/新弓及账本保留；先移除的弩主手保持空；v9输入配置实际保存v10 PASS。标杆为NoAI静止，不外推活跃跨维度战斗 |
| 独立QA | qa-v3使用最终QA JAR；wither_birth/zombified_birth各在最多512个未加入世界的成年原生候选内找到命中，默认实际163次，不改全局配置，不手动发弓/弩。真实Goal射箭、inspect/check/off/cleanup、地形和夹具退役 PASS；无规则/零概率不能伪造通过 |
| 客户端回归 | client-e2a-v1实际原版左右/主副手模型、同步门、两色蘑菇和门盾16坐标样本 PASS；肉眼/GUI专项 NOT RUN。此探针复验已有渲染，不冒充新出生分布证明 |
| 上游对照 | upstream-v1：pinned Beta实际原生出生弓/弩 PASS（限场）；明确将两概率调100%、阈值0，目标夹具为千分制1000；正式版仅源码对照，完整L3 NOT RUN |
| 包边界 | 正式/源码JAR无QA、GameTest、外部探针或原版类；QA独立Mod；旧26.3-tests的169个跟踪文件哈希一致，见收据 |

验证收据见外层[verification.json](../../docs/rebuild/evidence/r4e2a/verification.json)；[服务器脚本](../../docs/rebuild/scripts/r4e2a_servers.py)、[客户端脚本](../../docs/rebuild/scripts/r4e2a_client.py)和全日志在外层，不随26.3 Git提交。

## 失败记录与修正

build-1出生/掉落新路径已通过，但旧E1-a“无出生弓”断言不再适用默认新功能；给该特性采样夹具加单项equipment_assignment退出标签，完整原断言及旧Trait概率保持，E2-a另测真实装备。首次新云友军夹具出现额外2点失血：旧孢子POISON云和新HARMING云混在同场；后将已真实发射的蘑菇偏转到远离旧云的地面，保留原友伤/亡灵治疗/直接新云伤害全部断言，隔离本测试对象；不宣称旧毒云所有友伤已审定。build-2为QA局部var推断Monster与Mob返回类型不匹配，改显式Mob。build-3/4为228 PASS。build-5新增近战夹具将速度置零、尚未进入原版攻击距离而失败，修正为允许原Goal真实接近目标并保留伤害断言。审查还把QA候选设为原生成年的ZombieGroupData(false,false)，避免在Session创建前产生原版鸡骑士副作用；新增真实幼体单独验证。build-6为231 PASS。

收尾修正QA无效场景错误提示，改为从全部17个枚举生成列表后，build-7复跑出现三项失败。两个人工创建的出生装备夹具未设置持久化，存在远处测试玩家触发原版距离消失的风险；现在出生即检查实物，并固定夹具持久化，在Tick后增加存活/未移除断言和日志。旧Vex重载用例日志显示changed=false、charges=0，尚未触发冲锋/重载；补固定随机种子，继续由原版Goal实际启动，不直接调用Goal。build-8全部231 PASS，Vex真实重载后target=null/charging=false，两个出生夹具alive=true/removed=false。三份旧测试仅调整夹具输入，未删除或放宽原断言；其余旧测试Java不改。build-7失败完整保留，不用build-6掩盖。正式行为未因该复跑修改。

隔离flat世界缺少layers日志、上游旧advancement解析错误、v9新字段首次默认修复提示均保留；不宣称日志零错误。**NOT RUN**：人工发行实例、Config GUI、完整L3、实际自然/刷怪笼概率分布、多人/整合包、长期性能、真实活跃跨维度/区块卸载瞬间、任意第三方武器AI。**BLOCKED**：历史Allay，不重试。

自检见[CODE_REVIEW](R4E2A_CODE_REVIEW.md)，人工见[清单](R4E2A_ACCEPTANCE_CHECKLIST.md)。本批一次本地提交后停止。下一建议E2-b-1：先僵尸猪灵真实弩AI与职业制箭师弩，沿真实物品和原版Goal核验，不一次开展全部钓竿/药水/幻术师。
