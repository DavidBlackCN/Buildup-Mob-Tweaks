# R4-E0 工作报告

日期2026-10-09。唯一目标`26.3/`，分支`26.3`，起始HEAD `6eec6387a33d23a98f63c15f78a77b1ea0891423`，起始工作树干净。用户只批准Phase1 R4-E0，本批不进入E1、Boss、R5、Phase2/3；26.3-tests只读。**R4-E0自动交付 PASS WITH RISKS；人工专项仍NOT RUN，Phase1未完成。**

## 完成范围与取舍

| 产物 | 结果 / 原因 |
| --- | --- |
| [PHASE1_FEATURE_DISPOSITION](PHASE1_FEATURE_DISPOSITION.md) | 68/68唯一裁决：PORT1、ADAPT50、REPLACE5、DEFER8、EXCLUDE4；每项来源、两版类/配置/默认值及旧源码引用、当前false、后续批次/门槛、相互影响和验证计划。裁决不是行为PASS |
| [PHASE1_ACTIVE_FEATURES](PHASE1_ACTIVE_FEATURES.md) | 56已启用ID逐入口/独立gate/相关测试索引和缺口；原135项保持不改，未恢复旧S2入口 |
| [PHASE1_UPSTREAM_GAPS](PHASE1_UPSTREAM_GAPS.md) | 38候选涵盖生态/宠物/山羊角/嗅探兽/史莱姆/幻翼/修复/Boss/工具奖励；271个两版Mixin声明都有归属。声明库存覆盖不等于所有分支恢复 |
| [PHASE1_QA_GUIDE](PHASE1_QA_GUIDE.md) | 独立开发Mod/源集及OP命令：spawn、force、inspect、target、check、off、cleanup；五标杆自动布置/恢复、真实Goal执行，正常停止退场 |
| [PHASE1_IMPLEMENTATION_PLAN](PHASE1_IMPLEMENTATION_PLAN.md) | E1-a/b/c及E2～E4具体名单、依赖、逐小批验收和停止点；下一批明确为焦骸/凋灵骷髅四项，未实施 |
| 剩余台账/装备说明/统一验收/阶段索引/README/AGENTS | 增加当前指向，保留历史正文。修正旧装备文档误导：只有池加载与查询，出生分配并未运行 |

当前生产白名单仍56项，`src/main`/`src/client`与既有行为测试字节均与基线相同。仅构建增加独立qa源集/qaJar和可选qaServer/qaClient运行配置，gametest包含同一QA实现和新场景安全测试。配置v9、ID、持久化格式、原Mixin JSON/AW、依赖均未改变。原版/上游反编译资料不打进任何JAR，Allay保留历史BLOCKED且不重试。

装备数据流/旧EquipmentService差异及E2迁移方案在取舍表末节。未register服务与不支持的ID意味着“配置默认开启”不代表装备分配；没有通过白名单扩张制造完成度。与初始SPEC的Boss目标/旧PLAN不自动Boss边界有冲突，本批按Phase1最新路线仅列候选，未来独立审批。

## 实际执行与行为证据

