# 26.3 工程补充约束

遵循根目录 AGENTS.md、MOB_TWEAKS_SPEC.md、CODEX_STAGE1_TASKS.md 和 CODE_REVIEW.md。

Phase 1现以外层PHASE1_RESTORATION_PLAN.md及用户本轮具体授权为准；旧R4-D停止声明保留历史语境。R4-E0只审计取舍/独立QA与回归，结果见docs/R4E0_REPORT.md；E1～E4/Boss须逐子批授权，不进入R5/Phase2/Phase3。QA源集只用于gametest/独立qaJar，严禁打入正式主包/源码包；不得整体恢复旧S2服务/Mixin。

- 唯一目标为本目录，Java 25、Wrapper、官方未混淆命名，保持 common/client 隔离。
- 正式阶段以外层 PLAN.md 为准；历史 R2-A/R2-B/R2-C 对应 R3/R4-A/R4-B，见 docs/STAGE_INDEX.md。R4-C 已实施，用户明确授权直接执行 R4-D，完成后统一人工验收完整 R4，无需再次方案停顿；本批范围/结果见 docs/R4D_REBUILD.md，剩余未迁移 ID 见 docs/R4_REMAINING_AUDIT.md。停止在 R4-D，不自动进入 R5/S3/Boss，不发布。S2 仅历史对照。
- Mod ID buildupmobtweaks，包 com.davidblackcn.buildupmobtweaks；项目 MIT，第三方许可独立保留。上游核心参考为 R0/R1 已核验、恢复的官方发行源码；查阅外层 docs/rebuild/ 的原始 JAR 哈希、许可证及行为证据，不把上游潜在 Bug 当作必须复制的行为。
- Fzzy Config 真实发布 API，不内嵌依赖；所有行为经 FeatureRegistry 独立开关。配置 v9，当前字段见 docs/R2A_REBUILD.md；未知附件版本原样保留，出生特性/装备不重抽，不补发丢失物品。
- 高影响技能互斥、可预判、有次数/冷却预算。三叉戟只保存所有权引用，无备用物品。恼鬼世界索引保留卸载名额、销毁释放；升级前未加载旧恼鬼的追溯限制必须披露。
- 尸壳三层沙地下潜遵循 mobGriefing，中断/重载恢复地表与物理；在途技能不恢复，冷却保存。标签是退出/过滤，不代表通用第三方武器或领地兼容。
- 新增附魔注册、战利品和玩法全部排除。Boss/末地让位仅设计，不预设 Stellarity 检测 ID。
- 默认客户端和服务端共用 run/，顺序执行；GameTest、持久化和并行多人必须隔离。缓存、下载、测试世界、日志和构建 JAR 不提交。
- 未执行验证标 NOT RUN；构建/窗口加载不替代 GUI、视觉、完整袭击与多人。旧报告保留历史证据，不伪装为当前重新执行。

用户持续授权每次任务完成验证及自检后，按 Gitmoji + Conventional Commits 做一次本地 commit，包含适用 UPDATE_NOTES.md；不推送、不发布。只提交本轮内容。父目录共享文档在本 Git 根之外，须披露未纳入提交。

R4-E1-a已逐批批准实施，范围/停止点见docs/R4E1A_REBUILD.md；下一批E1-b仍待批准。SkeletonBehavior.supported范围保持旧三种，ExtendedSkeleton模块与附件独立；wither_skeleton_bow仅既有普通弓行为，出生发弓仍E2。

R4-E1-b本轮经用户“可以开始”明确批准；限定结果见docs/R4E1B_REBUILD.md，当前64支持/60未生效。先原版同类仇恨复现，保持Piglin Brain与ZP原Goal；独立QA共12场景，正式包无QA。E1-c/E2未批准，不能用路线批准扩大实施范围。旧“E1-b待批准”只表示E1-a历史时点。批末验证自检、本地提交后停止。

R4-E1-c本轮用户授权方案核验后直接实施，无需二次批准；范围/停止点见docs/R4E1C_REBUILD.md。两项新增及两个REPLACE，stray_omen_snow纠正为LEGACY_ONLY/Phase2 DEFER，当前66/58、QA15。上述E1-c待批准仅历史。旧S2不恢复，E2及后续不在本批；验证自检、本地提交后停止，人工与完整L3独立验收。
