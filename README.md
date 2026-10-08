# Buildup Mob Tweaks — 26.3

S0 基线，仅有初始化入口与用户模板原有的空示例 Mixin，尚未加入生物玩法。

| 项目 | 版本/标识 |
|---|---|
| Minecraft | 26.3 |
| Java | 25（本轮 Oracle JDK 25.0.3） |
| Gradle Wrapper | 9.7.1；已配置分发 ZIP 官方 SHA-256；Wrapper JAR 实测匹配官方值 |
| Fabric Loom | 1.18.3（固定原模板实际解析版本） |
| Fabric Loader | 0.19.5 |
| Fabric API | 0.162.0+26.3 |
| 命名 | 26.3 官方未混淆命名；无 Yarn/mappings 依赖 |
| Mod ID / 名称 | buildupmobtweaks / Buildup Mob Tweaks |
| Java 包 / Maven group | com.davidblackcn.buildupmobtweaks |
| 模板版本号 | 1.0.0（保留模板值，不代表已发布） |

## 本地验证

在本目录执行，设置仅对当前 PowerShell 生效：

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.3'
.\gradlew.bat --version
.\gradlew.bat build
.\gradlew.bat runClient
# 先退出客户端，再运行服务端；模板共用 run/。
.\gradlew.bat runServer --args=nogui
```

服务端首次运行要求阅读并同意 Minecraft EULA。本轮用户已明确同意，仅本地 `run/eula.txt` 设置为 true。测试服务端绑定 `127.0.0.1:25585`；这些运行文件不属于发布配置。命令行服务端退出使用 `stop`。

本轮构建和启动证据、环境警告及未测项目见 [测试报告](../docs/TEST_REPORT.md)。客户端窗口初始化不等于单机世界/联机测试完成；S0 没有功能测试套件，Gradle 的 `test NO-SOURCE` 不能当成测试用例通过。

## 范围与来源

设计以 [SPEC](../MOB_TWEAKS_SPEC.md) 为准。Fzzy Config、Traits、装备池和 Boss Profile 暂不实现，新增附魔全部排除。正式生物功能需独立开关，只有验收通过的功能才默认开启。

此工程保留用户生成的 Fabric 模板结构和 common/client 源码分离。模板 [LICENSE](LICENSE) 为 CC0-1.0，未被改写；请参阅 [NOTICE](NOTICE.md) 区分模板许可与上游参考许可。本项目不是 Mob AI Tweaks 官方续作。

用户已授权在本目录初始化 Git 并进行本地提交，`.github/workflows/build.yml` 因而位于 Git 根内；未执行远端 CI、推送或发布。父目录共享规范与 docs/ 审计文档不在此 Git 仓库内，单独 clone 本仓库不会包含它们。以后每次任务收尾提交一次，见 AGENTS.md。
