# MIT 调整与 S1-A 阶段报告

日期：2026-10-08（Asia/Shanghai）。Git 目标：26.3/，基于 f407fe6；用户已授权本次许可调整、进入下一步及任务收尾本地提交。

## 本阶段完成情况

- 阶段/子批次：MIT 许可调整 + S1-A 配置与模块注册。
- 结果：**许可调整 PASS；S1-A PARTIAL**。代码和专用服务端路径已验证；GUI 点击、单机内置服务器和真实多人同步尚未验收。不开始 S1-B。
- CORE-01–04：已实现（IMPLEMENTED），不标为完整 TESTED。Fzzy 真实发布版、10 个分组、中英说明、独立诊断开关、数值限制、BOTH 同步注册、等级 4 修改权限、配置版本 1 与迁移策略。
- 新增 FeatureId、声明与 FeatureRegistry 统一查询；只有 diagnostic_probe 一个无玩法演示。不预先登记未实现的生物功能。
- 不含 Traits、装备池、Boss、村民或新增附魔；没有复制 Mob AI Tweaks 实现/资源。

| 改动文件 | 原因 |
|---|---|
| LICENSE、NOTICE.md、licenses/Fabric-template-CC0.txt | 项目改 MIT；原模板 CC0 原文另存；第三方许可单独说明 |
| build.gradle、gradle.properties、fabric.mod.json | 固定 Fzzy/Kotlin/可选 Mod Menu；MIT 元数据；二进制及源码 JAR 包含来源说明 |
| BuildupMobTweaks.java、config/BuildupConfig.java | common 入口注册配置、分组、校验、权限与版本 |
| feature/FeatureId.java、feature/FeatureRegistry.java | 稳定功能标识、声明及总/独立开关统一查询 |
| command/DiagnosticCommands.java | 只读状态、等级 4 的有界诊断回复；不修改世界 |
| assets/buildupmobtweaks/lang/{en_us,zh_cn}.json | 29 个对应语言键 |
| README.md、AGENTS.md、docs/CONFIGURATION.md、本报告、UPDATE_NOTES.md | 清理过时 S0/CC0 项目说明、记录实际范围与验证 |
| 父目录 README.md、docs/UPSTREAM_AUDIT.md、FEATURE_MATRIX.md、TEST_REPORT.md | 更新现状；S0 报告加历史标注。位于本 Git 根之外，不随本次提交保存 |

Java 路径保持 src/main/java/com/davidblackcn/buildupmobtweaks/。未修改 Wrapper、用户原有空示例 Mixin、其他版本工程、远端或 Git 身份。

## 验证记录

环境：Windows / Oracle JDK 25.0.3（C:\Program Files\Java\jdk-25.0.3）、MC 26.3、Gradle 9.7.1、Loom 1.18.3、Loader 0.19.5、Fabric API 0.162.0+26.3，官方未混淆命名。

| 命令/测试 | 环境 | 结果 | 证据 |
|---|---|---|---|
| `$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.3'; .\gradlew.bat build` | 完整新依赖 | PASS，exit 0，21s | Java/common/client 编译，二进制与源码 JAR 生成 |
| `.\gradlew.bat build --console=plain` | 最终代码/元数据/打包 | PASS，exit 0，13s | 7 tasks，3 executed；test NO-SOURCE，不计单元测试通过 |
| `.\gradlew.bat --console=plain --no-configuration-cache -I .gradle/s1a/server-input.gradle runServer --args=nogui` | 回环 127.0.0.1:25585；无 Mod Menu | PASS，6 轮全部 exit 0 | 下表；每轮 status/probe、save-all flush、stop |
| `.\gradlew.bat runClient --console=plain -PwithModMenu=true` | Fzzy + Kotlin + Mod Menu 21.0.0 | 启动 PASS；完整交互 NOT RUN | 11:53:44 读取 main 配置；11:53:45 SDL window；11:53:47 资源/音频初始化。保留窗口供人工检查，未把启动任务记作已正常退出 |
| 不装 Mod Menu 的客户端 | 当前 S1-A | NOT RUN | 纯服务端无 Mod Menu 已测；客户端无入口时使用 Fzzy configure 命令待验收 |
| 中英 JSON/键、JAR 许可/依赖/禁用内容扫描 | 最终产物 | PASS | 两侧 29 个键对应；MIT 元数据与许可一致；无内嵌 Fzzy、上游类或新增附魔数据 |
| GUI 实际操作、单机、双客户端同步/权限隔离 | 真实游戏 | NOT RUN | Windows computer-use 的 node_repl 在初始化时失败：helper_unknown_error: setup refresh had errors；已请求可选人工反馈 |
| git diff --check | 当前改动 | PASS | 仅既有 LF/CRLF 转换提醒，无空白错误 |

本机测试控制脚本 `.gradle/s1a/run-server-test.ps1 -Case <名称>` 使用 .NET 重定向标准输入调用上述 Wrapper 命令。临时 init 脚本仅将 runServer 的 standardInput 设为 System.in；不改 Mod 行为。脚本、配置快照与原始日志保留在忽略目录 `.gradle/s1a/`，不进入提交。

