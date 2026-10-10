# R4-E1-c CODE_REVIEW 自检

自检结论 **PASS WITH RISKS**，适用检查通过，无新代码阻断；验证以[验证收据](../../docs/rebuild/evidence/r4e1c/verification.json)和[报告](R4E1C_REBUILD.md)为准。

Findings：BLOCKER 无；MAJOR 无未解决项；MINOR 无未解决项。审查中已修正药水入口描述符、幼体与药水测试夹具、已知技能载体筛选、未知版本保护和报告的最终收据引用。上述修复已通过真实调度验证；人工及兼容性未运行范围列为下述非阻断风险。

按外层 CODE_REVIEW.md 逐项检查：

- 范围：唯一26.3/分支26.3，起始工作树干净；旧169跟踪文件哈希对照。未恢复S2服务或改其他版本源码；外层只维护明确相关计划/矩阵/证据。既有178项测试Java不修改。
- 版本：MC26.3/JDK25.0.3/Wrapper9.7.1/Loom1.18.3/官方未混淆命名，Loader0.19.5/API0.162.0+26.3/Fzzy0.7.7+fix3+26.3/Kotlin1.14.1+kotlin.2.4.20。依赖、Wrapper、元数据、AW不变。
- 调用链：LivingEntity.startUsingItem HEAD/RETURN只识别新的实际使用；DODGE内一次释放，没有另一个Goal。新功能FeatureRegistry有门而非只加白名单，visual独立开启可用原版Goal。类型/实体/武器排除、总门、NoAI、目标及未知状态过滤。
- Mixin：javap核对完整描述符。ThrowableProjectile.getDefaultGravity()D RETURN仅对合法模块蘑菇为.05，普通雪球保持.03，以真实一Tick飞行对照。Snowball.onHit共享实体/方块一次burst；AreaEffectCloud药水判定在 **serverTick(ServerLevel)**，tick错误注入已在失败日志保留并修正。新WrapOperation保留非模块云的原版判定，先过滤再应用药水，含亡灵治疗。defaultRequire=1保留；实际GameTest、专服和客户端确认加载。
- 客户端：同步附件在初始化早期注册；服务端布尔门+实际用弓手/主臂；原版模型/SwingDescription，不改mainArm或制造箭物品。四组实际模型和关门同步通过，门盾16实际坐标样本通过；肉眼不冒充自动PASS。
- 生命周期：owner/target固定、100期限、两球两云预算、120旧冷却，状态不重抽、不退冷却。卸载回调排队，结束Tick退役；重载已知技能弹体/云不恢复。未知DATA/不适用载体原样保留；只有真实弹体/云的已知version=1能触发孤儿清理，未知技能附件退出管理并回到原版，真实保存/Tick验证其载体及payload均保留。静止NoAI冷重启不解释为活跃飞行卸载瞬间。
- 装备/资源：不发装备，实际弓组件/耐久保留，原生成剑死亡规则回归；MUSHROOM仅原版弹体视觉，原版雪雪球销毁不掉物品。语言键、资源JSON、许可/NOTICE及配置v9检查。
- 证据：真实Server Tick/GoalSelector，不直接调用Goal/entity.tick/new release/impact；云伤害直接来源事件区分原毒；baby只为测试子类守卫。旧索敌固定时刻失败不删断言，保留完整失败历史；Beta专服对照/正式源码对照与完整L3分开。
- 包：正式/源码包无QA、GameTest、探针/MC类，独立QA不含正式rebuild类。构建包哈希、docs链接、UTF8、UPDATE_NOTES追加格式、git diff --check由审计复核。外层证据不入26.3 Git。

剩余风险：人工视觉/干净发行/自然刷怪器/双人/整合包/性能/任意第三方武器与标签组合/完整L3未执行；原固定时刻索敌有时序敏感性；隔离世界layers及上游旧advancement日志警告原样披露。Allay历史BLOCKED不重试。完成适用收尾检查后仅一次本地提交，不推送/发布、不进入E2。
