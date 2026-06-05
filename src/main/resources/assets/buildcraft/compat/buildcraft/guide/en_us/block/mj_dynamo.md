<lore>
In the scenario where you've come across machines or contraptions that don't accept MJ, this "engine" will convert power to a format more familiar to them.
</lore>
<no_lore>
The MJ Dynamo converts MJ to FE.
</no_lore>
<chapter name="Information"/>
The MJ Dynamo accepts MJ on any non-output side and stores up to <bold>1,000 MJ</bold> in its internal buffer, converting it to FE on the fly.
It generates a base <bold>4 MJ/t worth of FE</bold> through the piston face. Exact FE-to-MJ ratios are governed by the current game configuration.
<recipes_usages stack="buildcraftenergy:mj_dynamo"/>

<chapter name="Upgrades"/>
Interact with the dynamo to see its interface to access four upgrade slots. Drop gears into the slots to raise the output rate:
- <bold>Iron Gear</bold>: +2 MJ/t worth of FE
- <bold>Gold Gear</bold>: +3 MJ/t worth of FE

Each filled slot adds independently, and the dynamo consumes more MJ per tick as the output rises (it becomes faster; it doesn't generate more FE from the same amount of MJ).

<chapter name="Engine Mechanics"/>
BuildCraft engines have 5 temperature stages, which determines the speed the engine runs at: Blue, Green, Yellow, Red and Black.
MJ Dynamos warm through these stages as they run but cap at Black and <bold>cannot overheat</bold>, so they never need to be cooled.
The dynamo will always push FE to the receiver on its piston face.
You can use a Wrench to rotate it to change which block it is powering.

MJ Dynamos can be "chained" in a line with up to 4 dynamos in total additively to improve throughput.

As with all engines, it <bold>requires a redstone signal to run.</bold>
Gates can be used to detect the dynamo's temperature stages to help you control them.