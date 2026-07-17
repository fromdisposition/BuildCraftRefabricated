package buildcraft.lib.fabric.client;

/**
 * Stub for nodes below 26.1 (versions/_lt_26.1). Level render-state extraction and the block-outline render
 * events do not exist there, so there is nothing to wire up; pipe placement previews use the older world-render
 * callbacks instead (see {@code PipePlacementHighlight}). Shadows the real 26.1+ copy (versions/_ge_26.1) and
 * keeps the shared caller compiling on these nodes.
 */
public final class BlockOutlineRegistration {
   private BlockOutlineRegistration() {
   }

   public static void install() {
   }
}
