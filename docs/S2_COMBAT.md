# S2 敌对生物行为与边界

当前实现为独立重设计，未复制上游源码或素材；MIT 与历史 L1 复制限制见 [NOTICE](../NOTICE.md)。功能台账见 [FEATURE_MATRIX](../../docs/FEATURE_MATRIX.md)，统一人工步骤见 [S2_ACCEPTANCE](S2_ACCEPTANCE.md)。本文件补全并取代旧 S2-A/B/C 报告中的“尚未实现”描述，历史验证记录不改写。

## 配置与回退

配置版本 8，新增项位于 `hostile.extended`。所有行为有独立默认 true 开关，经 FeatureRegistry 和 `general.enabled` 查询；高级出生特性另受 `traits.enabled` 控制。旧 `hostile.skeleton/zombie/drowned/raid/vex` 字段保留。修复项集中列在 extended，`fixes` 空分类不含假开关。

开关更新在下一次服务器逻辑查询生效；正在运行的自有 Goal 在下次检查退出。概率只影响新实体，关闭不会删除已经生成的普通装备、坐骑、史莱姆或恼鬼。已射出的普通弹体不凭空撤回；带寿命的自有特殊弹体依然按到期时间清理。服务端文件修改须停服后编辑再启动；`/reload` 只重载数据包。

新版高级特性保存于 `major_trait` version 1，互斥组 `major_attack`：先尊重既有骷髅/僵尸高级特性，再进行一次新池加权抽取。总权重超过 1000 时归一化。新生成/命令/刷怪笼按同一入口抽取；旧存档、LOAD、CONVERSION、DIMENSION_TRAVEL 不重新抽取。既有附带数据优先，未知版本原样保留、不尝试新技能。猪灵闪避默认 7%，其他可调千分权重见配置；骑乘/图腾默认 1%，多数攻击默认 7%。没有通用永久属性叠加。

`hostile_combat` version 1 保存绝对游戏 tick 截止时间；所有技能起手即预留冷却。卸载不跳过冷却，在途动作不恢复。唤魔者三火球与末影人连击共用可调 `special_cooldown`（默认 400，范围 200–2400）；不是共享实体全局冷却池。二者是不同实体，各自保存期限。

## 行为索引

下表字段统一省略 `hostile.extended.`。全部开关已做独立门控回归；**这不等于全部视觉、场景和平衡已经人工验收**，精确自动覆盖见报告。

