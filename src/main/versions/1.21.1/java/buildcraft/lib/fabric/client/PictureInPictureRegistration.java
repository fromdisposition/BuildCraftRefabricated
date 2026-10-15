package buildcraft.lib.fabric.client;

// 1.21.1 has no picture-in-picture subsystem; this stub shadows the per-version copies (_ge_1.21.10_lt_26.1,
// _ge_26.1) so the shared caller still compiles on this node.
public final class PictureInPictureRegistration {
   private PictureInPictureRegistration() {
   }

   public static void register() {
   }
}
