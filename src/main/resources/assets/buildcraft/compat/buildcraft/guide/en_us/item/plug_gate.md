<lore>
Redstone takes up a lot of space nowadays. Why not have finer control over your system in a more compact space using pipes; just place a Gate on a pipe and start thinking.
</lore>
<no_lore>
The Gate is an upgradeable pluggable used to provide a logistical framework to help manage your pipe system.
</no_lore>

// Each gate lists its crafting-table recipe (the slow, no-machine-needed way) immediately
// followed by its Assembly Table recipe, so the player sees both routes together. Crafting
// recipes are pinned by JSON id; the assembly entries use <recipe_cycle> to cycle the AND and
// OR variants through ONE panel rather than showing two near-identical ones. Crafting recipes
// exist only for the AND variants — to get an OR gate by hand you craft the AND gate then flip
// it with a swap recipe, and those swaps are the two cycling panels at the end. Each assembly
// match substring uniquely selects one gate's AND+OR pair (e.g. "-IRON-LAPIS" → the Iron+Lapis
// AND and OR recipes); "-<MAT>-NO_MODIFIER" selects the base (unmodified) gate of that material.
//
// Basic (clay brick) — crafting only; it has no Assembly Table recipe (terminal item).
<recipe id="buildcraftsilicon:gate_basic"/>
// Iron
<recipe id="buildcraftsilicon:gate_iron_and"/>
<recipe_cycle type="assembling" match="-IRON-NO_MODIFIER"/>
// Nether Brick
<recipe id="buildcraftsilicon:gate_nether_brick_and"/>
<recipe_cycle type="assembling" match="-NETHER_BRICK-NO_MODIFIER"/>
// Gold — Assembly Table only (no crafting recipe)
<recipe_cycle type="assembling" match="-GOLD-NO_MODIFIER"/>
// Iron + Lapis
<recipe id="buildcraftsilicon:gate_iron_and_lapis"/>
<recipe_cycle type="assembling" match="-IRON-LAPIS"/>
// Iron + Quartz
<recipe id="buildcraftsilicon:gate_iron_and_quartz"/>
<recipe_cycle type="assembling" match="-IRON-QUARTZ"/>
// Iron + Diamond — Assembly Table only
<recipe_cycle type="assembling" match="-IRON-DIAMOND"/>
// Nether Brick modifiers — Assembly Table only
<recipe_cycle type="assembling" match="-NETHER_BRICK-LAPIS"/>
<recipe_cycle type="assembling" match="-NETHER_BRICK-QUARTZ"/>
<recipe_cycle type="assembling" match="-NETHER_BRICK-DIAMOND"/>
// Gold modifiers — Assembly Table only
<recipe_cycle type="assembling" match="-GOLD-LAPIS"/>
<recipe_cycle type="assembling" match="-GOLD-QUARTZ"/>
<recipe_cycle type="assembling" match="-GOLD-DIAMOND"/>
// AND<->OR swaps — one shapeless craft flips a gate's logic. Each panel cycles through all 12
// material/modifier variants in that direction (craft an AND gate, then swap it to OR, or back).
<recipe_cycle match="_to_or"/>
<recipe_cycle match="_to_and"/>

<chapter name="Gate Placement"/>
A Gate is a pluggable so it can only be placed on pipes and can be placed on any side of a pipe.
Any pipe placed next to it on that side will not connect.

<chapter name="Gate Tiers"/>
Gates come in different tiers that provide different amounts of slots:
Basic Gate — Provides 1 set of slots (this cannot be modified).
Iron Gate — Provides 2 sets of slots.
Nether Brick Gate — Provides 4 sets of slots.
Gold Gate — Provides 8 sets of slots.

<chapter name="Gate Modifiers"/>
Gates can be modified to provide parameter slots:
Lapis Expansion — Provides 1 parameter slot to each trigger.
Quartz Expansion — Provides 1 parameter slot to each set (will cut the amount of slots the gate has by half).
Diamond Expansion — Provides 3 parameter slots to each set (will cut the amount of slots the gate has by half).

<chapter name="Gate Variants"/>
Using an AND Gate ensures that <bold>all</bold> connected Triggers are active before the connection to the actions is made active.
Using an OR Gate ensures that <bold>one or more</bold> connected Triggers are active before the connection to the actions is made active.

<chapter name="Triggers"/>
The icons on the left-hand side are known as Triggers. These are dragged into the slots on the left side of the GUI.
Different Triggers are used to detect different things, each with a different criteria. When a Trigger's criteria is met, the adjacent connection will become active.

<chapter name="Actions"/>
The icons on the right-hand side are known as Actions. These are dragged into the slots on the right side of the GUI.
Different Actions result in different tasks being completed. When the adjacent connection is active, the connected Actions are undertaken.

<chapter name="Parameters"/>
When Parameter slots are installed, extra slots next to some triggers/actions become available.
These extra slots can provide extra functionality to make your triggers/actions more specific.
In these extra slots you either: click the slot to access more options or place items in as a filter.

<chapter name="Connections"/>
The lines down the middle are used to connect the Triggers to the Actions, so they can work together.
They can be clicked to be connected/disconnected to require multiple triggers/actions.
When a trigger is met, the line in the middle will light up (only partially if there are multiple triggers required).
When a line is lit up, then the actions they are connected to will be undertaken.