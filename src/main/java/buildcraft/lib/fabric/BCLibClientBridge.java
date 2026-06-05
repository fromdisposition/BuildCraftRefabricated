package buildcraft.lib.fabric;

import net.minecraft.client.Minecraft;

import buildcraft.lib.client.guide.GuideManager;

public final class BCLibClientBridge {
    private BCLibClientBridge() {}

    public static void openGuideScreen(String bookName) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        GuideManager.INSTANCE.ensureLoaded();
        mc.setScreen(new buildcraft.lib.client.guide.GuiGuide(bookName));
    }
}
