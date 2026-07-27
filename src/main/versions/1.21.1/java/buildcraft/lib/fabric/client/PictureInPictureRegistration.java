package buildcraft.lib.fabric.client;

/**
 * 1.21.1 stub (versions/1.21.1). The picture-in-picture subsystem the other nodes register renderers into does
 * not exist on 1.21.1, so there is nothing to register; this shadows the per-version copies that do the real
 * work (see _ge_1.21.10_lt_26.1 and _ge_26.1) and keeps the shared caller compiling on this node.
 */
public final class PictureInPictureRegistration {
   private PictureInPictureRegistration() {
   }

   public static void register() {
   }
}
