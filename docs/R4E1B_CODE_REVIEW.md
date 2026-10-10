# R4-E1-b 自检

Code Review: **PASS WITH RISKS**。按外层 `CODE_REVIEW.md` 逐节检查完整变更；未将本地提交作为人工验收。

Scope：26.3/分支26.3；四项猪灵ID、独立附件/生命周期、8个窄Mixin、注册入口、现有白名单和四项翻译描述、独立QA、新24项真实调度测试及其测试资源、阶段文档和UPDATE_NOTES。起点工作树干净，旧测试Java未改。共享计划/脚本/日志不在目标Git内。

Findings：BLOCKER **无**；MAJOR **无未解决项**；MINOR：E0僵尸猪灵来源分类不完整，已更正并保留历史说明。

| 检查 | 结论与依据 |
| --- | --- |
| 范围和权限 | 没有重装旧S2/其他生物AI、发弩、正式新物品、配置格式改变或迁移依赖；只读169个旧工程文件哈希一致 |
| 版本 | 26.3/Java25/Wrapper9.7.1/Loom1.18.3/official unobfuscated/Loader0.19.5/API0.162.0+26.3；依赖及AW未改，实际Wrapper build |
| 调用链 | Piglin仍原Brain/CrossbowAttack；ZP仍HurtByTargetGoal/TargetGoal，按实际同类受伤先复现再窄修；测试未直接调用Goal/Brain或攻击方法 |
| 门控 | 总门、独立ID、实体退出标签、精确type/排除tags、未知附件保守；跨CrossbowAttack/ProjectileUtil/BehaviorUtils共享钩子只对受门控Piglin，TargetGoal只对ZP同类 |
| 物品 | 真实ItemStack唯一源，原版射击扣耐久；不复制主副手、清弹药、设置隐藏装备；真实黄金替换/拾取/死亡/装填重载/转换守恒测试 |
| 生命周期 | 独立持久附件v1，未知v99不覆写；出生一次/加载转换不重抽，取消不退款；ENTITY_UNLOAD只清已跟踪精确Piglin，END_SERVER_TICK快照迭代，SERVER_STOPPED清集合 |
| Mixin | 完整描述符逐项对照9份目标版本javap；HEAD/RETURN/TAIL，无ordinal/slice/localcapture；required/defaultRequire=1，common不引用client，实际专服及客户端无本模块注入错误 |
| 客户端 | 同步附件在初始化bootstrap；原版Piglin getArmPose读取实际弩，赏金/跳舞优先；原版客户端收到副手useItemHand/CROSSBOW_CHARGE及主副手Swing，探针独立不入包 |
| 资源/QA | JSON可解析、语言键无增删；空排除Tag遵循现有扩展方式；QA源码单独源集/Mod，权限等级4、唯一会话、预算/TTL、清理实体及恢复地形，无全局概率改写 |
| 回归 | 最终178必需真实调度测试及独立QA专服；原154项包括门盾及木剑，未通过删测试或降低旧断言掩盖问题 |
| 证据诚实性 | 历史失败保留；上游只有限对照，NoAI静止卸载不能证明活跃充能卸载瞬间；客户端RenderState不是肉眼；样本finalizeSpawn不是自然刷怪器 |
| Update Notes | 验证和审查后追加Gitmoji+Conventional Commits，面向已验证实际行为；同本次commit纳入 |

审查修正：卸载只操作loaded集合中的Piglin，tick迭代使用快照；安全落脚额外排除脚部火/灵魂火，出生抽签明确排除幼体。实际副手被原版移走的根因通过窄守卫修复，黄金替换单独验证。native转换金粒可被转换者拾进主手，守恒断言合计双手/地面，并查原库存为空；后跳夹具只在落地蓄力中段安排输入。玩家模式测试改为观察实际蓄力后切换，两段须在原120tick时限完成，不再假设传感器15tick内更新。未放宽实际位移/次数/时限要求；旧154项Java未改。

Verification：命令、运行参数、包哈希、逐场景日志和旧文件快照见 [verification.json](../../docs/rebuild/evidence/r4e1b/verification.json) 及 [报告](R4E1B_REBUILD.md)。`git diff --check`、包内容、ID计数、JSON/路径检查 PASS。

Residual Risks：第三方同方法Mixin可能冲突，继承CrossbowItem且保留原版组件/发射约定以外不承诺兼容；肉眼/用户整合包、完整L3、自然刷怪器、多人和长期性能 NOT RUN。旧原生索敌夹具固定时刻断言曾间歇失败，历史日志保留，最终通过不代表精确时机确定性。没有已知必须再改代码才能交付的阻断项；上述风险由 [人工清单](R4E1B_ACCEPTANCE_CHECKLIST.md) 继续验证。Allay历史BLOCKED，不再尝试。
