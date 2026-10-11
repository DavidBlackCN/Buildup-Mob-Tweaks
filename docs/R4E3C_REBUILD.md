# R4-E3-c 工作报告

> 最新人工反馈：baby仍显示成年大小、弓握持偏移，均已复现。共享模型需要单独应用ageScale并保留成年网格握持；前次UV修复保持。真实前后矩阵/同距/近景及449完整回归见[第二次补修](R4E3C_ACCEPTANCE_FIXES_2.md)。修复后的人工补验仍待反馈，本轮不进入下一阶段。

> 2026-10-11人工反馈：除baby模型外其余主功能无问题。幼体盔甲的成年网格/专用幼体贴图不匹配已复现；本次窄修和实际客户端四槽证据见[补修报告](R4E3C_ACCEPTANCE_FIXES_1.md)。本文原客户端PASS覆盖年龄/尺寸/绘制及交互，漏验盔甲贴图，不代表该缺陷已被原测试覆盖。修复后的用户补验仍等待，不进入下一阶段。

2026-10-11，目标26.3/分支26.3，起始干净HEAD e86e80d82456f6742d4b0406bb6ae7493704229f。用户批准整c直接执行，并明确批准雪傀儡临时护甲+10、旋风人总3发/±20°/100 tick且与跳跃爆发共用预算。仅本批六项，未进入E4/Boss/R5/Phase2/3，不恢复旧S2、不推送发布。

## 实现与决策

| 新ID | 来源 / 实际行为 |
| --- | --- |
| snow_golem_pumpkin_return | G16 ADAPT；真正雕刻南瓜、裸头一次消费1、创造不消费；原生剪切/原版一个南瓜掉落保留。服务端权威，独立同步交互门供客户端预测 |
| snow_golem_pumpkin_defense | G16 ADAPT；戴南瓜时临时ADD_VALUE10护甲，只移除自身修饰器，保留其他基础值/装备/修饰器；独立门 |
| snow_golem_ranged_behavior | G16 ADAPT；原生目标取得/Goal调度，10 tick/20格、裸头散布2；100 tick最多32原始候选、最多3个正攻击自己的敌对者逐发扫射。不复制上游无限轮次旋转/跳跃 |
| snow_golem_throw_visual | G16 ADAPT；6 tick准备+4 tick随挥、交替手、真实手模型与原版雪球物品提交；准备没有世界雪球或持久化乘客，退出/重载取消。发出的普通雪球遵循原版碰撞，烈焰人3点、其他0点原始伤害；使用26.3原版1.6速度，非上游释放后的1.0 |
| skeleton_baby_variant | G33 ADAPT；五种确切原生类型，一次出生默认100/1000，原生0.5年龄尺寸/眼高/碰撞/渲染，不成长/加速/加血/补装备；保存与真正转换保留选择。自然/区块/蛋/普通和试炼刷怪笼抽取，命令/载入/转换不新抽；关门暂时成年，恢复使用旧选择 |
| breeze_shotgun | G34原OP专用分支，经用户批准独立Vanilla+ ADAPT；原生Shoot Brain生成中弹后+2，精确水平±20°保持原速度/y，与起跳/落地共用100 tick期限，仅该功能开启时参与共享。原版中弹/Brain冷却不改；额外弹100 tick期限、退出/卸载/重载退役，mobGriefing=false限制额外爆风环境交互 |

当前139声明/102支持/37未生效，配置v12、独立QA50场。旧68取舍PORT1/ADAPT47/REPLACE6/DEFER10/EXCLUDE4不变；G33错峰拉弓仍E4，不计本批完成。六个新开关独立默认true，不依赖Traits总门；general.enabled、实体vanilla_ai/disable_ID、ability_ai_excluded仍有效。未知附件版本原样保留，NoAI不抹除出生选择或物理年龄，战斗行动暂停。

MC26.3、JDK25.0.3、Wrapper9.7.1、Loom1.18.3、官方未混淆命名、Loader0.19.5、FAPI0.162.0+26.3、Fzzy0.7.7+fix3+26.3非传递、Kotlin1.14.1+kotlin.2.4.20、可选ModMenu21.0.0均保持。无新依赖/AW。common不引用client；新增严格required Mixin真实启动校验。详细范围见[方案](R4E3C_MIGRATION_PLAN.md)。

## 验证与证据

原始证据在仓库外层 `docs/rebuild/evidence/r4e3c/`，测试脚本在 `docs/rebuild/scripts/`，均不纳入26.3本地提交。正式/source JAR不含QA、GameTest或外部客户端探针。

