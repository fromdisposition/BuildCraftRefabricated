<lore>
Perhaps you've come into possession of a form of power incompatible with your machines. This engine will take it, and convert it to MJ.
</lore>
<no_lore>
The Energy Engine consumes E and converts it into MJ to drive BuildCraft machines and pipes. It accepts E through any of its non-output faces and outputs MJ through its piston face.
</no_lore>
<chapter name="Information"/>
The Energy Engine actively pulls E from any block on its sides or back that exposes the Energy capability, and stores up to <bold>10,000 E</bold> internally before converting it.
By default, it outputs at a rate of <bold>4 MJ/t</bold> through the piston face. Exact E-to-MJ ratios are governed by the current game configuration.
<recipes_usages stack="buildcraftenergy:engine_rf"/>

<chapter name="Upgrades"/>
Open the engine's interface (crouch-click, or click without a wrench/pipe in hand) to access four upgrade slots. Drop gears into the slots to raise the output rate:
- <bold>Iron Gear</bold>: +2 MJ/t
- <bold>Gold Gear</bold>: +3 MJ/t

Each filled slot adds independently, and the engine consumes more E per tick as the output rises (it becomes faster; it doesn't generate more MJ from the same amount of E).

<chapter name="Engine Mechanics"/>
BuildCraft engines have 5 temperature stages, which determines the speed the engine runs at: Blue, Green, Yellow, Red and Black.
Energy Engines warm through these stages as they run but cap at Black and <bold>cannot overheat</bold>, so they never need to be cooled.
Engines will always connect to the nearest compatible MJ consumer.
You can use a Wrench to rotate it to change which block it is powering.

Energy Engines can be "chained" in a line with up to 5 engines in total, passing MJ through each other to increase output.

As with all engines, it <bold>requires a redstone signal to run.</bold>
Gates can be used to detect the engines temperature stages to help you control them.