# R4 验收反馈：门盾与苦力怕箭命中（2026-10-09）

范围仅为本次两项验收反馈；目标 `26.3/`、分支 `26.3`，起点 `e33c2a0`，开始时工作树干净。不进入 R5/S3，不推送或发布。外层用户记录 `R4-record.txt` 保留原样。

## 结论与实现

| 项目 | 结果 | 证据范围 |
| --- | --- | --- |
| 门盾两半浮在头部、重叠 | CONFIRMED → FIXED | 修复前实际客户端两半标记/编号均 null、都在脚上方 2.0125 格；修复后真实渲染状态分别为脚部和脚上方一格、横向坐标一致 |
| 苦力怕首次普通箭不能嵌入 | NOT REPRODUCED | 实际 Server Tick 首箭 HP 100→98、嵌入数 0→1、原弹体移除；禁用回退相同 |
| 受伤冷却中连续等伤害箭反弹 | PASS（原版规则） | 实际第二箭速度反向、嵌入数仍 1；冷却后第三箭 HP 18→16、嵌入数变 2 |
| 苦力怕身体显示可见嵌箭模型 | 清单预期已纠正 | 原版 CreeperRenderer 仅有 CreeperPowerLayer；原版 ArrowLayer 限定 PlayerModel，不支持苦力怕模型 |

用户随后补充“普通弓、有伤害、没看到嵌入、刷怪蛋召唤”，与原版显示限制一致；该补充不证明实际嵌入数为零。苦力怕生产逻辑不修改，保留原版无敌帧/穿透规则；不新增可见嵌箭渲染玩法。

门盾根因是 `ZombieState` 延迟加载。精确 Fabric Attachment 2.2.31 源码表明配置阶段按当时已注册的可同步附件协商支持集合；门盾类型后注册，客户端收不到门盾标记和上下半扇编号，定位 Mixin 回落至原版乘客位置。现在 common 初始化调用 `ZombieState.bootstrap()`，确保加入协商前注册。保留附件 ID/Codec/存档格式、材质、位置算法、独立开关和已有 Mixin 注入点，不引入客户端类到专服。

存档重启复验又复现了门盾清理的 `ConcurrentModificationException`：ENTITY_UNLOAD 中立即 discard 门盾，修改了原版 PersistentEntitySectionManager 正在遍历的实体区段。现在加载的遗留展示实体和卸载的旧门盾进入身份集合，卸载时标记为非活动并释放本模块引用，正常运行在 END_SERVER_TICK 处理；关服释放队列引用，原版保存的遗留乘客在下次加载后退役。死亡/禁用仍即时清理。既有重载测试调整为断言下一真实 Tick 已删除遗留展示实体，同时保留原耐久、两半、无孤儿/重复、禁用、死亡检查。

## 实际验证与失败历史

版本：Minecraft 26.3 / Java 25.0.3 / Gradle 9.7.1 / Loom 1.18.3 / 官方 unobfuscated 命名（无 mappings 配置）/ Loader 0.19.5 / Fabric API 0.162.0+26.3。依赖与构建配置未改。

- 目标目录，`JAVA_HOME=C:\Program Files\Java\jdk-25.0.3`，执行 `./gradlew.bat build -PtestEula=true --console=plain`：最终 `build-4.log` **PASS，135 个必需 GameTest 全通过**（原 131 + 新 4）。新增测试使用实际 Server Tick、原版 Arrow 飞行/碰撞、实际引信爆炸；不直接调用 Goal/onHitEntity，不设置嵌入数代替命中。
- 普通箭真实命中后的爆炸释放一个实际不可拾取箭；全关回退零散射；穿透箭实际扣血但嵌入数为零、零散射；连续等伤害箭原版反弹后恢复：**PASS**。
- 隔离客户端 `before` 正常冷启动登录，复现附件缺失/原版座位偏移。`after` 和 `rotations` 从各自世界副本重新冷启动登录，标记/半扇编号正常。`rotations` 在真实 Client Tick 抽取原版渲染状态，12 个样本覆盖两个半扇及多次转向，对位置和朝向作运行时断言：**PASS**。诊断 Probe 为独立开发 Mod，不在发行 JAR 中；没有操作或覆盖用户世界。
- `reload` 从保存的 `rotations` 世界副本重新登录：坐标正确但关服异常，生命周期验收 **FAIL**，即使 Gradle 退出码为零也不能算完整通过。修复后 `reload-fixed` 从独立存档副本重新登录，24 个旧/新门盾样本坐标断言通过，所有维度保存、正常退出，无集合修改/关服异常：**PASS**。诊断脚本同步加入关服异常检查，并修正历史脚本写入 simulationDistance=4 不符合本版最小5的环境选项；之前的选项错误日志保留。
- 既有门盾配置禁用、死亡/转换清理、装备保存等实际行为回归随完整构建通过。旧工程 169 个受跟踪文件及 Git 状态/HEAD/分支/远端前后快照一致；发行包/源码包哈希、包内容检查和 `git diff --check` 见最终核验。

失败记录全部保留：`before-gametest.log` 是新夹具误用私有穿透方法；`before-gametest-2.log` 是穿透夹具加载后位置复位和爆炸箭 owner 过滤错误，已修正，另有一项既有骷髅日间转换随机测试失败。`build-1.log` 有一项既有尸壳路线变更随机测试失败；`build-2.log` 是新测试括号错误，已修正。`build-3.log` 在注册修复后135项通过；发现上述关服异常并修复后，以 `build-4.log` 及 `reload-fixed` 为最终依据。没有移除测试、放宽断言、延长截止或修改上述历史随机测试；此历史仍说明旧夹具存在随机敏感性。玩家弓 UI 操作不能视为自动测试已执行。

## 产物、自检与限制

- 发行包 `build/libs/buildup-mob-tweaks-1.0.0.jar`，源码包同目录 `buildup-mob-tweaks-1.0.0-sources.jar`。
- [最终核验](../../docs/rebuild/evidence/r4-feedback-1/verification.json)、[外层工作报告](../../docs/rebuild/REBUILD_REPORT_R4_FEEDBACK_1.md)；原始日志在外层 `docs/rebuild/evidence/r4-feedback-1/`，目标/Fabric 反编译在外层 `reverse-engineering/26.3-r4-feedback-1/`。
- Code Review：**PASS WITH RISKS**，无 BLOCKER/MAJOR；按 CODE_REVIEW.md 检查完整差异，附件生命周期与 client/common/server 边界保持一致。Update Notes 仅追加已验证门盾修复。
- 用户原整合环境复验、人工肉眼、新的独立专服/客户端连接、上游 JAR 实际箭飞行新对照：**NOT RUN**。客户端坐标断言不替代人工视觉验收；“首次命中实际嵌入失败”在隔离环境未复现。
- 外层共享报告、脚本、证据不属于 `26.3` Git 根，不包含在本地 commit 中；`26.3-tests` 严格只读。

## 人工复验

- [ ] 使用当前发行包，完整退出客户端再加入；既有和新生门盾两半连贯，底部贴近脚部，无头部重叠；绕行并观察移动、转向。
- [ ] 重新登录/重启后门盾仍正确；正面挡伤、侧后扣血、破盾、禁用和死亡后无残留。
- [ ] 普通弓无穿透单箭首次命中确认扣血；连续命中间隔至少一秒，随后引爆观察不可拾取散射箭；关闭嵌箭散射后正常扣血/爆炸但不散射。
- [ ] 如首次单箭确实反弹且不扣血，保存弓/弩、其他 Mod 与日志信息；身体没有可见箭模型不能判定嵌入失败。
