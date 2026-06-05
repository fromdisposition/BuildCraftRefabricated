package buildcraft.transport.recipe;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import buildcraft.transport.BCTransportItems;
import buildcraft.transport.item.ItemPipeHolder;

public class PipeColourRecipe extends CustomRecipe {
    public static final PipeColourRecipe INSTANCE = new PipeColourRecipe();
    public static final MapCodec<PipeColourRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, PipeColourRecipe> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<PipeColourRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public PipeColourRecipe() {
        super();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return analyse(input).output() != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        Result result = analyse(input);
        return result.output() != null ? result.output().copy() : ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        Result result = analyse(input);
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        if (result.bleachIndex() >= 0) {
            remaining.set(result.bleachIndex(), new ItemStack(Items.BUCKET));
        }
        return remaining;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    private static Result analyse(CraftingInput crafting) {
        int pipeCount = 0;
        int pipeFirstIndex = -1;
        int dyeIndex = -1;
        int bleachIndex = -1;
        ItemPipeHolder pipeItem = null;
        DyeColor pipeColour = null;
        boolean mixedPipeTypes = false;
        boolean mixedPipeColours = false;

        for (int i = 0; i < crafting.size(); i++) {
            ItemStack stack = crafting.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof ItemPipeHolder holder) {
                if (!holder.definition.canBeColoured) {
                    return Result.INVALID;
                }
                if (pipeFirstIndex == -1) {
                    pipeFirstIndex = i;
                    pipeItem = holder;
                } else if (stack.getItem() != pipeItem) {
                    mixedPipeTypes = true;
                }
                pipeCount++;
                DyeColor colour = stack.get(BCTransportItems.PIPE_COLOUR.get());
                if (pipeColour == null) {
                    pipeColour = colour;
                } else if (colour != pipeColour) {
                    mixedPipeColours = true;
                }
            } else if (stack.is(Items.WATER_BUCKET)) {
                if (bleachIndex != -1 || dyeIndex != -1) {
                    return Result.INVALID;
                }
                bleachIndex = i;
            } else if (getDyeColour(stack) != null) {
                if (bleachIndex != -1 || dyeIndex != -1) {
                    return Result.INVALID;
                }
                dyeIndex = i;
            } else {
                return Result.INVALID;
            }
        }

        if (mixedPipeTypes || mixedPipeColours || pipeCount == 0) {
            return Result.INVALID;
        }
        if (bleachIndex == -1 && dyeIndex == -1) {
            return Result.INVALID;
        }

        ItemStack template = crafting.getItem(pipeFirstIndex);
        ItemStack output = template.copy();
        output.setCount(pipeCount);

        if (bleachIndex != -1) {
            if (pipeColour == null) {
                return Result.INVALID;
            }
            output.remove(BCTransportItems.PIPE_COLOUR.get());
            return new Result(output, bleachIndex);
        }

        DyeColor newColour = getDyeColour(crafting.getItem(dyeIndex));
        if (newColour == null || newColour == pipeColour) {
            return Result.INVALID;
        }
        output.set(BCTransportItems.PIPE_COLOUR.get(), newColour);
        return new Result(output, -1);
    }

    private static DyeColor getDyeColour(ItemStack stack) {
        for (DyeColor colour : DyeColor.values()) {
            if (stack.is(getDyeItem(colour))) {
                return colour;
            }
        }
        return null;
    }

    private static Item getDyeItem(DyeColor colour) {
        return switch (colour) {
            case WHITE -> Items.WHITE_DYE;
            case ORANGE -> Items.ORANGE_DYE;
            case MAGENTA -> Items.MAGENTA_DYE;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_DYE;
            case YELLOW -> Items.YELLOW_DYE;
            case LIME -> Items.LIME_DYE;
            case PINK -> Items.PINK_DYE;
            case GRAY -> Items.GRAY_DYE;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_DYE;
            case CYAN -> Items.CYAN_DYE;
            case PURPLE -> Items.PURPLE_DYE;
            case BLUE -> Items.BLUE_DYE;
            case BROWN -> Items.BROWN_DYE;
            case GREEN -> Items.GREEN_DYE;
            case RED -> Items.RED_DYE;
            case BLACK -> Items.BLACK_DYE;
        };
    }

    private record Result(ItemStack output, int bleachIndex) {
        private static final Result INVALID = new Result(null, -1);
    }
}
