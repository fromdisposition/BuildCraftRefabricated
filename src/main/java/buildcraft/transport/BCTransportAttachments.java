package buildcraft.transport;

import java.util.Locale;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;

import buildcraft.fabric.BCRegistries;
import buildcraft.lib.misc.AdvancementUtil;

public final class BCTransportAttachments {
    public static AttachmentType<WireColoursPlaced> WIRE_COLOURS_PLACED;
    public static AttachmentType<PluggablesPlaced> PLUGGABLES_PLACED;

    private static final Identifier ALL_PLUGGED_UP =
            Identifier.parse("buildcrafttransport:all_plugged_up");

    private BCTransportAttachments() {}

    public static void register() {
        WIRE_COLOURS_PLACED = AttachmentRegistry.create(
                BCRegistries.id(BCTransport.MODID, "wire_colours_placed"),
                builder -> builder
                        .initializer(WireColoursPlaced::new)
                        .persistent(WireColoursPlaced.CODEC)
                        .copyOnDeath());
        PLUGGABLES_PLACED = AttachmentRegistry.create(
                BCRegistries.id(BCTransport.MODID, "pluggables_placed"),
                builder -> builder
                        .initializer(PluggablesPlaced::new)
                        .persistent(PluggablesPlaced.CODEC)
                        .copyOnDeath());
    }

    public static WireColoursPlaced wireColours(Player player) {
        return player.getAttachedOrCreate(WIRE_COLOURS_PLACED);
    }

    public static PluggablesPlaced pluggables(Player player) {
        return player.getAttachedOrCreate(PLUGGABLES_PLACED);
    }

    public static void recordPluggablePlacement(Player player, PluggablesPlaced.Kind kind) {
        if (player.level().isClientSide()) {
            return;
        }
        if (pluggables(player).markPlaced(kind)) {
            AdvancementUtil.unlockAdvancement(player, ALL_PLUGGED_UP, kind.criterionName());
        }
    }

    public static final class WireColoursPlaced {
        public static final int ALL_COLOURS_MASK = 0xFFFF;
        static final Codec<WireColoursPlaced> CODEC = Codec.INT.xmap(
                mask -> {
                    WireColoursPlaced data = new WireColoursPlaced();
                    data.mask = mask;
                    return data;
                },
                data -> data.mask);

        private int mask;

        public boolean markPlaced(DyeColor colour) {
            int bit = 1 << colour.getId();
            if ((mask & bit) != 0) {
                return false;
            }
            mask |= bit;
            return true;
        }

        public boolean isComplete() {
            return mask == ALL_COLOURS_MASK;
        }
    }

    public static final class PluggablesPlaced {
        public enum Kind {
            BLOCKER, POWER_ADAPTOR, WIRE, GATE, LENS, PULSAR, LIGHT_SENSOR, TIMER;

            public int bit() {
                return 1 << ordinal();
            }

            public String criterionName() {
                return name().toLowerCase(Locale.ROOT);
            }
        }

        public static final int ALL_KINDS_MASK = (1 << Kind.values().length) - 1;
        static final Codec<PluggablesPlaced> CODEC = Codec.INT.xmap(
                mask -> {
                    PluggablesPlaced data = new PluggablesPlaced();
                    data.mask = mask;
                    return data;
                },
                data -> data.mask);

        private int mask;

        public boolean markPlaced(Kind kind) {
            int bit = kind.bit();
            if ((mask & bit) != 0) {
                return false;
            }
            mask |= bit;
            return true;
        }

        public boolean isComplete() {
            return mask == ALL_KINDS_MASK;
        }
    }
}
