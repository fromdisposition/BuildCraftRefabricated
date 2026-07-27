<lore>
For each of the 16 pipe wire colors, you can send and recieve a unique signal to and from other gates.
The wire in its darker, unpowered state is off. When there's a signal going through the wire, it begins glowing, which means that it is on.
</lore>
<no_lore>
Pipe Signals are what travel down pipe wires when a gate sends an action or reads from a trigger. Each color pipe wire has a corresponding set of pipe signals that match its color, for 16 total sets of independent signals.
A Pipe Wire glows when its connected gate emits a signal, and stops glowing when no gate emits one. Pipe Signal triggers and actions let other gates read or emit that glow.
</no_lore>

<chapter name="As an Action"/>
Emitting a Pipe Signal is done on the Actions side of the gate. Select the color of pipe signal you wish to send, and when the gate is triggered, it will activate, causing the associated pipe wire color to glow.

<link to="buildcraft:item/wire"/>

<chapter name="As a Trigger"/>
There are two trigger states for each color of Pipe Signal; "Pipe Signal On" will trigger the gate when that color of Pipe Wire is glowing, and "Pipe Signal Off" will trigger the gate when that color of Pipe Wire is <italic>not</italic> glowing.
<chapter name="Parameters"/>
When a gate has parameter slots free, the Pipe Signal On / Off entries for every connected wire colour become available as parameters. Adding one to a <bold>Trigger</bold> means the gate fires only when that secondary wire matches its required state, so you can require the states of multiple wires to matched before an Action is activated.
Adding one to an <bold>Action</bold> is the equivalent of activating that color of pipe wire, so for a gate with 1 Action Parameter, each Action slot could activate 2 Pipe Wires.
