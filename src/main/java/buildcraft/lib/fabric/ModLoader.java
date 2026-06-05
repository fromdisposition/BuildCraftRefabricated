package buildcraft.lib.fabric;

import buildcraft.lib.attachments.RegisterAttachmentsEvent;

public final class ModLoader {
    private ModLoader() {}

    public static void postAttachmentsRegistration(RegisterAttachmentsEvent event) {
        buildcraft.lib.attachments.AttachmentHooks.init();
    }
}
