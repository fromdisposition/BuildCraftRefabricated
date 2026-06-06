package buildcraft.lib.gui;

import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.net.IPayloadWriter;
import net.minecraft.network.FriendlyByteBuf;

public abstract class Widget_Neptune<C extends BcMenu> {
   public final C container;

   public Widget_Neptune(C container) {
      this.container = container;
   }

   public boolean isRemote() {
      return this.container.player.level().isClientSide();
   }

   protected final void sendWidgetData(IPayloadWriter writer) {
      this.container.sendWidgetData(this, writer);
   }

   public void handleWidgetDataServer(BCPayloadContext ctx, FriendlyByteBuf buffer) {
   }

   public void handleWidgetDataClient(BCPayloadContext ctx, FriendlyByteBuf buffer) {
   }
}
