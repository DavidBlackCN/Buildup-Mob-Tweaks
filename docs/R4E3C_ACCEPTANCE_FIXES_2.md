# E3-c 幼体体型与持弓位置补修

> 2026-10-11用户最新实测验收 **PASS**，允许进入下一阶段；本次体型/持弓缺陷收口，见[阶段收尾](R4E3_CLOSURE.md)。下文“用户补验NOT RUN”和停止点为原交付时点；第三方替换模型、多人及未提供的极端组合仍NOT RUN。本轮没有重新执行或改写原自动证据。

2026-10-11，用户实测指出baby仍为正常体型、弓手持位置偏移。唯一目标26.3/分支26.3，起始干净HEAD `3072e6e78c25beb998b55207d1bb4898d488d64f`。本次两项均为**真实缺陷**；保留其他主功能，不进入下一阶段。

## 根因与修复范围

26.3 `AbstractSkeletonRenderer` 为成年/幼体共享成年本体和盔甲模型。`AgeableMobRenderer.submit` 只根据isBaby选模型，`LivingEntityRenderer.submit` 应用的是属性scale，并不会自动对共享模型应用ageScale。服务器isBaby、半尺寸碰撞和眼高虽然正确，之前实际绘制仍是成年大小。上一轮的“保留半尺寸”只证实物理尺寸；盔甲贴图修复并未覆盖真实本体缩放，这次纠正该结论。

原生 `ItemInHandLayer.useBabyOffset` 则因isBaby采用短臂幼体网格的握持偏移 `(0,1,-4.5)/16`，共享成年骷髅手臂实际需要成年偏移 `(±1,2,-10)/16`。两个问题分开修正，不能只缩小本体而继续保留错位握持。

- `SkeletonBabyScaleMixin` 在已核验的原生 `LivingEntityRenderer.scale(LivingEntityRenderState,PoseStack)` 返回处，仅对五类原生SkeletonRenderState幼体应用ageScale（当前0.5）。本体、盔甲、衣物和手持物共享这一父矩阵，只缩放一次；属性scale、物理尺寸、年龄、原生已按ageScale处理的阴影均不改。
- `SkeletonBabyHandMixin` 在精确 `ItemInHandLayer.useBabyOffset(ArmedEntityRenderState)` 返回处，为同一共享成年网格保留成年握持偏移；不改变实际物品、左右手、动画、使用时钟或弓Goal。
- `SkeletonBabyVisuals` 集中五类+SkeletonRenderState判定；上一轮 `SkeletonBabyArmorMixin` 使用同一判定，保留成年网格对应盔甲UV修复。成年、原版幼年僵尸、小盔甲架及其他类型不走新增缩放/握持修正。

两新Mixin只注册在required/defaultRequire1/JAVA25客户端清单。无正式服务端生产代码或GameTest改动，无新依赖/AW/反射/持久化变更。MC26.3、官方未混淆命名、JDK25.0.3、Wrapper9.7.1、Loom1.18.3、Loader0.19.5、FAPI0.162.0+26.3及其他依赖保持；139声明/102支持/37未生效、v12/QA50不变。

## 实际运行证据

外层 `docs/rebuild/evidence/r4e3c-baby-fix-2/` 保存原版签名/字节码、所有运行日志与收据、未经处理的Framebuffer截图。`BabyBody*.java` / `r4e3c_baby_body_client.py` 为独立外部探针，不打入正式/source/QA JAR，不在目标Git提交内。

修复前是 `git archive` 导出的精确上一版3072e6e源码，位于本目标忽略的run/baby-body-baseline-3；不是回退当前工作树或修改26.3-tests。客户端前后均在隔离世界让五类幼体/成年同距并排、带真实四槽铁甲及弓，原生Server Tick/Goal自然索敌/射箭，未直接调用渲染器或Goal。实际模型提交处测量父矩阵；实际弓提交处按当帧模型骨骼及已核验原版成年握持数学变换校验矩阵，不用实体isBaby或绘制次数替代位置正确性。

