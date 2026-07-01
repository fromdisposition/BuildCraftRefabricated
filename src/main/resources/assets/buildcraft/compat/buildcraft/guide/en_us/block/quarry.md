<lore>
A <link inline="buildcraft:block/mining_well"/> drills a single column. A Quarry chews entire chunks out of the world. With the right power supply and a large enough <link inline="buildcraft:block/marker_volume"/> frame, it strip-mines down to bedrock all by itself.
</lore>
<no_lore>
The Quarry strip-mines a marked area down to bedrock, depositing every block it harvests into adjacent inventories or pipes.
</no_lore>
<chapter name="Information"/>
Surround the area you want mined with Land Marks (up to 64×64 by default), place the Quarry next to one of the marks, and supply it with power. The Quarry builds a frame around the area first, then a head travels inside that frame to mine each block in turn. Output flows into any adjacent inventory or pipe; if there's nowhere to put a drop, the Quarry will eject the item into the world.
The Quarry continues until it reaches bedrock or runs out of unmineable blocks. A red <red>LED</red> on its sides indicates it has work but no power; a green <green>LED</green> indicates it is actively mining; both <red>LE</red><green>Ds</green> together mean it's idle or finished.
<recipes_usages stack="buildcraftbuilders:quarry"/>
<chapter name="Mining Tier"/>
The Quarry's head harvests blocks as if wielding a diamond pickaxe. It will only skip mining if it encounters an impassible fluid (such as lava or oil), or a block that cannot be successfully mined by a diamond pick.
Experience orbs from breaks (redstone ore, lapis, diamond, coal, etc.) spawn at the Quarry block itself, not in the pit, so you can collect XP at the machine without rappelling down to bedrock.
<chapter name="Power"/>
As with the Mining Well, more power means faster mining. Redstone signal pauses the Quarry without resetting its progress.
