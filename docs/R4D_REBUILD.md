# R4-D 行为重建与验收

用户于 2026-10-09 授权直接开始 R4-D，并在完成后统一人工验收完整 R4。本批实现恶魂、烈焰人、蠹虫、末影人及苦力怕、蜘蛛/洞穴蜘蛛、旋风人的选定机制；其余旧 S2 功能已列入 [剩余审计台账](R4_REMAINING_AUDIT.md)。本批实现通过限定 L2，不表示所有旧 S2 功能或完整 R4 已验收。不进入 R5/S3/Boss，不推送或发布。

## 实现与实际证据

核心参考为两版已核验官方发行源码 CExkBTN8 / bFb56Zw2。26.3 原版 JAR 的签名、选取源码和哈希位于外层 [source-evidence.json](../../docs/rebuild/evidence/r4d/source-evidence.json)、[target-signatures.txt](../../docs/rebuild/evidence/r4d/target-signatures.txt)。原版提供真实 Goal/Brain 调度、攻击、投射物、援军和爆炸；新模块限定技能预算与生命周期，不整体恢复旧 S2 入口。

| 单元 | 重建与主动修正 | 最终实际 Tick / native Goal/Brain 观测 |
| --- | --- | --- |
| G01 恶魂 | 实际原版 ShootGoal，40 tick 前摇、发射后 100 个完整安静 tick；保存世界冷却；独立 0.6 慢弹与同步蓄力。慢弹是 Buildup 设计，上游等价不成立 | 两枚真实 LargeFireball 间隔 140，accelerationPower=0.06；全部关闭间隔 60、power=0.1；实际墙遮挡、创造退出、NoAI 中断/完整前摇恢复、完整实体保存加载 PASS |
| B01 烈焰人 | 原版实际 SmallFireball 乘客；固定序号半环、每弹 40 tick 后瞄准释放；2/3/4 难度弹数；原版 MoveControl.strafe 横移 | 普通三枚唯一实体、峰值三乘客、三次释放及真实伤害；横移位移 15.62 格（175 tick 夹具）；专服简单/普通/困难峰值 2/3/4；创造、独立 orbit 禁用、死亡、递归乘客加载 PASS |
| F01 蠹虫躲藏 | 跳起→着地隐身侧移→有限时长恢复；不复制全伤害免疫，不以钻块替代躲藏，原版寄生仍保留 | 原版 JumpControl 起跳高差 1.252、实际隐身、期满原标志恢复；躲藏中真实伤害 1、禁用恢复、保存加载保留冷却 PASS |
| F02 蠹虫援军/铲子 | 躲藏期间延后而非永久取消原版援军；独立停步、按实际寄生块破坏发送粒子；直接主手铲子近战 2 倍 | 隐匿期间块未破坏，结束后原版实际释放一只新 UUID 同伴；一块/一粒子；停步关闭仍移动 2.46 格且援军正常；铲子命中启用/关闭 HP=6/7 PASS |
| N01 末影人 | 保留仇恨/中立生命周期，原版 MeleeAttackGoal 子类；默认 70/1000 首次自然/刷怪蛋出生选择；60 tick 接近，有限三次原版安全传送、三次原版近战尝试；使用 special_cooldown（默认 400） | 定向真实受击仇恨→一阶段三次传送及三次原版命中；旁观中断、独立关闭原版近战、保存冷却/携带方块、临时 -15% 速度与关闭恢复 PASS；配置 800 时到 640 tick 仍仅一阶段、三传送三命中 PASS |
| C01 苦力怕 | 原版爆炸后、死亡清理前释放嵌入箭，上限 32，不可拾取；受击退火不覆盖手动点燃；燃烧仅独立粒子 | 真正引爆后观察三枚不可拾取 Arrow，关闭为零；实际 SwellGoal 受击引信回退 PASS。粒子观感 NOT RUN |
| S07 蜘蛛/洞穴蜘蛛 | 仅增加原版 NearestAttackableTargetGoal 猎蠹虫/末影螨，保留原版 AttackGoal | 普通蜘蛛自然取得虫目标并实际造成伤害、独立关闭退出 PASS；专服猎末影螨 HP=994，原版为 1000。洞穴蜘蛛类继承接入 L1，独立洞穴蜘蛛实战 NOT RUN |
| BR01 旋风人 | 原版 Brain LongJump 起/落钩子，仅困难、两项独立风爆与 100 tick 保存冷却；半径 2.5，mobGriefing=false 不触发方块 | 真实 Brain 跳跃高差 4.23、原版 WindCharge 与 12 次额外起落爆发（800 tick）；普通有真实跳跃而零额外爆发；困难起跳关闭/落地现场关闭，仅一次爆发，保护规则下 lever 未激活 PASS |

