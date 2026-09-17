<lore>
Blueprints and templates lay things out in such a way that even a machine could read them. The Builder is that machine; give it what it wants, and it'll do the building for you.
</lore>
<no_lore>
The Builder reads a volume snapshot (a Blueprint or Template captured by an Architect Table) and reconstructs the captured volume at a new location, drawing items and fluids from its internal inventory and tanks.
</no_lore>
<chapter name="Information"/>
Insert your snapshot paper into the input slot in the top middle. The resource inventory is directly below. The builder will accept required items from any side of the block. To the right at the top is what is currently left in the queue to be placed, and at the bottom right are the 4 fluid tanks for builds that require the placement of fluid sources. The first stage of building will start as soon as the builder recieves power.
<chapter name="Continuous Building"/>
Place the Builder behind a <link inline="buildcraft:block/marker_path"/> that connects to 1 or more other path markers to have it build along a path. It will build incrementally, block by block; it does not take into account the size and shape of the snapshot, instead "smearing" it along the path.
<recipes_usages stack="buildcraftbuilders:builder"/>
<chapter name="Clearing Lasers"/>
If the builder encounters blocks in the same volume as the build, it will remove via a deconstruction robot. Items from this will be deposited into the inventory of the builder at the same efficiency of an iron pickaxe. If the builder's inventory is full or it encounters blocks that iron picks can't mine, the items will not be collected and the builder will continue.
<chapter name="Fluid Clearing"/>
The fluid-mode button cycles between three behaviours for fluid blocks that fall inside the build area:
- <bold>Skip Fluids</bold> (Barrier icon) ignores existing fluids in a way that source blocks are left untouched and the snapshot is built around them.
- <bold>Replace Fluids</bold> (Bricks icon) places blocks from the snapshot on top of fluids. This can lead to blocks becoming waterlogged.
- <bold>Clear Fluids</bold> (Bucket icon) sweeps every fluid out of the build area before placement. Fluids are collected in the internal tanks. If there is nowhere for them to go, they are discarded.