| Case | 输入配置（总开关 / 独立开关 / 行数） | 实际结果 |
|---|---|---|
| default | true / true / 1 | enabled=true，probe 1/1 |
| disabled | true / false / 3 | enabled=false，返回 disabled，无演示回复 |
| upper-bound | true / true / 99 | Fzzy 警告并写回 5，probe 1/5 至 5/5 |
| master-off-lower-bound | false / true / -8 | Fzzy 警告并写回 1，enabled=false；总开关优先 |
| saved-three | true / true / 3 | probe 1/3 至 3/3，正常保存退出 |
| restart-three | 不再改配置，重启上一轮 | 仍为 true / true / 3，回复三行，正常保存退出 |

六轮都达到 Done，按命令保存、停止，无客户端类缺失或配置注册崩溃。测试结束将本地配置恢复默认。命令行控制器首次因工作目录错误未找到 gradlew.bat（exit 1）；改为从脚本位置解析项目路径后成功，未掩盖该环境失败。越界告警是预期测试输出。

客户端保留开发账号 user-properties HTTP 401 与 Realms FabricMC token 错误；这些不表示本 Mod 崩溃，也不代表正版认证或联机已经通过。未以本次窗口初始化证明 GUI 可用。

二进制 JAR SHA-256：`902634e82a7429b9c61ea1ac2c9e90c105999084de4ada40909982c28beb4a51`（36 entries）。
Sources JAR SHA-256：`4756550b607268673fc75c9e32755ed4dd52d87c3e684b3992cb90c40be1e734`（33 entries）。
两者均包含 MIT 的 LICENSE_buildup-mob-tweaks、NOTICE.md、licenses/Fabric-template-CC0.txt。产物仅供本地验证，未发布。

## 风险与遗留

- **验收余项**：GUI 点击、中英排版、单机权限/即时更新、多人权威同步与普通玩家越权拒绝尚未测试；S1-A 不得宣称全部通过，也不据此发布。
- 源码/API：读取发布者同版本 sources JAR；Fzzy 二进制 SHA-1 `373b6365bb9f447d2810c151ec2f698ee789366f` 与 Modrinth YkOumqzV 元数据相同。Kotlin eRRZzGMc 与 Mod Menu kyy7dbrZ 均明确列 26.3。
- 依赖：Fzzy 自带 tomlkt/Jankson/Permissions API；显式声明 Loader、API、Kotlin，禁用其 POM 自动引入可选 Mod Menu。Mod Menu 仅 clientRuntimeOnly、由开发参数启用；Fzzy 不 include、不改其 TDL-M 许可。
- 本项目 MIT 与 Mob AI Tweaks 主分支许可标识一致；这不消除历史 ZIP 的许可/来源边界。上游 L1 仅阻止直接复制，独立实现配置不受影响。原模板 CC0 记录保留，Gradle Wrapper 原 Apache 声明保持不变。
- 配置版本只有 1；旧配置迁移是明确策略，未虚构旧版本迁移实现或升级测试。
- 共用 run/，未来真实同时联机前需要隔离目录，避免日志和配置互相覆盖。旧存档、性能、其他 Mod/Boss 兼容都不在本轮测试范围。

## 自检

Code Review: **PASS WITH RISKS**（实现可审查，阶段验收仍 PARTIAL）。

- Scope：本表所有 26.3 修改与四份共享文档；没有覆盖用户已有未提交改动。
- BLOCKER（已实现代码）：无已发现编译/加载错误；完整 S1-A 验收仍缺真实 GUI/联机证据。
- MAJOR：无已确认代码缺陷；服务端权限与同步的真实多人路径未测，暂不作发布承诺。
- MINOR：无遗留格式错误；自检中统一新增入口缩进、补元数据末尾换行并限定 Maven 组范围。
- common 代码只使用 Fzzy 公共配置 API、Fabric server command API 与 Minecraft common 类；没有增加 Mixin/反射、扫描回调、装备或附魔注册。
- Verification：构建、六轮服务端、客户端依赖初始化、JSON 和许可打包检查通过；缺失项目逐一 NOT RUN。
- Update Notes：仅记录实际 MIT 调整和已验证的服务端配置诊断，不声称完整 GUI/同步验收通过。

## 人工验收清单

- [ ] Mod Menu → Buildup Mob Tweaks 配置页可打开；中英文名称/说明及空分组提示显示正确。
- [ ] 无 Mod Menu 时，入世界后 `/configure buildupmobtweaks.main` 能打开配置。
- [ ] 单机修改 diagnosticProbe：probe 停用/恢复；总开关关闭时子开关不能绕过；行数限制有效。
- [ ] GUI 应用修改后立即生效；退出重启后仍保留。这里要验证 GUI 保存，而非重复已经测过的手工 TOML 重启。
- [ ] 两名客户端连入独立目录的测试服：等级 4 修改被双方同步，普通玩家只能查看，不能改写服务器配置。
- [ ] 客户端本地冲突值不能覆盖服务端；重连、新入服和断线返回单机时配置行为符合说明。
- [ ] 核对 MIT 版权、独立依赖许可和此 PARTIAL 报告；确认不把未实现分组当作已有玩法。

## 下一阶段建议

下一任务块先收口 **S1-A 人工 GUI / 单机 / 多人验收**。通过且获得下一条指令后再做 S1-B Traits；真实上游代码迁移仍需独立处理来源/许可及 Migration Plan 审批。不自动推送或发布。

**当前停止在 S1-A，等待人工验收与下一条指令。**