以上均为受控真实服务器行为，不直接调用生产 Goal 或手动 tick AI。默认概率另以 1000 次原版 finalizeSpawn(NATURAL) 路径、种子 381000+i 取样：69 次 combo；这是出生路径样本，**不是自然刷怪器统计**。命令召唤不保证出现稀有连击，验收稀有技能应使用刷怪蛋/自然出生或下述明确的定向夹具。

## 独立配置与存档

配置 v9、字段 ID、依赖和映射不变。新增白名单 19 项：ghast_{slow_fireball,cooldown,telegraph}、blaze_{orbit,difficulty_volley,strafe}、silverfish_{burrow,call_pause,call_particles,shovel_weakness}、enderman_{speed,combo,combo_animation}、spider_hunts_pests、creeper_{embedded_arrows,hit_delay,fire_visual}、breeze_{takeoff_burst,landing_burst}。受 general.enabled 与 hostile.extended 对应字段约束（hostile 是分组，无独立总开关）；末影人出生选择另受 traits.enabled 与 enderman_combo_chance 约束。旧 special_cooldown 对已启动阶段只影响之后的预算，不重写已保存期限。

`buildupmobtweaks:remaining_ai_excluded` 实体类型标签退出本模块；实体 `buildupmobtweaks:vanilla_ai` 和 `buildupmobtweaks:disable_<feature_id>` 支持整组/单项退出。只读 OP 命令 `/buildupmobtweaks remaining <实体选择器>` 显示保存状态、目标及运行计数；计数是瞬态观测，重启清零，不能当作全历史累计。

持久附件 `remaining_rebuild` v1 保存出生选择、ghast_ready/hide_ready/combo_ready 与旋风人两项 ready；未知版本原样保留、不安装本模块 Goal。在途阶段不恢复；旧绑定弹在加载后删除，隐身仅恢复本模块持有的标志。保留真实外部乘客、副手盾、末影人携带方块，不补发武器/奖励。

加载/卸载回调的弹体删除延后到 END_SERVER_TICK，避免在原版实体容器遍历中变更列表。真实专服主动绑定两弹→解除强制加载、移走出生区→查询找不到实体→重新加载，同一 UUID 的旧乘客为零，冷启动后仍为零；无并发修改异常。另一专服保留实际末影人 combo_ready=1199 与控制恶魂持久化哨兵 ghast_ready=999，冷启动/真实区块卸载再加载值与 UUID 均不变；999 是手工持久化夹具，不能当真实恶魂战斗冷却证据（真实发射保存另有 GameTest）。

## 验证状态

