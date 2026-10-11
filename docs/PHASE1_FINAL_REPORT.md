# Phase 1 / R5 最终工作报告

2026-10-11。用户已验收 R4-E4，授权 R5，指定先验证原版依赖组合，并将 Boss 基础批次延后至 **Phase 2 完成后**。本报告是限定范围的工程收口，不代表已发布或所有未知环境均兼容；最终人工确认见 [验收清单](PHASE1_ACCEPTANCE.md)。起点为干净的 `26.3` 分支 `a000afc`。

## 功能与实现决策

保留 **141 声明 / 121 支持 / 20 未生效**、配置 **v13**、独立 QA **71 场景**。旧 68 项取舍仍 PORT1 / ADAPT47 / REPLACE6 / DEFER10 / EXCLUDE4；取舍分类不是运行验收结果。没有恢复旧 S2 服务或 AI，没有新增 Boss、附魔、独立奖励或物品。G25～G29 五个 Boss 候选改为 DEFER，待 Phase 2 完成后再独立规划和审批；Allay 历史 BLOCKED 按用户要求不重试。

本次实际行为修复：26.3 原版 `MeleeAttackGoal.start()V` 会将攻击计数归零，导致疲劳效果间隔被目标/路径重启绕过。`E4MeleeMixin` 在同一 Goal 上保存临时的下一次攻击时间，并在原生 start 尾部恢复剩余等待，受独立开关、原生世界时间及 200 刻上限约束；关闭时清理并回退原版。无新增持久字段、额外攻击或手工调度。新增真实目标移除、原生 Goal 停止、重新自然取得目标的正反测试：启用 200 刻、关闭 40 刻；既有疲劳/急迫/关闭稳定间隔仍 30/16/20 刻。

配置修正只改变注释与双语说明。16 个未接入旧技能和 3 个演示 Marker 明确标为未生效；替代项指向现有实现，延期项不承诺开启即生效。保留所有字段、默认值及用户保存值，未升级配置版本。Traits 主开关实际管理已接入的高级出生能力，基础装备独立；历史 Marker 和对应权重不参与正式抽取。20 个未支持声明由 16 个旧技能、3 个 Marker 和库存用 TRAITS ID 组成；TRAITS ID 未注册为独立技能，不等于实际 traits.enabled 字段无效。更新启动日志与元数据描述以反映实际范围。

陷阱误伤测试原来固定高度可能未碰到骑乘实体；改为使用实际受害者高度发射原生箭，保留真实伤害和报复断言，没有取消失败用例或直接调用 Goal。

## 验证结果

| 项目 | 结果 | 实际证据与限制 |
| --- | --- | --- |
| Wrapper build / qaJar | PASS | 最终 `build-full-4.log`：BUILD SUCCESSFUL，全部 **504** 必需真实 Server Tick / Goal / Brain 测试通过 |
| 正式包生命周期 | PASS | `lifecycle-load-5` 实际 ENTITY_UNLOAD、实体与区块均离开内存，再从磁盘重载；`lifecycle-cold-5` 同一存档全进程冷重启 |
| 保存守恒 | PASS | 六个原 UUID、弓耐久 17、备用武器、鸡骑士关系、门盾血量 37.5、未知附件 v99 均保持；原生 Goal 投出的耐久 23 三叉戟始终只有一件 |
| 双客户端专服 | PASS（限定） | `network-4` 两个真实 KnotClient 同时登录原生专服；本地配置关闭被服务器开启值同步覆盖，门盾/幼体/冻结附件同步和关闭清理通过 |
| 战斗和玩家退出 | PASS（限定） | 联机期间原生 Goal 发出 4 支箭且实际扣血，切创造后退出目标；两客户端随后自动退出，所以结束日志 players=0 |
| 原生刷怪笼 | PASS（限定） | 同一联机测试内原生刷怪笼实际生成 6 个不同 UUID 的骷髅，非直接调用生成函数 |
| 三组性能 | 见 [性能报告](R5_PERFORMANCE.md) | 完整逐 tick 原始数据；不等同多人容量、刷怪塔或长期生产世界性能 |
| 正式/源码/QA 隔离与只读边界 | 见 [自检](R5_CODE_REVIEW.md) | 外部探针独立 JAR，正式包不含 GameTest、QA 命令或 Minecraft 类；依赖与历史资料校验见 verify.json |
| 本批真人最终体验 | NOT RUN | R4-E4 用户验收已通过；本次自动联机不冒充真人操作 |
| 第三方实际整合包 / ModMenu GUI | NOT RUN | 用户指定先测原版依赖组合；不宣称未知领地、武器、Boss 控制者兼容 |
| 完整上游两版本 L3 / 长期自然分布 | NOT RUN | 本批没有追加完整对照，旧有限证据仍保持原边界 |
| Allay 恢复 | BLOCKED（历史） | 已批准不再重试，不阻断当前限定范围 |

