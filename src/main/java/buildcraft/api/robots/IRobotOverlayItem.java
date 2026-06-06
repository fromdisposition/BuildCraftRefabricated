package buildcraft.api.robots;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.item.ItemStack;

public interface IRobotOverlayItem {
   boolean isValidRobotOverlay(ItemStack var1);

   void renderRobotOverlay(ItemStack var1, TextureManager var2);
}
