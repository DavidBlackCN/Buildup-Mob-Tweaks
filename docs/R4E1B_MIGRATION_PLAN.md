# R4-E1-b 已批准实施方案

用户在只读 Environment Report / Migration Plan 后明确回复“可以开始”。基线为 26.3 / cef3aa0，工作树干净，26.3-tests 严格只读。

仅实现 piglin_shot_swing、piglin_item_dodge、piglin_crossbow_compatibility，以及先复现后窄修的 zombified_piglin_anger_fix。僵尸猪灵出生发弩/使用弩仍属 E2；不进入 E1-c、Boss、R5 或 Phase 2/3。

环境保持 MC26.3、Java25.0.3、Wrapper9.7.1、Loom1.18.3、官方未混淆名称（无 Yarn）、Loader0.19.5、API0.162.0+26.3、Fzzy0.7.7+fix3+26.3、Kotlin1.14.1+kotlin.2.4.20，配置v9。参考已核验 CExkBTN8 / bFb56Zw2 的 PiglinEntityMixin、PiglinBrainMixin、RangedCrossbowAttackTaskMixin、ZombifiedPiglinEntityMixin；保留 MIT 版权声明。

| 顺序 | 单元 / 分类 | 实施与验证 |
| --- | --- | --- |
| 1 | 仇恨复现 / 条件 ADAPT | 原版 HurtByTargetGoal 没有同类排除。真实同类伤害与群体传播先取证；只在复现后阻止同类报复目标，伤害、玩家和其他攻击者警戒保留 |
| 2 | 弩兼容 / ADAPT | 不替换 Brain；补齐 Piglin.canUseNonMeleeWeapon、PiglinAi.hasCrossbow、CrossbowAttack 起止、ProjectileUtil 实际手及 BehaviorUtils 主副手射程。共享钩子仅精确 Piglin + 当前门控生效，原版矛支持保留 |
| 3 | 挥手 / ADAPT | 实际原版 CrossbowItem 射击后使用真实射击手，空射不计，关闭只退出新增挥手 |
| 4 | 后跳 / ADAPT | 成人出生仅一次70/1000；距有效 Brain 目标小于2格、地面正在蓄弩；最多10tick动作窗口，200tick冷却保存。水平冲量按速度属性×2，上限0.65，竖直0.2；碰撞、落脚/危险地面先检查，不反复累加速度 |
| 5 | 生命周期 / ADAPT | 独立附件，不复活 S2 Marker。未知版本保持不透明；载入/转换不重抽、不补装备。在途动作不恢复；目标退出/NoAI/禁用/死亡/卸载取消新增动作，原版 Brain 仍调度 |
| 6 | QA / ADAPT | 独立开发包增加猪灵弩/后跳/同类仇恨场景；强制只改已知出生状态，不改服务器配置。真实 Tick/Brain、弹体命中、组件/耐久/装备守恒、交易拾取、关闭回退、保存重启/卸载；全量原 R4 回归 |

物品策略：持有的真实 ItemStack 是唯一装备来源；不复制装备、不发弩、不注册正式物品。原版弹药机制保持；停止蓄力/换手时不通过空 getUseItem 写组件，不清掉已装填弹药。同步兼容状态供原版猪灵姿态读取，保留跳舞/赏金优先级；第三方武器仅承诺可验证的 CrossbowItem 继承路径。

完成 Wrapper build / qaJar、对应专服/客户端启动、可执行的上游有限对照、CODE_REVIEW 与 UPDATE_NOTES 后本地提交并停止。人工视觉、完整L3、自然刷怪器、多人/整合包/性能独立标记，不由自动测试推断通过。
