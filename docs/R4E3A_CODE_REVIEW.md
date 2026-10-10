# E3-a Code Review

**Code Review: PASS WITH RISKS**；2026-10-10，用户已批准骑乘安全，开发范围覆盖、产品阻塞解除，完整a人工待验。下文首批审查保留历史；本轮增量审查见末尾和[R4E3A_RIDER_ADDENDUM](R4E3A_RIDER_ADDENDUM.md)。没有以代码自检代替用户验收。

Scope：审查 `git diff` 和本批全部 untracked 源码/资源/文档；干净起点f8602e3，目标26.3、分支26.3。新relations/riding、5个窄Mixin、注册/白名单、SupportGoal优先级、两种类型标签、双语描述、独立E3aQa、35项GameTest及唯一旧猪灵夹具调整；build.gradle仅添加测试include，无依赖/版本迁移。规划/实施/矩阵/验收/UPDATE_NOTES包含于审查，共享文档单独留存。

Findings：

- BLOCKER（实现代码）：无已发现项；产品范围BLOCKED另记，不称整a完成。
- MAJOR（已修）：未知旧状态不能安装新Goal；关系不阻止原5优先级支援；Brigadier合并不能放开旧命令权限；转化保留装备和既有出生/冷却；QA失败清理不泄漏会话，cleanup不注入伤害。相关原断言/新增严格断言通过。
- MINOR/风险：原猪灵185tick已发射但目标HP未下降的两次失败原因未证实；夹具保留原发射期限，实际命中分开225tick断言。外部客户端首轮可见性失败、标记有效时绘制通过，但不能证明所有同步时序。详见报告。

Correctness：关系只拥有自己设置的目标，原玩家/村民/报复/Johnny/支援优先；精确类型/治疗/模式/LOS/20tick/16格/16候选。骑乘主骑手与现有坐骑，NoAI/死亡/目标/关门/卸载取消，有限冷却/安全空间和接触预算，原Goal攻击不复制。原SpearUseGoal已覆盖不安装第二个。原陷阱事件4+4、三个追加骑手移交实际装备。原convertTo仅精确Skeleton，外部备用品实际转交与一次后续死亡，生成剑退役；不循环转化。持久状态version，未知版本原样、不恢复在途动作。临时动作与持久冷却分离。

Compatibility：仅目标official命名，本地原版13类签名/字节码核验；common无client引用，新Mixin为Mob.tick TAIL、HurtBy.canUse HEAD/TargetGoal continuation、SkeletonTrapGoal两个原生添加调用ModifyArg、LivingEntity.die HEAD精确过滤。最终GameTest/正式专服/QA专服/实际客户端均启动，未发现本批注入失败。没有改AW、元数据兼容范围、框架、全局伤害或旧S2文件；类型排除标签继承已有排除规则，但不承诺任意第三方AI/武器兼容。

Verification：

- Wrapper `build qaJar -PtestEula=true --console=plain --no-configuration-cache`：PASS，最终336必需测试（301旧+35新），真实Tick/Goal/Brain/弹体；不是直接调用Goal。
- 最终正式专服/冷重启、独立QA专服8场景及GameTest9场景：限定PASS，完整组合NOT RUN。
- 客户端client-2实际门盾/弩/8组钓线提交：限定PASS，肉眼骑乘动画NOT RUN。
- Beta上游关系/致死转化：LIMITED；自身advancement错误保留；完整L3 NOT RUN。
- 旧26.3-tests169跟踪文件、两上游JAR/18恢复源码哈希：PASS，未改。
- JSON61文件（允许已有BOM）、双语key集、正式/源码包无QA，独立QA包含E3aQa：PASS。
- UPDATE_NOTES：PASS，追加两条Gitmoji + Conventional Commits，只记录九项已验证行为与窄报复修正。
- Git diff --check、文档新链接/状态/支持数：PASS，详见外层verification.json。

Residual Risks：完整人工/默认自然出生分布、所有出生源/生命周期组合、多人整合包与长期性能NOT RUN；两个历史夹具首次失败精确原因保留；专服flat设置解析提示不是Mod无错世界生成验收。骑乘过滤LEGACY_ONLY取舍待用户明确答复。上述风险不通过测试数量掩盖。

本地commit只归档已验证九项及对应文档，不代表整a或人工验收通过；不推送/发布，不进入下一组。

## 用户裁决后的最终增量自检

以上BLOCKED/九项/336项是前批历史。用户已批准窄安全修正，当前开发范围已覆盖十项、原矛REPLACE、日烧DEFER；整体人工仍NOT RUN。新提交起点d860821，工作树干净，无用户改动被覆盖。

审查RiderProjectileMixin、FeatureRegistry、瞬时计数、双语描述、QA rider场景及其LeftOwner/返回箭/清理、RiderProjectileTests14项、原35项QA升级与马接触夹具调整、测试source include/入口和全部文档。无依赖、版本、AW、配置持久格式变更。common新增注入为精确Projectile.canHitEntity(Entity)Z HEAD，仅server AbstractArrow及有效Mob射手，实时树关系和独立门，负例保持原生结果；不缓存/扫描树、不改hurt，不提供其他树/玩家/其他伤害免疫。已有clone箭独立过滤保持。

必需Wrapper最终build-3全350实际调度PASS；14项含原弓Goal真的发射/离开/返回，实际箭/三叉戟HP差异。新增QA实际返回箭断言防初始query计数假阳性；最终独立专服开启/关闭的马HP差异以及清理PASS。正式/源码包无QA；旧169跟踪文件/源JAR许可哈希、JSON/语言key集、diff检查和包SHA见新verification.json。UPDATE_NOTES新增Gitmoji + Conventional Commits窄安全记录，未重复前批九项。

增量BLOCKER/MAJOR：无已发现代码项。Code Review仍 **PASS WITH RISKS**：本轮首版6项失败和夹具调整全保留；前批猪灵首命中和客户端首次失败的精确历史原因未证实。新增骑乘客户端肉眼/全面网络时序、同树嵌套乘客全部组合、所有冷重启/出生源、自然分布、整合包/多人/性能NOT RUN；不得以350数目代替。对照来源无此功能，只能作为Buildup主动安全修正，完整上游L3未完成。

本轮验证自检后一次本地提交，根外共享文档/证据另存；停止等待整a人工，不推送发布，不进入E3-b/c。
