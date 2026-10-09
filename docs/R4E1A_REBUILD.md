# R4-E1-a 工作报告

日期2026-10-09；唯一目标26.3，分支26.3，起始HEAD caf78b8，起始工作树干净。用户明确批准[具体迁移方案](R4E1A_MIGRATION_PLAN.md)后实施。**本批限定自动交付PASS WITH RISKS；人工验收NOT RUN，完整R4/Phase1尚未完成。** 不进入E1-b/E2/Boss/R5/Phase2/3。

## 实现与兼容性

- `ExtendedSkeletonState/Behavior/Goal`是独立模块，旧SkeletonBehavior仍只支持SKELETON/STRAY/BOGGED。新增持久化附件extended_skeleton_rebuild和skeleton_owned_skull、同步parched_spin；不改旧DATA/RESERVE或配置v9，不生成任何备用装备。所有附件在公共初始化提前注册。
- 焦骸：出生默认70/1000一次选择，原生Goal优先级3/MOVE+LOOK；20tick声音/粒子预警，跳跃和最多10tick真实旋转横移，120tick冷却。正式版上游3.5×移动属性为参考，冲量上限0.65，预检实地/碰撞；不反复放大速度、不全量清除外部击退。保留原版虚弱箭及弓调度。客户端复用RenderState，焦骸绕Y轴，旧流浪者/沼骸仍绕X轴。
- 凋灵骷髅已持弓：26.3原版本就有主/副手弓Goal、装备重评估和燃烧箭，登记原版覆盖REPLACE证据，不冒充新射击AI。新增的独立开关控制真实原版弓耐久消耗和计数；关后仍原版射击/近战。排除武器标签生效，不宣称第三方BowItem通用兼容。**出生发弓未实现，留E2；此ID只有行为部分交付。** 不凭自然弓属性补发剑或弓。
- 头颅：默认70/1000一次选择，30tick前摇，每次一枚，400tick冷却，≤1在途/射手，100tick寿命。原版非危险WitherSkull保留直接伤害8、半径1爆炸与原版击杀回复；取消持续凋零，爆炸NONE且不点火，不新增上游的额外回血。保护按弹体出生附件判定，不靠射手当前attackType，其他凋灵/Boss头颅不受影响。
- 追踪：仅本模块头颅，可见固定目标具有Bad/Raid/Trial Omen、≤32格、最初40tick；速度0.8+方向0.1微调，调整值≤0.6。原版随后执行惯性/碰撞；目标退出不改追别人。独立关闭追踪仍弹道命中，关闭头颅清除在途。
- 保存出生选择/冷却，取消不返还；已加载或转换/跨维度不重抽，未知附件版本保留并不运行。更换/死亡/失效/创造旁观目标、NoAI、配置/标签禁用均退出。原版可能在续用检查之间tick旧Goal，因此tick自身再次验证。卸载跨实体清理延至END_SERVER_TICK；保存后载入旧头颅不恢复。
- 启动前置：焦骸3～24格，非骑乘、地面且持弓已拉10tick；头颅6～24格。traits/general/四单项配置、skeleton_ai_excluded及vanilla_ai/disable标签各自门控。

## 修改位置

生产：BuildupMobTweaks注册，FeatureRegistry四ID白名单，新增ExtendedSkeleton三类状态/行为/Goal及三个局部Mixin；SkeletonAiMixin/RemainingEntityMixin仅调用独立模块，旧范围不扩张。客户端SkeletonRenderer/Rotation复用同步状态。中英配置说明修正了旧出生发弓及20tick横移误导。

开发：独立QA新增parched/wither_bow/wither_skull/wither_homing，已有五场景保留；force不更换外部装备，check解释门控/装备/冷却/不祥前置，off/cleanup沿既有规则。新增ExtendedSkeletonTests和独立test_environment，构建只将测试放gametest；正式/源码包继续排除QA/probe。build.gradle仅增加测试include，不升级依赖。

## 实际验证

