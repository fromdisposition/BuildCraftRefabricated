# BuildCraft Refabricated

An unofficial Fabric port of BuildCraft for modern Minecraft: pipes, engines, the quarry, oil, gates and robots in one jar, built on the Fabric Transfer API and Team Reborn Energy.

| Minecraft | Support | Fabric Loader | Fabric API | Java |
|-----------|---------|---------------|------------|------|
| 26.3 | Mainline | 0.19.5+ | 0.160.1+26.3 | 25 |
| 26.2 | LTS | 0.19.5+ | 0.159.0+26.2 | 25 |
| 26.1.x | LTS | 0.19.5+ | 0.155.2+26.1.2 | 25 |
| 1.21.11 | Maintenance | 0.19.5+ | 0.141.6+1.21.11 | 21 |
| 1.21.10 | Maintenance | 0.19.5+ | 0.138.4+1.21.10 | 21 |
| 1.21.1 | Legacy | 0.19.5+ | 0.116.7+1.21.1 | 21 |

All six versions are built from the same source tree and get the same fixes. Anything older than 1.21.1 is not planned. Bug reports go to the [issue tracker](https://github.com/fromdisposition/BuildCraftRefabricated/issues).

## What's in the port

The whole of classic BuildCraft 8.0.x for Forge 1.12.2, verified end to end on the current line, machine by machine and screen by screen:

- **Core** — land markers, path markers and volume boxes; redstone and creative engines; springs, paintbrush, lists and map locations.
- **Transport** — item, fluid and kinesis pipes of every material, MJ and RF alike; pipe behaviours and pluggables, facades, gates with wire systems, lenses, the pulsar and the filtered buffer.
- **Energy** — stone and iron engines; oil, fuels, refining fluids and oil worldgen; the RF engine and the MJ dynamo.
- **Factory** — mining well, tank, pump, flood gate, distiller, heat exchange, auto workbench and chute.
- **Builders** — quarry, architect table and builder, filler, replacer and the electronic library.
- **Silicon** — assembly, integration, advanced crafting, programming, charging and stamping tables, lasers and the packager.
- **Robotics** — robots with every board, docking stations, the zone planner and the requester.

This is not a byte-for-byte clone of BC 8 — modern Minecraft APIs differ too much from 1.12.2 for that — but every machine behaves the way you remember it.

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