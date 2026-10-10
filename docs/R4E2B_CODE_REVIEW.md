# R4-E2-b 自检

Code Review: **PASS WITH RISKS**，依据外层CODE_REVIEW.md。

- Scope：26.3分支，从b209c5ef起始干净基线；完整五项职业武器/G38/独立QA及必要回归夹具。旧26.3-tests与上游恢复源码未改，外层文档/证据不在目标Git提交内。
- Version：MC26.3、官方未混淆命名、Java25、固定Wrapper/Loom/Loader/API/Fzzy/Kotlin均一致；配置v10、原ID/数据包装备池格式和正式元数据不升级。
- Correctness：一次原生出生、真实持手物品实例/组件/耐久/掉率、已装弹保持、有限药水消费、单钩归属/未标记的外部浮漂让位、冷却与退出、未知版本、转换/治愈/死亡/原版回退均有实际Tick证据。有限候选扫描，不用旧S2共享AI。分包等待只有客户端最多3tick。
- Mixins：四个common、五个client完整描述符/注入目标以26.3原JAR校验，实际专服/客户端加载通过；目标参数、Invoker、WrapOperation/WrapMethod和环境声明复核。浮漂渲染用RETURN覆盖非玩家返回分支；没新增Access Widener。
- Resources：JSON和中英键一致；排除Tag命名空间正确；QA/GameTest/临时探针与正式JAR/源码JAR隔离。MIT/NOTICE及原上游来源哈希保留。
- Verification：build-11 build+qaJar **272 required PASS**；client-v5真实模型/包/线/cast及门盾16样本PASS；qa-v2独立专服五个新场景及cleanup PASS；target-v2/v3冷重启实际装备/装弹/已消费瓶/冷却PASS；upstream-v2仅限定运行对照；verify-6包隔离、124/73/51、原169哈希与工具链核对PASS。
- Fixes during review：已装填弩可继续追赶；保护原生高难度附魔对外部主手实例的影响；自有浮漂同步来源标记防止抢占另一钩；药水同时核验owner UUID。修正客户端早到use包和非玩家返回注入。旧四处夹具只固定输入/RNG，原断言和真实Goal调度保留。
- Evidence tooling：verify-5在Windows默认GBK解码英文资源时失败；审计脚本全部显式UTF-8后verify-6通过，未修改资源以绕过检查。
- Update Notes：追加feat(profession)与test(qa)，准确记录已验证用户行为，保留旧记录。
- BLOCKER：无。MAJOR：无。MINOR：无。
- Residual Risks：人工观感/GUI、完整L3量化、自然出生分布、多人与整合包/第三方AI武器/性能NOT RUN；只支持明确原版物品/类型，排除Tag是退出约定而非通用控制者兼容保证。上游仅有限跨版本对照，不宣称等价。

自检与本地commit不替代人工验收；停止在完整E2-b。
