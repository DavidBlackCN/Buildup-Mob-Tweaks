# R5 Code Review

Code Review: **PASS WITH RISKS**（限定原版依赖组合；最终静态证据见外层 `verify.json`）

Scope: `26.3/` 本次代码、双语资源、元数据、测试、文档及 UPDATE_NOTES；起点干净 `a000afc`。外层计划和证据是明确任务范围，旧 S2 / 反编译输出只读。

Findings:

- BLOCKER: None。
- MAJOR: None（已发现的近战重启绕过间隔已修复并全量验证）。
- MINOR: 历史报告保留对应时点计数和停止声明；各现行入口以最新 R5 段落及最终报告为准，不覆盖原日志。

实现与 Mixin：`MeleeAttackGoal.start()V`、`resetAttackCooldown()V`、`getAttackInterval()I` 的目标 26.3 签名和计数归零来自已解析原版字节码。新增 common TAIL 注入使用运行中 Goal 独立字段，无客户端依赖、反射、全局目标覆盖或附件升级。剩余等待有上限，独立禁用清理，原版攻击/目标调度保持。真实专服启动与完整正反调度测试通过；没有只凭编译判断注入成功。

配置与资源：仅增加实际 Fzzy `Comment` 注解及双语说明，字段、默认值、数据版本和注册白名单不变；Traits 高级出生主开关与废弃 Marker 区分。JSON/语言键一致性与生产包隔离由静态核验复核。主日志/描述只更正实际产品范围。依赖、Wrapper、AW、Mixin JSON 均未修改。

测试：修正陷阱夹具发射高度以保证真实命中，保留伤害和报复断言；新增近战 Goal 原生停止、目标消失、重新索敌两条回归。没有直接调用 Goal、手动 entity.tick 或跳过失败。前两次性能输入不合格记录为失败，最终有效样本另列，不挑选旧最好数据。

Verification:

- Wrapper `build qaJar`（最终 full-4 隔离目录）：PASS，504 必需真实调度测试。
- 生产包区块卸载/磁盘重载/冷重启：PASS（代表夹具）。
- 两个真正客户端及原生箭/刷怪笼/创造退出：PASS（限定）。
- 三组性能：详见 R5_PERFORMANCE，保留所有 24 组原始样本。
- 包隔离、语言 JSON、保存字段、已构建源码一致性、旧资料哈希、文件路径及 `git diff --check`：PASS，外层 verify.json 共1065个检查，含153个运行依赖副本、604个旧S2文件和本次引用的上游资料不变。
- Update Notes：追加两条面向发行使用者的已验证修复，保留历史记录。

Residual Risks: 第三方实际整合包、ModMenu GUI、本批真人最终体验、长期自然分布/生产压力和完整两版 L3 未运行；这属于已说明的验收范围限制，没有以 PASS 覆盖。Boss 按用户裁决延期，不能声称已完成 Boss 兼容或控制权退让。自检无未解决阻断项；完成本地提交后停止等待人工确认。
