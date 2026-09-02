# BuildCraft Refabricated

An unofficial Fabric port of BuildCraft for modern Minecraft: pipes, engines, the quarry, oil, gates and robots in one jar. Gameplay code traces back to [legoj15's NeoForge port](https://github.com/legoj15/BuildCraft), reworked here for the Fabric Transfer API and Team Reborn Energy.

| Minecraft | Support | Fabric Loader | Fabric API | Java |
|-----------|---------|---------------|------------|------|
| 26.2 | Mainline | 0.19.3+ | 0.155.2+26.2 | 25 |
| 26.1.x | LTS | 0.19.3+ | 0.155.2+26.1.2 | 25 |
| 1.21.11 | Maintenance | 0.19.3+ | 0.141.5+1.21.11 | 21 |
| 1.21.10 | Maintenance | 0.19.3+ | 0.138.4+1.21.10 | 21 |
| 1.21.1 | Legacy | 0.19.3+ | 0.116.7+1.21.1 | 21 |

All five versions are built from the same source tree and get the same fixes. Anything older than 1.21.1 is not planned. Bug reports go to the [issue tracker](https://github.com/fromdisposition/BuildCraftRefabricated/issues).

## What works

Feature status against classic BuildCraft 8.0.x for Forge 1.12.2. *Stable* means played and tested regularly, *working* means ported and recently fixed up but still collecting playtime, *needs testing* means ported but not seriously exercised yet.

| Module | Feature | Status |
|--------|---------|--------|
| Core | Landmarks, path markers, volume boxes | Stable |
| Core | Redstone and creative engines | Stable |
| Core | Springs, paintbrush, lists, map location | Stable |
| Transport | Item pipes | Stable |
| Transport | Fluid pipes | Working |
| Transport | Power pipes, MJ and RF | Working |
| Transport | Pipe behaviours and pluggables | Working |
| Transport | Facades | Working |
| Transport | Gates and wire systems | Working |
| Transport | Lenses, pulsar, filtered buffer | Needs testing |
| Energy | Stone and iron engines | Stable |
| Energy | Oil, fuels, refining fluids, worldgen | Stable |
| Energy | RF engine and MJ dynamo | Stable |
| Factory | Mining well | Stable |
| Factory | Tank, pump, flood gate | Working |
| Factory | Distiller, heat exchange | Needs testing |
| Factory | Auto workbench, chute | Needs testing |
| Builders | Quarry | Stable |
| Builders | Architect table, builder | Working |
| Builders | Filler, electronic library, replacer | Needs testing |
| Silicon | Assembly, integration, advanced crafting tables | Needs testing |
| Silicon | Programming, charging, stamping tables, lasers, packager | Needs testing |
| Robotics | Robots and docking stations | Working |
| Robotics | Zone planner | Working |
| Robotics | Requester | Needs testing |

This is not a byte-for-byte clone of BC 8 — modern Minecraft APIs differ too much from 1.12.2 for that — but the goal is that every machine behaves the way you remember it.

## Energy

Everything inside BuildCraft runs on MJ, and the UI always shows MJ. Team Reborn Energy is a separate network: RF pipes and machines move E natively, MJ pipes and engines move MJ natively, and nothing converts silently between the two. The bridge is explicit hardware — the RF Energy Engine turns E into MJ, the MJ Dynamo turns MJ back into E, and each one shows the real unit on its own side. The conversion ratio is `mjRfConversion` in `config/buildcraft/buildcraftrefabricated-common.json`.

## Install

Put the jar for your Minecraft version into `mods/` together with Fabric API. JEI and REI are both supported but optional, and any mod speaking Team Reborn Energy (TechReborn and friends) can power the RF side.

## Building from source

Any recent JDK is enough to run Gradle — the build downloads the Java toolchains it actually compiles with (21 for 1.21.x, 25 for 26.x) on its own.

```sh
git clone https://github.com/fromdisposition/BuildCraftRefabricated.git
cd BuildCraftRefabricated
./gradlew build
```

That builds every Minecraft line at once (on Windows use `.\gradlew.bat`). Building a single line is faster:

```sh
./gradlew :26.3:build
```

Stonecutter drives one node per line (`1.21.1`, `1.21.10`, `1.21.11`, `26.1`, `26.2`, `26.3`). Each jar lands in `versions/<line>/build/libs/` as `BCRefabricated-<yy.M.d>+mc<version>.jar`, versioned by build date.

## Credits

BuildCraft was created by SpaceToad and the BuildCraft team and is licensed under the [MPL-2.0](LICENSE); the original repository is [BuildCraft/BuildCraft](https://github.com/BuildCraft/BuildCraft). 
[legoj15](https://github.com/legoj15) ported it to NeoForge, and [fromdisposition](https://github.com/fromdisposition) maintains this Fabric port.