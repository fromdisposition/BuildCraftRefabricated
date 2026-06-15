/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.mj;

import buildcraft.lib.BCLibConfig;
import java.text.DecimalFormat;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MjAPI {
   public static final long ONE_MINECRAFT_JOULE = getMjValue();
   public static final long MJ = ONE_MINECRAFT_JOULE;
   /** Team Reborn / RebornCore {@code PowerSystem.ABBREVIATION} — cable unit label, not MJ. */
   public static final String EXTERNAL_ENERGY_UNIT = "E";
   public static final DecimalFormat MJ_DISPLAY_FORMAT = new DecimalFormat("#,##0.##");
   public static IMjEffectManager EFFECT_MANAGER = MjAPI.NullaryEffectManager.INSTANCE;

   /**
    * Fabric BlockApiLookup for each MJ interface. Use these to register providers on block
    * entities and to look up MJ capability on neighboring blocks.
    *
    * The same instances double as capability-token constants passed to
    * {@code PipeFlow.getCapability(Object, Direction)} for internal pipe routing — identity
    * comparison ({@code capability == MjAPI.CAP_RECEIVER}) still works because
    * {@link BlockApiLookup} is an {@link Object}.
    *
    * Register: {@code MjAPI.CAP_RECEIVER.registerForBlockEntity((be, side) -> ..., MY_TYPE);}
    * Look up:  {@code MjAPI.CAP_RECEIVER.find(level, pos, null, null, side);}
    */
   @Nonnull
   public static final BlockApiLookup<IMjConnector, Direction> CAP_CONNECTOR =
       BlockApiLookup.get(Identifier.fromNamespaceAndPath("buildcraftrefabricated", "mj_connector"),
                          IMjConnector.class, Direction.class);
   @Nonnull
   public static final BlockApiLookup<IMjReceiver, Direction> CAP_RECEIVER =
       BlockApiLookup.get(Identifier.fromNamespaceAndPath("buildcraftrefabricated", "mj_receiver"),
                          IMjReceiver.class, Direction.class);
   @Nonnull
   public static final BlockApiLookup<IMjRedstoneReceiver, Direction> CAP_REDSTONE_RECEIVER =
       BlockApiLookup.get(Identifier.fromNamespaceAndPath("buildcraftrefabricated", "mj_redstone_receiver"),
                          IMjRedstoneReceiver.class, Direction.class);
   @Nonnull
   public static final BlockApiLookup<IMjReadable, Direction> CAP_READABLE =
       BlockApiLookup.get(Identifier.fromNamespaceAndPath("buildcraftrefabricated", "mj_readable"),
                          IMjReadable.class, Direction.class);
   @Nonnull
   public static final BlockApiLookup<IMjPassiveProvider, Direction> CAP_PASSIVE_PROVIDER =
       BlockApiLookup.get(Identifier.fromNamespaceAndPath("buildcraftrefabricated", "mj_passive_provider"),
                          IMjPassiveProvider.class, Direction.class);

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

   /** True when another mod in the pack exposes Team Reborn {@link #EXTERNAL_ENERGY_UNIT}. */
   public static boolean isExternalEnergyEcosystemPresent() {
      return BCLibConfig.externalEnergyEcosystemPresent.get();
   }

   /**
    * Whether UI should label power as {@link #EXTERNAL_ENERGY_UNIT} instead of MJ.
    * MJ_ONLY always uses MJ. DISPLAY_RF forces E. Otherwise E is shown when interop is on and
    * the modpack includes a Team Reborn energy consumer (e.g. Tech Reborn).
    */
   public static boolean displaysExternalEnergyUnits() {
      BCLibConfig.PowerMode mode = BCLibConfig.powerMode.get();
      if (!mode.autoconvert) {
         return false;
      }

      return mode.displayExternal || isExternalEnergyEcosystemPresent();
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
