# BuildCraft Refabricated

Unofficial **Fabric** port of BuildCraft for **modern Minecraft** — pipes, engines, quarries, oil, gates, and automation in one JAR.


| Minecraft Version | Support Status | Fabric Loader | Fabric API | Java |
|-------------------|----------------|------------|------------|------|
| **26.2**| 🟢 **Active** (Upstream) | ≥ 0.19.3 | ≥ 0.154.2+26.2 | 25 |
| **26.1.x** | 🟢 **Active** (Long term support) | ≥ 0.19.3 | ≥ 0.154.2+26.1.2 | 25 |
| **1.21.11** | 🟡 **Active** (Maintenance)  | ≥ 0.19.3 | ≥ 0.141.4+1.21.11 | 21 |
| **1.21.10** | 🟡 **Active** (Maintenance)  | ≥ 0.19.3 | ≥ 0.138.4+1.21.10 | 21 |
| **1.21.1** | 🟠 **Active** (Best effort)  | ≥ 0.19.3 | ≥ 0.116.7+1.21.1 | 21 |
| **<1.21.1** | 🔴 **Not supported and not planned**  | | | |

**Repository:** [github.com/fromdisposition/BuildCraftRefabricated](https://github.com/fromdisposition/BuildCraftRefabricated)  
**Issues:** [github.com/fromdisposition/BuildCraftRefabricated/issues](https://github.com/fromdisposition/BuildCraftRefabricated/issues)

**Gameplay code traces to [legoj15's NeoForge port](https://github.com/legoj15/BuildCraft), adapted here for Fabric Transfer API and Team Reborn Energy (`E`) interop.**

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

Status vs classic **BuildCraft 8.0.x** (Forge 1.12.2).

### Core

| Feature | Status |
|---------|--------|
| Landmark / path markers | 🟢 DONE |
| Volume box system | 🟢 DONE |
| List mod | 🟢 DONE |
| Creative / redstone engines | 🟢 DONE |
| Oil springs | 🟢 DONE |
| Water springs | 🟢 DONE |
| Paintbrush | 🟢 DONE |
| Map location | 🟢 DONE |

### Transport

| Feature | Status |
|---------|--------|
| Item pipes | 🟢 DONE |
| Fluid pipes | 🟡 NEED TESTING |
| Power pipes | 🔴 NEED HEAVY TESTING |
| Pipe behaviours & pluggables | 🔴 NEED HEAVY TESTING |
| Gates, facades, lenses, pulsar | 🔴 NEED HEAVY TESTING |
| Wire systems | 🔴 NEED HEAVY TESTING |
| Filtered buffer | 🔴 NEED HEAVY TESTING |

### Energy

| Feature | Status |
|---------|--------|
| Stone / creative engines | 🟢 DONE |
| Iron (combustion) engine | 🟢 DONE |
| Oil / fuel / residue fluids | 🟢 DONE |
| Fluid buckets & worldgen | 🟢 DONE |
| E↔MJ bridge blocks (engine / dynamo) | 🟢 DONE |

### Factory

| Feature | Status |
|---------|--------|
| Tank, pump, flood gate | 🟡 NEED TESTING |
| Distiller, heat exchange, chute | 🟡 NEED TESTING |
| Auto workbench (items) | 🔴 NEED TESTING |
| Mining well | 🟢 DONE |

### Builders

| Feature | Status |
|---------|--------|
| Quarry | 🟢 DONE |
| Filler (+ planner addon) | 🟡 NEED TESTING |
| Architect table, builder | 🟡 NEED TESTING |
| Electronic library, replacer | 🟡 NEED TESTING |

### Silicon

| Feature | Status |
|---------|--------|
| Assembly / integration / advanced crafting tables | 🔴 NEED HEAVY TESTING |
| Programming / charging / stamping tables | 🔴 NEED HEAVY TESTING |
| Lasers | 🔴 NEED HEAVY TESTING |
| Gates & silicon pluggables | 🔴 NEED HEAVY TESTING |

### Robotics

| Feature | Status |
|---------|--------|
| Zone planner | 🔴 NEED HEAVY TESTING |
| Deployable robots | 🔴 NEED HEAVY TESTING |
| Docking stations | 🔴 NEED HEAVY TESTING |
| Requester | 🔴 NEED HEAVY TESTING |

---

## Energy interop

Internal logic uses **MJ**:

- BuildCraft's internal energy is **MJ**; it stays MJ everywhere in the UI.
- **RF/E (Team Reborn) energy** and MJ are separate networks. RF pipes/machines move E natively; MJ pipes/engines move MJ natively — no silent auto-conversion between them.
- The bridge between the two is explicit hardware: the **RF Energy Engine** (E → MJ) and the **MJ Dynamo** (MJ → E). Each shows the real unit on its own side of the conversion.

Config: `config/buildcraft/buildcraftrefabricated-common.json` → `mjRfConversion` (the MJ-per-E ratio used by the converter blocks).

---

## Install

1. Fabric Loader for Minecraft
2. Fabric API in `mods/`
3. BuildCraft Refabricated JAR in `mods/`
4. Optional: JEI or REI, TechReborn

---

## Building from source

1. You'll need a JDK: **Java 25** for the 26.x lines and **Java 21** for 1.21.x.

2. No Gradle install required — the bundled wrapper (`gradlew`) already runs the right version.

3. Clone the repository and enter it:

   ```sh
   git clone https://github.com/fromdisposition/BuildCraftRefabricated.git
   cd BuildCraftRefabricated
   ```

4. Build the mod. BuildCraft Refabricated uses **Stonecutter** to target several Minecraft lines from one source tree (one node per MC line: `1.21.1`, `1.21.10`, `1.21.11`, `26.1`, `26.2`).

   - Build **all** lines at once:

     ```sh
     ./gradlew build
     ```

   - Build a **single** line via its node task (faster):

     ```sh
     ./gradlew :1.21.11:build
     ```

   Swap `1.21.11` for any other version. On Windows use `.\gradlew.bat` instead of `./gradlew`.

5. Each line's jar lands in `versions/<line>/build/libs/`, named `BCRefabricated-<version>+mc<mc>.jar` — e.g. `versions/1.21.1/build/libs/BCRefabricated-26.7.1+mc1.21.1.jar` (`<version>` is the build date, `yy.M.d`).

---

## Known gaps

- Not a byte-for-byte BC 8 clone — modern MC APIs differ from 1.12.2.
- MJ pipes stay BC-internal; cross-mod energy uses Team Reborn `E` API.

---

## Credits

- **SpaceToad & BuildCraft Team** — original mod ([MPL-2.0](LICENSE))
- **[legoj15](https://github.com/legoj15)** — NeoForge port
- **[fromdisposition](https://github.com/fromdisposition)** — Fabric port & maintenance

---

*BuildCraft Refabricated — unofficial Fabric port. Original mod by [BuildCraft/BuildCraft](https://github.com/BuildCraft/BuildCraft).*