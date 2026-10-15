# 26.3 工程约束

当前阶段（2026-10-15）：Phase 3-A2.5 Farmer Behavior Refinement 已完成工程与限定自动验证，见外层[实现报告](../development-records/phase3-a2.5/IMPLEMENTATION_REPORT.md)、[测试](../development-records/phase3-a2.5/TEST_REPORT.md)和[A2→A3 交接](../development-records/phase3-a2.5/A2_TO_A3_HANDOFF.md)。Core 与 Farmer 默认均关闭；持久格式与 Phase 2 行为保持不变。提交后等待人工审核，未进入 Profession Activity；下方旧阶段描述保留历史时点，用户最新任务优先。

遵循工作区外层 AGENTS.md、CODE_REVIEW.md、VERSION_MIGRATION.md 和用户最新授权。唯一目标版本为本目录；其他版本，尤其26.3-tests，只读。

- Java25、Gradle9.7.1、Loom1.18.3、Minecraft26.3官方未混淆名称；依赖以gradle.properties和build.gradle为准，不猜测API或升级版本。
- Mod ID为buildupmobtweaks；common/client严格隔离，保持独立开关、真实装备所有权、目标退出与未知附件版本原样保存。
- 不恢复旧S2冲突AI/Mixin，不自动接管Boss，不新增附魔、奖励或装备成长。Boss按用户最新安排暂缓至Phase2核心规范确定后再规划，后续实施仍需明确授权。
- QA和GameTest只用于独立开发源集/包，不进入正式主包或源码包。行为验证使用真实Server Tick及原版Goal/Brain，不以直接调用Goal代替验收。
- 代码、资源和构建修改必须用Wrapper构建并按外层CODE_REVIEW.md自检；纯文档修改检查内容、链接与差异，不虚报测试。
- 缓存、下载、测试存档、日志和构建JAR不提交。开发报告、阶段方案、验收台账与原始证据统一保留在版本Git之外的工作区根目录；本仓库docs仅保留玩家/管理员使用文档。
- 原有开发文档已归档到工作区根目录development-records/26.3；外层docs/rebuild保存原始证据。工作区路线和最新验收状态由外层PHASE1_RESTORATION_PLAN.md与用户消息确定，不从旧报告推断新授权。
- Phase2-A共享设计见外层[设计索引](../docs/phase2-design/README.md)，当前为v0.3设计规范候选版，已纳入Vanilla Friendly、两层抽取与Mixed优先等共识，并区分待冻结参数。候选文档等待人工审核，实际代码、配置和保存格式的调整以之后明确的实施任务为准；共享文档不在本版本Git内。
- Phase2-B-0静态架构审计见外层[审计索引](../development-records/phase2-b-0/README.md)，含141项FeatureId矩阵及概率、装备、存档分析。本条保留审计依据，后续分类由B-1候选文档细化，不新增实施授权。
- Phase2-B-1已按用户最新任务完成文档分类与状态边界候选，见外层[分类矩阵](../development-records/phase2-b-1/TRAIT_CLASSIFICATION_MATRIX.md)、[状态归属](../development-records/phase2-b-1/STATE_OWNERSHIP_SPEC.md)和[生成来源](../development-records/phase2-b-1/SPAWN_RULE_SPEC.md)。本轮B-1替代旧计划中的直接架构实施安排；等待人工审核，未实施Trait框架、概率／存档迁移或API，B-2仍需明确任务。
- Phase2-B本轮B-2-1～B-2-6首批七项已实现，见外层[架构报告](../development-records/phase2-b-implementation/ARCHITECTURE_REPORT.md)与[人工验收](../development-records/phase2-b-implementation/FINAL_ACCEPTANCE.md)。用户确认默认保持Phase1行为：unified_selection默认false，新规则可选，旧有效概率等价映射，潜沙／孢子新概率未配置。旧模块附件继续分立，旧TraitService未启用；后续候选与发布默认值待审核。等待人工总体验收，不自动进入Phase3。
- 用户持续授权任务完成、验证和自检后做一次本地commit，遵循Gitmoji + Conventional Commits，包含适用UPDATE_NOTES；不推送、不发布、不切换分支。明确说明工作区根目录归档不在版本提交内。
