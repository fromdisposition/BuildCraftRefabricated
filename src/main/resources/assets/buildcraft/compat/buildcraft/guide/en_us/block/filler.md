<lore>
Sometimes you don't want a structure. A flat floor, a sphere, wide stairs, or a hollowed-out room. The Filler allows for picking a geometric pattern without the need for a template in a defined volume.
</lore>
<no_lore>
The Filler builds geometric patterns (boxes, spheres, walls, etc.) inside a marker box and can be configured to invert the pattern or excavate the interior before filling.
</no_lore>
<chapter name="Information"/>
Place the Filler at the corner of a <link inline="buildcraft:block/marker_volume"/>, drag and drop a pattern from the left of the GUI into the Filler's pattern slot and supply it with power and materials. The Filler optionally takes a few parameters (corner offsets, sizes, percentages), and places blocks from its resource inventory into the matching positions inside the marker volume.

The <bold>Excavate</bold> toggle clears the interior of the shape before filling. Excavated blocks will be ejected from the filler (assuming they are mineable by an Iron-tier pick).
The <bold>Invert</bold> toggle augments the pattern by affecting the blocks that <italic>are not</italic> a part of the pattern's shape.
The <bold>Control mode</bold> indicator (set by pipe gates) cycles between ON(default state), OFF, and LOOP. A padlock will appear if an adjacent gate is currently setting the pattern settings.

<recipes_usages stack="buildcraftbuilders:filler"/>