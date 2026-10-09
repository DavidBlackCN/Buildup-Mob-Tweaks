# R4-D Code Review

Code Review: **PASS WITH RISKS**

## Scope

唯一目标 `26.3/`、`26.3` 分支，初始 clean；审查本批所有 tracked 修改与新增文件。根目录 PLAN、矩阵、行为基准、类映射及证据属于本轮明确涉及的共享文档；不在该 Git 根内。旧 `26.3-tests` 只读快照逐项验证。

## Findings

- BLOCKER：无未解决项。本批选定核心行为与必需验证均通过；未迁移旧机制不隐藏，仍为台账状态，完整 R4 不作已完成声明。
- MAJOR：无未解决项。真实弱引用清空、原版 Goal 重新启动、卸载实体容器、实际横移和冷却配置问题已修复并重测。
- MINOR：无交付阻断项。未做无关版本升级、批量格式化或旧源码恢复；旧报告/注入入口隔离保持。

## Version / API / Mixin

MC26.3、Java25、Wrapper9.7.1、Loom1.18.3、官方未混淆命名、Loader0.19.5、API0.162.0+26.3，Fzzy0.7.7+fix3+26.3、Kotlin1.14.1+kotlin.2.4.20；元数据/toolchain/依赖不变。源码与 javap 核对全描述符、静态/实例、ordinal 与实际调用点；NoAI 外层 tick 和 die 入口的生命周期不是直接重写原版攻击或复活。没有新 AW/反射/客户端类进入 common。41 个 common 与4个 client 重建 Mixin 均打包，专服/GameTest/客户端没有注入失败。

## Correctness / Compatibility

每项 FeatureRegistry 独立 gate、总开关与实体/类型退出；hostile 只是配置分组，无额外分组总开关，不新增不存在字段。末影人使用原配置 special_cooldown，不覆盖已有出生/冷却与携带方块。未知附件版本原样保留。只清理本模块的绑定火球、隐匿标志和速度 Modifier；外部乘客/盾/基础属性不移除。真实投射物具有限生命周期；保存后不恢复旧在途技能；原版目标选择/模式过滤/近战/援军/爆炸/Brain 链仍负责实际行为。

修正：加载/卸载回调的弹体删除延迟到 END_SERVER_TICK，关服清空待删除引用；烈焰人额外 step 重置受相关 gate 约束，全部关闭保留原版停止语义；末影人不写入自己未拥有的 sprint 标志。蠹虫可受伤；粒子数量基于真实寄生块破坏，不把 block_drops=false 时无同伴当作实际释放。

## Verification

完整 Wrapper build、实际 Server Tick 与原版 Goal/Brain；专服正常保存/重启、真实远区块卸载与主动绑定弹阶段中断；独立难度2/3/4、有限官方上游/原版对照；客户端独立世界真实登录且正常退出（L1）。具体命令、最终计数与原始证据见 [本批报告](R4D_REBUILD.md) 和外层 verification.json，不将统计替代行为。

全部旧95测试保留；溺尸三项定向回归与 Vex 恢复场景固定 RNG，超时场景tick10输入原版真实受击仇恨，原时间截止/守恒/方向/恢复/拾取断言不变；蠹虫夹具显式设置原版规则、用不可寄生地板，真实援军 UUID 不以最终已移动的同伴当前位置代替。失败日志全保留，修复后运行必需验证，不跳过旧失败用例。

资源 JSON/双语键一致、实际 UI 描述准确；Release JAR 不含旧 combat、测试、原版类或新附魔注册。MIT/第三方许可与 NOTICE 原作者署名保留。UPDATE_NOTES：PASS，追加五条 Gitmoji + Conventional Commits，保留此前全部记录，描述已验证玩家可见行为。

## Residual Risks

人工视觉/GUI、多人/完整袭击、自然刷怪器统计、独立洞穴蜘蛛战斗、旧 S2 数据与第三方整合包、性能及完整 L3：NOT RUN。68 个非生效旧 ID 未迁移，Allay 历史 BLOCKED 不重试。本次 target v1 对照早于最终横移/冷却配置修正，最终实际 GameTest 验证新实现；不假装所有专服历史日志运行的是最后源码。完整 R4 统一清单由用户验收，不自动进入 R5/S3。
