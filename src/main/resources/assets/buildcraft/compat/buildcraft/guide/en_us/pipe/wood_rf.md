<lore>
Every E grid needs a way in. Sit one against a running dynamo and let the energy pour through.
</lore>
<no_lore>
A Wooden Energy Pipe draws E out of an adjacent source and feeds it into a network of Energy Pipes.
</no_lore>

<recipes stack="buildcrafttransport:pipe_wood_rf"/>

<chapter name="Pipe Mechanics"/>
The Wooden Energy Pipe is how E enters a network. An MJ Dynamo and other E sources connect to it; it pulls their output in and passes it along to the plain Energy Pipes attached to its other sides, which carry it on to the machines that consume it.

It moves up to 160 E/t by default. Like the Wooden Transport Pipe, it will not connect to another wooden Energy Pipe.

<chapter name="Powering"/>
An MJ Dynamo, or any E generator from another mod, can feed a Wooden Energy Pipe.
<link to="buildcraft:block/mj_dynamo"/>
The network then delivers that energy to any E-consuming machine, such as the Energy Engine, which converts it back into MJ.
<link to="buildcraft:block/engine_rf"/>

<usages stack="buildcrafttransport:pipe_wood_rf"/>
