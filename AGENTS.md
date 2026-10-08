# 26.3 / S0 补充约束

遵循根目录 AGENTS.md、MOB_TWEAKS_SPEC.md 与 CODEX_STAGE1_TASKS.md，不替代既有规则。

- 唯一工程目标为本目录，Java 25；使用 Wrapper，保持 common/client 隔离及官方未混淆命名。
- S0 完成报告后停止；没有用户新指令不得开始 S1、生物 AI 或 Boss 实现。
- Mod ID 为 `buildupmobtweaks`，包名沿用 `com.davidblackcn.buildupmobtweaks`。
- 上游复制前检查 ../docs/UPSTREAM_AUDIT.md 的许可阻塞；JAR 不是源码基线。
- 不引入新增附魔、相关战利品/玩法；Fzzy Config、Traits、装备、Boss Owner 只预留设计。
- 客户端与服务端共用 run/，顺序执行；不将缓存、测试世界、日志、下载 ZIP 或构建 JAR 纳入源码交付。
- 未运行的验证明确标记 NOT RUN，所有玩法当前未实现；不凭编译通过宣称兼容。

用户持续授权：本目录已初始化 Git。每次会话任务收尾、完成验证与自检后，按 Gitmoji + Conventional Commits 做一次本地 commit，包含适用 UPDATE_NOTES.md；不自动推送或发布。仅提交当前任务内容，不把 PARTIAL 阶段标为完成。父目录 docs/ 与共享规范在 Git 范围之外，必须披露，不暗自改变 Git 根目录。
