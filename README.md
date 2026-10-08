# Buildup Mob Tweaks — 26.3

Minecraft 原版增强项目，采用 MIT。S1-A 已获用户验收。本轮实现 S1-B：牛和僵尸系的持久化 Traits 诊断标记、互斥抽取和生命周期验证。正式生物 AI、装备池、Boss 与村民机制尚未实现。详见 [Traits 说明](docs/TRAITS.md) 与 [S1-B 报告](docs/S1B_REPORT.md)。

| 项目 | 版本/标识 |
|---|---|
| Minecraft / Java | 26.3 / 25（本轮 Oracle JDK 25.0.3） |
| Gradle Wrapper / Loom | 9.7.1 / 1.18.3 |
| Fabric Loader / API | 0.19.5 / 0.162.0+26.3 |
| Fzzy Config | 0.7.7+fix3+26.3，必须单独安装 |
| Fabric Language Kotlin | 1.14.1+kotlin.2.4.20，必须单独安装 |
| Mod Menu | 21.0.0，仅客户端可选 |
| 命名 | 26.3 官方未混淆命名；无 Yarn/mappings 依赖 |
| Mod ID / Java 包 | buildupmobtweaks / com.davidblackcn.buildupmobtweaks |
| 模板版本号 | 1.0.0（保留模板值，不代表已发布） |

依赖来源：[Fzzy Config](https://modrinth.com/mod/fzzy-config/version/YkOumqzV)、[Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin/version/eRRZzGMc)、[Mod Menu](https://modrinth.com/mod/modmenu/version/kyy7dbrZ)。配置文件、中文/英文入口、服务端权限、生效时机及迁移策略见 [配置说明](docs/CONFIGURATION.md)。Fzzy 和 Mod Menu 不内嵌于本项目 JAR。

## 本地验证

在本目录执行，环境变量只对当前 PowerShell 生效：

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.3'
.\gradlew.bat build -PtestEula=true
.\gradlew.bat runClient
# 可选：开发客户端同时加载 Mod Menu
.\gradlew.bat runClient -PwithModMenu=true
# 先退出客户端，模板共用 run/；服务端控制台输入 stop 退出
.\gradlew.bat runServer --args=nogui
```

服务端首次运行要求阅读并同意 Minecraft EULA。用户已明确同意，本地开发与隔离测试目录据此设置 eula=true；不将接受协议的运行文件提交到 Git。测试服务端绑定 `127.0.0.1:25585`；这些运行文件不属于发布配置。并行联机测试须先隔离客户端与服务端运行目录。

控制台命令：`buildupmobtweaks status` 和 `buildupmobtweaks probe`。后者仅限等级 4 / 控制台，独立开关与总开关均有效。默认回复 1 行，可配置 1–5 行；不改世界或实体。新增的服务端 GameTest 通过 build/check 执行；不是独立 JUnit 套件。查询实体使用 `buildupmobtweaks traits <单个实体选择器>`；持久化脚本见 Traits 说明。

## 许可与范围

本项目使用 [MIT License](LICENSE)，版权属于 DavidBlackCN 与项目贡献者。Fabric 模板原 CC0 来源及外部依赖许可见 [NOTICE](NOTICE.md)。项目参考 Mob AI Tweaks，当前未复制其实现或素材，也不是其官方续作。

新增附魔的注册、战利品及关联玩法全部排除。Stellarity 等项目的 Boss 让位机制只保留设计方向，不宣称已兼容。本轮停在 S1-B；S1-C 需要人工验收与下一条指令。

工作区共享资料：[SPEC](../MOB_TWEAKS_SPEC.md)、[任务表](../CODEX_STAGE1_TASKS.md)、[功能矩阵](../docs/FEATURE_MATRIX.md)、[上游审计](../docs/UPSTREAM_AUDIT.md)、[历史 S0 报告](../docs/TEST_REPORT.md)。这些父目录文件不在本 Git 仓库内，单独 clone 不会包含它们；本目录 docs/ 的配置和当前阶段报告会随提交保存。

用户持续授权每次任务收尾做一次本地 commit；不自动推送或发布。未执行远端 CI。
