<lore>
Hand crafting and mechanical shaping of your ingredients can only go so far. Thankfully, the power of focused light is so precise you can slice up blocks into individual sides, or even etch fine circuits into chipsets!
</lore>
<no_lore>
The Assembly Table is a laser-powered crafter for BuildCraft-only recipes — chipsets, gate plugs, wires, lens plugs, pulsars, light sensors, timers, the gate copier, and facades. Multiple recipes can be queued at once; the table batches them round-robin while laser power accumulates.
</no_lore>
<chapter name="Information"/>
The 3×4 grid on the <bold>left</bold> of the GUI is the ingredient buffer. Resources can be inserted from any side of the table.
The 3×4 grid on the <bold>right</bold> is the recipe catalogue. As soon as the contents of the buffer match any registered assembly recipe, that recipe's output appears in the catalogue. Once a valid power source is registered, click an output once to queue it. Multiple recipes can be queued at once; a bright red outline means that is the current recipe, and a dark red outline means it is in the queue. To pause, skip, or remove a recipe from the queue, click on it again, and a gray outline will appear.
The MJ the table has accumulated stays in its internal battery, so if focus shifts to a different recipe, the new one starts from the level already in the progress bar rather than from zero.
Outputs eject to any adjacent inventory or pipe on any face, or spill into the world if there is nowhere to put them.
<recipes_usages stack="buildcraftsilicon:assembly_table"/>
<chapter name="Power"/>
Like the <link inline="buildcraft:block/advanced_crafting_table"/>, the Assembly Table is a Laser target. Aim one or more Lasers at it and they fill the progress bar on the GUI's left edge. More Lasers means faster batches.
Recipe costs span three orders of magnitude. Lens plugs, the Light Sensor, the Timer, and the Gate Copier are at the cheap end (500 MJ); Pulsar plugs and basic Gate plugs sit at about 1,000 MJ. Wires cost 5,000 MJ per craft and produce 8 wires. Chipsets are the expensive tier: 10,000 MJ for Red, 20,000 for Iron, 40,000 for Gold, 60,000 for Quartz, and 80,000 for Diamond. JEI shows the per-recipe cost on every recipe page.
<chapter name="What it makes"/>
<bold>Chipsets:</bold> Redstone, Iron, Gold, Quartz, and Diamond Redstone Chipsets. These are the base-level crafting materials used for making pipe pluggables.
<bold>Plugs:</bold> Pulsar (a gate-driven engine kicker), Light Sensor (a trigger source for light level), Timer (a three-rate clock), Gate Copier (clones gate logic between gates), and Lens / Filter Lens variants in every dye colour.
<bold>Wires:</bold> Pipe Wires in all sixteen dye colours, eight per batch. Wires tie gates on different pipes into a single signal network.
<bold>Gates:</bold> Basic AND/OR Gate plugs in Iron, Netherbrick, and Gold materials, plus the modifier upgrades (Lapis, Quartz, Lapis+Quartz, Diamond) that promote a basic gate into a more capable variant.
<bold>Facades:</bold> Decorative wrappers around almost any block, applied to a pipe to disguise it as that block.
