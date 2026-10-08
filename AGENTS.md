# 26.3 工程补充约束

遵循根目录 AGENTS.md、MOB_TWEAKS_SPEC.md、CODEX_STAGE1_TASKS.md 和 CODE_REVIEW.md。

- 唯一工程目标为本目录，Java 25；使用 Wrapper，保持 common/client 隔离及官方未混淆命名。
- S1-A 至 S2-B 已获用户验收或后续授权。本轮为 S2-C1 袭击核心批次；整个 S2-C 仍 PARTIAL。完成后停止，后续 S2-C 子批次、S2-D 或 Boss 需新指令。
- Mod ID 为 `buildupmobtweaks`，包名为 `com.davidblackcn.buildupmobtweaks`；本项目使用 MIT，第三方许可独立保留。
- 上游复制前检查 ../docs/UPSTREAM_AUDIT.md 的许可边界；JAR 不是源码基线。
- Fzzy Config 使用真实发布 API，不内嵌其 JAR；所有正式功能通过 FeatureRegistry 查询独立开关。新增附魔及其战利品/玩法全部排除。
- 牛和僵尸系的旧诊断 Traits 保留；三种骷髅、僵尸/尸壳使用各自持久特性与冷却；溺尸三叉戟附件仅保存引用与掉率，禁止备用物品补发；装备池仅对新僵尸/尸壳/溺尸尝试一次、只填空槽、保持掉率。未知 Traits/装备分配数据保留且不重试；袭击新冷却独立保存，女巫未完成前摇在重载时取消；恼鬼仅局部加载范围计数，不宣称全局硬上限。Boss Owner、村民不实现。
- 默认客户端与服务端共用 run/，顺序执行；持久化脚本与 GameTest 使用独立目录，并行联机验收同样必须隔离。26.3 Tag 必须在原版正式绑定后解析，规则数据归当前服务端资源快照所有。缓存、测试世界、日志、下载 ZIP 或构建 JAR 不提交。
- 未运行验证明确标记 NOT RUN；构建及窗口启动不替代 GUI/单机/多人验收。

用户持续授权：每次会话任务收尾、完成验证与自检后，按 Gitmoji + Conventional Commits 做一次本地 commit，包含适用 UPDATE_NOTES.md；不自动推送或发布。仅提交当前任务内容，不把 PARTIAL 阶段标为完成。父目录 docs/ 与共享规范在 Git 范围之外，必须披露，不暗自改变 Git 根目录。
