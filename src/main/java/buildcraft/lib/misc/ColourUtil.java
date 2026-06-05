package buildcraft.lib.misc;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

public class ColourUtil {

    public static final DyeColor[] COLOURS = DyeColor.values();

    private static final ChatFormatting[] FACE_TO_FORMAT = new ChatFormatting[6];

    static {
        FACE_TO_FORMAT[Direction.UP.ordinal()] = ChatFormatting.WHITE;
        FACE_TO_FORMAT[Direction.DOWN.ordinal()] = ChatFormatting.DARK_GRAY;
        FACE_TO_FORMAT[Direction.NORTH.ordinal()] = ChatFormatting.RED;
        FACE_TO_FORMAT[Direction.SOUTH.ordinal()] = ChatFormatting.BLUE;
        FACE_TO_FORMAT[Direction.EAST.ordinal()] = ChatFormatting.YELLOW;
        FACE_TO_FORMAT[Direction.WEST.ordinal()] = ChatFormatting.GREEN;
    }

    private static final int[] LIGHT_HEX = {
        0xe4_e4_e4,
        0xEA_78_35,
        0xD9_43_C6,
        0x66_AA_FF,
        0xFF_D9_1C,
        0x39_D5_2E,
        0xD9_71_99,
        0x7A_7A_7A,
        0xa0_a7_a7,
        0x29_97_99,
        0x7e_34_bf,
        0x25_31_93,
        0x89_50_2D,
        0x00_7F_0E,
        0xBE_2B_27,
        0x18_14_14,
    };

    private static final ChatFormatting[] COLOUR_TO_FORMAT = new ChatFormatting[16];

    static {
        COLOUR_TO_FORMAT[DyeColor.WHITE.ordinal()] = ChatFormatting.WHITE;
        COLOUR_TO_FORMAT[DyeColor.ORANGE.ordinal()] = ChatFormatting.GOLD;
        COLOUR_TO_FORMAT[DyeColor.MAGENTA.ordinal()] = ChatFormatting.LIGHT_PURPLE;
        COLOUR_TO_FORMAT[DyeColor.LIGHT_BLUE.ordinal()] = ChatFormatting.AQUA;
        COLOUR_TO_FORMAT[DyeColor.YELLOW.ordinal()] = ChatFormatting.YELLOW;
        COLOUR_TO_FORMAT[DyeColor.LIME.ordinal()] = ChatFormatting.GREEN;
        COLOUR_TO_FORMAT[DyeColor.PINK.ordinal()] = ChatFormatting.LIGHT_PURPLE;
        COLOUR_TO_FORMAT[DyeColor.GRAY.ordinal()] = ChatFormatting.DARK_GRAY;
        COLOUR_TO_FORMAT[DyeColor.LIGHT_GRAY.ordinal()] = ChatFormatting.GRAY;
        COLOUR_TO_FORMAT[DyeColor.CYAN.ordinal()] = ChatFormatting.DARK_AQUA;
        COLOUR_TO_FORMAT[DyeColor.PURPLE.ordinal()] = ChatFormatting.DARK_PURPLE;
        COLOUR_TO_FORMAT[DyeColor.BLUE.ordinal()] = ChatFormatting.BLUE;
        COLOUR_TO_FORMAT[DyeColor.BROWN.ordinal()] = ChatFormatting.GOLD;
        COLOUR_TO_FORMAT[DyeColor.GREEN.ordinal()] = ChatFormatting.DARK_GREEN;
        COLOUR_TO_FORMAT[DyeColor.RED.ordinal()] = ChatFormatting.DARK_RED;
        COLOUR_TO_FORMAT[DyeColor.BLACK.ordinal()] = ChatFormatting.DARK_GRAY;
    }

    private static final int[] FACE_TO_COLOUR = new int[6];

    static {
        FACE_TO_COLOUR[Direction.DOWN.ordinal()] = 0xFF_33_33_33;
        FACE_TO_COLOUR[Direction.UP.ordinal()] = 0xFF_CC_CC_CC;
    }

    public static int getColourForSide(Direction face) {
        return FACE_TO_COLOUR[face.ordinal()];
    }

    public static String getTextFullTooltip(@Nullable DyeColor colour) {
        if (colour == null) return "Clean";
        String name = colour.getName();

        String[] parts = name.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
            sb.append(parts[i].substring(1));
        }
        ChatFormatting format = COLOUR_TO_FORMAT[colour.ordinal()];
        return format.toString() + sb + ChatFormatting.RESET;
    }

    public static String getTextFullTooltip(Direction direction) {
        String localized = buildcraft.lib.misc.LocaleUtil.localize("direction." + direction.getName());
        ChatFormatting format = FACE_TO_FORMAT[direction.ordinal()];
        return format.toString() + localized + ChatFormatting.RESET;
    }

    public static ChatFormatting convertColourToTextFormat(DyeColor colour) {
        return COLOUR_TO_FORMAT[colour.ordinal()];
    }

    public static int getLightHex(DyeColor colour) {
        return LIGHT_HEX[colour.ordinal()];
    }

    public static int swapArgbToAbgr(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }
}
