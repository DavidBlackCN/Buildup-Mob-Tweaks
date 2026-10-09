> R2-A 提示：本文保留旧 S2 配置说明。当前只有掠夺者重建功能生效，其他旧行为休眠；现行差异见 [R2A_REBUILD.md](R2A_REBUILD.md)。

# 配置（S1-A 至 S2）

安装 Fzzy Config `0.7.7+fix3+26.3`、Fabric Language Kotlin `1.14.1+kotlin.2.4.20`、Fabric API 和 Fabric Loader。项目 JAR 不内嵌这些依赖。可选客户端 Mod Menu `21.0.0` 仅提供入口；服务端不需要它。

Fzzy 使用 `RegisterType.BOTH` 注册 `buildupmobtweaks:main`。文件位于游戏运行目录 `config/buildupmobtweaks/main.toml`。专用服务端读自己的文件，客户端修改本地文件不能改写服务端规则；GUI 更新沿用 Fzzy 的权限及同步流程。修改权限设为等级 4，普通玩家可查询状态。S1-A 已获用户人工验收确认；原始自动测试边界保留于阶段报告。

入口：装有 Mod Menu 时选择 Buildup Mob Tweaks 的配置按钮；无 Mod Menu 时进入世界，执行 Fzzy 的 `/configure buildupmobtweaks.main`。S1-A 界面验收已由用户确认，S1-B 已获用户验收；S1-C 已获用户验收；S2-A 已获后续授权；S2-B 已获后续授权；S2-C1 已获后续授权；S2-C2 恼鬼字段与实际多人同步仍需人工复查。

| 分组/选项 | 默认值 | 范围与作用 | 生效时机 |
|---|---|---|---|
| general.enabled | true | 统一功能总开关；状态命令不受影响 | GUI 应用后，下次功能查询或新实体初始化 |
| general.diagnosticProbe | true | 独立开关，仅允许诊断演示回复 | 同上 |
| performance.diagnosticLines | 1 | 每次回复 1–5 行；越界时校正并写回 | 同上 |

`neutral`、`passive`、`bosses`、`fixes`、`compatibility` 是明确标注的空分组，没有玩法或无效占位开关。`villagers` 只保留规范中的扩展位置，未添加字段。后续已验收行为逐项增加默认开启的独立选项，不用分组总开关代替。

只读状态：`/buildupmobtweaks status`。演示：`/buildupmobtweaks probe`（等级 4 / 服务端控制台）。演示仅向命令执行者回复，不创建物品、不扫描或修改实体、不注册 tick 回调、不广播给其他玩家。关闭功能后命令返回失败结果 0。

直接编辑 TOML 应先停止对应游戏/服务端，保存后重启读取。不要假设原版 `/reload` 会重新读取磁盘配置。GUI 已应用的更新按 Fzzy 同步流程生效，FeatureRegistry 每次调用读取当前值；不缓存开关。S1-A 的三个设置均不要求重载世界或重启，依赖/Mod 变动则要求重启。

## 版本演进

- 当前 `@Version(8)`：v1→v2 增加 traits，v2→v3 增加 equipment，v3→v4 增加 hostile.skeleton，v4→v5 增加 hostile.zombie / hostile.drowned，v5→v6 增加 hostile.raid，v6→v7 增加 hostile.vex，v7→v8 增加 hostile.extended；保留旧键。真实服务端升级已保留 diagnosticProbe=false、diagnosticLines=4、traits.commonMarker=false。由 Fzzy 添加默认字段和写回版本，无需改名转换逻辑。
- 稳定保持 mod ID、文件名、分组和字段键。新增字段采用默认值，保留现有已知字段的用户值。
- 删除/改名/改变字段语义前必须增加版本并实现 Fzzy `Config.update(int)` 迁移，保留配置备份并测试旧版本样本；当前没有虚构的升级映射。
- 不手动修改自动生成的 `version`，不把未来版本配置当作已支持的回退格式。降级前备份整个配置目录。
- Fzzy 默认 `SaveType.OVERWRITE` 的同步配置会写入客户端本地文件；不要把该文件当作某个服务器之外独立的规则。联机前如需保留本地值请备份。

