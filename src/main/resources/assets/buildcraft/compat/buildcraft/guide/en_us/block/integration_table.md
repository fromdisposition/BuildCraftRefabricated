<lore>
Some upgrades are too delicate for a workbench. The Integration Table fuses extra components into an existing item — gate modifiers, chipset tiers, and other NBT-level changes — using focused laser power instead of a shaped crafting grid.
</lore>
<no_lore>
The Integration Table is a laser-powered workstation that upgrades a single target item by consuming additional ingredients placed around it. Unlike the <link inline="buildcraft:block/assembly_table"/>, recipes here modify an existing stack (usually a gate or chipset) rather than crafting a new item from scratch.
</no_lore>
<chapter name="Information"/>
The large slot on the <bold>left</bold> is the target item — the gate, chipset, or other base you want to upgrade.
The 3×3 grid on the <bold>right</bold> holds the components to integrate. Only layouts that match a registered integration recipe are accepted.
The single slot at the <bold>bottom</bold> receives the finished item once enough MJ has been accumulated.
<recipes_usages stack="buildcraftsilicon:integration_table"/>
<chapter name="Power"/>
The Integration Table is a laser target like the Assembly and Advanced Crafting tables. Aim one or more <link inline="buildcraft:block/laser"/>s at it; the vertical bar on the GUI shows MJ progress toward the current recipe.
The table only draws laser power when a valid recipe is detected and there is room in the output slot. MJ already stored is kept if ingredients change, so partial progress is not lost while waiting for parts.
Integration costs vary by recipe — gate material upgrades and chipset promotions are typically more expensive than simple plug attachments. JEI lists per-recipe energy where available.
