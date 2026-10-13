package buildcraft.builders.tooltip;

import buildcraft.builders.snapshot.Snapshot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record BlueprintPreviewTooltipComponent(Snapshot.Header header) implements TooltipComponent {
}
