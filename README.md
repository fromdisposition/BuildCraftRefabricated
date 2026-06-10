# BuildCraft Refabricated

Unofficial **Fabric** port of BuildCraft for **Minecraft 26.1.2** — pipes, engines, quarries, oil, gates, and automation in one JAR.

| | |
|---|---|
| **Mod ID** | `buildcraftrefabricated` |
| **Version** | `26.1.2-1` |
| **Platform** | Fabric Loader ≥ 0.19.2, Fabric API |
| **Java** | 25+ |
| **License** | [MPL-2.0](LICENSE) |

**Repository:** [github.com/fromdisposition/BuildCraftRefabricated](https://github.com/fromdisposition/BuildCraftRefabricated)  
**Issues:** [github.com/fromdisposition/BuildCraftRefabricated/issues](https://github.com/fromdisposition/BuildCraftRefabricated/issues)

Gameplay code traces to [legoj15's NeoForge 26.1.2 port](https://github.com/legoj15/BuildCraft), adapted here for Fabric Transfer API and Team Reborn Energy (`E`) interop.

---

## Modules

| Package | Role |
|---------|------|
| `buildcraft.core` | Markers, volume boxes, list mod, springs |
| `buildcraft.lib` | GUI, tiles, transfer, rendering, guide |
| `buildcraft.energy` | Engines, oil/fuel fluids, worldgen |
| `buildcraft.transport` | Item/fluid/MJ/E pipes, pluggables, wires |
| `buildcraft.factory` | Tank, pump, distiller, auto workbench, mining well |
| `buildcraft.builders` | Quarry, filler, builder, architect table |
| `buildcraft.silicon` | Laser tables, gates, facades |
| `buildcraft.robotics` | Zone planner, robots, docking stations |
| `buildcraft.fabric` | Registries, config, networking, bootstrap |

---

## BC 8.0.x parity

Status vs classic **BuildCraft 8.0.x** (Forge 1.12.2). Legend: **DONE** · **WIP** · **—** (n/a).

### Core

| Feature | Status |
|---------|--------|
| Landmark / path markers | DONE |
| Volume box system | DONE |
| List mod | DONE |
| Creative / redstone engines | DONE |
| Oil springs | DONE |
| Paintbrush | DONE |
| Map location | DONE |
| Robot goggles | WIP |
| Engine tester | WIP |

### Transport

| Feature | Status |
|---------|--------|
| Item pipes | DONE |
| Fluid pipes | DONE |
| MJ power pipes | DONE |
| E (Team Reborn) pipes | DONE |
| Pipe behaviours & pluggables | DONE |
| Gates, facades, lenses, pulsar | DONE |
| Wire systems | DONE |
| Filtered buffer | DONE |

### Energy

| Feature | Status |
|---------|--------|
| Stone / creative engines | DONE |
| Iron (combustion) engine | DONE |
| Oil / fuel / residue fluids | DONE |
| Fluid buckets & worldgen | DONE |
| E↔MJ bridge blocks (engine / dynamo) | DONE |

### Factory

| Feature | Status |
|---------|--------|
| Tank, pump, flood gate | DONE |
| Distiller, heat exchange, chute | DONE |
| Auto workbench (items) | DONE |
| Mining well | DONE |
| Auto workbench (fluids) | WIP |

### Builders

| Feature | Status |
|---------|--------|
| Quarry | DONE |
| Filler (+ planner addon) | DONE |
| Architect table, builder | DONE |
| Electronic library, replacer | DONE |

### Silicon

| Feature | Status |
|---------|--------|
| Assembly / integration / advanced crafting tables | DONE |
| Programming / charging / stamping tables | DONE |
| Lasers | DONE |
| Gates & silicon pluggables | DONE |

### Robotics

| Feature | Status |
|---------|--------|
| Zone planner | DONE |
| Deployable robots | DONE |
| Docking stations | DONE |
| Requester | DONE |

### Lib

| Feature | Status |
|---------|--------|
| MJ API & statements | DONE |
| Fabric Transfer interop (`BcTransfers`) | DONE |
| Guide book | WIP |
| Forge caps | — |

---

## Energy interop

Internal logic uses **MJ**. By default (`MJ_AUTOCONVERT_RF`):

- BC machines accept **Team Reborn E** when conversion is enabled.
- UI shows **E** when another `team_reborn_energy` mod (e.g. Tech Reborn) is in the pack; otherwise **MJ**.
- `MJ_ONLY` — MJ only, no E. `DISPLAY_RF` — always show E.

Config: `config/buildcraft/buildcraftrefabricated-common.json` → `powerMode`, `mjRfConversion`.

---

## Build

**JDK 25** required.

```bash
git clone https://github.com/fromdisposition/BuildCraftRefabricated.git
cd BuildCraftRefabricated
./gradlew build          # Unix / macOS
gradlew.bat build        # Windows
```

Output: `build/libs/BCRefabricated-26.1.2-1.jar`

---

## Install

1. Fabric Loader for Minecraft 26.1.2
2. Fabric API in `mods/`
3. BuildCraft Refabricated JAR in `mods/`
4. Optional: JEI

---

## Known gaps

- Not a byte-for-byte BC 8 clone — modern MC APIs differ from 1.12.2.
- MJ pipes stay BC-internal; cross-mod energy uses Team Reborn `E` API.
- Guide book incomplete; some dev-only blocks behind `-Dbuildcraft.dev=true`.
- Wood pipe + MJ required for passive extraction (BC design).

---

## Credits

- **SpaceToad & BuildCraft Team** — original mod ([MPL-2.0](LICENSE))
- **[legoj15](https://github.com/legoj15)** — NeoForge 26.1.2 port
- **[fromdisposition](https://github.com/fromdisposition)** — Fabric port & maintenance

---

*BuildCraft Refabricated — unofficial Fabric port. Original mod by [BuildCraft/BuildCraft](https://github.com/BuildCraft/BuildCraft).*
