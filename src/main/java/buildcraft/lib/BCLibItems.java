package buildcraft.lib;

import buildcraft.core.BCCore;
import buildcraft.fabric.BCRegistries;
import buildcraft.lib.item.ItemDebugger;
import buildcraft.lib.item.ItemGuide;
import buildcraft.lib.item.ItemGuideNote;

public final class BCLibItems {
    public static ItemGuide GUIDE;
    public static ItemGuide GUIDE_CONFIG;
    public static ItemGuideNote GUIDE_NOTE;
    public static ItemDebugger DEBUGGER;
    private BCLibItems() {}

    public static void register() {
        GUIDE = BCRegistries.registerItem(BCLib.MODID, "guide",
                props -> new ItemGuide(props, "buildcraftcore:main"),
                props -> props.stacksTo(1));
        GUIDE_CONFIG = BCRegistries.registerItem(BCLib.MODID, "guide_config",
                props -> new ItemGuide(props, "buildcraftlib:config"),
                props -> props.stacksTo(1));
        GUIDE_NOTE = BCRegistries.registerItem(BCLib.MODID, "guide_note", ItemGuideNote::new, props -> props.stacksTo(1));
        if (BCLib.DEV) {
            DEBUGGER = BCRegistries.registerItem(BCLib.MODID, "debugger", ItemDebugger::new, props -> props.stacksTo(1));
        }
    }
}
