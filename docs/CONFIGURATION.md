# 配置（S1-A / S1-B）

安装 Fzzy Config `0.7.7+fix3+26.3`、Fabric Language Kotlin `1.14.1+kotlin.2.4.20`、Fabric API 和 Fabric Loader。项目 JAR 不内嵌这些依赖。可选客户端 Mod Menu `21.0.0` 仅提供入口；服务端不需要它。

Fzzy 使用 `RegisterType.BOTH` 注册 `buildupmobtweaks:main`。文件位于游戏运行目录 `config/buildupmobtweaks/main.toml`。专用服务端读自己的文件，客户端修改本地文件不能改写服务端规则；GUI 更新沿用 Fzzy 的权限及同步流程。修改权限设为等级 4，普通玩家可查询状态。S1-A 已获用户人工验收确认；原始自动测试边界保留于阶段报告。

入口：装有 Mod Menu 时选择 Buildup Mob Tweaks 的配置按钮；无 Mod Menu 时进入世界，执行 Fzzy 的 `/configure buildupmobtweaks.main`。S1-A 界面验收已由用户确认，S1-B 新增字段仍需人工复查。

| 分组/选项 | 默认值 | 范围与作用 | 生效时机 |
|---|---|---|---|
| general.enabled | true | 统一功能总开关；状态命令不受影响 | GUI 应用后，下次功能查询或新实体初始化 |
| general.diagnosticProbe | true | 独立开关，仅允许诊断演示回复 | 同上 |
| performance.diagnosticLines | 1 | 每次回复 1–5 行；越界时校正并写回 | 同上 |

`hostile`、`neutral`、`passive`、`bosses`、`equipment`、`fixes`、`compatibility` 是明确标注的空分组，没有玩法或无效占位开关。`villagers` 只保留规范中的扩展位置，未添加字段。后续已验收行为逐项增加默认开启的独立选项，不用分组总开关代替。

只读状态：`/buildupmobtweaks status`。演示：`/buildupmobtweaks probe`（等级 4 / 服务端控制台）。演示仅向命令执行者回复，不创建物品、不扫描或修改实体、不注册 tick 回调、不广播给其他玩家。关闭功能后命令返回失败结果 0。

直接编辑 TOML 应先停止对应游戏/服务端，保存后重启读取。不要假设原版 `/reload` 会重新读取磁盘配置。GUI 已应用的更新按 Fzzy 同步流程生效，FeatureRegistry 每次调用读取当前值；不缓存开关。S1-A 的三个设置均不要求重载世界或重启，依赖/Mod 变动则要求重启。

## 版本演进

- 当前 `@Version(2)`，由 v1 增加 traits 字段，不改已有键名；由 Fzzy 加载默认字段和写回版本，已测试保留 v1 的 diagnosticProbe=false、diagnosticLines=3。无需改名转换逻辑。
- 稳定保持 mod ID、文件名、分组和字段键。新增字段采用默认值，保留现有已知字段的用户值。
- 删除/改名/改变字段语义前必须增加版本并实现 Fzzy `Config.update(int)` 迁移，保留配置备份并测试旧版本样本；当前没有虚构的升级映射。
- 不手动修改自动生成的 `version`，不把未来版本配置当作已支持的回退格式。降级前备份整个配置目录。
- Fzzy 默认 `SaveType.OVERWRITE` 的同步配置会写入客户端本地文件；不要把该文件当作某个服务器之外独立的规则。联机前如需保留本地值请备份。

接口依据：[发布源码仓库](https://github.com/fzzyhmstrs/fconfig)、[官方 Maven](https://maven.fzzyhmstrs.me/me/fzzyhmstrs/fzzy_config/0.7.7+fix3+26.3/)、[配置文档](https://moddedmc.wiki/en/project/fzzy-config/docs/config-design/New-Configs)。本轮直接读取与二进制同版本的 sources JAR，核对 ConfigApiJava、Config、ConfigSection、ValidatedBoolean、ValidatedInt、RegisterType 和 Version。

## S1-B Traits

traits 分组已加入总开关、三项独立诊断标记开关及 cow / zombieFamily 两套千分权重。开关立即影响 active 查询；权重只影响新实体。存档与互斥规则见 [Traits 说明](TRAITS.md)。配置版本 2 与实体 Traits 数据版本 1 是不同的版本号。