Java25.0.3 / Gradle Wrapper9.7.1（SHA锁定）/ Loom1.18.3 / MC26.3官方未混淆命名，无mappings/Yarn / Loader0.19.5 / API0.162.0+26.3 / Fzzy0.7.7+fix3+26.3 / Kotlin1.14.1+kotlin.2.4.20；可选ModMenu21未增加。服务器配置快照、工具/发行包SHA256、旧工程逐文件及Git状态核验在[verification.json](../../docs/rebuild/evidence/r4e0/verification.json)。上游两版输入再次核对R0已记哈希，版权/来源保持。

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.3'
.\gradlew.bat build qaJar -PtestEula=true --console=plain
```

| 检查 | 本轮状态 | 证据及限度 |
| --- | --- | --- |
| Wrapper完整构建+所有必要真实调度测试 | **PASS** | [build-6.log](../../docs/rebuild/evidence/r4e0/build-6.log)：136项全部通过，原135+1连续五场景/安全测试；不直接调用生产Goal或手工tick AI |
| QA五标杆实际执行 | **PASS / L2** | 同日志五条R4E0_QA_PROOF：掠夺者2次切换、真实弩箭；骷髅走射/真实箭/2次切剑；末影人1阶段3传送3攻击和实际伤害；唤魔者1施法3释放；门盾2实际半扇且禁用退场 |
| QA安全和恢复 | **PASS（限定）** | 真实tick中选中旧僵尸→门→2tick退出，birth/门状态恢复；外部木剑/耐久/新增标签保留，实际冷却不返还；未知v99拒绝，block entity场地拒绝；非OP权限拒绝 |
| 独立QA JAR的专服指令 | **PASS** | `python ../docs/rebuild/scripts/r4e0_server.py v4`；[qa-server-v4.json](../../docs/rebuild/evidence/r4e0/qa-server-v4.json)和[原始日志](../../docs/rebuild/evidence/r4e0/qa-server-v4.log)：实际从mods加载独立QA包，5场景/inspect/check/off/cleanup，地板屋顶恢复与无夹具实体哨兵，正常save-all/stop exit0，无解析错误/CME |
| 门盾反馈定向复核 | **PASS（自动）** | 既有材质/损伤/死亡/重载/转换/禁用回归原样；`python ../docs/rebuild/scripts/r4_feedback_client.py r4e0 r4-feedback-client-reload-fixed`复用只在独立世界安装的诊断probe；[客户端回执](../../docs/rebuild/evidence/r4-feedback-1/client-r4e0.json)：36实际RenderState坐标样本、true标志/半扇、转向、已保存世界再载、正常关服，无集合修改异常；**不是肉眼验收** |
| 骷髅备用木剑反馈复核 | **PASS（自动）** | 最终build原测试S02_generated_death：3种生成剑0、原弓3；S02_reload_death：外部剑/弓各1组件保留；S02_generated_reload_disable生成剑0原弓1；S02_generated_orphan_external外部默认木剑1、生成剑0、弓1；旧无来源迁移歧义仍沿历史说明 |
| 正式/源码JAR隔离、独立QA包边界 | **PASS** | verification脚本检ZIP/class/JSON/源码，无QA类/指令/GameTest/原版类进入正式或正式源码包；QA包不包含生产rebuild类、原版类或新附魔 |
| 实际默认概率 | **PASS（限定出生夹具）/其余NOT RUN** | 原已定义的finalizeSpawn样本随全量测试重跑，不改样本数/容差/种子。不是自然刷怪器统计；QA固定状态不能代替随机分布 |
| 新的完整上游L3对照 | **NOT RUN** | 本轮无新生产AI；两版源/配置审计PASS、原有限L3历史证据保留，不宣称所有候选等效 |
| 用户原环境肉眼、独立QA客户端GUI、双人配置/网络、整合包/性能 | **NOT RUN** | 须用户短清单及后续授权，不能由窗口打开/测试数提升 |
| Allay恢复 | **BLOCKED（历史）** | 按用户不再尝试，不阻断E0；不因此宣称原作者全部功能恢复 |

独立QA是开发测试能力：配置gate仍权威；具体状态/目标/装备前置由fixture提供，生产Selector自行仲裁。场景的target移动也是明确的输入，不是手工推动Goal。工具只支持五种定向场景，其他已有机制提供真实GameTest夹具索引；通用Brain记忆查询、任意技能强制、崩溃/卸载恢复不在最小工具保证范围。

## 失败历史与修正

所有失败日志保留，没有删除/改原R4测试、放宽断言或白名单排除失败项。build-1是CompoundTag.get真实返回Tag而非Optional的编译错误；build-2是Brigadier checked异常传递；均按构建真实签名修正。build-3五场景与136项首次PASS，build-4清理边界调整后PASS，build-5权限/外物/冷却边界后PASS，build-6最终PASS。没有把编译失败称PASS。

专服v1仅生产Mod加载，QA指令未知：可选Loom运行配置并不自动加载独立qa源集，改为在唯一隔离mods目录加载实包。v2/v3场景生效，但查询标签解析失败；v2旧回执脚本的文字哨兵判断过松，**不列完整指令PASS**。查目标26.3 EntitySelectorOptions.tag实际使用readUnquotedString，改开发标记为`buildupmobtweaks_qa_fixture`，最终v4零解析错误，哨兵必须实际Server输出。选择器取证位于外层reverse-engineering/26.3-r4e0-selector，未修改其输出。与主生产标签/数据不发生迁移。

审查又限定清理只移除会话中新产生的同主人弹体/恼鬼、保留原已有子实体，查询范围96格；只移除工具自己添加的退出标签，不回滚外部新标签；地形只恢复工具布置且仍为石头的块，不覆盖外部修改/方块实体。服务端配置/世界规则不由工具自动更改。脚本的100TPS和spawn_mobs/mob_griefing只在其独立废弃专服世界设置。

## 产物与交付边界

- 正式包：`build/libs/buildup-mob-tweaks-1.0.0.jar`；正式源码包同目录`-sources.jar`。
- **开发QA包**：`build/libs/buildup-mob-tweaks-1.0.0-qa.jar`，仅测试世界单独安装，SHA256/大小见verification.json。
- 目标Git文档及源码见本报告上表，自动原始证据/脚本在外层`docs/rebuild/evidence/r4e0/`、`docs/rebuild/scripts/r4e0_*.py`；门盾新客户端日志沿原取证目录以r4e0唯一前缀保留。
- 外层PHASE1计划/PLAN/功能矩阵及证据不在26.3 Git根，**不随本地提交覆盖**；旧工程逐文件/Git不变，无推送/发布/切分支。

| 包 | 大小（字节） | SHA256 |
| --- | ---: | --- |
| 正式JAR（与起始生产包完全一致） | 344106 | `5d492fefe9bdfa6998167d22d675e105998d427768f98c8f59ccb758cab0f15e` |
| 正式源码JAR | 162178 | `61e08fb6b9c069e04b43bae53cffd7f4dcf143cb7980bbaa63316a7fe0524cc3` |
| 独立开发QA JAR | 28414 | `bb910334b4800077ca603875270d3c01e7f99bf7c61228983eea72f4f819893a` |

`UPDATE_NOTES.md`追加开发验收说明（不声称新增正式生物AI）。完整[自检](R4E0_CODE_REVIEW.md)：PASS WITH RISKS，无未解决BLOCKER/MAJOR。所有未验证项和最小工具界限公开，未把全R4/Phase1视为已人工验收。

## 人工验收与下一批

请按[QA指南末尾短清单](PHASE1_QA_GUIDE.md)在5～10分钟内运行五场景，重点门盾连续转向/禁用/死后清理和骷髅生成剑不掉落、真实外部剑保留；普通玩家无法操作，正式实例无QA指令。记录当前MC/依赖/Mod列表和日志。若单项未触发，先check装备/目标/gate/冷却/原生Goal，不重复刷稀有怪。

下一批建议**R4-E1-a：焦骸旋转位移、已持弓凋灵骷髅使用弓、有限头颅攻击及追踪**，完整具体任务/依赖见[实施计划](PHASE1_IMPLEMENTATION_PLAN.md)。本轮到此停止，等待用户确认，不自行开始。
