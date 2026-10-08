# 来源与许可

Buildup Mob Tweaks 使用 [MIT License](LICENSE)。
Copyright (c) 2026 DavidBlackCN and Buildup Mob Tweaks contributors.

工程基于 Fabric 官方模板，保留 common/client 源码分离。原模板以 CC0-1.0 提供，其许可记录保存在 [licenses/Fabric-template-CC0.txt](licenses/Fabric-template-CC0.txt)。

功能设计参考 [Mob AI Tweaks](https://github.com/Unknowneth/Mob-AI-Tweaks)，作者 Unknowneth / N0t_UN_Owen。其主分支使用 MIT。本项目是独立项目，并非原作者的官方续作；R2-A 的掠夺者行为依据恢复的官方发行源码进行了改编，未复用美术素材。

项目自身采用 MIT 不改变第三方代码的许可。本次参考 Mob AI Tweaks 官方发行版本 CExkBTN8（26.2 主参考）及 bFb56Zw2（1.21.11 交叉参考）的 PillagerEntityMixin、PillagerSwitchItemsGoal、RangedCrossbowAttackGoalMixin 和 PillagerEatItemToHealGoal。适用声明为 Copyright (c) 2024 N0t_UN_Owen，完整 MIT 文本保存在 [licenses/Mob-AI-Tweaks-MIT.txt](licenses/Mob-AI-Tweaks-MIT.txt)。对应 Buildup 实现位于 rebuild/pillager 与 mixin/rebuild。按项目维护者已确认的许可口径统一使用 MIT；上游 JAR 内残留的 CC0 元数据保留于外层审计证据，不将其来源推测写为作者确认。详细来源、哈希和差异记录位于工作区 docs/rebuild/。

Fzzy Config 是外部依赖，使用 Timefall Development Licence – Modified 1.3；不随本项目 JAR 内嵌分发，也不改为 MIT。Fabric Language Kotlin 与可选 Mod Menu 同样由使用者单独安装。

R2-B 骷髅系列继续按同一 MIT 声明改编 CExkBTN8 的 AbstractSkeletonEntityMixin、RangedBowAttackGoalMixin、RangedAttackGoalMixin、SkeletonSpecificGoal、StrayEntityMixin、BoggedEntityMixin 与 LivingEntityRendererMixin。对应实现为 rebuild/skeleton、mixin/rebuild/Skeleton* 与客户端 client/mixin/rebuild/Skeleton*。Copyright (c) 2024 N0t_UN_Owen。真实备用物品所有权、生命周期清理、安全落脚和独立屋顶搜索由 Buildup 重写；不复用上游入口、奖励链或资源素材。
