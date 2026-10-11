# E3-a / E3-b 联合验收反馈复核（2026-10-11）

目标仅26.3分支，初始干净HEAD `3ff9ea3d007f4c073c47acb567fa68873db37801`。本批是用户四项验收反馈的判定和必要修复，不是E3-c或其他新功能批。未恢复旧S2、不升级环境/依赖/映射/配置、不推送发布。133声明/96支持/37未生效、配置v11、QA44场景保持。

## 四项判定

| 反馈 | 判定 | 证据与处理 |
|---|---|---|
| chicken持剑幼年僵尸正常索敌时双手下垂 | **预期，保留** | 26.3官方 `AnimationUtils.animateZombieArms` 对 `isBaby && 主手非空` 使用零基础抬臂角，挥击另计算；不受 aggressive 真假改变基础角。最终实际客户端三组各379个模型样本：持剑幼年0.019rad、空手幼年/持剑成年均约-2.075rad。真实鸡骑士原生ZombieAttackGoal仍运行、实际扑击3次。不改服务端攻击标志、客户端模型或姿态Mixin |
| horse冲刺后一直保持抬身 | **真实缺陷，修复** | 26.3 `AbstractHorse.setStanding(int)` 总会开启flag32；传0时standCounter=0，原版tick只对正倒计时做清除，故不自然退出。修复使用公开原生 `clearStanding()`，覆盖冲刺开始、命中、超时/碰撞退出与取消；只释放本Goal拥有的预告姿态，保留无关原生动作 |
| rider箭约3格后从下方掉头 | **预期QA输入，保留** | E3aQa.observe等原箭真正飞离射手且LeftOwner=true，再移到马下身前4格、设向后速度。这是独立工具的同树碰撞验收；正式实现只过滤canHitEntity，没有任何改位置/速度逻辑。新增正常骑乘原生弓Goal测试实际向前飞行7样本、无反转、目标真实扣血、坐骑1000HP。既有返回箭/三叉戟正负例仍保留 |
| trap不同骷髅误伤后互相报复 | **预期原版报复，保留** | 原版AbstractSkeleton装优先级1 HurtByTargetGoal，忽略数组为空；TRAP_ORIGIN只标记事件后代，不是阵营/联盟。真实陷阱触发四骑手后，以真实箭伤观察：Stray受WitherSkeleton箭伤和原版Skeleton受Skeleton箭伤均降至998.1568HP、原生targetSelector自然锁定射手，sameTree=false。本轮不增加跨坐骑同族免伤/联盟；已批准的rider安全规则仅当前同树 |

用户已开展联合人工验收，但本次四条反馈不自动等于全部E3-a/b清单通过。未反馈项及补修肉眼复验保持独立记录。

## 实现与夹具边界

- `RidingGoal.java`：替换错误退出API；临时姿态所有权不存档，不改20刻预告、200刻冷却、至多20刻冲刺、接触伤害上限4和已有目标/装备链。
- `E3aQa.java`：仅horse场景将骑手/马普通移动速度设为0，确保200刻冷却前不靠普通步行提前到达目标；不设NoAI，不直接调用Goal，不加速冷却，不改全局概率。冲刺仍由真实世界物理和 .6水平冲量执行。正式AI普通移动速度未改。
- `E3AcceptanceTests.java`及环境/入口：实际Server Tick/Goal调度、真实箭和原生陷阱；马完成、接触、独立关闭、NoAI、下马、目标退出、骑手死亡及无关原版抬身保护。旧掠夺者测试只增加失败诊断，不改条件、期限或生产代码。
- 外层诊断客户端为独立临时mod，实际客户端实体包/原版Renderer/模型计算；不进入正式主包、源码包或独立QA包。没有新生产客户端代码/Mixin/AW/依赖，配置格式保持v11。

## 验证记录

证据在外层 `docs/rebuild/evidence/r4e3-acceptance-1/`，脚本在 `docs/rebuild/scripts/`，不在26.3 Git根内。

