<lore>
Gates turn pipes into logic: "if the furnace is empty, pull more ore." Wires carry that decision across the base.
</lore>
<no_lore>
Gates read triggers and fire actions; wires let gates share signals over distance. Together they automate a base without redstone dust.
</no_lore>
<chapter name="Gates"/>
A gate is a small device bolted onto a pipe face — see <link to="buildcraft:concept/pluggables"/>. Each gate pairs <bold>triggers</bold> (conditions, such as "inventory full", "engine red", or "redstone on") with <bold>actions</bold> (such as "emit redstone" or "set pipe direction"). Higher-tier gates have more slots and can watch more at once. Gates are etched and programmed at the silicon tables — see <link to="buildcraft:concept/silicon_and_chipsets"/>.
<chapter name="Wires"/>
A <link inline="buildcraft:item/wire"/> comes in colours and clips onto pipes beside gates and plugs. A gate can switch a wire on, and any other gate touching that same colour can read it as a trigger — so a sensor on one machine drives an action on another far away.
<chapter name="Common Patterns"/>
Pulse an engine only while there is work to do; stop a quarry when its chest is full; alternate items between furnaces. For coordinating larger builds, 16-colour pipe signals are explained in <link to="buildcraft:concept/pipe_signals"/>.
