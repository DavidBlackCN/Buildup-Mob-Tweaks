# R1 upstream behavior audit — 2026-10-08

本轮完成 PLAN.md R1 的研究交付，等待人工验收；没有开始 R2、重写 AI 或修改 Mod 配置。当前源码仍为原 S2 基线，不能称为重建版。

- 上游输入：Mob AI Tweaks 1.11.0-beta / MC 26.2（Modrinth CExkBTN8），1.11.3 / MC 1.21.11（bFb56Zw2）；Vineflower 1.12.0。
- 最后一次 Allay 反编译加入匹配版本游戏依赖后仍失败；按用户要求停止尝试，Allay 保留 BLOCKED。
- 按用户决策，本项目后续声明使用 MIT，保留适用上游版权与出处；发行包 CC0 取证原件保持原样，“模板残留”未作为已确认事实。
- 完成 31 组上游类文本对照和 139 个源码文件的哈希/定位索引，重点建立掠夺者、骷髅、僵尸基准，扩展尸壳/溺尸及部分袭击生物。
- 旧 S2 的背包/手持换装、食物回复、狙击、变种技能、门盾、下潜和环绕火球均存在需要分项处理的行为差异。其目标过滤、持久化预算、装备守恒与安全退出可作为设计保留候选，尚未证明运行等价。
- 所有运行检查及 L1/L2/L3 均为 NOT RUN；本轮只有文档/研究产物，未执行 Gradle 构建，不追加 Release 用 UPDATE_NOTES。

完整报告位于工作区外层 `docs/rebuild/REBUILD_REPORT_R1.md`，功能矩阵为 `FEATURE_REBUILD_MATRIX.md`，行为与实战方案为 `BEHAVIOR_BASELINES.md`，证据位于 `docs/rebuild/evidence/r1/`。这些共享资料和 `reverse-engineering/` 不在本 Git 根内，本次提交仅保存本摘要，不包含反编译源码、上游 JAR 或工具。

停止点：人工确认 R1 结论及行为取舍后再进入 R2。无推送、发布或分支切换。