接口依据：[发布源码仓库](https://github.com/fzzyhmstrs/fconfig)、[官方 Maven](https://maven.fzzyhmstrs.me/me/fzzyhmstrs/fzzy_config/0.7.7+fix3+26.3/)、[配置文档](https://moddedmc.wiki/en/project/fzzy-config/docs/config-design/New-Configs)。本轮直接读取与二进制同版本的 sources JAR，核对 ConfigApiJava、Config、ConfigSection、ValidatedBoolean、ValidatedInt、RegisterType 和 Version。

## S1-B Traits

traits 分组已加入总开关、三项独立诊断标记开关及 cow / zombieFamily 两套千分权重。开关立即影响 active 查询；权重只影响新实体。存档与互斥规则见 [Traits 说明](TRAITS.md)。S1-B 当时引入配置版本 2（当前为 8）；配置版本与实体 Traits 数据版本 1 是不同的版本号。

## S1-C 装备

`equipment.poolAssignment` 默认 true，是独立功能开关，仍受 general.enabled 控制。`equipment.assignmentChance` 默认 100，范围 0–1000，单位千分比。两项 GUI 应用后仅影响以后出生的实体；不移除已有装备，不对已有实体重试。与 Traits 启停和概率完全独立。

JSON 决定实体/物品匹配、环境过滤和权重；配置控制总开关和总体概率。数据包 `/reload` 不重读磁盘 TOML。详见 [装备池说明](EQUIPMENT_POOLS.md) 和 [S1-C 报告](S1C_REPORT.md)。
## S2-A 骷髅战斗

hostile.skeleton 的七项独立行为默认开启，三种千分概率默认 70/70/10；基础装备行为与 Traits 解耦。详细条件、关闭回退、生效时机和持久化边界见 [骷髅战斗说明](SKELETON_COMBAT.md)。

## S2-B 僵尸系与溺尸

hostile.zombie 增加五个行为开关、四套千分权重和独立冷却；hostile.drowned 增加三个开关与回收超时。八项默认开启。高级特性受 Traits 控制，基础盾牌和三叉戟独立；总开关关闭仍安全清理已有三叉戟归属。完整默认值、升级与关闭语义见 [僵尸系说明](ZOMBIE_COMBAT.md)。

## S2-C1 袭击核心批次

hostile.raid 新增七项默认开启的独立行为和四个数值配置；不依赖高级 Traits。v5→v6 保留旧字段；持久冷却和前摇取消规则见 [袭击战斗](RAID_COMBAT.md)。新增字段 GUI 和真实多人同步仍需人工验收。

## S2-C2 恼鬼冲刺

hostile.vex 增加 fixedCharge、recoveryPause、closeRangeGuard 三项独立开关，默认 true；recoveryTicks 默认 20（10–60 tick），minimumChargeDistance 默认 3（2–6 格）。不依赖 Traits。配置 v7；开关、恢复预留、重载中断和 40 tick 上限的精确语义见 [恼鬼说明](VEX_COMBAT.md)。GUI 字段、实际多人服务端同步本轮仍需人工验收。
## S2 收口

当前 v8 新增 91 个独立行为开关及概率/冷却数值，默认开启；修复与战斗开关独立，字段、出生抽取、关闭边界、数据包退出标签见 [S2_COMBAT](S2_COMBAT.md)。所有新增开关已做逐项独立门控回归，369 对双语键完成静态校验；GUI 视觉与真实多人同步仍列人工验收。

真实独立服 v7→v8 已保留 diagnosticProbe=false、diagnosticLines=3，并写入新增默认值；同一世界第二次启动验证关闭 evoker_fireball 和 skeleton_aim_fix 后各自为 false，已有高级特性/冷却不被删除。旧批次 v7 历史测试不改写成 v8。

## 正式 R4-C 当前生效字段

配置 v9 不变，历史 S1/S2 章节保留，不代表全部旧功能生效。R4-C 启用 hostile.raid 的 witchWindup / witchThrowCooldown / evokerVexLimit / evokerSummonCooldown 和对应参数；hostile.vex 的 fixedCharge / recoveryPause / closeRangeGuard / recoveryTicks / minimumChargeDistance；hostile.extended 的 witch_leaping_potion / witch_jump_throw / evoker_fireball / evoker_totem / evoker_fireball_no_fire、两项 chance 和 special_cooldown。默认值、瞬态中断和出生概率仅影响新实体等语义见 [R4C_REBUILD](R4C_REBUILD.md)。门控受 general.enabled，出生特性还受 traits.enabled。未批准的 evoker_flee_speed、evoker_avoid_target_fix、vex_projectile_weakness 仍不生效。
