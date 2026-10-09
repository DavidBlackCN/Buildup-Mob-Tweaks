# 正式阶段编号与历史证据对照

PLAN.md 第 4 节是阶段定义。此前 R2-A 合并了基础框架与掠夺者标杆，但后续继续称 R2-B/R2-C 是 Agent 编号延续错误；不将全部迁移工作归入 R2。

| 正式阶段 | 内容 | 历史证据文件名 | 状态 |
| --- | --- | --- | --- |
| R2 | 干净工程与小型兼容框架 | 随 R2-A 基线共同实现 | 已接入；不等于其他生物验收 |
| R3 | 掠夺者标杆 | [R2A_REBUILD.md](R2A_REBUILD.md) | 用户验收；死亡食物反馈已修正 |
| R4-A | 骷髅、流浪者、沼骸 | [R2B_REBUILD.md](R2B_REBUILD.md) | 用户已实机验收并反馈木剑掉落；[本轮修复](R4A_SWORD_FIX.md) |
| R4-B | 僵尸、尸壳、溺尸 | [R2C_REBUILD.md](R2C_REBUILD.md) | 用户已进行实机验收；未提供的专项结果不补写 PASS |
| R4-C | 女巫、唤魔者、恼鬼及袭击交互 | [R4C_REBUILD.md](R4C_REBUILD.md) | 限定 L2 行为已验证，随 R4-D 统一等待人工验收；完整袭击/多人/L3 NOT RUN |
| R4-D | 恶魂/烈焰人/蠹虫/末影人与选定其余生物、剩余 S2 审计 | [R4D_REBUILD.md](R4D_REBUILD.md) | 本批限定 L2 PASS；完整 R4 待统一人工验收；未迁移 ID 另列 |
| R5 | 集成性能收口 | 后续阶段 | 未进入 |

历史文件名、日志前缀、测试资源 ID、提交消息和原始验证结果保留，避免破坏引用；历史 R2-A/R2-B/R2-C 只作别名。正式阶段号不会改变 L1/L2/L3 判定，也不会将尚未验证的多人、自然生成或完整对照自动升级为 PASS。

当前统一人工清单：[R4_ACCEPTANCE_CHECKLIST.md](R4_ACCEPTANCE_CHECKLIST.md)。R4 批次全部交付不表示 68 个非生效旧 ID 已迁移；[逐项台账](R4_REMAINING_AUDIT.md)。
