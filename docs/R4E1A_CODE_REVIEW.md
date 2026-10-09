# R4-E1-a 完整改动自检

Code Review: PASS WITH RISKS（以最终verification.json执行结果为准）。唯一目标26.3/分支26.3；起始caf78b8无用户未提交改动；源26.3-tests169个tracked文件、Git状态/HEAD/分支/remote哈希核验一致。外层共享文档/脚本/证据不在目标Git根，须单独保存。

## 审查范围与结论

- 注册/调用链：公共初始化提前bootstrap同步附件；独立ENTITY_LOAD→ExtendedSkeletonGoal(3/MOVE/LOOK)→原生Selector，旧SkeletonBehavior三类型不扩张。服务器技能调用不引用client；客户端只从同步SPIN提取RenderState。未恢复旧S2服务/Mixin。
- 门控：general/traits/单项配置、实体排除Tag、vanilla_ai/disable；四项只增加本批对应入口。既有弓原版行为不会因关闭新增开关消失；出生装备分配未启用。配置v9、ID、依赖和旧数据格式保持。
- 生命周期/守恒：无reserve/装备copy或发放；真实主副手弓消耗已有耐久。出生一次、不为LOAD/转换/跨维度补发；未知v99不覆盖。技能有前摇/有限次数/期限/距离/冷却，退出不返还冷却。每tick合法性复核避免原版延迟continuation检查；卸载跨实体退休排队，载入旧弹体不续战。
- Mixin：Mob.serverAiStep()V及Entity.tick()V既有路径只追加独立类型调用；AbstractSkeleton.performRangedAttack(LivingEntity;F)V RETURN统计/耐久；AbstractHurtingProjectile.tick()V HEAD仅本模块WitherSkull；WitherSkull.onHit(HitResult)V的Level.explode(Entity;DDDFZExplosionInteraction)V index6局部NONE；onHitEntity(EntityHitResult)V调用LivingEntity.addEffect(MobEffectInstance;Entity)Z前仅本模块取消持续凋零。实际26.3 javap/源与运行时启动核验，无关闭required/defaultRequire或新增AW。其他来源头颅无附件，不改原流程。
- 客户端：Kind3仅焦骸，绕Y轴，上游LivingEntityRendererMixin同轴；旧Kind1/2 X轴保持。10实际客户端同步/RenderState和64门盾坐标样本通过；未检逐帧最终矩阵或人眼视觉。
- 资源/构建：中英JSON解析、键保留、描述对应最终行为；无新增依赖/映射混用。qaJar/gametest边界由ZIP核验，不进入正式/源码包；诊断probe只在外层独立开发Mod。
- 测试：新增18项全是实际Server Tick/Selector输入观察，不直接调用Goal/AI tick。原135+E0 QA共136项全部保留；旧排除断言改为明确旧R4-A范围并增加隔离条件。原日夜/玩家长时目标1000HP，三叉戟两个夹具仅固定接收输入，恼鬼/女巫固定随机源；不改原概率/计数/断言/超时。随机瞄准精度不是这些修订夹具的验收结论，完整失败日志保留。
- 实包/持久化：独立QA专服命令、真正区块卸载后No entity found、同UUID/birth/冷却/装备冷启动、无旧弹体恢复，客户端正常停服；不是靠编译或测试数量宣称行为完成。

BLOCKER：最终必需构建/机器检查通过后无。MAJOR：无已确认生产缺陷。MINOR/RISKS：旧原版随机夹具出现过多次失败，本批修订输入/随机源，不以最终单次PASS证明所有测试长期无波动；客户端肉眼、多人、自然刷怪器、整合包/领地/第三方弓、性能与完整L3仍NOT RUN。上游Beta实际专服仅有限场景，正式版本批未新增实跑；Allay历史BLOCKED不重试。静止NoAI保存实体的真实卸载不能代表精确活动弹体卸载瞬间。

UPDATE_NOTES.md按Gitmoji+Conventional追加两条已验证玩法/独立QA变化，纳入同一次本地提交；无推送/发布/切分支。具体命令/原始回执见[R4E1A_REBUILD](R4E1A_REBUILD.md)。
