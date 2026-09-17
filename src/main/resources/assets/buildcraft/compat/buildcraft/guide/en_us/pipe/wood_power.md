<lore>
Every power grid needs a way in. Sit one against a running engine and let the energy pour through.
</lore>
<no_lore>
A Wooden Kinesis Pipe draws MJ power out of an adjacent power source and feeds it into a network of Kinesis Pipes.
</no_lore>

<recipes stack="buildcrafttransport:pipe_wood_power"/>

<chapter name="Pipe Mechanics"/>
The Wooden Kinesis Pipe is how power enters a network. Engines and other MJ sources connect to it; it pulls their output in and passes it along to the plain Kinesis Pipes attached to its other sides, which carry the power on to the machines that consume it.

It moves up to 16 MJ/t by default with very little loss along the way. Like the Wooden Transport Pipe, it will not connect to another wooden power pipe.

<chapter name="Powering"/>
Any engine other than the Redstone Engine can feed a Wooden Kinesis Pipe.
<link to="buildcraft:block/engine_stone"/>
<link to="buildcraft:block/engine_iron"/>
A Pipe Adapter lets the carried power cross into a power-accepting transport or fluid pipe at the far end.
<link to="buildcraft:item/plug_power_adaptor"/>

<usages stack="buildcrafttransport:pipe_wood_power"/>
