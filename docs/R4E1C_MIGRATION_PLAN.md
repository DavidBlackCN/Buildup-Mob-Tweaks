# R4-E1-c 实施方案与环境核验

本轮用户授权“确认方案后无需停下等我确认，可直接开始工作”，据此核验后直接实施；批末验证、自检、本地提交并停止。只修改 `26.3` 和明确相关共享台账/证据，`26.3-tests` 只读。

## 环境与基线

分支 `26.3`，起始 HEAD `99f8eb319965f923eee9a7c215f3a423c64e4840`，工作树干净。MC 26.3 / JDK 25.0.3 / Wrapper 9.7.1 / Loom 1.18.3 / 官方未混淆命名（无 Yarn）/ Loader 0.19.5 / Fabric API 0.162.0+26.3 / Fzzy 0.7.7+fix3+26.3 / Kotlin 1.14.1+kotlin.2.4.20。保持依赖、Wrapper、Access Widener、配置 v9 和旧 S2 编译隔离。

## 来源纠正与冻结范围

- `skeleton_offhand_visual`：ADAPT。上游 `RangedBowAttackGoalMixin` 对拉弓触发另一只手的挥动。26.3 使用 `LivingEntity.startUsingItem(InteractionHand)`、实际 `getUsedItemHand()` 和 `swingForAttack()`，只在一次新的有效拉弓时挥另一只手，不复制箭或装备。客户端使用服务器同步门控及真实使用手；原版 HumanoidModel 已按实际手绘制弓和 SwingDescription，无需重写模型或改变 mainArm。
- `bogged_omen_mushrooms`：ADAPT。在现有沼骸 DODGE 成功阶段内投出红/棕两枚原版 Snowball，不增加 Goal。参考两版 `SkeletonSpecificGoal`：目标具有 BAD_OMEN 或 TRIAL_OMEN；两枚随机小速度弹体，重力 .05（普通雪球 .03 保持）；命中伤害 difficulty+1，碰撞产生 HARMING 云（20 tick、等待10、半径 .4、增长 .1、使用增长 .1、WARPED_SPORE）。不启用上游 OP/crazy 分支。绑定阶段目标并在发射时重查效果，规避上游 start 时快照变旧。弹体最大100 tick；每个沼骸最多2枚在途弹体/2朵新增云；目标退出、禁用、NoAI、死亡/卸载/重载均退役，不恢复在途技能。原孢子云和120冷却不变。
- `stray_omen_snow`：纠正为 LEGACY_ONLY / DEFER（Phase 2）。两版上游 FLIP 都没有 ominous 分支：beta 只有 Bogged 行480、正式版只有 Bogged 行502 使用 `ominous`；流浪者额外缓慢由 MOBS_ARE_OP 而非不祥驱动。旧 S2 `SkeletonExtras` 的 snow_omen 渐加速是自创实现。本批不得将其当上游恢复，ID 保持未生效，并纠正 E0 取舍依据。
- `skeleton_aim_fix`：REPLACE。验证现有原版 Walk/Sniper 看向控制、箭的实际命中和目标退出；旧重复瞄准 ID 不开启。
- `stray_snow_barrage`：REPLACE。验证现有 STRAY_JUMP_SHOT 的同一次跃射与三雪球，旧独立稀有 Goal/ID 不开启；不祥与普通目标均沿相同链，不复活 S2。

## 兼容性与验收

新两项经 FeatureRegistry、总门、精确类型、实体独立禁用标签、vanilla_ai、排除标签与未知附件版本共同过滤。装备不新增/复制，生成备用木剑既有死亡策略保持。模块云只作用于可攻击的非盟友，创造/旁观受保护。卸载回调只排队，不在实体索引迭代中 discard。

独立 QA 新增实际手拉弓、沼骸不祥/普通、流浪者同链场景；不进入正式/源码包。GameTest 用真实 Server Tick / GoalSelector 观察动作、弹体碰撞/云、门控和退出，不直接调用 Goal。运行原178项及新测试，实际客户端核验左右主臂×主副手四组合及服务器门控；尽可能执行上游隔离对照。未执行项标 NOT RUN，环境阻断标 BLOCKED，失败日志保留。门盾与备用剑原测试回归。

构建命令 `./gradlew.bat build qaJar -PtestEula=true`；完成后按 CODE_REVIEW.md 自检，追加已验证 UPDATE_NOTES，进行一次本地 commit，不推送/发布，不进入 E2 或后续阶段。
