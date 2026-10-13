<lore>
A single <link inline="buildcraft:block/autoworkbench_item"/> is fine when you only want one recipe at a time. By the time you've stamped out a Redstone Chipset, though, you've already built Lasers and seen what they can do. A crafting table that runs straight off them is a natural next step.
</lore>
<no_lore>
The Advanced Crafting Table is a laser-powered automatic crafter. Load a vanilla crafting recipe into its blueprint grid, pipe the materials in, and pipe finished items out.
</no_lore>
<chapter name="Information"/>
Define the desired crafting recipe by placing the ingredients in the crafting grid or by selecting a recipe from the crafting book.
The 5×3 buffer on the left is the materials supply. Ingredients can be piped in from any side, and the output can be extracted from any side as well.
Any recipe usable in a crafting table will work.
<recipes_usages stack="buildcraftsilicon:advanced_crafting_table"/>
<chapter name="Power"/>
Each craft costs an equivalent 500 MJ in laser power. The more Lasers you point at it, the faster it accumulates power and the faster it crafts.
The table only asks for laser power once it has a valid recipe in the blueprint grid; an empty table is invisible to Lasers, so they go looking for other work. Any power it has already built up is kept across recipe changes, so swapping blueprints or pausing to wait for materials doesn't waste MJ.
