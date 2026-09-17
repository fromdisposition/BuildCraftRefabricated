package buildcraft.builders.tooltip;

import buildcraft.api.schematics.ISchematicBlock;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record SchematicPreviewTooltipComponent(ISchematicBlock schematic) implements TooltipComponent {
}
