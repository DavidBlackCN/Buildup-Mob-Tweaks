# R4-E2-c 自检

Code Review: **PASS WITH RISKS**（按外层CODE_REVIEW.md）。

Scope：26.3目标分支完整本次差异，起始干净44260ac；仅四ID、相应客户端/QA/测试/矩阵/文档。26.3-tests只读；共享文档明确由本任务涉及。

Findings：BLOCKER 无；MAJOR 无。MINOR：测试先前未控制部分随机夹具导致失败；已限范围调整输入/边界并保留全部原断言。旧唤魔者保存回归一次未复现失败保留日志和诊断，不能称根因已确认。

| 规范项 | 自检结论 |
| --- | --- |
| 版本/Wrapper/映射/依赖/许可 | PASS：本项目已解析26.3源码/javap/实际编译，不猜Yarn；正式元数据/Wrapper/依赖/NOTICE/LICENSE未更改 |
| 调用链/注册/类型边界 | PASS：仅原生Illusioner/Vindicator，common不引用client类，client注入在client JSON；新Goal仅支援5/TARGET，无旧S2复活 |
| Mixin/Access Widener | PASS：完整描述符及Monster泛型字段经javap；HEAD/TAIL/RETURN注入，无ordinal/local猜测；实际GameTest/客户端/专服加载，无新Access Widener |
| 生命周期/未来版本/状态 | PASS：v1未知不接管、不重抽、不补发；VISUAL/runtime不保存、箭有owner/期限和卸载清理；不删除外部武器/箭/目标 |
| 阶段互斥/预算/危险路径 | PASS：原生施法/弓Goal保持；位置四个/安全检查、swap200、clone120/TTL200；同盟过滤仅标记自有箭、扫描8候选/20tick/有界距离 |
| 实际行为/兼容 | PASS：真实伤害/位移/箭源/附魔/TargetGoal/原Raid波次；不以直接Goal调用/测试数/编译代替证据 |
| QA隔离/资源/翻译 | PASS：QA独立产物，正式及源码包无QA/GameTest/probe；JSON可解析、中英键相等、翻译反映实际双箭替代和阶段恢复 |
| 旧断言/用户改动/旧工程 | PASS：起始干净；5个旧测试组的balanced assert调用序列相等；旧169文件/HEAD/分支/状态快照相等，未回退/覆盖用户修改 |
| Update Notes | PASS：追加两条Gitmoji + Conventional Commits，描述已验证正式行为/独立QA，不记录未来开发 |
| 文档/证据/停止点 | PASS：当前77/47/QA25/298与历史快照区分；早期失败/有限上游/五波受控击杀/人工NOT RUN明示；仅建议下一批，不执行 |

Fixes Applied During Review：真实分身箭过射修正原生目标1/3高度；原生onProjectileSpawned保持附魔；行走幻影anchor同步；同盟8格实际球形限制；限定左持弓模型镜像；双箭替代时恢复原射箭声。测试API先查实际床集合签名；猪灵reload恢复fixture RNG、末影人保存fixture隔离其合法随机放块、旧配置与三种备用剑死亡守恒测试提供合法目标、旧手部fixture距离6、附魔伤害断言含边界。两次持剑前置失败的原始目标状态未记录，不能称随机取敌是已证实的唯一根因。自检还修复了PowerShell管道造成的本次UPDATE_NOTES编码丢失，历史记录保持原样。以上不修改已有对应正式AI或移除旧断言。

Verification：目标Wrapper build+qaJar/298原生GameTest、独立客户端、QA专服、正式专服静态冷重启、资源/包隔离/旧快照/断言审计、diff --check，详见R4E2C_REBUILD和外层after.json。Mixin环境未发生注入错误。开发客户端的Realms认证与假测试玩家关闭连接日志属于已知测试环境现象，不冒充真实多人连接验证。

Residual Risks：人工肉眼/连续完整玩家袭击、完整两版量化L3、默认自然概率分布、多人整合包/第三方武器和性能NOT RUN；五波自动验证有受控击杀和临时测试强加载，不承诺正式性能。旧唤魔者一次随机fixture失败仍需关注，诊断后多轮通过但没有充分根因证据；旧天然随机散布/Goal取得目标存在采样波动，不能以改变正式精度规避。上游Allay历史BLOCKED未重试。上述非发布就绪声明，本批提交后停止等待人工反馈。
