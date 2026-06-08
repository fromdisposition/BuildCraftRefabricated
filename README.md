# BuildCraft Refabricated

**Fabric port of BuildCraft for Minecraft 26.1.2**

**Repository:** [github.com/fromdisposition/BuildCraftRefabricated](https://github.com/fromdisposition/BuildCraftRefabricated)  
**Issues:** [github.com/fromdisposition/BuildCraftRefabricated/issues](https://github.com/fromdisposition/BuildCraftRefabricated/issues)

| | |
|---|---|
| **Mod ID** | `buildcraftrefabricated` |
| **Version** | `26.1.2-1` |
| **Platform** | Fabric Loader ≥ 0.19.2 |
| **Minecraft** | 26.1.2 |
| **Java** | 25+ |
| **License** | [MPL-2.0](LICENSE) |

BuildCraft Refabricated extends Minecraft with pipes, engines, quarries, gates, oil processing, auto-crafting, and the rest of the classic BuildCraft automation toolkit. This is the **unofficial Fabric port** for **Minecraft 26.1.2**, developed and published from **[fromdisposition/BuildCraftRefabricated](https://github.com/fromdisposition/BuildCraftRefabricated)**.

The modern 26.1.2 gameplay code traces back to **[legoj15's NeoForge port](https://github.com/legoj15/BuildCraft)** (NeoForge, not Fabric). **This repository** is where that tree is migrated to Fabric and maintained: Transfer API interop (`FabricTransferBridge`), BC 8.0-faithful fluid rendering, pipe/attachment fixes, transactional transfer cleanup, and release work all land here.

---

## Table of contents

1. [What this mod is](#what-this-mod-is)
2. [Repository and development](#repository-and-development)
3. [Lineage and attribution](#lineage-and-attribution)
4. [License](#license)
5. [Requirements and installation](#requirements-and-installation)
6. [BC 8.0.x compatibility matrix](#bc-80x-compatibility-matrix)
7. [Fabric port improvements](#fabric-port-improvements)
8. [Transfer API interoperability](#transfer-api-interoperability)
9. [Known limitations](#known-limitations)
10. [Building from source](#building-from-source)
11. [Reporting issues](#reporting-issues)
12. [Links and references](#links-and-references)

---

## What this mod is

BuildCraft Refabricated ships as a **single Fabric mod** that bundles every module that BC 8 distributed as separate Forge JARs:

| Module | Package | Contents (summary) |
|--------|---------|-------------------|
| **Core** | `buildcraft.core` | Landmarks, volume boxes, marker tools, map location, robot goggles, list mod, springs, creative/redstone engines |
| **Lib** | `buildcraft.lib` | Shared GUI, tiles, transfer system, client rendering, mixins, guide infrastructure |
| **Energy** | `buildcraft.energy` | Stone/iron/FE engines, dynamo, oil & fuel fluids (3 heat tiers), buckets, worldgen |
| **Transport** | `buildcraft.transport` | Item, fluid, MJ, and RF pipes; pluggables; wire systems; filtered buffer |
| **Factory** | `buildcraft.factory` | Tank, pump, flood gate, distiller, heat exchange, chute, auto workbench (items + fluids), mining well |
| **Builders** | `buildcraft.builders` | Quarry, filler, architect table, builder, blueprints, schematics |
| **Silicon** | `buildcraft.silicon` | Assembly/integration/advanced crafting tables, lasers, gates, facades, lenses |
| **Robotics** | `buildcraft.robotics` | Zone planner, deployable robots, docking stations, requester (see matrix for gaps) |
| **Fabric adapter** | `buildcraft.fabric` | Registries, networking, config, client events, module bootstrap |

**Entry points:**

- Server/common: `buildcraft.fabric.BuildCraftFabricMod`
- Client: `buildcraft.fabric.BuildCraftFabricClient`

This is **not** a byte-for-byte clone of every BC 8 behaviour. Minecraft 26.1.2 differs from 1.12.2 in recipes, components, rendering, and entity APIs. The goal is **ideological continuation** of BuildCraft — pipe logic, MJ network, oil ecosystem, quarry automation — with honest engineering for Fabric and modern MC.

---

## Repository and development

All source, issues, and releases for this Fabric port live in **[fromdisposition/BuildCraftRefabricated](https://github.com/fromdisposition/BuildCraftRefabricated)**.

### Clone

```bash
git clone https://github.com/fromdisposition/BuildCraftRefabricated.git
cd BuildCraftRefabricated
```

### Git remotes

| Remote | URL | Purpose |
|--------|-----|---------|
| **origin** | `https://github.com/fromdisposition/BuildCraftRefabricated.git` | This repository — commit and push here |

Optional — NeoForge reference tree for comparison or cherry-picks:

```bash
git remote add upstream https://github.com/legoj15/BuildCraft.git
```

### Typical workflow

```bash
git add -A
git commit -m "describe your change"
git push -u origin 26.1.2
```

Default branch for this repository is **`26.1.2`**. Active fixes may land on **`fixed`** first and merge into `26.1.2`.

---

## Lineage and attribution

```
Original BuildCraft (SpaceToad, BuildCraft Team)
        │
        ▼
BuildCraft 8.0.x (Forge 1.12.2 — original mechanics & content)
        │
        ▼
BuildCraft unofficial — NeoForge 26.1.2 (legoj15)
        │   https://github.com/legoj15/BuildCraft
        ▼
BuildCraft Refabricated — Fabric 26.1.2 (fromdisposition — this repository)
        https://github.com/fromdisposition/BuildCraftRefabricated
```

- **Original BuildCraft** — Created by SpaceToad and the BuildCraft Team. Pipes, MJ, engines, quarries, and the automation philosophy behind the mod. Licensed under [MPL-2.0](LICENSE).
- **BuildCraft 8.0.x** — Last major classic line (Forge, Minecraft 1.12.2). The [compatibility matrix](#bc-80x-compatibility-matrix) compares **this Fabric build** against BC 8.0.x **gameplay**.
- **BuildCraft unofficial — [legoj15](https://github.com/legoj15) NeoForge 26.1.2** — Modern community port on **NeoForge** ([legoj15/BuildCraft](https://github.com/legoj15/BuildCraft)): unified mod JAR, BC 8 modules brought to 26.1.2, attachment-style capabilities, registries, networking, and most gameplay code. **Not Fabric.** This tree is the **primary code source** adapted into the Fabric build below.
- **BuildCraft Refabricated — [fromdisposition/BuildCraftRefabricated](https://github.com/fromdisposition/BuildCraftRefabricated) (this repo)** — **The Fabric port.** Source of truth for the Fabric build: `buildcraft.fabric.*`, `FabricTransferBridge`, Loom/Fabric API wiring, fluid pipe rendering, interop, and ongoing maintenance.

**Credits:** SpaceToad and the BuildCraft Team (original mod); **[legoj15](https://github.com/legoj15)** (NeoForge 26.1.2 port — major code source); **[fromdisposition](https://github.com/fromdisposition)** (Fabric 26.1.2 port — maintainer of this repository).

---

## License

This project is licensed under the **[Mozilla Public License 2.0](LICENSE)** (MPL-2.0), consistent with upstream BuildCraft.

In short:

- MPL-licensed source files remain under MPL when modified.
- Modifications to MPL files must be distributed under MPL.
- You may combine MPL code with other code in a larger work under different terms, as described in the [MPL FAQ](https://www.mozilla.org/en-US/MPL/2.0/FAQ/).

Some files under `buildcraft.lib.attachments` and related packages carry **NeoForged LGPL-2.1** headers from upstream porting work. Those files remain under their stated upstream license where applicable.

---

## Requirements and installation

### Runtime

| Requirement | Version |
|-------------|---------|
| Minecraft | **26.1.2** |
| Fabric Loader | **≥ 0.19.2** |
| Fabric API | Required (`fabric-api` 0.150.0+26.1.2 or compatible) |
| Team Reborn Energy API | Bundled with the mod JAR (`teamreborn:energy` ≥ 5.0.0) |
| Java | **25+** |

### Optional

- **JEI** (Just Enough Items) — suggested in `fabric.mod.json`; recipe viewers for engines and assembly table.

### Install

1. Install Fabric Loader for Minecraft 26.1.2.
2. Place **Fabric API** and the BuildCraft Refabricated JAR in your `mods` folder.
3. Launch the game.

Pre-built JARs are produced by `./gradlew build` (see [Building from source](#building-from-source)).

---

## BC 8.0.x compatibility matrix

This table lists gameplay features from **BuildCraft 8.0.x** on **Forge 1.12.2** and
their status in **BuildCraft Refabricated** on **Fabric 26.1.2** (this repository).
The **BC 8.0.x** column is the historical baseline. The **Fabric 26.1.2** column is
the current port status in this build.

✅ done · 🚧 WIP (present with gaps) · ❌ TODO (significant scope missing) · ➖ n/a (not applicable).
**DONE** in **Notes** means parity for normal gameplay.

|                              | BC 8.0.x       | Fabric 26.1.2  | Notes |
| ---------------------------- | -------------- | -------------- | ----- |
| **Core** (`buildcraft.core`) |                |                |       |
| Landmark / path markers      | ✅             | ✅             | DONE — path/volume markers, connector, client render |
| Volume box system            | ✅             | ✅             | DONE — saved data, client sync, filler/architect integration; `volume_box` is creative-only (no survival recipe), same as BC 8 |
| List mod                     | ✅             | ✅             | DONE — GUI + NBT, survival recipe; pipes/robots/gates filter by list. Builder/filler resource matching uses exact stack merge (`canMerge`), not list substitution — same as BC 8 |
| Creative engine              | ✅             | ✅             | DONE  |
| Engine tester (dev)          | ✅             | 🚧             | `BlockPowerConsumerTester` registered only with `-Dbuildcraft.dev=true` |
| Oil springs                  | ✅             | ✅             | DONE — `BlockSpring` + `TileSpringOil` liquid generation |
| Paintbrush (pipes & blocks)  | ✅             | ✅             | DONE — colours pipes and vanilla paintable blocks |
| Map location                 | ✅             | ✅             | DONE — survival item, zone planner map export |
| Robot goggles                | ✅             | ✅             | DONE — survival item, in-world zone overlay when worn |
| **Transport** (`buildcraft.transport`) |      |                |       |
| Item pipes                   | ✅             | ✅             | DONE — smooth client item interpolation |
| Fluid pipes                  | ✅             | ✅             | DONE — dyed waterproofing band via runtime mask tint (no atlas `dye_replace`) |
| MJ power pipes               | ✅             | ✅             | DONE  |
| RF / Redstone Flux pipes     | ✅             | ✅             | DONE — `PipeFlowRedstoneFlux`, Team Reborn `EnergyStorage` neighbour transfer; optional `disableRfPipe` config |
| Pipe behaviours              | ✅             | ✅             | DONE  |
| Pipe pluggables              | ✅             | ✅             | DONE  |
| Silicon pipe pluggables      | ✅             | ✅             | DONE — facade, lens, gate, pulsar, timer, light sensor |
| Wire systems                 | ✅             | ✅             | DONE — `WireManager`, `SavedDataWireSystems`, gate/pipe payload sync |
| Filtered buffer              | ✅             | ✅             | DONE  |
| Passive fluid pull (non-wood)| ➖             | ➖             | By BC design — wood pipe + MJ only |
| **Energy** (`buildcraft.energy`) |            |                |       |
| Stone engine (solid fuel)    | ✅             | ✅             | DONE  |
| Iron engine (liquid fuel)    | ✅             | 🚧             | Combustion loop works; non-output faces use combined fuel/coolant/residue storage (see limitation #10) |
| Redstone / creative engines  | ✅             | ✅             | DONE  |
| FE engine + Dynamo MJ        | ✅             | ✅             | DONE — `TileEngineRF`, `TileDynamoMJ`; `MJ_ONLY` gates MJ-machine RF bridge only |
| Oil / fuel / residue fluids  | ✅             | ✅             | DONE  |
| BC fluid buckets             | ✅             | ✅             | DONE  |
| Oil spring worldgen          | ✅             | ✅             | DONE  |
| Engine iron GUI              | ✅             | ✅             | DONE — fuel/coolant/residue tanks (BC 8.0.1+) |
| **Factory** (`buildcraft.factory`) |          |                |       |
| Tank (multi-column)          | ✅             | ✅             | DONE  |
| Pump                         | ✅             | ✅             | DONE  |
| Flood gate                   | ✅             | ✅             | DONE  |
| Distiller                    | ✅             | ✅             | DONE  |
| Heat exchange                | ✅             | ✅             | DONE  |
| Chute                        | ✅             | ✅             | DONE  |
| Auto workbench (items)       | ✅             | ✅             | DONE  |
| Auto workbench (fluids)      | ✅             | ✅             | DONE — 2×2 crafting, dual tanks, sided fluid insert (tank1: DOWN/NORTH/WEST; tank2: UP/SOUTH/EAST) |
| Mining well                  | ✅             | ✅             | DONE — MJ vertical mining, tube extension, drop routing; halts on dense fluids (lava/oil) |
| **Builders** (`buildcraft.builders`) |        |                |       |
| Quarry                       | ✅             | ✅             | DONE — frame build, strip-mine to bedrock, MJ, chunkloading, item output; skips fluids, drill passes through (not a pump) |
| Filler (+ planner addon)     | ✅             | ✅             | DONE — `TileFiller`, pattern registry, planner addon GUI |
| Architect table              | ✅             | ✅             | DONE — blueprint/schematic scan and output |
| Builder                      | ✅             | ✅             | DONE — blueprint/template build, fluid tanks, MJ |
| Electronic library, replacer | ✅             | ✅             | DONE — snapshot download/upload, schematic replace |
| **Silicon** (`buildcraft.silicon`) |          |                |       |
| Assembly table               | ✅             | ✅             | DONE — laser-powered assembly, JEI plugin |
| Integration table            | ✅             | ✅             | DONE — integration recipes, laser target |
| Advanced crafting table      | ✅             | ✅             | DONE — blueprint crafting, laser target |
| Laser(s)                     | ✅             | ✅             | DONE — cone scan, MJ delivery to laser tables |
| Gates (pipe pluggable)       | ✅             | ✅             | DONE — triggers/actions via `GateLogic`, wire-input polling, wire power wake |
| Facades, lenses, pulsar, etc.| ✅             | ✅             | DONE — pluggables, item models, bakers |
| **Robotics** (`buildcraft.robotics`) |        |                |       |
| Zone planner                 | ✅             | ✅             | DONE — survival block, GUI, map export to map location |
| Deployable robots            | ✅             | 🚧             | `EntityRobot` + AI boards; no crafting recipe for empty robot shell |
| Robot docking stations       | ✅             | ✅             | DONE — pipe pluggable + docking station pipe |
| Requester                    | ✅             | ✅             | DONE  |
| **Lib / shared** (`buildcraft.lib`) |         |                |       |
| Guide book                   | ✅             | 🚧             | `GuiGuide` works; 164 pages under `assets/buildcraft/compat/`; robotics = `zone_planner.md` only; missing entries auto-stub as WIP |
| Statements / triggers        | ✅             | ✅             | DONE — core, transport, builders, silicon statements + gate resolution |
| MJ API (`MjAPI`)             | ✅             | ✅             | DONE  |
| Forge fluid/item caps        | ✅             | ➖             | Replaced by attachments + Fabric Transfer API |
| Fabric fluid/item interop    | ➖             | ✅             | DONE  |

---

## Fabric port improvements

The items below are **Fabric-port and maintenance work** in [BuildCraftRefabricated](https://github.com/fromdisposition/BuildCraftRefabricated), on top of the NeoForge-derived 26.1.2 base (and relative to classic BC 8.0.x on Forge 1.12.2). Everything listed is code-backed, not aspirational.

### Platform and distribution

- **Single mod JAR** instead of BC8's separate Core / Transport / Energy / Factory / Builders / Silicon / Robotics modules.
- **Minecraft 26.1.2** with Fabric Loader, Fabric API, and Java 25.
- **Deferred and runtime registries** via `buildcraft.fabric.registry.*` and `BCRegistries` for blocks, items, fluids, block entities, and data components.
- **Unified JSON config** (`buildcraftrefabricated-common.json` via `BCFabricConfig`) covering core, lib, energy, factory, and transport settings; reloadable on datapack reload.
- **Custom Fabric networking** (`BuildCraftFabricNetworking`) for pipe items, payloads, wire systems, and landing effects.

### Transfer system rewrite

BC8 used Forge `IFluidHandler` and `IItemHandler` directly. This port introduces a unified transactional layer:

- **`ResourceHandler<T>`** — typed insert/extract with slot support (`buildcraft.lib.transfer`).
- **`Transaction` / `TransactionContext`** — simulate-then-commit semantics across pipes, tanks, and bridges.
- **`IFluidHandlerAdv`** — modern fluid API extending `ResourceHandler<FluidResource>` (replaces legacy `lib/fluids/capability/IFluidHandler`).

**Removed dead legacy** (no longer referenced anywhere):

- `lib/fluids/capability/IFluidHandler.java`
- `lib/fluids/capability/templates/FluidTank.java`
- `lib/misc/MultiTankResourceHandler.java`

### Attachments API (capability replacement)

NeoForge-style attachments adapted for Fabric (`buildcraft.lib.attachments`):

| Attachment | Contexts |
|------------|----------|
| `Attachments.Fluid.BLOCK` / `ITEM` | Sided blocks, fluid container items |
| `Attachments.Item.BLOCK` / `ITEM` / `ENTITY` | Inventories, bundles, player automation |
| `Attachments.Energy.BLOCK` | Internal FE storage (engines, dynamo, RF pipes) |

**Vanilla providers** (`AttachmentHooks.init()`):

- Chests, trapped chests, furnaces, blast furnaces, smokers, hoppers, barrels, dispensers, droppers, shulker boxes
- All vanilla `BucketItem`s (except `buildcraftenergy` buckets registered separately)
- Players (inventory wrappers)
- Composter
- **Cauldron fluid content** — new vs BC8 (`CauldronFluidContent`)

**Per-module registration** in `BCCoreFabric`, `BCEnergyFabric`, `BCFactoryFabric`, `BCTransportFabric`, `BCBuildersFabric`, etc.

**Resolution chain** (`BlockAttachment.getCapability`):

1. Registered block/BE provider
2. `FabricTransferBridge.tryWrapForward` for external Fabric mods
3. `null`

`AttachmentLevelAccess` (level mixin) adds caching and invalidation on block changes.

### Fabric Transfer API bridge

`FabricTransferBridge` (`buildcraft.lib.fabric.transfer`) is the core interoperability layer.

**BC → Fabric (fallbacks registered at init):**

| Fabric API | BC wrapper |
|------------|------------|
| `FluidStorage.SIDED` | `BcFluidStorage` |
| `ItemStorage.SIDED` | `BcItemStorage` |
| `FluidStorage.ITEM` | `BcFluidStorage` (BC providers only, via `getCapabilityFromProvidersOnly`) |

**Fabric → BC (forward wrap in attachment lookup):**

| Fabric API | BC wrapper |
|------------|------------|
| `FluidStorage.SIDED` | `FabricFluidResourceHandler` |
| `ItemStorage.SIDED` | `FabricItemResourceHandler` |
| `FluidStorage.ITEM` | `FabricFluidResourceHandler` (via `ItemAttachment` + `tryWrapItemFluid`) |

**Safety mechanisms:**

- `ThreadLocal REENTRANT` guard prevents infinite BC↔Fabric lookup loops.
- `FabricTransactionMirror` / `FabricToBcTransactionMirror` nest Fabric and BC transactions so simulate/commit stay aligned.
- `TransferConvert` maps Fabric droplets to BC millibuckets (81 droplets = 1 mB); cross-mod moves use **whole mB only**.

**Item fluid containers:**

- `ContainerItemContextItemAccess` maps Fabric `ContainerItemContext` to BC `ItemAccess` for correct read/write.
- BC buckets registered in `BCEnergyFabric.registerBucketAttachments()`.
- Fragile fluid shards in `BCCoreFabric`.
- `AttachmentHooks` skips `buildcraftenergy` buckets to avoid double registration.

### Pipe transport enhancements

Improvements over BC8 pipe behaviour and interop:

| Area | Change | Key files |
|------|--------|-----------|
| Fluid section extract | Real `Section.extract` with `drainInternal`, `canOutput`, fluid match, empty cleanup | `PipeFlowFluids.java` |
| Advanced fluid filter | `tryExtractFluidAdv` applies `IFluidFilter.matches()` before extract | `PipeFlowFluids.java` |
| Section NBT | Writes `"amount"`, reads with fallback `"capacity"` | `PipeFlowFluids.java` |
| Sided fluid dedup | `SidedFluidHandlers.insertOnly` / `extractOnly` for pump, distiller, heat exchange | `SidedFluidHandlers.java`, `BCFactoryFabric.java` |
| Fluid item bridge | Mod fluid cells visible to diamond filter and `FluidUtilBC.isFluidContainerItem()` | `FabricTransferBridge.java`, `ItemAttachment.java` |
| Item pipe Fabric insert | `PipeItemInjectHandler` on `PIPE_HOLDER` — external mods insert via `ItemStorage.SIDED` | `PipeItemInjectHandler.java`, `BCTransportFabric.java` |
| Item extract from tiles | `tryExtractItems` uses `Attachments.Item.BLOCK` + transactions | `PipeFlowItems.java` |
| Force inject client sync | `insertItemsForce` calls `addItemTryMerge` → `sendItemDataToClient` | `PipeFlowItems.java` |
| Power/RF display dedup | Shared `PipeEnergyEnumFlow`, `PipeEnergyDisplaySupport` (client anim, network sync, neighbour propagate) | `PipeEnergyEnumFlow.java`, `PipeEnergyDisplaySupport.java` |
| PipeRegistry API | `createItemForPipe` / `createUnnamedItemForPipe` implemented via `BCRegistries` | `PipeRegistry.java` |

### Fluid rendering (BC 8.0 visual fidelity)

BC8 used `AtlasSpriteFluid` on 1.12.2. The Fabric port reimplements that look on modern vertex consumers:

| Component | Role |
|-----------|------|
| `FluidUtilBC.FluidRenderContext` | Centralized render context (world/BER/GUI vs pipe recolor) |
| `FluidUtilBC.FluidAppearance` / `vertexRgba()` | Single appearance API for tint and alpha |
| `BcFluidTintUtil` | Heat-template bake and per-vertex pipe recolor |
| `BcFluidBakeSpriteSource` | Atlas-stitched baked still/flow sprites for world/BER/GUI |
| `BcHeatWhiteSpriteSource` | White pipe sprites with `.mcmeta` animation metadata copied into `SpriteContents` |
| `BcFluidQuadEmitter` | 16×16 subdivided quads with per-vertex heat gradient in pipes |
| `BcFluidGuiDrawer` | Shared tiled GUI tank drawing |
| `BcFluidQuadEmitter.emitTankQuad` / `emitTankHorizontal` | Deduped BER tank rendering |
| `BCEnergyFabricClient` | `FluidRenderingRegistry` registration with baked sprites |

**Pipe fluid UV fix:** face-grid `(u,v)` converted to sprite `normalizedU/V(tu,tv)` so heat gradients align correctly on pipe faces.

### Pipe dye bands and blocks atlas

BC8 baked ~350 dyed fluid-pipe sprites via `dye_replace` into the blocks atlas. The Fabric port replaces that with **runtime vertex tint**:

| Area | Change | Key files |
|------|--------|-----------|
| Atlas | Removed `buildcraftlib:dye_replace` entries and `DyeReplaceSpriteSource` (~350 fewer stitched sprites) | `blocks.json`, `BCSpriteSourcesFabric.java` |
| World pipes | BER mask overlay tints the waterproofing band; fluid pipes use alpha 255 (opaque dye), other colourable pipes keep alpha 76 | `RenderPipeHolder.java`, `PipeBaseModelGenStandard.generateMaskMutable` |
| Item pipes | Overlay quads coplanar with base model; fluid item tint alpha `0xFF` | `PipeItemModel.java` |
| Item travel | Client-side simulation when items are in transit (no choppy BER animation) | `PipeFlowItems.hasClientSimulationWork()` |

### Code hygiene (recent cleanup)

- Removed `DyeReplaceSpriteSource` and all `dye_replace` atlas sources; pipe dye bands use mask + vertex colour instead.
- Fixed item pipe client interpolation (`hasClientSimulationWork` no longer skips the client).
- Removed empty `PipeFlowRendererItems` stub; colour overlay quads moved to `PipeItemColourQuads`.
- Removed unused `IFluidHandlerAdv` imports from `PipeFlowFluids`, `FluidUtilBC`, `IFlowFluid`.
- Removed dead method `getFirstNonEmptySet()` from `PipeFlowItems`.
- Bucket attachment dedup: `AttachmentHooks` skips `buildcraftenergy` namespace buckets already registered in `BCEnergyFabric`.
- AUDIT hot-path performance and nativeness fixes (caching, Fabric-native APIs on pipe/transfer/render paths).

---

## Transfer API interoperability

Reference for mod developers connecting to BuildCraft on Fabric.

| Resource | BC attachment / API | Fabric API | Direction |
|----------|---------------------|------------|-----------|
| Block fluids | `Attachments.Fluid.BLOCK` | `FluidStorage.SIDED` | **Both ways** |
| Block items | `Attachments.Item.BLOCK` | `ItemStorage.SIDED` | **Both ways** |
| Item fluids (buckets, cells) | `Attachments.Fluid.ITEM` | `FluidStorage.ITEM` | **Both ways** |
| Block energy (RF) | `Attachments.Energy.BLOCK` | `EnergyStorage.SIDED` (Team Reborn) | **BC → Fabric** when RF autoconversion enabled |
| MJ power | `MjAPI.CAP_RECEIVER` / `CAP_CONNECTOR` / `CAP_PASSIVE_PROVIDER` | — | BC internal only |
| RF pipes | `PipeFlowRedstoneFlux` | `EnergyStorage.SIDED` on `PIPE_HOLDER` | **Both ways** with Team Reborn API mods |

### What works without custom compat code

- Fabric fluid tanks, hoppers, pipes → BC fluid pipes, tanks, pump, engines (via sided fluid storage).
- Fabric item automation → BC item pipes, chests, machines (bidirectional via `PipeItemInjectStorage` / sided item storage).
- Fabric fluid container items → BC diamond fluid filter, tank GUI fill/drain, `isFluidContainerItem()`.
- BC buckets and fluid shards → visible to other mods querying `FluidStorage.ITEM`.

### What does not bridge

- External **MJ** mods — BC MJ API only.
- **MJ↔RF conversion** on BC machines — requires disabling `MJ_ONLY` and enabling RF autoconversion in config; off by default.
- Item pipe **nested transaction rollback** — `PipeItemInjectStorage` can leave partial state if a nested Fabric transaction aborts mid-flow.

---

## Known limitations

Honest list of current gaps and design constraints:

1. **Maintenance scope** — Focus is bugfixes and port stability, not major new features.
2. **Not Minecraft 1.12.2** — Recipes, components, redstone, entities, and the mod ecosystem differ from BC8.
3. **RF/MJ energy interop gated by config** — Team Reborn `EnergyStorage` is bundled and wired for RF pipes and several machines; external mods using the same API can interact when RF autoconversion is enabled. MJ pipes remain BC-internal; `MJ_ONLY` is the default.
4. **Fluid bridge granularity** — Cross-mod fluid moves truncate to whole millibuckets (81 Fabric droplets = 1 mB).
5. **Wood pipe required for extraction** — Cobble/gold/iron fluid and item pipes do not passively drain neighbours; wood + MJ is by BC design.
6. **Multi-tank fluid extract** — Unfiltered external fluid pulls may return the first tank view in iteration order, not a merged multi-tank handler.
7. **Robotics survival loop gap** — No recipe for empty `buildcraftrobotics:robot` shell; chain is redstone board → programming table → integration table → docking station deploy.
8. **Guide book content incomplete** — 164 pages load; robotics guide has only `zone_planner.md`; several silicon tables (e.g. programming table) also lack writeups; missing entries auto-stub as WIP.
9. **Dev-only blocks** — Engine power tester and decorated debug blocks register only with `-Dbuildcraft.dev=true`; creative engine is always available.
10. **Engine explosion default** — `canEnginesExplode` defaults to `false`; BC 8 commonly used `true`.
11. **RF autoconversion off by default** — Config `MJ_ONLY`; enabling RF↔MJ conversion exposes `EnergyStorage` on BC machines and is required for cross-mod RF tank/engine interop beyond RF pipes.
12. **Pipe flow transactions** — Pipe internals mutate immediately; nested transaction rollback does not fully undo travelling items (especially `PipeItemInjectStorage`).
13. **MJ loss particles** — Engine spark effects are no-op (`NullaryEffectManager`); gameplay MJ loss still applies.

---

## Building from source

### Prerequisites

- **JDK 25**
- **Git**

Gradle wrapper is included; a separate Gradle install is optional.

### Commands

From a clone of [BuildCraftRefabricated](https://github.com/fromdisposition/BuildCraftRefabricated):

```bash
./gradlew build        # Unix / macOS
gradlew.bat build      # Windows
```

On success, `build/libs/` contains:

- `BCRefabricated-{version}.jar` — release mod JAR (e.g. `BCRefabricated-26.1.2-1.jar`)
- `BCRefabricated-{version}-sources.jar` — sources (optional)

Copy the release JAR into your Minecraft `mods` folder manually (or use `gradlew runClient` for a dev instance).

### Project layout

```
BuildCraftRefabricated/              ← repository root (clone of this repo)
├── src/main/java/buildcraft/        ← mod source (api, core, lib, modules, fabric)
├── src/main/resources/
│   ├── assets/                      ← BC textures, models (per-module namespaces)
│   │   ├── buildcraft/lang/         ← master lang file (BC 8.0.x layout)
│   │   ├── buildcraft/compat/       ← guide scripts and pages
│   │   ├── buildcraft*/models/compat/ ← variable engine models (core, energy, lib)
│   │   └── buildcraftbuilders/compat/ ← builder snapshot rules (BC 8.0.x layout)
│   ├── data/                        ← recipes, tags, loot tables (per-module)
│   │   ├── buildcraft*/advancement/ ← advancement tree split by BC module
│   │   └── buildcraft*/compat/      ← guide book scripts (core + lib)
│   ├── fabric.mod.json              ← Fabric mod metadata
│   ├── buildcraft.mixins.json       ← mixin config
│   └── buildcraft.accesswidener     ← access widener (Fabric port)
├── gradle/ + gradlew*
├── gradle.properties
├── build.gradle.kts
├── settings.gradle.kts
├── LICENSE
└── README.md
```

Gitignored (local only): `build/`, `.gradle/`, `run/`, `misc/`, `scripts/`, `.github/`, IDE folders.

Parity labels describe how closely **this Fabric 26.1.2 build** ([BuildCraftRefabricated](https://github.com/fromdisposition/BuildCraftRefabricated)) matches classic **BC 8.0.x** on Forge 1.12.2.

---

## Reporting issues

Please [open an issue](https://github.com/fromdisposition/BuildCraftRefabricated/issues) if you encounter unexpected behaviour.

For crashes or bugs, include:

- Confirmation the issue is caused by **BuildCraft Refabricated** (interactions with other mods are valid reports).
- At least one of:
  - A **crash report** or relevant **log excerpt**
  - **Steps to reproduce** ("What were you doing when it happened?")
  - **Screenshots or video** demonstrating the problem
- **Mod version** — e.g. `26.1.2-1`; if built from source, link to the commit or tree.
- **Other mods** in use, especially rendering/performance mods (Sodium, Iris, etc.) — very helpful for reproduction.

Check existing issues before filing. Duplicate fluid-tank tooltips and similar BC8-era bugs may already be tracked.

---

## Links and references

| Resource | URL |
|----------|-----|
| **Source & releases** | https://github.com/fromdisposition/BuildCraftRefabricated |
| **Bug reports & feature requests** | https://github.com/fromdisposition/BuildCraftRefabricated/issues |
| **Maintainer** | https://github.com/fromdisposition |
| **Official BuildCraft (GitHub)** | https://github.com/BuildCraft/BuildCraft |
| NeoForge 26.1.2 upstream (legoj15) | https://github.com/legoj15/BuildCraft |
| Original BuildCraft (website, MPL-2.0) | https://www.mod-buildcraft.com |
| MPL-2.0 license text | https://www.mozilla.org/MPL/2.0/ |
| Fabric Transfer API (fluids) | https://wiki.fabricmc.net/tutorial:transfer-api-fluids |
| Fabric Transfer API (fluid items) | https://wiki.fabricmc.net/tutorial:transfer-api_fluid-containing-items |

---

*BuildCraft Refabricated — [fromdisposition/BuildCraftRefabricated](https://github.com/fromdisposition/BuildCraftRefabricated) — unofficial Fabric port; original mod by [BuildCraft/BuildCraft](https://github.com/BuildCraft/BuildCraft).*
