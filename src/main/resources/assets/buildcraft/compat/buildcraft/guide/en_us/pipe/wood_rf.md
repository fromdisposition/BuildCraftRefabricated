<lore>
Every Forge Energy grid needs a way in. Sit one against a running dynamo and let the energy pour through.
</lore>
<no_lore>
A Wooden FE Pipe draws Forge Energy out of an adjacent source and feeds it into a network of FE Pipes.
</no_lore>

<recipes stack="buildcrafttransport:pipe_wood_rf"/>

<chapter name="Pipe Mechanics"/>
The Wooden FE Pipe is how Forge Energy enters a network. An MJ Dynamo and other FE sources connect to it; it pulls their output in and passes it along to the plain FE Pipes attached to its other sides, which carry it on to the machines that consume it.

It moves up to 160 FE/t by default. Like the Wooden Transport Pipe, it will not connect to another wooden FE pipe.

<chapter name="Powering"/>
An MJ Dynamo, or any Forge Energy generator from another mod, can feed a Wooden FE Pipe.
<link to="buildcraft:block/mj_dynamo"/>
The network then delivers that energy to any FE-consuming machine, such as the FE Engine, which converts it back into MJ.
<link to="buildcraft:block/engine_rf"/>

<usages stack="buildcrafttransport:pipe_wood_rf"/>