| 验证 | 结果与边界 |
| --- | --- |
| `python docs/rebuild/scripts/r4e3c_baby_body_client.py 3 before` | **复现PASS**；五类幼体本体各771次均尺度约1，手持各771次，握持矩阵最大误差各0.342..0.416；四槽各771次，旧UV修复仍正确。wrongScale=19275、wrongGrip=3855、wrongUv=0。原生Goal真实箭28 |
| `python docs/rebuild/scripts/r4e3c_baby_body_client.py 4 fixed` | **限定PASS**；同一夹具五类幼体本体/手持各771次均尺度约0.5、握持误差0；成年约1；关闭后原幼体各598次均成年尺度约1。四槽各771次，wrongScale/wrongGrip/wrongUv全0；原生幼年僵尸/小盔甲架各5476次装备负对照保留，真实箭28 |
| `python docs/rebuild/scripts/r4e3c_baby_body_client.py 5 fixed` | **近景限定PASS**；新增普通/凋灵同场近景；三种矩阵错误仍0，关闭仍正常、真实箭27。该次仍为单目标夹具，不声称每类幼体都射箭 |
| `python docs/rebuild/scripts/r4e3c_baby_body_client.py 6 fixed` | **五类实际射箭补核PASS**；五个静止铁傀儡输入各在原生索敌范围内，实际幼体作为owner的箭按普通/流浪者/沼骸/凋灵/焦骸为3/3/2/3/2，总真实箭44；五类幼体当帧本体/弓提交各723/770/770/656/652次，四槽同次数，三种矩阵/UV错误仍0。新增靶子是确定性输入，不是手动调用攻击，也不把该扩展组与旧版单靶组当作严格上游L3 |
| `python docs/rebuild/scripts/r4e3c_baby_body_client.py 7 fixed` | **三轴与握持负对照PASS**；明确同时验证XYZ三轴，实际报告Y高度尺度五类均0.5、成年/off均1；本体/弓各716/770/770/656/653次，四槽同次数，三种错误仍0。原生幼年僵尸保留短臂握持1241次，小盔甲架保留原成年握持1273次，实际矩阵均正确；五类幼体箭3/3/2/3/2，总真实箭42。此补核不改正式代码 |
| `./gradlew.bat build qaJar -PtestEula=true --console=plain --no-configuration-cache` | **PASS**；26.3目录/JDK25.0.3，build-1.log exit0/BUILD SUCCESSFUL、449必需真实Tick/Goal/Brain测试全部通过，未删改或过滤旧断言 |
| 用户补验/第三方模型包/整合包多人 | **NOT RUN**；仅原生共享网格和本次范围已验证，替换成年/幼体模型及同方法覆盖的Mod另需兼容验收 |

原始画面：[旧版同距](../../docs/rebuild/evidence/r4e3c-baby-fix-2/screenshot-3-pairs.png) / [新版同距](../../docs/rebuild/evidence/r4e3c-baby-fix-2/screenshot-4-pairs.png) / [普通近景](../../docs/rebuild/evidence/r4e3c-baby-fix-2/screenshot-5-skeleton-close.png) / [凋灵近景](../../docs/rebuild/evidence/r4e3c-baby-fix-2/screenshot-5-wither-close.png) / [关闭恢复成年](../../docs/rebuild/evidence/r4e3c-baby-fix-2/screenshot-5-off.png)。均经代理实际查看；这里不等于用户验收通过。近景中幼体约成年高度一半，弓随手与本体缩放，盔甲无此前顶片。

补核五类射箭的原始近景也已查看：[普通](../../docs/rebuild/evidence/r4e3c-baby-fix-2/screenshot-6-skeleton-close.png) / [凋灵](../../docs/rebuild/evidence/r4e3c-baby-fix-2/screenshot-6-wither-close.png)。成年演员的既有闪避/效果箭仍自然运行，未为拍照禁用这些非本批能力。