- `before-fix-build.log`：完整408项运行中，六项马退出检查失败，均standing=true/standAnim=1；接触路径在该运行原版计时自行退出。原生向前箭及两个陷阱报复基准通过，旧397项通过；失败日志保留。
- `after-fix-build-1.log`：七种马退出均standing=false/standAnim=0，新增11项通过；全套仍有2项失败：旧掠夺者50刻近战断言、旧horse QA在修正后不再意外冻结马。前者时序根因尚未证明；后者日志显示普通步行10.76格且未触发冲刺，按上文固定QA步行输入修正，不弱化charges>0断言。
- `client-1.json`：实际客户端 **限定PASS**，退出0；20个抬身预告样本、61个恢复样本、原版马模型body.xRot=0、charges=1，真实鸡骑士pounces=3。三组幼年/成年手势模型对照通过；人工截图观感与全部多人时序不在这个自动证据中通过。
- 最终 Wrapper `build qaJar -PtestEula=true --console=plain --no-configuration-cache`、最终日志与审计收据见本批收尾记录。编译或测试数量不替代上述行为判定。

## 人工补验（尚待确认）

1. 更换正式/独立QA JAR后，重新生成 `horse`：看到20刻预告和真实冲刺，随后回到普通姿态；等待冷却再试。预告中分别关闭技能、设NoAI、下马或移除目标，确认姿态释放。
2. `chicken` 持主手武器时幼年僵尸下垂手势应保留；原版挥击、伤害和扑击仍正常。空手幼年/成年为原版对照，不要求持剑幼年常驻抬臂。
3. `rider` 继续看到工具刻意返回的箭，check的 returnedArrows/projectileSkips 和vehicleHP用于判定；在无QA场景的普通骑乘战斗验证正常向前箭，不把返回夹具当正式轨迹。
4. `trap` 保留跨坐骑误伤报复；同一次事件仍4马4骑手/四类型各一、装备守恒。此反馈不是通用同族防误伤功能的授权。
5. 原E3-a/b其余清单与掠夺者近战/开关回归继续记录；完整两版L3、多人整合包/保护与长期性能 **NOT RUN**。本批收尾后停止，不进入新阶段。

## 最终结果与自检

最终 `after-fix-build-4.log`：Wrapper build/qaJar **PASS，409/409必需测试通过**（原397+本批12）。`after-fix-build-2.log` 的408项也全部通过；随后自检补充临时姿态所有权和第12项真实调度保护测试，既有冷却中Goal被取消不会抹除原版另行触发的抬身。build-3仅旧horse接触夹具失败：charges=1、hits=0、目标已被原弓伤害，结束距离3.55格；具体失败时序根因未完全证明，不改变旧碰撞断言或生产预算，日志保留。最终生产代码的实际客户端证据 `client-2.json` **限定PASS**，具体样本/退出在收据；不复用client-1冒充最终改动的验证。

Code Review: **PASS WITH RISKS**。Scope：本批完整diff、目标26.3官方命名/Java25/Wrapper9.7.1/Loom1.18.3/Loader0.19.5/FAPI0.162.0+26.3、common/client边界、原生公开API、瞬时姿态所有权、12项测试与真实输入、旧测试仅诊断、独立QA、文档和Release说明。BLOCKER: None；MAJOR: None。MINOR：旧掠夺者近战/horse接触夹具此前时序失败，根因未完全证明；生产代码与原断言/期限不变，风险保留，不以重跑通过宣称根因已修。Fixes Applied During Review：错误setStanding(0)退出、七种退出与非自有抬身保护、horse QA普通步行输入。未增加Mixin/AW或依赖，没有持久化格式变化或新功能接入。

Verification：最终Wrapper/实际GameTest与客户端、JSON重复键/标签/文档链接/正式与源码/独立QA隔离、旧169跟踪文件/两版官方JAR/既有恢复证据哈希、Git差异校验见 `../../docs/rebuild/evidence/r4e3-acceptance-1/verify.json`。Update Notes: PASS，本地提交含说明。父目录共享计划、脚本和日志在26.3 Git根外，未纳入该提交。

Residual Risks：补修肉眼确认、完整联合清单、完整两版上游量化L3、多人/复杂AI/保护Mod与长期性能NOT RUN；旧夹具时序波动尚未完全诊断。无开发阻断，收尾后停止等待反馈，不自动进入下一批。
