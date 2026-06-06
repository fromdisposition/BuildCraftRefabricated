package buildcraft.core.marker.volume;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;

public interface IFastAddonRenderer<T extends Addon> {
   void renderAddonFast(T var1, Player var2, float var3, PoseStack var4, MultiBufferSource var5);

   default IFastAddonRenderer<T> then(IFastAddonRenderer<? super T> after) {
      return (addon, player, partialTicks, poseStack, bufferSource) -> {
         this.renderAddonFast(addon, player, partialTicks, poseStack, bufferSource);
         after.renderAddonFast(addon, player, partialTicks, poseStack, bufferSource);
      };
   }
}
