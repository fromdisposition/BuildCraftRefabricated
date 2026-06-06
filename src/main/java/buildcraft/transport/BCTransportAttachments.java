package buildcraft.transport;

import buildcraft.fabric.BCRegistries;
import buildcraft.lib.misc.AdvancementUtil;
import com.mojang.serialization.Codec;
import java.util.Locale;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;

public final class BCTransportAttachments {
   public static AttachmentType<BCTransportAttachments.WireColoursPlaced> WIRE_COLOURS_PLACED;
   public static AttachmentType<BCTransportAttachments.PluggablesPlaced> PLUGGABLES_PLACED;
   private static final Identifier ALL_PLUGGED_UP = Identifier.parse("buildcrafttransport:all_plugged_up");

   private BCTransportAttachments() {
   }

   public static void register() {
      WIRE_COLOURS_PLACED = AttachmentRegistry.create(
         BCRegistries.id("buildcrafttransport", "wire_colours_placed"),
         builder -> builder.initializer(BCTransportAttachments.WireColoursPlaced::new).persistent(BCTransportAttachments.WireColoursPlaced.CODEC).copyOnDeath()
      );
      PLUGGABLES_PLACED = AttachmentRegistry.create(
         BCRegistries.id("buildcrafttransport", "pluggables_placed"),
         builder -> builder.initializer(BCTransportAttachments.PluggablesPlaced::new).persistent(BCTransportAttachments.PluggablesPlaced.CODEC).copyOnDeath()
      );
   }

   public static BCTransportAttachments.WireColoursPlaced wireColours(Player player) {
      return (BCTransportAttachments.WireColoursPlaced)player.getAttachedOrCreate(WIRE_COLOURS_PLACED);
   }

   public static BCTransportAttachments.PluggablesPlaced pluggables(Player player) {
      return (BCTransportAttachments.PluggablesPlaced)player.getAttachedOrCreate(PLUGGABLES_PLACED);
   }

   public static void recordPluggablePlacement(Player player, BCTransportAttachments.PluggablesPlaced.Kind kind) {
      if (!player.level().isClientSide()) {
         if (pluggables(player).markPlaced(kind)) {
            AdvancementUtil.unlockAdvancement(player, ALL_PLUGGED_UP, kind.criterionName());
         }
      }
   }

   public static final class PluggablesPlaced {
      public static final int ALL_KINDS_MASK = (1 << BCTransportAttachments.PluggablesPlaced.Kind.values().length) - 1;
      static final Codec<BCTransportAttachments.PluggablesPlaced> CODEC = Codec.INT.xmap(mask -> {
         BCTransportAttachments.PluggablesPlaced data = new BCTransportAttachments.PluggablesPlaced();
         data.mask = mask;
         return data;
      }, data -> data.mask);
      private int mask;

      public boolean markPlaced(BCTransportAttachments.PluggablesPlaced.Kind kind) {
         int bit = kind.bit();
         if ((this.mask & bit) != 0) {
            return false;
         }

         this.mask |= bit;
         return true;
      }

      public boolean isComplete() {
         return this.mask == ALL_KINDS_MASK;
      }

      public enum Kind {
         BLOCKER,
         POWER_ADAPTOR,
         WIRE,
         GATE,
         LENS,
         PULSAR,
         LIGHT_SENSOR,
         TIMER;

         public int bit() {
            return 1 << this.ordinal();
         }

         public String criterionName() {
            return this.name().toLowerCase(Locale.ROOT);
         }
      }
   }

   public static final class WireColoursPlaced {
      public static final int ALL_COLOURS_MASK = 65535;
      static final Codec<BCTransportAttachments.WireColoursPlaced> CODEC = Codec.INT.xmap(mask -> {
         BCTransportAttachments.WireColoursPlaced data = new BCTransportAttachments.WireColoursPlaced();
         data.mask = mask;
         return data;
      }, data -> data.mask);
      private int mask;

      public boolean markPlaced(DyeColor colour) {
         int bit = 1 << colour.getId();
         if ((this.mask & bit) != 0) {
            return false;
         }

         this.mask |= bit;
         return true;
      }

      public boolean isComplete() {
         return this.mask == 65535;
      }
   }
}
