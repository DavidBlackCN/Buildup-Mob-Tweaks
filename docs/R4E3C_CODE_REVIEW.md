# R4-E3-c 自检

> 后续人工反馈发现本次原探针漏验幼体盔甲网格/贴图配对。年龄和交互的原PASS保留其范围，不作为盔甲正确的证据；补修审查/实际截图和四槽提交证据见[R4E3C_ACCEPTANCE_FIXES_1](R4E3C_ACCEPTANCE_FIXES_1.md)。其他主功能用户反馈无问题，修复后的baby补验待反馈。

**Code Review: PASS WITH RISKS**，依据外层 CODE_REVIEW.md 全项检查。

Scope：唯一目标26.3/分支26.3，起始干净e86e80d；审阅本轮新增Ability模块、九个新增common/client Mixin及既有SkeletonLifecycle与RemainingExtras改动、独立配置/FeatureRegistry/双语、QA/真实调度测试和构建入口、README/计划/矩阵/验收及UPDATE_NOTES。外层计划、证据与脚本为本任务共享文档范围，不在26.3 Git根内。

Findings：BLOCKER无；MAJOR无；MINOR无阻断项。版本/依赖/映射/元数据未升级；build仅加精确测试白名单，QA独立源集仍不入正式/source JAR。所有新JSON解析、无重复键、中英键集合一致，六独立gate/default/当前支持计数校验通过。

API/Mixin：已从目标common/merged JAR验证实际方法及完整描述符、构造器/实例语义、原生Shoot唯一spawn结果、convertTo唯一AfterConversion调用点（先保留原回调）、BreezeWindCharge.explode的index8环境参数及客户端模型延迟提交链。required/defaultRequire1/JAVA25，common与client清单分离；GameTest、实际专服及客户端均启动通过，无Mixin注入失败。未加AW/反射/第三方依赖。

Correctness：雪傀儡独立临时修饰器、不覆盖外部属性/真实装备；仅有一攻击Goal，禁用恢复原对象；准备瞬态无世界雪球/重载乘客，不补球。32原始候选/三目标/100刻扫射预算，LOS/模式/目标/关闭退出。幼体选择保存、真正转换复制未知未来内容但不运行，原生同期年龄与refreshDimensions，不成长/不抽载入/不补物品。风弹只拥有两额外原生实体，100刻期限/离场退休与共享预算持久，不改变中弹；额外环境交互尊重破坏门。生命周期卸载退役延迟至END_SERVER_TICK避免修改正在迭代的世界实体集合。

Fixes Applied：基于真实失败修正目标交互签名/泛型；护甲夹具剥离原头盔后断言不变；原Vec3.yRot查表精度改精确旋转，严格角度/速度断言不变；长跳夹具取消屋顶而不放宽跳跃/间隔断言；客户端首次实体包/年龄同步等待10刻后仍严格验证。发现客户端不可读取未同步持久附件判交互门，改同步门并通过真实网络往返守恒；持球层按延迟提交正确setupAnim。没有修改已有409测试断言、关校验或引入绕过。

Verification：完整Wrapper build/qaJar **PASS**，449必需真实Tick/Goal/Brain（40新增）全部通过；实际客户端自动物品/模型/年龄/剪切归还网络 **PASS**；门盾/真实钓线客户端与旧死亡木剑/马姿态行为回归 **PASS**；独立QA六场/关门两观察窗口/地形实体回收、最终专服v11→v12及同世界冷重启 **限定 PASS**；官方Beta真实雪球与64次出生对照 **限定 PASS**。read-only交付审计47检查与diff --check **PASS**，169旧S2文件与恢复源码/两官方JAR/原生JAR哈希一致。具体命令、失败及观测见[报告](R4E3C_REBUILD.md)与外层verify.json，未用测试数量替代实际现象。

Update Notes：**PASS**，仅追加一条 `✨ feat(abilities)` 面向Release的已验证六项变化，同本次提交，不记录下一阶段计划。

Residual Risks：人工清单/肉眼模型、完整两版L3、配置GUI/真实多人/整合包和长期性能 **NOT RUN**；N1000为受控出生样本，不是长期自然分布。共享Mixin仍可能与第三方同方法覆盖冲突，排除标签不是普遍领地/枪械兼容保证。build-2既有horse调度测试一次失败根因未证实，后续构建该既有用例均通过、最终449全量通过，未隐瞒或修改断言。用户本批数值裁决已解除，无BLOCKED。本地提交后停止，不推送发布、不进入后续阶段。
