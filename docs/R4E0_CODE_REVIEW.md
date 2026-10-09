# R4-E0 自检

Code Review: **PASS WITH RISKS**

Scope：26.3分支；build.gradle、独立src/qa、QaHarnessTests、gametest元数据、68项裁决/上游缺口/现有入口/QA指南/后续计划/报告与相关历史文档指向。外层共享计划/矩阵/取证脚本按本轮明确请求更新；26.3-tests严格只读。生产源码、依赖、配置v9、资源/AW/Mixin与原135项测试保持基线。

Findings：BLOCKER无；MAJOR无；MINOR无未解决项。未宣称所有上游类分支/全部自然概率/所有脑记忆可查询；271项仅Mixin声明覆盖。QA源集不进入main/client/sourceJar，普通包无开发命令。独立测试JAR有原版权NOTICE/licenses，但不内嵌生产类或依赖。

Fixes Applied：据目标实际签名处理CompoundTag/Brigadier错误；独立QA通过mods实际加载；查26.3选择器源码改安全标记，收紧专服回执拒绝解析失败；恢复仅本工具状态/标签与未被外部修改的方块，不复制装备、不返还冷却、不删原子实体；每服务器一会话、1200真实tick限期、正常关服退场。原失败日志保留，旧测试没有删除/放宽。

Verification：Wrapper `build qaJar -PtestEula=true --console=plain`最终build-6 **PASS /136必要真实测试**；独立专服实包五场景指令v4、查询/停用/清理/保存exit0 **PASS**；门盾原客户端probe此次36个RenderState坐标样本/保存重载/正常退出 **PASS**；备用木剑死后零生成掉落及真实外物/组件/重载 **PASS**；旧工程快照/生产与既有测试未变、68唯一裁决/56白名单、上游原输入/source哈希、JAR排除QA/原版代码、JSON/文档相对路径、git diff --check **PASS**。最终可复查[verification.json](../../docs/rebuild/evidence/r4e0/verification.json)。

Update Notes：**PASS**；一次追加`✅ test(qa)`，与本地提交主要变更一致。开发工具说明不冒称新增发行AI。

Residual Risks：HUMAN视觉与实际Mod环境、独立QA客户端GUI、双人/默认自然刷怪器分布、完整L3/性能 **NOT RUN**；工具仅保证加载隔离场地的正常结束，崩溃/强杀/卸载不自动恢复会话；不要用于实际世界。selected实体生命周期/健康不回滚，环境外改/未被观察的远弹体不承诺全量撤销，指南明确。Allay历史BLOCKED不重试。大批后续候选仍未实施；原版已修或预算不合格的候选在具体批准方案可有证据地改REPLACE/DEFER。下一批仍需用户确认。
