<lore>
Instead of manually collecting the shape and intricacies of a structure by hand, this block can capture all the data within a marked volume, and when given to a <link inline="buildcraft:block/builder"/>, you can either rebuild it 1:1, or use just the shape of the structure.
</lore>
<no_lore>
The Architect Table scans the contents of a Land Mark volume into a Blueprint or Template, producing a filled snapshot item that the <link inline="buildcraft:block/builder"/> can reconstruct elsewhere.
</no_lore>
<chapter name="Information"/>
Place the Architect at the corner of a volume defined with <link inline="buildcraft:block/marker_volume"/>s to register that as the starting corner to capture. Breaking or removing the Architect Table will remove the volume, and it will need to be re-marked with Land Markers
Place a <link inline="buildcraft:item/blueprint_clean"/> or <link inline="buildcraft:item/template_clean"/> into the input slot. Type a name into the field below the slot to differentiate between other snapshots whilst in your inventory or a <link inline="buildcraft:block/library"/>. The 3D preview at the top of the GUI rotates a live model of the marked area's current contents so you can see what will be captured.
Scanning starts as soon as any blueprint or template is inserted. Scanning runs over the whole volume, and ejects a filled (used) snapshot into the output slot. Scans cost no power, and you can use already used blueprint and templates again to overwrite them.
<recipes_usages stack="buildcraftbuilders:architect"/>
<chapter name="Blueprint vs Template"/>
<bold>Blueprints</bold> record the full block state of every position plus any entities in the volume. This will capture the rotation, state, and inventory of all blocks. The Builder reconstructs them with the same materials they were captured with.
<bold>Templates</bold> record only whether each position is occupied or empty. The Builder fills every captured position with whichever single block you stock it with; useful for repetitive reusable structures like columns or walls. On a technical note, templates scan three times faster than blueprints and serialize into a much smaller file.
