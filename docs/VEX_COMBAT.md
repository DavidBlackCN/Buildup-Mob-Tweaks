# S2-C2 — 恼鬼冲刺

独立重设计 VEX-01/02/03，目标 Minecraft 26.3。S2 已新增 VEX-04 弹射物伤害 ×1.5、雪球伤害 2 的独立开关。当前没有新增物品、附魔、属性强化、方块破坏或全世界扫描。

## 行为与配置

三个功能均默认开启，经 FeatureRegistry 查询 general.enabled 和各自开关；不依赖 Traits。

| hostile.vex 字段 | 默认 / 范围 | 行为 |
|---|---|---|
| fixedCharge | true | 原版 start 采样目标眼睛位置；只屏蔽 tick 中三格内重新瞄准。不会瞬间纠正已有速度方向，因此轨迹可能受惯性影响 |
| recoveryPause | true | 冲刺停止时清除一次自身动量，默认暂停自主飞行一秒；不逐 tick 清零外部击退、不增加无敌 |
| closeRangeGuard | true | 只有超过 minimumChargeDistance 才允许原版随机起手；不取消已经进入近距离的冲刺 |
| recoveryTicks | 20 / 10–60 tick | 影响之后预留和结束时写入的冷却；20 tick = 一秒（服务器正常 20 TPS 时） |
| minimumChargeDistance | 3 / 2–6 格 | 下次起手立即使用；关闭 closeRangeGuard 后仍有原版两格限制 |

fixedCharge 或 recoveryPause 任一开启时，起手记录目标 UUID，冲刺最多 40 tick。换目标、目标失效或超时会结束本次攻击，保留 AI 当前目标，不向新目标追加碰撞攻击。结束后只有 recoveryPause 控制是否停顿；关闭 fixedCharge 不会连带关闭停顿，关闭停顿不影响固定落点。

恢复期间移动控制器不执行 AI 加速，并清除游走 Goal 写入的目的地；不每 tick 清零速度，因此受到击退仍可移动。仅近距离开关开启时，不额外限制原版冲刺持续时间。正常命中仍调用原版 doHurtTarget、保留武器/难度效果、原版音效与红色冲刺状态。没有新增前摇动画；视觉可读性与平衡需人工判断。

GUI 应用开关后下次查询生效。暂停关闭立即不再拦截飞行，已保存冷却保留；重新开启时仍未过期的冷却继续有效。总开关关闭使本批注入走原版分支，但 Mixin 仍装载，不能靠开关解决其他 Mod 的装载级冲突。已在全关状态起手的冲刺没有运行态截止时间，启用后的最长时限从下一次起手建立。

直接编辑 TOML 请先停服，保存后重启；原版 /reload 不重读配置。配置由 v6 升为 v7，仅增加 hostile.vex，保持旧字段值和实体旧附件。

## 持久化与回退

- 新附件 `buildupmobtweaks:vex_combat`，CompoundTag version=1，`recover_until` 为服务端 gameTime 截止时间。与 Traits、raid_combat 和 Fzzy 配置版本相互独立。
- 冲刺运行态（目标 UUID、active、结束时间）不序列化。开启停顿时，起手先预留「当前时间 + 40 + 配置停顿」；正常结束后改为「结束时间 + 配置停顿」。默认在冲刺中重载最多剩余 60 tick 恢复，不重开攻击。最长配置下为 100 tick。
- 卸载期间游戏时间继续推进，已到期后无需额外等待；停服期间游戏时间不推进。不会以墙上时钟计算。
- 停顿开启且预留未过期时，从进行中的冲刺载入会清除保存的冲刺动量；不还原原攻击目标。冷却结束后由原版选敌和 Goal 恢复战斗。
- 不删除未知版本附件，不重写未知字段；未知 version 时三个策略全部退出，使用原版行为。关闭功能不删除存档数据。
- 本批只使用当前实体状态，常量时间判断，不统计恼鬼总数。S2 已升级为世界持久所有权索引，见 [S2_COMBAT](S2_COMBAT.md)。

诊断命令：`/buildupmobtweaks vex <单个实体选择器>`，等级 4，只读 UUID、附件与三个配置门控。gates 是配置开关，并不表示某个未知数据版本的实体实际启用策略。其他实体只显示 null 附件，不创建数据。

## 已核实的接入点

依据本机已解析的 26.3 Minecraft common JAR，使用 javap -c -p 核对；没有把上游发行 JAR 当作源码复用。

| 目标 | 描述符与注入 | 作用 |
|---|---|---|
| Vex$VexChargeAttackGoal | canUse()Z HEAD | 附加恢复/起手距离限制，不替换原版随机判定 |
| 同上 | start()V TAIL、stop()V TAIL | 原版状态建立后记录；停止后结算一次冷却 |
| 同上 | canContinueToUse()Z HEAD、tick()V HEAD | 超时与换目标保护，避免中途改目标命中 |
| 同上 | tick()V 内 MoveControl.setWantedPosition(DDDD)V Redirect | 仅屏蔽冲刺中重新瞄准，start 中原始目的地不变 |
| Vex$VexMoveControl | tick()V HEAD | 冷却期间禁止自身飞行加速 |

两个 Mixin 的外部实例字段均为 `Vex this$0`；common Mixin、JAVA_25、defaultRequire=1。没有 Access Widener、反射或新增生产 Goal。其他 Mod 若重写这两个内部 Goal/控制器，仍可能冲突，需要按具体版本验证。

## 验证与人工步骤

自动证据见 [S2-C2 报告](S2C2_REPORT.md)。实际 Goal、控制器、保存读回和服务端测试覆盖代码边界；客户端启动不等同于视觉和多人验收。

- 在开阔区域以生存玩家引诱恼鬼冲刺，红色起手/声音后横移。预期它继续飞向原位置、结束后约一秒暂停；失败现象为持续拐弯追踪或反复无间隔命中。
- 单独关闭 fixedCharge：预期恢复原版近距离追踪，停顿仍有效；再单独关闭 recoveryPause：固定落点仍有效，结束后可以游走。
- 在约 2.5 格且未冲刺时观察：默认不再起手；仅关闭 closeRangeGuard 后按原版随机条件起手，两格以内仍原版不启动。
- 暂停期间攻击恼鬼：预期正常受伤，外部击退不被每 tick 强行抹除；确认没有无敌或视觉悬挂。
- 冲刺/停顿时存档退出重进，预期不重放旧攻击，剩余恢复结束后可继续作战；无永久停止或冷却归零漏洞。
- 独立服两位玩家交替吸引、目标退出/死亡、召唤者死亡、不同难度、复杂墙体和高密度袭击需补测。原版穿墙不改，故不新增破坏方块路径；本轮不证明第三方领地/AI/优化 Mod 兼容或性能无回退。