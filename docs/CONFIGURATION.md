# 配置（S1-A）

安装 Fzzy Config `0.7.7+fix3+26.3`、Fabric Language Kotlin `1.14.1+kotlin.2.4.20`、Fabric API 和 Fabric Loader。项目 JAR 不内嵌这些依赖。可选客户端 Mod Menu `21.0.0` 仅提供入口；服务端不需要它。

Fzzy 使用 `RegisterType.BOTH` 注册 `buildupmobtweaks:main`。文件位于游戏运行目录 `config/buildupmobtweaks/main.toml`。专用服务端读自己的文件，客户端修改本地文件不能改写服务端规则；GUI 更新沿用 Fzzy 的权限及同步流程。修改权限设为等级 4，普通玩家可查询状态。实际双客户端同步与越权测试尚待人工验收。

入口：装有 Mod Menu 时选择 Buildup Mob Tweaks 的配置按钮；无 Mod Menu 时进入世界，执行 Fzzy 的 `/configure buildupmobtweaks.main`。界面点击验收尚未完成，命令入口依据本次发布版源码确认。

| 分组/选项 | 默认值 | 范围与作用 | 生效时机 |
|---|---|---|---|
| general.enabled | true | 统一功能总开关；状态命令不受影响 | GUI 修改应用后，下次命令调用 |
| general.diagnosticProbe | true | 独立开关，仅允许诊断演示回复 | 同上 |
| performance.diagnosticLines | 1 | 每次回复 1–5 行；越界时校正并写回 | 同上 |

`hostile`、`neutral`、`passive`、`bosses`、`equipment`、`traits`、`fixes`、`compatibility` 是明确标注的空分组，没有玩法或无效占位开关。`villagers` 只保留规范中的扩展位置，未添加字段。后续已验收行为逐项增加默认开启的独立选项，不用分组总开关代替。

只读状态：`/buildupmobtweaks status`。演示：`/buildupmobtweaks probe`（等级 4 / 服务端控制台）。演示仅向命令执行者回复，不创建物品、不扫描或修改实体、不注册 tick 回调、不广播给其他玩家。关闭功能后命令返回失败结果 0。

直接编辑 TOML 应先停止对应游戏/服务端，保存后重启读取。不要假设原版 `/reload` 会重新读取磁盘配置。GUI 已应用的更新按 Fzzy 同步流程生效，FeatureRegistry 每次调用读取当前值；不缓存开关。当前三个设置均不要求重载世界或重启，依赖/Mod 变动则要求重启。

## 版本演进

- `@Version(1)` 是第一版配置；S0 没有本项目配置文件，无旧格式需要转移。
- 稳定保持 mod ID、文件名、分组和字段键。新增字段采用默认值，保留现有已知字段的用户值。
- 删除/改名/改变字段语义前必须增加版本并实现 Fzzy `Config.update(int)` 迁移，保留配置备份并测试旧版本样本；当前没有虚构的升级映射。
- 不手动修改自动生成的 `version`，不把未来版本配置当作已支持的回退格式。降级前备份整个配置目录。
- Fzzy 默认 `SaveType.OVERWRITE` 的同步配置会写入客户端本地文件；不要把该文件当作某个服务器之外独立的规则。联机前如需保留本地值请备份。

接口依据：[发布源码仓库](https://github.com/fzzyhmstrs/fconfig)、[官方 Maven](https://maven.fzzyhmstrs.me/me/fzzyhmstrs/fzzy_config/0.7.7+fix3+26.3/)、[配置文档](https://moddedmc.wiki/en/project/fzzy-config/docs/config-design/New-Configs)。本轮直接读取与二进制同版本的 sources JAR，核对 ConfigApiJava、Config、ConfigSection、ValidatedBoolean、ValidatedInt、RegisterType 和 Version。