## 复现及产物

在 `26.3/` 执行的最终命令：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.3'
.\gradlew.bat build qaJar -PtestEula=true -I ../docs/rebuild/evidence/r5/full-4.gradle --console=plain --no-configuration-cache
```

正式包、源码包、独立 QA 包在 `build/libs/`；精确 SHA256、大小和 ZIP 隔离核验保存于 [artifacts.json](../../docs/rebuild/evidence/r5/artifacts.json)。安装正式依赖组合：Minecraft26.3、Java25.0.3、Gradle9.7.1、Loom1.18.3、官方未混淆名称、Loader0.19.5、Fabric API0.162.0+26.3、Fzzy0.7.7+fix3+26.3、Kotlin1.14.1+kotlin2.4.20；没有升级依赖或构建配置。

外层 [R5 证据目录](../../docs/rebuild/evidence/r5/) 保存命令、日志、配置索引、JSON 与采样；[脚本目录](../../docs/rebuild/scripts/) 的 `r5_runtime.py`、`r5_lifecycle.py`、`r5_network.py`、`r5_bench.py` 使用已解析本地依赖和真正的生产 JAR，由独立探针观察原生 Tick，不加载主工程输出类目录。重新运行使用新的编号，脚本拒绝覆盖已有运行目录。网络测试需要可用 Windows SDL/OpenGL 环境及现有 26.3 assets，可通过 `R5_ASSETS` 指定资产目录。离线身份仅用于 loopback 临时测试服务器。一次性 `r5_prepare.py` 不应重复执行。

最终成功命令为根目录 `python docs/rebuild/scripts/r5_lifecycle.py 5`、`python docs/rebuild/scripts/r5_network.py 4` 和 `python docs/rebuild/scripts/r5_bench.py 3`。网络与关闭配置基线取第一次成功 lifecycle-5 自动生成的完整 v13 文件，因此须先完成该生命周期运行并保留配置；首次干净复现流程见[证据说明](../../docs/rebuild/evidence/r5/README.md)。生命周期为加速保存/卸载检查设为100tick/s；网络及性能为正常20tick/s，不混作同一性能数据。不要在普通存档部署探针；内含固定夹具、保存和清理操作。失败尝试保留：先前测试夹具高度/近战间隔失败已修复后全量重跑；生命周期夹具等待区块就绪问题、客户端 SDL/资产权限/临时白名单问题已解决。性能前两版采样计数/实体清理输入不正确，不能用于最终性能结论。开发离线登录引起的 Realms 授权、系统 WMI 和保留后处理警告不视为行为或 Mixin 成功证据。

只读旧 `26.3-tests/` 和上游资料不变。按持续授权，仅对目标 Git 的相关代码、资源、文档和 UPDATE_NOTES 作一次本地提交。父目录计划、矩阵、脚本和证据 **不在该提交内**，需单独保留。完成后停止，不自动推送发布或启动 Phase 2。

未来待定事项见 [交接](PHASE2_HANDOFF.md)；当前兼容范围见 [兼容矩阵](COMPATIBILITY_MATRIX.md)。