失败记录未覆盖：client-1外部探针JOML通配符导致Math导入歧义，编译FAIL；client-2目标放在原生索敌范围外，所需真实箭未观察到，运行FAIL。修正明确导入和目标站位，保留矩阵/负对照/真实箭断言后才得到client-3/4通过，未修改发行实现或伪造攻击。

## 最小人工补验与停止点

替换正式 `build/libs/buildup-mob-tweaks-1.0.0.jar`（版本号未变，勿同时保留旧的同mod包）；需QA时同装现有 `buildup-mob-tweaks-1.0.0-qa.jar`，在临时遮阳世界进行：

1. `/buildupqa e3c spawn baby`，在同一地平面召唤成年骷髅作对照；观察幼体真实高度约一半，而非只依check中的width/eye。
2. 从正面和侧面观察静止持弓与实际拉弓/发箭；弓握柄应接在手上，随本体缩小，头盔无漂浮薄片。
3. `/buildupqa e3c off` 后原幼体恢复成年，装备不补发，握持仍正确；cleanup后对 `baby_wither` 重复上述过程，最后cleanup。

本批只修幼体渲染；通过审查后追加UPDATE_NOTES、一次本地提交并停止等待补验，不推送发布或进入E4/Boss/R5/Phase2/3。外层证据、脚本及共享矩阵不在26.3 Git根内，未被该提交覆盖。最终自检及补充观测见下方。

## 最终自检与交付

**Code Review: PASS WITH RISKS**，依据外层CODE_REVIEW.md逐项审查。

- Scope：五份客户端代码/资源（两新Mixin、共用判定、原盔甲Mixin和清单）、本次五份目标报告/清单与UPDATE_NOTES；外层探针、证据、矩阵及行为基线独立审查。起始干净，无用户改动被覆盖，未改任何正式服务端、GameTest、依赖、Wrapper或版本元数据。
- Findings：BLOCKER无，MAJOR无，MINOR无阻断项。无假造年龄、临时篡改isBaby、重新补装备、双重ageScale阴影、碰撞格式变更或S2冲突恢复。
- API/Mixin：已核对目标实例方法、完整描述符、返回语义与唯一调用链。scale在模型与所有层提交前执行且五类没有覆盖该方法；握持仅修改私有helper返回布尔值，原物品动作保留；required/defaultRequire1/JAVA25客户端Mixin实际启动/自动绘制成功，无注入失败。无局部变量猜测/ordinal/AW/反射/common客户端加载。
- Verification：Wrapper build/qaJar **PASS**，449必需真实Server Tick/Goal/Brain全过；客户端基线复现、半尺寸XYZ、成年/off恢复、完整弓矩阵、四槽UV、五类幼体自然发箭和原生握持负对照均 **限定PASS**。前后同距/近景原图经代理查看 **PASS**，客户端窗口/编译本身不作为行为证据。
- Delivery：`python docs/rebuild/scripts/r4e3c_baby_body_verify.py` **PASS，96项**（verify.json）；169旧S2文件、12恢复源码及两官方JAR哈希保持。正式/source JAR含新修复、NOTICE与MIT归属，无QA/GameTest/Probe/测试环境泄漏；独立QA仍单独产物。文档链接、更新记录、diff --check通过，提交前另检查完整暂存差异。
- Update Notes：**PASS**，末尾追加 `🐛 fix(render)` 一条，描述实际半尺寸/握持修复与成年回退，纳入本次提交，旧记录保留。
- Residual Risks：用户修复包补验、第三方替换骷髅网格/装备资产、整合包多人、长期自然分布 **NOT RUN**；主手含右/左主臂已观察，本次未新增验证所有材料和所有主副手组合。无BLOCKED裁决项。

三个交付产物仍在 `26.3/build/libs/`，SHA-256及大小记录在外层verify.json。独立探针和隔离客户端已结束；提交按持续授权仅为本地任务收尾，不替代人工验收。