| 字段 | 中文名称 | 实际行为 |
|---|---|---|
| `skeleton_aim_fix` | 骷髅垂直瞄准修复 | 修复横移拉弓时 LookControl 清零俯仰；适用于骷髅和幻术师。 |
| `golem_friendly_fire_fix` | 傀儡友伤目标修复 | 保留伤害，阻止傀儡选取其他傀儡和排除标签成员为目标。 |
| `zombified_piglin_anger_fix` | 僵尸猪灵内讧修复 | 保留伤害，阻止同类成为报复目标。 |
| `strafe_hazard_check` | 横移危险检查 | 仅普通地面 MoveControl；阻止液体、悬崖和危险落脚，不强行向前。 |
| `movement_jump_input` | 跳跃运动输入 | 跳跃后最多 12 tick 保留最后横移输入；不替换飞行或游泳控制器。 |
| `movement_clear_strafe` | 清除残余横移 | 地面停步或前进时清除残留侧向输入。 |
| `strafe_obstacle_jump` | 横移越障跳跃 | 仅在一格障碍及上方空间安全时请求跳跃。 |
| `piglin_shot_swing` | 猪灵射击挥手 | 猪灵弩射击后使用原版挥手动画。 |
| `piglin_item_dodge` | 猪灵用物闪避 | 出生抽取 7% 互斥特性；近距离使用物品时向后跳，冷却 200 tick。 |
| `skeleton_offhand_visual` | 骷髅副手弓姿势 | 按实际使用手显示标准 BowItem 的拉弓姿势。 |
| `drowned_swim_visual` | 溺尸游泳摆臂 | 游泳且未使用物品时添加交替摆臂。 |
| `burning_projectile_visual` | 燃烧弹射物火焰 | 燃烧弹射物每 4 tick 显示火焰粒子；不增加点火能力。 |
| `burn_freeze_visual` | 燃烧与冰冻粒子 | 敌对生物燃烧或受冻时每 10 tick 显示粒子。 |
| `undead_horse_sunburn` | 亡灵马日光燃烧 | 骷髅马和僵尸马露天白昼燃烧；水中或雨中不触发。 |
| `hostile_escape_seat` | 敌对生物脱困 | 有战斗目标时检查周围安全落脚并离开船或标签座椅；每 100 tick 尝试。 |
| `hostile_spawn_effect` | 出生临时效果 | 新生成且没有高级特性的敌对生物有 3% 获得 600 tick 速度或抗火；只抽一次。 |
| `spider_hunts_pests` | 蜘蛛捕食小虫 | 无目标时每 40 tick 在 8 格内查找蠹虫或末影螨。 |
| `creeper_fire_visual` | 燃烧苦力怕爆炸粒子 | 爆炸时添加火焰粒子，不另行点火或改变原版爆炸。 |
| `creeper_hit_delay` | 受击延缓引爆 | 受伤时倒退 5 tick 引信，最多每 10 tick 一次。 |
| `creeper_embedded_arrows` | 爆炸释放嵌入箭 | 最多释放 8 支低伤害、不可拾取的嵌入箭，不增加可回收弹药。 |
| `illusioner_omen_spawn` | 不祥动物袭击遭遇 | 带不祥效果攻击动物时限频抽取幻术师遭遇；32 格内最多一只，成功后冷却 1200 tick。 |
| `illager_zombie_conflict` | 灾厄村民与僵尸敌对 | 普通僵尸与灾厄村民空闲时互相选敌；灾厄村民优先可见玩家，不抢已有非僵尸目标。 |
| `illager_zombie_villager` | 灾厄村民攻击僵尸村民 | 每 40 tick 有界查询，不覆盖已有其他战斗。 |
| `illager_boats` | 海上灾厄村民用船 | 新袭击或巡逻个体在深水中最多创建一艘船；只控制自己创建且自己驾乘的船。 |
| `husk_spear_charge` | 骆驼骑士持矛冲锋 | 尸壳实际持矛、骑骆驼尸壳时，20 tick 预警后有限冲锋；冷却 240 tick。 |
| `husk_camel_circle` | 骆驼尸壳环绕协作 | 骑乘尸壳请求安全环绕路径，并向同乘的 Parched 共享当前目标。 |
| `skeleton_horse_charge` | 骷髅马骑士冲锋 | 稀有新骷髅可获得一匹马；已有骑士也可进行带前摇的有限冲锋。 |
| `chicken_jockey_charge` | 鸡骑士冲锋 | 抽中特性的幼年僵尸骑现有鸡时冲锋，不补发鸡。 |
| `zombie_slime_carrier` | 僵尸史莱姆投掷 | 稀有个体只创建一次原版二级史莱姆；20 tick 预警后投出原乘客，不补发。 |
| `zombie_horse_leader` | 僵尸马领袖 | 稀有新僵尸最多创建一匹僵尸马；空头槽添加不可掉落旗帜。 |
| `piglin_crossbow_compatibility` | 猪灵标准模组弩适配 | 识别标准 CrossbowItem 与实际使用手；保留原版 Brain。 |
| `pillager_food_heal` | 掠夺者缓慢进食 | 脱战 100 tick 后用真实副手食物进食 80 tick，回血 2；保留原版消费效果和容器。 |
| `pillager_shield_break` | 掠夺者破盾 | 实际持斧对 3 格内连续格挡 60 tick 的玩家施加 100 tick 盾冷却；自身冷却 200 tick。 |
| `drowned_fishing_pull` | 溺尸钓竿牵引 | 实际持钓竿，20 tick 可见前摇后轻拉目标并消耗一点耐久；冷却 160 tick。 |
| `zombified_piglin_spawn_crossbow` | 僵尸猪灵稀有出生弩 | 原版初始化武器时 1% 改为弩；不覆盖已有拾取装备。 |
| `zombified_piglin_crossbow` | 僵尸猪灵使用弩 | 使用真实主手标准弩蓄力射击，保留物品组件和原版弹药处理。 |
| `zombie_fisherman_rod` | 僵尸渔夫钓竿 | 新渔夫空主手分配一根钓竿，已有钓竿可用；牵引消耗耐久。 |
| `zombie_cleric_potions` | 僵尸牧师药水 | 20 tick 前摇后向敌人或附近受伤亡灵投掷伤害药水；冷却 160 tick。 |
| `zombie_fletcher_crossbow` | 僵尸制箭师弩 | 新制箭师空主手分配一把弩；使用实际武器，不重新补发。 |
| `rider_friendly_fire` | 骷髅骑士友伤保护 | 阻止骑骷髅马的骷髅伤害自己的马及其他同类骑士。 |
| `skeleton_trap_variants` | 骷髅陷阱四骑士 | 四名骑士依次为骷髅、流浪者、沼骸、凋灵骷髅，保留装备和坐骑。 |
| `skeleton_wither_conversion` | 凋零死亡转化 | 骷髅在凋零状态或凋零伤害致死时转为凋灵骷髅，转移装备，不额外掉落。 |
| `wither_skeleton_bow` | 凋灵骷髅稀有弓 | 原版武器初始化 1% 用弓；新命令生物空手时也只尝试一次。 |
| `wither_skeleton_homing` | 不祥凋灵头追踪 | 目标有不祥效果时，已有特殊凋灵头在最初 40 tick 轻微追踪可见目标。 |
| `wither_skeleton_skull` | 凋灵骷髅远程头颅 | 出生互斥特性；30 tick 前摇后仅发一枚弱凋灵头，冷却 400 tick，弹体寿命 100 tick。 |
| `parched_dodge` | Parched 旋转闪避 | 出生互斥特性；20 tick 预警后横移 20 tick，结束清除侧向惯性；冷却 120 tick。 |
| `bogged_omen_mushrooms` | 沼骸不祥蘑菇 | 近距离不祥目标触发两枚慢速蘑菇弹，落点短暂小范围伤害云；冷却 300 tick，无物品掉落。 |
| `stray_omen_snow` | 不祥雪球加速 | 不祥目标使特殊雪球由慢到快；不单独生成弹幕。 |
| `stray_snow_barrage` | 流浪者下落雪球 | 互斥特性；困难或未持弓时，20 tick 前摇后跳跃并在下落时发三枚雪球；冷却 400 tick。 |
| `skeleton_shelter` | 狙击骷髅寻找遮蔽 | 仅已有狙击特性；每 100 tick 最多检查八个位置，寻路最多持续 100 tick。 |
| `illusioner_swap` | 幻术师分身换位 | 隐身受击后向安全分身位置换位；冷却 200 tick，不提供无敌。 |
| `illusioner_shoot_pause` | 幻术师射击停步 | 射击时停止当前导航和移动请求。 |
| `illusioner_clone_arrows` | 幻术师分身箭 | 互斥特性；隐身射击最多增加两支不可拾取箭，冷却 120 tick。 |
| `enderman_combo_animation` | 末影人连击挥手 | 特殊连击使用原版挥手动画，独立于攻击开关。 |
| `enderman_combo` | 末影人三连击 | 互斥特性；40 tick 预警后最多三次安全传送近战尝试，目标丢失取消。 |
| `enderman_speed` | 末影人速度平衡 | 独立临时修正使移动速度降低 15%；关闭移除修正，不覆盖基础属性。 |
| `evoker_fireball_no_fire` | 唤魔者火球不点火 | 本项目唤魔者火球不点燃实体或方块；不影响烈焰人等原版火球。 |
| `evoker_totem` | 唤魔者一次图腾 | 稀有互斥特性；低于 30% 生命且未施其他法术时举真实图腾最多 40 tick，一生一次、不掉落。 |
| `evoker_fireball` | 唤魔者三火球 | 互斥特性；环绕粒子预警 40 tick 后依次射出三枚火球，不复制奖励。 |
| `hostile_rest` | 敌对生物短暂休息 | 满血空闲的僵尸、骷髅、蜘蛛、苦力怕限频小概率休息 200 tick；受伤苏醒。 |
| `rest_sound_wake` | 声音唤醒休息 | 休息生物响应 8 格内未排除的振动事件；潜行忽略事件仍忽略，不模拟羊毛遮音传播。 |
| `witch_leaping_potion` | 女巫饮用跳跃药水 | 高处目标时将自饮迅捷替换为跳跃；保留治疗、抗火等原版优先级。 |
| `witch_jump_throw` | 女巫跳投 | 高处目标且头顶安全时在投掷末段起跳；关闭通常前摇时仍有 8 tick 跳投前摇。 |
| `silverfish_burrow` | 蠹虫受伤钻块 | 半血以下检查相邻六格，20 tick 后进入可寄生方块；遵循 mobGriefing。 |
| `silverfish_call_pause` | 蠹虫求援停步 | 原版唤醒同伴 Goal 期间占用 MOVE 并停止导航。 |
| `silverfish_call_particles` | 蠹虫求援数量粒子 | 每成功唤醒一个寄生方块发一个粒子。 |
| `silverfish_shovel_weakness` | 蠹虫铲子易伤 | 实际主手铲子标签的直接近战伤害乘以 1.5。 |
| `breeze_takeoff_burst` | 旋风人起跳风爆 | 仅困难，起跳产生风弹式爆发；100 tick 冷却，mobGriefing=false 不触发方块。 |
| `breeze_landing_burst` | 旋风人落地风爆 | 仅困难，落地产生风弹式爆发；与起跳分别冷却。 |
| `evoker_flee_speed` | 唤魔者逃跑速度 | 将逃避 Goal 的速度倍率限制为 0.8。 |
| `evoker_avoid_target_fix` | 唤魔者逃离非玩家目标 | 对当前非玩家目标添加八格逃避 Goal，施法中不启用。 |
| `sense_glowing` | 发光目标感知 | 32 格内感知发光目标可穿墙，仍保留原版目标合法性和实际射击视线检查。 |
| `sense_blindness` | 失明感知 | 失明观察者感知倍率 0.25，四格外不能直接瞄准。 |
| `sense_darkness` | 黑暗感知 | 黑暗观察者感知倍率在 0.35 与 0.65 间波动，使用最强减益。 |
| `sense_nausea` | 反胃感知与精度 | 反胃感知倍率 0.7，并降低弹射物精度。 |
| `sense_invisibility` | 隐身目标感知 | 隐身感知上限 0.35；近期攻击者对受害者短暂暴露。 |
| `sense_crawling` | 爬行目标感知 | 对爬行目标使用至多 0.4 的姿态倍率。 |
| `sense_crouching` | 潜行目标感知 | 对潜行目标使用至多 0.65 的姿态倍率。 |
| `melee_effect_interval` | 效果影响近战间隔 | 急迫和疲劳调整近战间隔；疲劳优先，限制等级与结果，不改变无效果原值。 |
| `melee_high_target_jump` | 近战高处目标跳跃 | 目标稍高且近距离、头顶安全时请求跳跃；冷却 40 tick。 |
| `ranged_reposition` | 射后小幅换位 | 发射后在安全地面侧移一格；冷却 60 tick。 |
| `omen_pressure` | 不祥近战压力 | 对不祥目标略缩短基础近战间隔，不叠加属性强化。 |
| `ghast_slow_fireball` | 恶魂慢速火球 | 将实际发射火球初速度与加速度乘以 0.6。 |
| `ghast_cooldown` | 恶魂较长冷却 | 每次发射后预留 100 tick 冷却，再进入原版 20 tick 前摇。 |
| `ghast_telegraph` | 恶魂蓄力视觉 | 客户端蓄力稍微膨胀并短暂闪色，保留原版张嘴动画。 |
| `blaze_orbit` | 烈焰人环绕蓄力 | 以环绕粒子预告弹数，不提前制造可伤人的实体。 |
| `blaze_difficulty_volley` | 烈焰人难度弹数 | 简单、普通、困难分别每轮一、二、三枚，保留原版间隔。 |
| `blaze_strafe` | 烈焰人射击横移 | 射击阶段在碰撞检查通过时请求侧向飞行。 |
| `vex_projectile_weakness` | 恼鬼弹射物易伤 | 雪球伤害为 2，其他弹射物伤害乘以 1.5。 |
| `pillager_crossbow_compatibility` | 掠夺者标准模组弩适配 | 识别 CrossbowItem 子类及真实使用手；不承诺非标准武器实现。 |
| `pillager_range` | 掠夺者射程 | 将接近与停止距离设为 15 格，与射击 Goal 一致。 |