| 验证 / 实际命令 | 结果 / 限定观测 |
| --- | --- |
| 26.3内 `./gradlew.bat build qaJar -PtestEula=true --console=plain --no-configuration-cache`，JAVA_HOME指向既有JDK25.0.3 | **PASS**，build-5.log，449 GAME TESTS COMPLETE/All449required，40新增+409旧；QA包构建通过，不仅编译 |
| snow原生Goal、真实飞行/命中、门切换与中断 | **PASS**，210 tick新增18球/10 tick间隔，全部关闭9球/20 tick间隔，分别只有SnowAttackGoal/原RangedAttackGoal。准备中NoAI/死亡/目标退出/遮挡/禁用/保存重载均不补球，原生模式退出与有限扫射通过 |
| baby原生出生/有效半尺寸/装备/弓/真正转换/保存 | **PASS**，五类实际width .3（凋灵.35）、eye .87（凋灵1.05），低眼高真实4箭命中；N1000受控出生98命中，预声明范围65..135，不修改默认概率。死亡转换保留损伤装备/年龄选择、无重复即时奖励 |
| Breeze原生Brain/真实风弹/严格角度速度/TTL/关闭/模式/重载/破坏门 | **PASS**，360 tick开门三组6额外，关闭0额外且中弹仍命中；hard720 tick真实跳跃与1组散弹/6爆发，事件间隔≥100，无同tick双事件。重载额外弹退役、期限保存，false破坏门实际碰撞不触发杆 |
| `python docs/rebuild/scripts/r4e3c_client.py 3` → 隔离真实runClient/外部探针 | **PASS**，client-3.json/log；自动左右持球提交257/269、实际手臂xRot1.7000000477、7308自动幼体绘制，五种每251同步样本；原生客户端剪切/归还/重复点击网络往返与消费守恒true。肉眼 **NOT RUN** |
| `python docs/rebuild/scripts/r4e3c_regression_client.py 1` | **PASS**，外层r4e3c-regression/client-1.json/log；16门盾样本，8类钓线真正提交/写顶点、16弩手组合及既有拉弓模型；备用木剑死亡与马姿态等在旧409全量真实调度回归PASS，不冒充本轮肉眼验收 |
| `python docs/rebuild/scripts/r4e3c_servers.py qa 2` → runQaServer -PqaHarness=true / 100TPS | **PASS**，六场真实专服；snow300 tick实际目标HP923.55994、真实27弹ID；baby/凋灵真实箭命中；shotgun300 tick三组6额外12实际弹ID，off后第二观察窗口新增计数不变、原版中弹继续命中。pumpkin控制台只验证夹具/护甲/退出，交互由GameTest与实际客户端另验；回收地形和自有实体PASS |
| `python docs/rebuild/scripts/r4e3c_servers.py target 2` → runServer/同世界冷重启 | **限定 PASS**，v11输入→v12新增默认，保留weaponSwitching=false；裸头雪球实际Blaze985HP；同UUID baby_choice=1保留，snow_sweep_ready=176L保存。不是完整所有技能重启穷举 |
| `python docs/rebuild/scripts/r4e3c_servers.py upstream 3` → 官方Beta26.2专服 | **限定 PASS**，默认非OP，真实裸头雪球/Blaze991HP、64无NBT原生出生8幼体。前upstream-1/2带NBT跳过finalizeSpawn，仅其雪球对照有效，不算出生概率失败或PASS；官方启动advancement ERROR保留。前版本1.21.11完整运行、OP等价/两版完整L3 **NOT RUN** |
| `python docs/rebuild/scripts/r4e3c_verify.py`、Git diff --check | **PASS**，verify.json 47收据检查，139/102/37/v12/QA50、JSON/双语、原官方/12新来源+18既有来源哈希、旧169文件只读、正式/source/QA隔离与许可证 |

测试设置真实输入、使用服务器自动tick和原生Goal/Brain；从未直接调用新技能、Goal.start/tick或实体tick来替代行为。QA与外部客户端探针不编译进正式JAR。原生finalize/交互/保存/转换调用是出生及动作输入，而非假装自动技能触发。

初次失败均保留：build-1是已纠正的目标交互签名与QA泛型编译错误；build-2南瓜夹具原有头盔2护甲漏计，以及既有horseActualWarningChargeAndNoAI一次调度失败（未改其断言/生产马逻辑）；build-3严格20°测试识别原版Vec3.yRot正弦表精度，改精确旋转且未放宽断言；build-4长跳夹具有屋顶，改为同既有长跳测试的无顶场景，断言不变。client-2在首次实体包与数据包间过早检查，加入明确10 tick同步窗口，后续检查仍严格。native.txt最后一条错误包名查询保留，正确完整字节码在native-extra/native-wind/native-render-submit/native-vector/native-client-interaction.txt。

人工视觉/本清单、完整两版L3、长期自然出生分布、真实多人/整合包/领地兼容、配置GUI和长期性能 **NOT RUN**。本批出生N1000仅受控原生finalize抽样，不代表长期生态分布。上游有限运行不证明G34改造与OP平衡等价。无本批 **BLOCKED** 或未通过必需检查；Allay历史BLOCKED保持、不重试。首次既有horse测试调度失败未证实根因，后续构建该既有用例均通过，最终449全量通过，未修改断言，保留为非阻断复现风险。

## 交付与停止点

源码主链：AbilityBehavior/State、SnowAttackGoal；六个common Mixin及SkeletonLifecycle年龄同步、RemainingExtras预算；三客户端Mixin/SnowballHandLayer；FeatureRegistry/config/双语；独立E3cQa与40项新真实调度测试。人工步骤见[清单](R4E3C_ACCEPTANCE_CHECKLIST.md)，自检见[R4E3C_CODE_REVIEW](R4E3C_CODE_REVIEW.md)。构建产物 `build/libs/buildup-mob-tweaks-1.0.0.jar` 与独立 `build/libs/buildup-mob-tweaks-1.0.0-qa.jar`，不发布。

下一具体任务仅建议E4规划：G07潜行接近动物、G33错峰拉弓、G35必要投射物/弩钩子，先复核26.3原版与现有模块是否已覆盖，分别确定REPLACE或窄ADAPT，真实调度/多射手节奏及禁用回退专项。未实施、需后续授权。批末本地提交后停止，等待整c人工验收。
