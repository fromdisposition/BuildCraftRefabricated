package buildcraft.transport.client.render;

public final class PipeRenderContext {
   private static final ThreadLocal<Integer> PACKED_LIGHT = ThreadLocal.withInitial(() -> 0);

   private PipeRenderContext() {
   }

   public static void setPackedLight(int packedLight) {
      PACKED_LIGHT.set(packedLight);
   }

   public static int getPackedLight() {
      return PACKED_LIGHT.get();
   }

   public static int blockLight() {
      return (getPackedLight() & 65535) >> 4;
   }

   public static int skyLight() {
      return (getPackedLight() >> 16 & 65535) >> 4;
   }
}
