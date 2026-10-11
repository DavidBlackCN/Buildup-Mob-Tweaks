# R4-E4 完整改动自检

按外层 CODE_REVIEW.md 执行。唯一实现目标26.3、分支26.3；起始无用户改动，旧S2只读。最终详细运行证据和限制见[R4E4_REBUILD](R4E4_REBUILD.md)。

| 检查 | 结论与证据 |
| --- | --- |
| 范围/既有行为 | 17旧ID ADAPT、危地REPLACE、2新ID与G35收口；不扩大Boss、其他Mob/第三方、装备/出生/持久化范围；旧S2 604文件哈希一致。 |
| 版本/构建 | 不升级工具链/依赖；官方名、Java25、AW official唯一枚举类；完整Wrapper build/qaJar及502 required PASS。 |
| 生命周期/边界 | 支持类型与现有状态/Tag/NoAI/配置门；初次发现和报复/攻击LOS分离；等待有限且不持久化；运动目标/阶段退出；视觉位server权威/预先注册/sync-only，关闭不清物理火冰。 |
| 性能预算 | 同类候选上限32、4tick查询、40tick等待；腾空输入12tick；近高目标起跳20tick；效果等级0～255、间隔6～200；粒子1/4tick；不建立全世界常驻索引。长期实测NOT RUN。 |
| Mixin/AW | 当前本地原版完整描述符、静态/实例及注入调用链已核验；普通专服、GameTest和真实客户端启动未出现本项目Mixin错误，未反射/吞异常/关闭默认Require。 |
| 客户端/兼容 | common无client类引用；新颜色/泳姿/原生伤害与关闭对照、五幼体比例/弓/护甲、门盾/弩/渔线实际自动绘制PASS。全整合包、GUI/多人NOT RUN，不能承诺所有外部renderer覆盖兼容。 |
| 测试真实性 | 世界/实体输入、真实Server Tick+Goal/Brain、实际碰撞/位置/命中时间/初始速度；无直接Goal或手动实体tick。旧测试只隔离随机/越界/无敌帧输入，原必要行为断言保留；失明报复与台面火补测。 |
| QA隔离/恢复 | 独立-qa.jar、正式/source无测试与探针；21场权限/互斥/关闭/1200tick/地形与实体恢复；manual场仍人工NOT RUN。 |
| 资源/声明 | 中英19组标签说明、v13升级保留旧值、141/121/20重核；G35不虚加ID，危地旧门不冒称启用；MIT/NOTICE与外部依赖许可不混淆。 |
| 证据/失败 | 全部失败尝试保留；最终PASS与未执行项区分；上游旧advancement解析错误披露，仅两种有限专服行为对照，不冒称完整L3。 |
| 发布说明/Git | UPDATE_NOTES追加已验证玩家变化，Gitmoji+Conventional；本次相关代码/文档本地提交，不推送发布。共享根文档/证据不在提交内。 |

最终交付检查包括JSON/翻译键/Mixin路径、源码/QA隔离、文档链接与台账计数、旧S2/参考哈希、git diff --check。无 BLOCKER/MAJOR；未验证项保持NOT RUN。证据：外层 docs/rebuild/evidence/r4e4/verify.json。编译既有deprecated、开发环境标签翻译/认证警告不等同功能通过；上游资源ERROR也未隐去。
