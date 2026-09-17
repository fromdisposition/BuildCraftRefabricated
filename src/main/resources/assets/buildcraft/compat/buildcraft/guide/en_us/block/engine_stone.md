<lore>
After experimenting with what can be done with a basic redstone engine, you discover a way to burn furnace fuels to to actually produce a decent amount of power at a rate of 1MJ/t.
</lore>
<no_lore>
The Stirling engine is a more powerful engine which uses furnace fuels to generate MJ. It produces a steady amount of power of 1 MJ/t.
</no_lore>
<chapter name="Information"/>
A Stirling Engines are just as efficient as a regular furnace when it comes to power production.
<recipes_usages stack="buildcraftenergy:engine_stone"/>
<chapter name="Engine Mechanics"/>
BuildCraft engines have 5 temperature stages, which determines the speed the engine runs at: Blue, Green, Yellow, Red and Black.
Stirling engines turn black and overheat if their internal power buffer fills up with nowhere to go. By default an overheating engine <red>explodes</red>, so always give its power an outlet. (On servers where engine explosions are disabled, it instead stalls and must be wrenched to cool.)
Engines will always connect to the nearest compatible MJ consumer.
You can use a Wrench to rotate it to change which block it is powering.
Stirling engines can be "chained" in a line with up to 3 engines in total.
As with all engines, it <bold>requires a redstone signal to run.</bold>
Gates can be used to detect the engines temperature stages to help you control them.