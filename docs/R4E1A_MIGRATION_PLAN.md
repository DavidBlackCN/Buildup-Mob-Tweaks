# R4-E1-a 已批准实施方案

用户在具体Environment Report / Migration Plan之后明确回复“确认，可以开始”。起始分支26.3/HEAD caf78b8，工作树干净；26.3-tests只读。

范围为parched_dodge、wither_skeleton_bow既有弓行为部分、wither_skeleton_skull、wither_skeleton_homing。自然发弓仍属E2；不进入E1-b、Boss、R5或Phase2/3。

环境：MC26.3/Java25.0.3/Wrapper9.7.1/Loom1.18.3/官方未混淆名称，无Yarn；Loader0.19.5/API0.162.0+26.3/Fzzy0.7.7+fix3+26.3/Kotlin1.14.1+kotlin.2.4.20；不改依赖、配置v9或旧附件。上游为R0已核验26.2 Beta CExkBTN8及1.21.11正式bFb56Zw2；正式版横移动量修正优先。原版WitherSkeleton已有普通主/副手弓Goal与燃烧箭；Parched已有虚弱箭。签名以已解析26.3类/javap核验。

| 顺序 | 单元 / 分类 | 最终实施 |
| --- | --- | --- |
| 1 | 生命周期 ADAPT | ExtendedSkeletonState/Behavior独立DATA/RUNTIME/SPIN/SKULL；ENTITY_LOAD安装Goal，出生仅一次，载入不抽；未知版本不改 |
| 2 | 焦骸 ADAPT | MOVE/LOOK优先级3；20tick预警+最多10tick旋转跳跃横移；120tick冷却；初次横向冲量≤0.65，碰撞/实地预检；不反复累加速度、不清零外部击退 |
| 3 | 持弓 ADAPT/原版部分REPLACE | 复用原版装备重评估、Goal及燃烧箭；只为既有原版弓增加真实耐久/计数；关闭恢复原版不耗耐久，不禁原版持弓；无备用剑或出生弓 |
| 4 | 头颅 ADAPT | 30tick前摇，每次一枚，400tick冷却，每射手≤1在途；100tick期限；非危险原版头颅。保留原版直接伤害8、爆炸半径1/击杀回复；不新增额外回血。局部移除持续凋零，ExplosionInteraction.NONE且不点火 |
| 5 | 追踪 ADAPT | 仅本模块弹体、不祥效果、可见固定目标；最初40tick且≤32格，速度混合0.8+方向0.1，调整值上限0.6；原版随后执行惯性/碰撞，不承诺整段恒速 |
| 6 | QA/验证 ADAPT | 独立qaJar四场景；真实Tick/Selector、碰撞/伤害/掉落/序列化及专服卸载冷启动；客户端附件/RenderState和门盾坐标；两版源码+Beta限场对照 |

启动距离：焦骸3～24格，地面非骑乘且持弓时已拉弓10tick；头颅6～24格。默认出生权重70/1000，traits/general/单项配置、skeleton_ai_excluded、vanilla_ai/disable标签生效。冷却保存，取消不返还。Goal自己的tick再次验证，因为原版续用检查不一定每tick执行。目标更换/失效、创造旁观、NoAI/死亡/禁用/卸载退出；载入旧在途头颅退休。跨实体卸载清理延至END_SERVER_TICK。

风险：不预设第三方武器支持；NONE不代表领地插件兼容；完整L3、人工视觉、多人/性能另验。结果见[R4E1A_REBUILD](R4E1A_REBUILD.md)。
