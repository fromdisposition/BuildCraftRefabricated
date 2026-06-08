# BuildCraft API (Refabricated)

Public entry points for addon and integration authors. Registries are assigned during mod initialization in the matching `*Fabric` module classes.

## Transport

- `buildcraft.api.transport.pipe.PipeApi` — pipe definitions, flow types, pluggable/stripes registries, transfer tuning.
- `buildcraft.api.transport.pipe.IPipeRegistry` — register custom pipe materials and behaviours.

## Energy

- `buildcraft.api.mj.MjAPI` — MJ constants, capability tokens, RF conversion helpers.
- `buildcraft.api.mj.IMjConnector` / `IMjReceiver` — attach MJ sources and sinks to blocks and tiles.

## Silicon & facades

- `buildcraft.api.facades.FacadeAPI` — facade item/registry, IMC message IDs, disable/map helpers.
- `buildcraft.api.recipes.BuildcraftRecipeRegistry` — integration, refinery, and programming-table recipe registries.

## Robotics

- `buildcraft.api.robots.*` — robot entities, docking stations, AI base classes.

## Events (`buildcraft.api.events`)

Event classes are part of the public API surface but are **not fired** by Refabricated yet. They are marked `@Deprecated(forRemoval = true)` until a Fabric event bus wires them up. Do not subscribe in production addons.

## Conventions

- Capability-like tokens on API classes (`CAP_*`, `pipeRegistry`, etc.) are set by BuildCraft during startup; check for null before use in cross-mod init.
- Prefer registering through the typed registry interfaces rather than mutating internal maps.