## 重要重设计

- 尸壳钻沙：双方地表为沙、路线下方连续三层沙、地表两格空间、`mobGriefing=true`。15 tick 前摇，20 tick 内下潜最多 2.2 格并水平前进最多 3 格；不挖方块。期间仅免疫 IN_WALL，其他伤害仍有效。取消或读取存档恢复记录的地表、重力与碰撞；出口被外部填埋时最多向上寻找八格，找不到则恢复原点并使用正常物理，不永久保留穿墙状态。
- 狙击选位：落地后才尝试，八个候选、一次路径计算/100 tick；自有 MOVE Goal 最多运行 100 tick，防止原版弓 Goal 立即取消路径。到达后恢复原版射击/走位，不锁死在单一屋顶。
- 环绕蓄力采用粒子提示，实体仅实际发射时生成。特殊凋灵头/雪球/唤魔者火球保存 100 tick 到期时间；克隆箭不可拾取。恶魂/烈焰人保留原版弹体机制。
- 图腾是带私有标记的真实原版物品：副手原本有物品时不覆盖；只在低血、未施其他法术时出现，一生一次、最多 40 tick。原版死亡保护消费它；不设置无敌。只清理自己的标记物品并恢复自己临时改过的掉率。
- 休息是暂时不选新目标并停止移动的行为，不改变阵营或加入新坐姿模型。真实伤害一定唤醒；声音使用 GameEvent 振动类别及潜行忽略规则，未模拟完整振动传播/羊毛遮挡。
- 钓竿使用可见粒子牵引线和真实耐久，未创建仅支持玩家所有者的 FishingHook。僵尸牧师使用原版伤害药水，可能影响旁观者，亡灵受治疗；不会新注册药水。
- 史莱姆携带采用原版 size=2（可造成原版接触伤害），每名中签僵尸最多创建一次。20 tick 前摇后投出原乘客；保留原版分裂、掉落和死亡规则，不补发、不召回。领袖旗帜和自动分配职业武器掉率为 0，已有装备不覆盖。
- 通用射后选位是最多每 60 tick 的安全一格导航请求，原版后续 Goal 仍可决定最终路径；不承诺所有自定义 Brain/飞行导航器都完成该移动。
- 地形规则只处理本项目实际操作：蠹虫钻块/尸壳钻沙受 `mobGriefing` 控制；旋风人禁破坏时仍可风爆击退但不触发方块；唤魔者防点火单独控制。没有虚构通用领地保护 API，第三方保护需实测。

