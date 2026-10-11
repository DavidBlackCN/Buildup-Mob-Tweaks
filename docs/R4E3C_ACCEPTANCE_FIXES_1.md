# E3-c 幼体骷髅盔甲验收修复

> 后续反馈确认本次只修盔甲UV，未修实际本体成年大小和短臂握持偏移。原“半尺寸”只覆盖服务器碰撞/眼高，不能代表本体绘制正确；两项实际矩阵补修与同距/近景证据见[第二次补修](R4E3C_ACCEPTANCE_FIXES_2.md)。历史运行结果保留各自范围。

2026-10-11，用户反馈“baby模型有误，其余无问题”。唯一目标26.3/分支26.3，起始干净0ca7ea4843d4dab4be7bcde990c5c4e6eb5556c8。保留其他已验收机制，本轮不进入E4或任何下一阶段。用户原两张截图中的头盔断层/悬空薄片是**真实缺陷**，不是幼体预期模型。

## 根因与改动

26.3原版 `AbstractSkeletonRenderer` 只烘焙成年骷髅本体和成年盔甲模型，并作为成年/幼体共享模型传入父渲染器。`HumanoidArmorLayer.renderArmorPiece` 看到 `state.isBaby` 后却选择 `EquipmentClientInfo.LayerType.HUMANOID_BABY`；该专用布局与共享成年盔甲网格UV不匹配。四个装备槽均受影响，头盔最显眼。此前E3-c探针只检查了有效年龄/尺寸和绘制计数，没有检查网格对应的盔甲资源，未覆盖此缺陷，原报告中的这项客户端PASS不能作为盔甲贴图正确的证据。

新增客户端 `SkeletonBabyArmorMixin`，只拦截上述方法里唯一读取 `HUMANOID_BABY` 的表达式：精确五种原生骷髅的 `SkeletonRenderState` 使用成年网格匹配的 `HUMANOID`，腿槽使用 `HUMANOID_LEGGINGS`。不修改isBaby/ageScale、模型/碰撞/眼高、实际装备、持久化、目标或Goal；成年分支、原版幼年僵尸、小盔甲架及其他渲染状态保留原路径。注册在required/defaultRequire1/JAVA25客户端Mixin JSON，没有common客户端引用、新依赖、AW、全局isBaby篡改或临时恢复状态的Hack。

MC26.3、官方未混淆命名、JDK25.0.3、Wrapper9.7.1、Loom1.18.3、Loader0.19.5、FAPI0.162.0+26.3及既有依赖不变。139声明/102支持/37未生效、v12/QA50均不变，未新增玩法或开关。

## 证据与验收

外层 `docs/rebuild/evidence/r4e3c-baby-fix/` 保留目标原版签名/完整字节码、实际客户端日志/收据和原始截图；外层 `docs/rebuild/scripts/BabyArmor*.java`、`r4e3c_baby_client.py` 为独立探针，不纳入正式或源码JAR，也不纳入26.3提交。native-model-authorized/native-layers记录成年模型共享、幼体资源分支和完整描述符。初次默认权限javap的缓存访问错误保留，授权后的完整字节码为有效依据。client-1是外部探针旧版时钟/天气/截图/GUI API和私有setSmall的编译失败，按26.3实际签名及原生命令修正后再运行；不算行为PASS。

| 验证 | 实际结果 |
| --- | --- |
| 修复前 `python docs/rebuild/scripts/r4e3c_baby_client.py 2 before` | **复现 PASS**；真实客户端自动装备层提交，五类幼体每四槽各986次，合计19720次成年网格错误选用BABY贴图；实际截图screenshot-2.png复现头部薄片与胸腿错位 |
| 修复后 `python docs/rebuild/scripts/r4e3c_baby_client.py 3 fixed` | **限定 PASS**；真实自动提交，五类幼体每四槽各1024次、wrongBabyTexture=0；成年每槽至少1023次；原版幼年僵尸4096次仍BABY贴图、小盔甲架4092次仍成年布局。required Mixin实际启动通过，无注入失败 |
| 实际截图查看 | **PASS（代理查看）**；原生Framebuffer screenshot-3.png中五类头盔薄片消失，头胸腿脚盔甲连续；不是只检查状态、直接调用渲染器或人工绘图 |
| 完整Wrapper | **PASS**；build-4.log：实际449必需测试全过，build/qaJar成功；前三轮失败另列下方，不覆盖日志 |
| 用户补验 / 第三方模型资源包 / 整合包多人 | **NOT RUN**；用户尚未验收修复包。原版路径已测；替换同一五类幼体网格/资产的Mod需另行兼容验证 |

原始画面对照：[修复前](../../docs/rebuild/evidence/r4e3c-baby-fix/screenshot-2.png) / [修复后](../../docs/rebuild/evidence/r4e3c-baby-fix/screenshot-3.png)。前排是五类戴全套铁甲幼体，后排有无甲幼体与成年对照；截图未后期修改。

