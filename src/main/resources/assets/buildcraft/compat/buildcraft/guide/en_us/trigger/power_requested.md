<lore>
There's no point pushing power into a pipe nobody is drinking from.
</lore>
<no_lore>
Power Requested is a gate trigger provided on any gate placed on a Kinesis Pipe or FE Pipe.
</no_lore>

<chapter name="Requirements"/>
When selected, the connected actions will activate while at least one downstream device on the pipe network is asking for power. Use it to gate engines so they only run when something is actually consuming, sparing fuel and avoiding wasted overflow.

<chapter name="Pipe Compatibility"/>
The trigger reads the live power-request value from the pipe's flow handler. It works on both MJ kinesis pipes and FE pipes — the flow handler reports requested-but-undelivered power for whichever flavour the pipe carries.
