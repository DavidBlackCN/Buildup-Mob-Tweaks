# 26.3 工程约束

遵循工作区外层 AGENTS.md、CODE_REVIEW.md、VERSION_MIGRATION.md 和用户最新授权。唯一目标版本为本目录；其他版本，尤其26.3-tests，只读。

- Java25、Gradle9.7.1、Loom1.18.3、Minecraft26.3官方未混淆名称；依赖以gradle.properties和build.gradle为准，不猜测API或升级版本。
- Mod ID为buildupmobtweaks；common/client严格隔离，保持独立开关、真实装备所有权、目标退出与未知附件版本原样保存。
- 不恢复旧S2冲突AI/Mixin，不自动接管Boss，不新增附魔、奖励或装备成长。Boss已由用户延期至Phase2完成后，后续实施仍需明确授权。
- QA和GameTest只用于独立开发源集/包，不进入正式主包或源码包。行为验证使用真实Server Tick及原版Goal/Brain，不以直接调用Goal代替验收。
- 代码、资源和构建修改必须用Wrapper构建并按外层CODE_REVIEW.md自检；纯文档修改检查内容、链接与差异，不虚报测试。
- 缓存、下载、测试存档、日志和构建JAR不提交。开发报告、阶段方案、验收台账与原始证据统一保留在版本Git之外的工作区根目录；本仓库docs仅保留玩家/管理员使用文档。
- 原有开发文档已归档到工作区根目录development-records/26.3；外层docs/rebuild保存原始证据。工作区路线和最新验收状态由外层PHASE1_RESTORATION_PLAN.md与用户消息确定，不从旧报告推断新授权。
- Phase2-A共享设计草案位于外层[MOB_DESIGN_PHILOSOPHY.md](../MOB_DESIGN_PHILOSOPHY.md)，并链接分级、特性、装备、配置与兼容规范。v0.1待人工审核，未批准前不得据此修改Phase1行为、配置或保存格式；草案不授权Boss或后续功能实施。共享文档不在本版本Git内，检索入口见外层README.md。
- 用户持续授权任务完成、验证和自检后做一次本地commit，遵循Gitmoji + Conventional Commits，包含适用UPDATE_NOTES；不推送、不发布、不切换分支。明确说明工作区根目录归档不在版本提交内。
