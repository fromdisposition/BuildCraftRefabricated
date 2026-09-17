<lore>
The collection of fluids by hand can be quite tedious and dangerous at times, creating the need for an automated way to gather fluids.
</lore>
<no_lore>
The pump is a way to automatically pick up any fluid sources found directly below it and any sources connected to it.
</no_lore>

To gather fluids from the world, place a pump above a source block of the fluid you want to pump up and supply it with power.
The fluids sources found below will be 'sucked' up from the top source downwards and will be auto-outputted into adjacent pipes/tanks.
It will pump fluids in a 64 blocks radius from top to bottom and the outer edges of the connecting fluids inwards.
<chapter name="Machine Mechanics"/>
As with most machines, it requires power. The more power you supply it, the faster it will pump.
Applying a redstone signal to the pump will stop the pump from pumping so be careful where you place your levers.
Two <bold>LEDs</bold> report the machine's state. The first glows from dark to bright red as its stored power fills up. The second is <green>green</green> while the machine is working, <red>red</red> when it has work but cannot proceed (no power or an invalid area), and dark when it is idle or finished.
<recipes_usages stack="buildcraftfactory:pump"/>