package buildcraft.lib.fabric;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class BCLibClientBridge {
   private BCLibClientBridge() {
   }

   public static void openGuideScreen(String bookName) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         GuideManager.INSTANCE.ensureLoaded();
         mc.setScreen(new GuiGuide(bookName));
      }
   }

   public static void openGuidePage(String bookName, Identifier pageId) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         GuideManager.INSTANCE.ensureLoaded();
         GuidePageFactory factory = GuideManager.INSTANCE.getFactoryFor(pageId);
         GuiGuide gui = new GuiGuide(bookName);
         if (factory != null) {
            gui.openPage(factory.createNew(gui));
         }

         mc.setScreen(gui);
      }
   }
}
