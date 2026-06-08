package buildcraft.api.mj;

import buildcraft.lib.BCLibConfig;
import java.text.DecimalFormat;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Minecraft Joules (MJ) constants, formatting, RF conversion, and capability tokens. */
public class MjAPI {
   public static final long ONE_MINECRAFT_JOULE = getMjValue();
   public static final long MJ = ONE_MINECRAFT_JOULE;
   public static final DecimalFormat MJ_DISPLAY_FORMAT = new DecimalFormat("#,##0.##");
   public static IMjEffectManager EFFECT_MANAGER = MjAPI.NullaryEffectManager.INSTANCE;
   @Nonnull
   public static final Object CAP_CONNECTOR = new Object();
   @Nonnull
   public static final Object CAP_RECEIVER = new Object();
   @Nonnull
   public static final Object CAP_REDSTONE_RECEIVER = new Object();
   @Nonnull
   public static final Object CAP_READABLE = new Object();
   @Nonnull
   public static final Object CAP_PASSIVE_PROVIDER = new Object();

   public static String formatMj(long microMj) {
      return formatMjInternal((double)microMj / MJ);
   }

   private static String formatMjInternal(double val) {
      return MJ_DISPLAY_FORMAT.format(val);
   }

   public static MjRfConversion getRfConversion() {
      return MjRfConversion.createParsed(BCLibConfig.mjRfConversionAmount.get());
   }

   public static boolean isRfAutoConversionEnabled() {
      return BCLibConfig.powerMode.get().autoconvert;
   }

   private static long getMjValue() {
      return 1000000L;
   }

   public enum NullaryEffectManager implements IMjEffectManager {
      INSTANCE;

      @Override
      public void createPowerLossEffect(Level world, Vec3 center, long microJoulesLost) {
      }

      @Override
      public void createPowerLossEffect(Level world, Vec3 center, Direction direction, long microJoulesLost) {
      }

      @Override
      public void createPowerLossEffect(Level world, Vec3 center, Vec3 direction, long microJoulesLost) {
      }
   }
}