| 检查 | 状态 | 证据 / 限制 |
| --- | --- | --- |
| Wrapper build + 实际 Server Tick / native Goal/Brain | PASS | [build-18.log](../../docs/rebuild/evidence/r4d/build-18.log)：131 项必需测试，既有 95 + 本批 36 |
| 单项门控、目标/模式/NoAI、死亡/递归加载与未知数据 | PASS | [behavior-results.json](../../docs/rebuild/evidence/r4d/behavior-results.json) 与测试具体断言；不是所有生物的每种模式组合都测试过 |
| 隔离专服初次运行、冷启动、远区块卸载 | PASS | target/active 两组 receipt；[active-server-v1.json](../../docs/rebuild/evidence/r4d/active-server-v1.json) |
| 各难度真实火球/有限官方上游与原版对照 | PASS（限定） | [upstream-comparison.json](../../docs/rebuild/evidence/r4d/upstream-comparison.json)；上游 26.2、原版/新版 26.3，HP 不作为严格等价判据 |
| 客户端独立集成世界登录/正常关闭 | PASS / L1 | [client-world-v1.json](../../docs/rebuild/evidence/r4d/client-world-v1.json)，exit 0，无强制终止；未宣称视觉验收 |
| 自然刷怪器分布、全技能完整 L3、多人、整合包、GUI/动画/粒子与性能基准 | NOT RUN | 待统一实机验收或后续授权 |
| Allay 恢复 | BLOCKED（历史） | 按用户指令不再尝试，与本批新增行为无关 |
| 剩余复杂 S2 | NOT_AUDITED / SOURCE_IDENTIFIED | [完整台账](R4_REMAINING_AUDIT.md)：68 个非生效 ID；不能声明完整 R4 已迁移 |

失败构建 01～14 全保留。有效问题及处理：Java 21 环境改为项目 Java 25；26.3 签名/Accessor 校正；弱引用攻击者清空防空指针；原版近战 Goal 停止导致 60 tick 预备不断重置修正；恶魂安静 tick 的边界 +101 修正；苦力怕箭移到物理爆炸之后；卸载删除改为延后；烈焰人实际横移改为 strafe。蠹虫夹具使用不可寄生地板避免受击前被原版寄生、援军显式设置原版规则并追踪实际新 UUID。两项既有溺尸定向测试固定 RNG 种子 430203/430204，保留逐 tick 守恒与真实回收全部原断言，避免随机散射是否命中成为序列化回归的偶然前置条件；它们不提供默认弹道概率结论。最终 build-15（129）与加入可配置冷却后的 build-16（130）、最终 build-18（131）均全通过；没有删测试、放宽时间/守恒/伤害要求或排除失败用例。

## 统一人工验收

请按 [完整 R4 实机清单](R4_ACCEPTANCE_CHECKLIST.md) 合并验收 R4-A/B/C/D。自动 PASS 不替代人工勾选；尤其检查旧木剑反馈、烈焰人半环方向、恶魂蓄力、蠹虫可见恢复、末影人接近/挥手和多人配置同步。

Code Review：PASS WITH RISKS；本批无未解决 BLOCKER/MAJOR，未实测项见上表。完整自检与产物 SHA256 见外层 verification.json。仅本次目标 Git 改动做一次本地提交；父目录报告、原始证据和脚本不在目标 Git 根内。

完整自检：[R4D_CODE_REVIEW.md](R4D_CODE_REVIEW.md)。最终审查另限定烈焰人额外 stop 重置只在本模块相关 gate 生效时应用，并追加全关闭实际原版三弹/零乘客场景；关服清空待删除引用。

最终审查后 build-17 新增36项均通过，但既有Vex恢复和溺尸超时拾取两项失败，日志完整保留。Vex 的随机原版冲刺可能到观察截止前才启动，溺尸首次索敌/发射也影响固定260 tick的超时前置条件；不把未启动/未到期当预算错误。固定Vex定向RNG440301，溺尸超时RNG430205并在tick10给予原版受击仇恨输入，保留原430/260/310截止、方向/恢复/超时/拾取全部断言；build-18最终131项全通过。这些是可重复定向夹具，不是默认行为概率测试。主动卸载流程开始时确有两枚绑定弹；等待原版卸载过程中弹体可能已释放或进入后续阶段，未宣称卸载瞬间恰好绑定两弹。
