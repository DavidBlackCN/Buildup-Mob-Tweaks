# R2-A implementation scope

User authorization: 2026-10-08, explicitly approves a clean 26.3 baseline and P01–P05 implementation and real Server Tick validation. This refines PLAN.md's original R2/R3 sequence; no other mobs are implemented.

Environment: 26.2 upstream CExkBTN8 (Java 25, official names, Loader >=0.19.3), with 1.21.11 bFb56Zw2 as behavior comparison (intermediary, class version recorded in R0). Upstream build/Gradle/Loom versions are unknown from release JARs. Target retains Minecraft 26.3, Java 25, Gradle 9.7.1, Loom 1.18.3, Loader 0.19.5, Fabric API 0.162.0+26.3 and existing Fzzy/Kotlin dependencies; no mapping dependency or dependency upgrade.

| Unit | Classification | Concrete scope | Verification |
| --- | --- | --- | --- |
| Baseline | Adapt | BuildupMobTweaks initialization; common/client Mixin lists; GameTest entrypoints. Archive previous lists in docs/legacy-s2. No source deletion. | JAR registration audit, build, server/client loading |
| Config/infrastructure | Adapt | Retain Fzzy fields and historical data; only rebuilt Pillager IDs active. Keep equipment pool loader and diagnostics; no legacy combat/trait/equipment mutation listeners. | Gate and unrelated-mob tests |
| P01 | Rewrite/Adapt | New rebuild/pillager module, owned main-hand/backpack exchanges, native container persistence, melee Goal and scoped crossbow integration | Actual target selection, swap→damage→ranged, save/load and item counts |
| P02/P05 | Adapt | Narrow mixins in mixin/rebuild; preserve vanilla crossbow scheduling; range, safe retreat, invalid-target cleanup and valid handoff | Actual arrows/motion, obstruction/range/mode changes, disable fallback |
| P03 | Rewrite | Safe inventory/offhand transaction, vanilla consumption completion, nutrition healing, interruption/reload recovery | Real use duration, count decrement, healing and original offhand recovery |
| P04 | Adapt | Approach prolonged blocking target with owned axe; actual melee uses vanilla weapon shield interaction, no remote shield disabling | Server player shield plus actual melee; report limitations |
| Evidence | Adapt | New PillagerBehaviorTests only; old S2 tests preserved but not registered because old runtime is intentionally isolated | Dedicated GameTest real tick, per-scenario evidence, upstream attempt |

Specific mutations are limited to 26.3/Buildup entrypoint, config/feature declarations, three registration JSON files, new rebuild classes/tests/diagnostics, notices/translations and stage documentation. Source evidence and reports remain outside Git. No writes to 26.3-tests, no shared-source cleanup, no push or release.

Avoid copying upstream bugs: fixed meal slot and actual consumption; bounded target memory; no unsafe recoil; preserve actual weapon components and drop policy; never synthesize lost inventory during reload. Keep basic actions independent of random Traits. New enchantments, guns, bow-based pillager expansion and other-mob AI are outside this benchmark.

Tests may arrange equipment/targets/world state, but must never call production Goal.tick/start/canUse/stop directly as behavior evidence. Do not weaken assertions to pass failures. L2 requires observed movement, consumption, projectiles/damage and lifecycle changes driven by the server/GoalSelector. L3 remains BLOCKED/NOT RUN if upstream cannot run reliably. Stop after R2-A report and manual checklist.