完整原始证据在外层[本批目录](../../docs/rebuild/evidence/r4e1a/)，机器回执[verification.json](../../docs/rebuild/evidence/r4e1a/verification.json)。

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.3'
.\gradlew.bat build qaJar -PtestEula=true --console=plain
python ../docs/rebuild/scripts/r4e1a_servers.py qa v2
python ../docs/rebuild/scripts/r4e1a_servers.py target v1
python ../docs/rebuild/scripts/r4e1a_servers.py upstream v1
python ../docs/rebuild/scripts/r4e1a_client.py r4e1a r4-feedback-client-r4e0
python ../docs/rebuild/scripts/r4e1a_verify.py
```

| 检查 | 结果与限度 |
| --- | --- |
| 最终Wrapper build+qaJar | **PASS**：[build-11.log](../../docs/rebuild/evidence/r4e1a/build-11.log)；154项必要测试，旧136+新18。数量不代替行为证据 |
| 焦骸真实调度 | PASS/L2：预警、同步旋转、实际跳跃横移（首次场景2.426格）、原版虚弱箭；禁用/目标切换前摇取消；普通骷髅不装专用状态 |
| 原版已持弓 | PASS/L2：主/副手真实箭与燃烧、原弓对象/命名/耐久；关后原版箭、实际换石剑近战；无复制备用物 |
| 头颅/追踪 | PASS/L2：真实前摇→发射→运动→碰撞，目标100→92；追踪移动目标后x方向0.1635并实际命中；关追踪仍伤害；目标更换/NoAI/死亡/禁用在途退出 |
| 有限期限、存档、死亡 | PASS/L2：外部 deflection 将已发射弹体停泊后100实际tick退休；不绕过原版tick。实际序列化/UUID、生效冷却/出生保留，旧弹体退休，未知v99保留。死亡只掉一把原有命名/17耐久石剑，无发弓或备用剑 |
| 实际默认出生采样 | PASS（夹具）：每种1000次native finalizeSpawn+ENTITY_LOAD，spin63/skull63；不改默认权重/容差。自然刷怪器分布NOT RUN |
| 独立QA专服 | v1及最终v2 **PASS**：实际加载独立QA包，四场景/inspect/check/off/cleanup、地形恢复和无实体残留、save/stop exit0；[最终v2回执](../../docs/rebuild/evidence/r4e1a/qa-v2.json)与日志核对，不以旧包代替最终QA诊断 |
| 实际卸载/冷启动 | PASS（限定）：data命令明确No entity was found；相同保存实体UUID、birth/skull、ready999999、实际石剑在重载及重启保留；无恢复头颅哨兵。保存标杆为NoAI静止，**不宣称卸载瞬间仍持活动弹体**；活动阶段/弹体退休另由真实Tick和序列化证明 |
| 客户端专项 | PASS（自动）：10个实际焦骸同步附件→RenderState样本kind3/ticks一致；64个实际门盾RenderState坐标/上下半扇样本、重登世界、正常退出。未逐帧截取旋转矩阵，肉眼视觉NOT RUN |
| 门盾/备用剑复核 | PASS（自动）：完整旧死亡/重载/禁用/组件掉落测试；真实客户端门盾坐标及无CME。用户原环境复验NOT RUN |
| 上游对照 | PASS（限场运行）/完整L3 NOT RUN：已核验Beta独立专服exit0，同输入原版仇恨、既有弓/焦骸/石剑头颅；实际Health与Motion/SpecialMove/弓damage快照。正式1.21.11本批只做源码差异审计，未新增实跑；不把Beta当黄金或宣称等时序/等伤害 |
| 正式包隔离/旧工程 | **PASS**：verification脚本核验正式/源码包无QA/GameTest/probe/原版类；旧169个tracked文件及Git快照与E0相同 |
| 人工/多人/整合包/性能/完整L3 | NOT RUN；详见[人工清单](R4E1A_ACCEPTANCE_CHECKLIST.md) |
| Allay | 历史BLOCKED，不再尝试；不阻断本批 |

## 失败历史与夹具修正

首次compile是live(WitherSkull)同参数方法重名，改setter名markLive。build1真实调度取消后额外tick导致空target崩溃，增加tick内合法性检查；build2旧排除测试仍把整个skeleton包视作禁用、QA自带birth缺初始cooldown。修正为明确R4-A隔离并新增无旧DATA/RESERVE/未知v99断言；已知新模块DATA缺冷却时安装初次预算。build3全部150 PASS。

build4/5出现旧日夜射击/玩家重获目标夹具失败，新模块18项分别实际通过。新测试环境隔离，旧长时目标改1000HP，原亮度/模式/真实箭/目标退出断言保留。build6旧三叉戟序列化回收未命中/未回收；随机轨迹与有限地板可能干扰（推断），按原生已发射路径放置接收目标，仍实际碰撞、路径回收和逐tick唯一武器断言；build7全部154 PASS。build8旧恼鬼原版回退和三叉戟实射夹具失败；恼鬼固定原生随机源481212，三叉戟实射同样固定一次接收输入，**不声明随机射击精度验收**，不改生产溺尸/恼鬼。build9全部154 PASS；build10旧女巫5%饮药机会夹具失败，增加固定实体随机源481002与状态诊断，不改原概率/时限/饮药跳跃断言；最终build11及专服v2见回执。没有删除测试、关闭必要测试或放宽断言阈值/超时；旧夹具修订明确列出，历史失败日志保留。语言说明脚本一次GBK解码失败后改UTF-8，未覆盖原文。

## 产物、审查与停止点

正式/源码/独立QA JAR位于build/libs/，最终SHA256/大小见verification.json。[自检](R4E1A_CODE_REVIEW.md)记录PASS WITH RISKS及可复查边界；发行说明只追加已验证变化。完成后一次本地Gitmoji+Conventional commit，提交哈希见最终答复/commit.json；外层共享计划/矩阵/原始证据与脚本不在26.3 Git根，未随提交覆盖。无推送/发布/切分支。

下一批建议E1-b：piglin_shot_swing、piglin_item_dodge（弩后跳）、piglin_crossbow_compatibility、zombified_piglin_anger_fix。**只列待批准任务，未开发**；先复现26.3原版Brain/仇恨再决定必要适配。自然凋灵骷髅发弓仍E2。
