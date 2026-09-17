package buildcraft.lib.fabric.client;

// Level render-state extraction and block-outline render events don't exist below 26.1; pipe placement previews
// use the older world-render callbacks instead ({@code PipePlacementHighlight}). Shadows the real _ge_26.1 copy.
public final class BlockOutlineRegistration {
   private BlockOutlineRegistration() {
   }

   public static void install() {
   }
}