补验只需换新正式JAR（QA包配合当前正式包），先 `/buildupqa e3c cleanup`，再 `spawn baby` / `baby_wither`；对照正常头盔，无漂浮顶片或脸部多层纹理，弓/箭正常；off恢复成年、盔甲仍正常、装备不换不补，最后cleanup。若补验自然其他三类，在临时遮阳世界使用刷怪蛋观察选定幼体，默认10%不保证单次。其他主机制按用户本次“其余无问题”记录反馈，不把未提供的极端/多人/L3结果补写为PASS。

## 自检范围与停止点

按CODE_REVIEW.md审阅新增窄客户端Mixin与清单注册、目标API/资源选择、客户端实际提交和负对照、Wrapper/包隔离、文档与UPDATE_NOTES。最终结论在完成记录中填写。源码只改渲染选择，26.3-tests严格只读；外层证据与文档未覆盖进提交。验证后按持续授权一次本地 `🐛 fix(render)` 提交，然后停止等待baby补验，不推送发布、不开始E4。

## 完整回归的失败记录与夹具修正

前三轮均实际运行全部449项，结果均为 **FAIL**，原始日志保留在同一证据目录：build-1仅snow_target_death_abort未满足70 tick退出断言；build-2仅real_player_creative_spectator_and_distance_release_target未在40 tick取得自然仇恨；build-3仅active_cooldown_cancellation_preserves_unowned_standing未在30 tick进入已调度冷却。其他两项在随后各轮已通过。失败没有删除、过滤或改为可选，也不能算完整构建PASS。

已核对原版NearestAttackableTargetGoal.canUse的随机准入（native-target-goal.txt）。这些夹具此前未固定随机输入，却在固定的短期限要求自然索敌完成。本轮仅给这三个夹具的演员固定种子，并在雪球准备/退出及马冷却断言前记录真实目标、状态和tick；未改断言、期限、测试数、原版Goal、正式服务端实现或全局配置，未直接调用Goal或伪造目标。未带诊断的首次失败，不能断言随机性是唯一根因；最终观察范围及整体结果另记下方。夹具种子只用于确定性验收，不代替自然分布/长期负载测试。

## 最终构建记录

在26.3目录，JDK25.0.3运行 `./gradlew.bat build qaJar -PtestEula=true --console=plain --no-configuration-cache`，最终build-4 **PASS**（exit0/BUILD SUCCESSFUL）；449必需真实Server Tick/原版Goal测试全过，无删减。雪球目标退出的实际70 tick观测为triggered=true、目标已删除、准备目标none、动画0、released=initialReleased=0；马30 tick诊断target与fixture UUID一致、action.active=true、phase=cooldown、standing=false，随后取消保留非自有站立的原断言通过。自然生存索敌/创造/旁观/距离退出原测试也通过。

正式产物 `build/libs/buildup-mob-tweaks-1.0.0.jar`；需要QA补验时同装 `buildup-mob-tweaks-1.0.0-qa.jar`，源码包为 `buildup-mob-tweaks-1.0.0-sources.jar`。版本号沿用原值，换包时删除旧的同mod文件，勿重复安装。实际客户端探针隔离在run/r4e3c-baby-fix-2/3，运行结束，无持续探针进程。

## 最终自检

**Code Review: PASS WITH RISKS**，依据外层CODE_REVIEW.md。Reviewed files：新增SkeletonBabyArmorMixin、client Mixin JSON、三个GameTest夹具、UPDATE_NOTES和本次四份目标文档；外层矩阵/行为基线/独立探针/证据另审，不纳入目标Git提交。

- BLOCKER：无；MAJOR：无；MINOR：无阻断项。起始工作区干净，无用户改动被覆盖，所有正式服务端生产代码不变；测试改变只固定输入/增加观测，无新测试、断言删改、加宽期限或跳过。
- Mixin：精确完整描述符/实例私有方法/唯一静态枚举读取；ModifyExpressionValue保留原表达式其他路径，required注入实际客户端成功；无局部变量捕获、ordinal猜测、取消原方法、反射或AW。客户端类仅客户端清单注册。
- 版本与资源：工具链/依赖/映射/资源标识符/配置结构均未变，JSON与同mod入口一致。正式/source JAR含修复和MIT归属NOTICE/license，无QA/GameTest/Probe/测试环境泄漏。
- Verification：Wrapper build/qaJar与449原生调度 **PASS**；修复前后实际客户端四槽/负对照与原始截图 **PASS**；`python docs/rebuild/scripts/r4e3c_baby_verify.py` **PASS，40项**（verify.json），169旧S2文件、12份恢复源码及两官方JAR哈希保持，diff --check与文档链接通过。
- Update Notes：**PASS**，末尾只追加一条 `🐛 fix(render)`，描述已验证可见变化，保留旧记录并纳入本次提交。
- Residual Risks：用户修复包补验、替换五类骷髅网格/装备资产的第三方Mod/资源包、整合包多人 **NOT RUN**；旧限时夹具首次失败的全部原因未证明，日志保留，固定输入后的整组通过仅是本次范围的证据。无必须用户裁决的BLOCKED项。

本地提交按持续授权完成后停止；外层共享文件不在26.3 Git根内，未被该提交覆盖，未推送发布或开始后续阶段。