## 恼鬼持久所有权

主世界 `vex_owners` version 1 索引按 owner UUID → vex UUID 记录；所有维度共享入口，不扫描全世界或强加载区块。设置 owner/ENTITY_LOAD 登记，卸载和换维度保留名额，真正死亡/销毁释放。召唤仍预留三只，保留原版随机判断及独立召唤冷却。未知索引版本禁止新的受控召唤。

真实独立服已验证：远处三只恼鬼卸载后仍计数 3，重载和新 JVM 重启保持，销毁后归零。**升级前从未再次加载过的旧恼鬼尚不在索引内**，首次加载才补登记，因此不宣称升级瞬间追溯所有旧生物。外部直接删实体存档或不发卸载事件的工具可能留下保守占位；本轮未实现破坏性全世界自动清理。不能仅因 owner 死亡就释放仍然存活的恼鬼。

## 数据包标签

命名空间均为 `buildupmobtweaks`，路径为 `data/buildupmobtweaks/tags/<类型>/<名称>.json`，原版 `/reload` 生效。标签约束不能让未知物品自动获得武器 API。

| 类型/名称 | 默认及范围 |
|---|---|
| entity_type/golems_never_target | 空；友伤修复开启时傀儡不选这些类型 |
| entity_type/zombies_not_attackable_by_illagers | 空；排除灾厄村民与普通僵尸的新增敌对 |
| entity_type/skeletons_use_vanilla_bow_checks | 空；对指定骷髅类型退出本项目弓/换手适配，保留原版检查 |
| entity_type/ranged_extensions_disabled | 空；退出高级火球/克隆箭/流浪者弹幕/凋灵头及新增弩适配；不是关闭所有远程 AI |
| item/ranged_items_excluded | 空；标准模组弓弩兼容适配的退出列表，原版识别仍保留 |
| entity_type/zombie_specials | 默认普通僵尸、尸壳；限制既有僵尸高级技能及新骑乘/携带，不扩大支持类型 |
| entity_type/escapable_seats | 空；自定义座椅 opt-in，原版船也检查安全出口 |
| game_event/rest_ignored_events | 空；休息声音监听的忽略事件 |

牧师可治疗标签的上游条目属于后续村民服务；本轮僵尸牧师只用有界、存活、受伤、亡灵、非 Boss 条件，不提前实现 Villager Intelligence。

## 诊断与限制

管理员等级 4：`/buildupmobtweaks s2 <实体>` 显示通用冷却、高级特性、已登记恼鬼数；`/buildupmobtweaks feature <路径>` 显示单个门控。既有 skeleton/zombie/raid/vex 命令继续保留；不提供隐式重掷或批量删除命令。

Boss 不进入本轮高级特性：凋灵、末影龙、远古守卫者、劫掠兽、监守者未被接管；幻翼与复杂史莱姆系统保持暂缓。通用敌对规则并不等于所有 Enemy 子类都已有专属 AI。无新增附魔、附魔奖励、专用武器、失明药水、额外 Boss 奖励或新进度。

原版瞄准/横移/武器检查等修复依据实际 26.3 类和可运行用例，未搬旧映射或宽泛捕获异常。标准测试 BowItem/CrossbowItem 不等于所有第三方武器。真实双人、完整袭击、领地保护、外部 AI/动画和高密度 MSPT/TPS 仍待人工/S4 验收。
