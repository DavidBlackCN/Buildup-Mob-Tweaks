# 26.3 工程补充约束

遵循根目录 AGENTS.md、MOB_TWEAKS_SPEC.md、CODEX_STAGE1_TASKS.md 和 CODE_REVIEW.md。

- 唯一工程目标为本目录，Java 25；使用 Wrapper，保持 common/client 隔离及官方未混淆命名。
- 当前任务为用户已批准的 S1-A 配置与模块注册。完成该子阶段后停止，未获新指令不得开始 S1-B、生物 AI 或 Boss 实现。
- Mod ID 为 `buildupmobtweaks`，包名为 `com.davidblackcn.buildupmobtweaks`；本项目使用 MIT，第三方许可独立保留。
- 上游复制前检查 ../docs/UPSTREAM_AUDIT.md 的许可边界；JAR 不是源码基线。
- Fzzy Config 使用真实发布 API，不内嵌其 JAR；所有正式功能通过 FeatureRegistry 查询独立开关。新增附魔及其战利品/玩法全部排除。
- Traits、装备池、Boss Owner、村民只预留设计；空配置分组不表示玩法已实现。
- 客户端与服务端共用 run/，顺序执行；并行联机验收前需使用独立运行目录。缓存、测试世界、日志、下载 ZIP 或构建 JAR 不提交。
- 未运行验证明确标记 NOT RUN；构建及窗口启动不替代 GUI/单机/多人验收。

用户持续授权：每次会话任务收尾、完成验证与自检后，按 Gitmoji + Conventional Commits 做一次本地 commit，包含适用 UPDATE_NOTES.md；不自动推送或发布。仅提交当前任务内容，不把 PARTIAL 阶段标为完成。父目录 docs/ 与共享规范在 Git 范围之外，必须披露，不暗自改变 Git 根目录。
