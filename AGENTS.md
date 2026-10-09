# 26.3 工程补充约束

遵循根目录 AGENTS.md、MOB_TWEAKS_SPEC.md、CODEX_STAGE1_TASKS.md 和 CODE_REVIEW.md。

- 唯一目标为本目录，Java 25、Wrapper、官方未混淆命名，保持 common/client 隔离。
- 正式阶段以外层 PLAN.md 为准；历史 R2-A/R2-B/R2-C 对应 R3/R4-A/R4-B，见 docs/STAGE_INDEX.md。用户已进行骷髅/僵尸实机验收并反馈木剑掉落，本次 R4-A 修复见 docs/R4A_SWORD_FIX.md；下一批为 R4-C 女巫/唤魔者/恼鬼。阶段进入授权已收到，但按 VERSION_MIGRATION.md，具体新 Migration Plan 输出后须单独等待批准；本轮不实施 R4-C、不扩展 S3/Boss、不发布。S2 仅历史对照。
- Mod ID buildupmobtweaks，包 com.davidblackcn.buildupmobtweaks；项目 MIT，第三方许可独立保留。上游核心参考为 R0/R1 已核验、恢复的官方发行源码；查阅外层 docs/rebuild/ 的原始 JAR 哈希、许可证及行为证据，不把上游潜在 Bug 当作必须复制的行为。
- Fzzy Config 真实发布 API，不内嵌依赖；所有行为经 FeatureRegistry 独立开关。配置 v9，当前字段见 docs/R2A_REBUILD.md；未知附件版本原样保留，出生特性/装备不重抽，不补发丢失物品。
- 高影响技能互斥、可预判、有次数/冷却预算。三叉戟只保存所有权引用，无备用物品。恼鬼世界索引保留卸载名额、销毁释放；升级前未加载旧恼鬼的追溯限制必须披露。
- 尸壳三层沙地下潜遵循 mobGriefing，中断/重载恢复地表与物理；在途技能不恢复，冷却保存。标签是退出/过滤，不代表通用第三方武器或领地兼容。
- 新增附魔注册、战利品和玩法全部排除。Boss/末地让位仅设计，不预设 Stellarity 检测 ID。
- 默认客户端和服务端共用 run/，顺序执行；GameTest、持久化和并行多人必须隔离。缓存、下载、测试世界、日志和构建 JAR 不提交。
- 未执行验证标 NOT RUN；构建/窗口加载不替代 GUI、视觉、完整袭击与多人。旧报告保留历史证据，不伪装为当前重新执行。

用户持续授权每次任务完成验证及自检后，按 Gitmoji + Conventional Commits 做一次本地 commit，包含适用 UPDATE_NOTES.md；不推送、不发布。只提交本轮内容。父目录共享文档在本 Git 根之外，须披露未纳入提交。
