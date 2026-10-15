<lore>
A valve for your power grid - turn it down when a machine only needs a trickle.
</lore>
<no_lore>
An Iron Kinesis Pipe carries MJ power and can throttle the amount allowed through it.
</no_lore>

<recipes stack="buildcrafttransport:pipe_iron_power"/>

<chapter name="Pipe Mechanics"/>
The Iron Kinesis Pipe carries up to 32 MJ/t by default and acts as a power limiter. Right-click it with a wrench to cycle the cap through seven steps - from the pipe's full rate, halving each time, down to nothing at all - so transfer can be choked back or stopped entirely.

The current limit is shown on the pipe and confirmed in an on-screen message each time you change it. Power is fed into the network by a Wooden Kinesis Pipe.
<link to="buildcraft:pipe/wood_power"/>

<chapter name="Gate Control"/>
A gate placed on the pipe can set the same limit automatically with the Set Power Limit actions, swapping the cap in response to any trigger.
<link to="buildcraft:set_power_limit"/>

<usages stack="buildcrafttransport:pipe_iron_power"/>
