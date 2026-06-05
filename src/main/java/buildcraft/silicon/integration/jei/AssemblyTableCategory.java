/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.integration.jei;

import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import buildcraft.lib.gui.BCGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import buildcraft.api.mj.MjAPI;

import buildcraft.silicon.BCSiliconItems;

public class AssemblyTableCategory extends AbstractRecipeCategory<AssemblyRecipeJei> {

    private static final int TEX_U = 3, TEX_V = 27;
    private static final int TEX_W = 89, TEX_H = 86;

    private static final int SLOT_X = 8 - TEX_U;
    private static final int SLOT_Y = 36 - TEX_V;
    private static final int SLOT_PITCH = 18;
    private static final int MAX_INPUT_SLOTS = 12;

    private static final int OUTPUT_X = TEX_W + 16;
    private static final int OUTPUT_Y = SLOT_Y;

    private static final int POWER_X = 4, POWER_Y = TEX_H + 2;
    private static final int POWER_COLOR = 0xFF404040;

    private static final int WIDTH = OUTPUT_X + 24;
    private static final int HEIGHT = TEX_H + 12;

    private final IDrawable background;

    public AssemblyTableCategory(IGuiHelper guiHelper) {
        super(
                AssemblyRecipeJeiTypes.ASSEMBLY,
                Component.translatable("gui.jei.category.buildcraft.assembly_table"),
                guiHelper.createDrawableItemLike(BCSiliconItems.ASSEMBLY_TABLE.get()),
                WIDTH, HEIGHT
        );
        this.background = guiHelper.createDrawable(
                Identifier.parse("buildcraftsilicon:textures/gui/assembly_table.png"),
                TEX_U, TEX_V, TEX_W, TEX_H);
    }

    @Override

    public void draw(AssemblyRecipeJei recipe, IRecipeSlotsView slots, net.minecraft.client.gui.GuiGraphicsExtractor graphics,
                     double mouseX, double mouseY) {

        background.draw(graphics);

        double mj = recipe.microJoules() / (double) MjAPI.MJ;
        String powerStr = Component.translatable(
                "gui.jei.category.buildcraft.assembly_table.power",
                buildcraft.lib.misc.LocaleUtil.localizeMj(recipe.microJoules())).getString();
        Font font = Minecraft.getInstance().font;
        new buildcraft.lib.gui.BCGraphics(graphics).text(font, powerStr, POWER_X, POWER_Y, POWER_COLOR, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AssemblyRecipeJei recipe, IFocusGroup focuses) {
        List<List<ItemStack>> inputs = recipe.inputSlots();
        int inputCount = Math.min(inputs.size(), MAX_INPUT_SLOTS);
        IRecipeSlotBuilder[] inputSlotBuilders = new IRecipeSlotBuilder[inputCount];
        for (int i = 0; i < inputCount; i++) {
            List<ItemStack> slot = inputs.get(i);
            if (slot.isEmpty()) continue;
            int col = i % 3;
            int row = i / 3;
            int x = SLOT_X + col * SLOT_PITCH;
            int y = SLOT_Y + row * SLOT_PITCH;
            inputSlotBuilders[i] = builder.addInputSlot(x, y).addItemStacks(slot);
        }

        IRecipeSlotBuilder outputSlotBuilder = null;
        if (!recipe.outputs().isEmpty()) {
            outputSlotBuilder = builder.addOutputSlot(OUTPUT_X, OUTPUT_Y)
                    .setOutputSlotBackground()
                    .addItemStacks(recipe.outputs());
        }

        int linkIdx = recipe.focusLinkInputIndex();
        if (linkIdx >= 0 && linkIdx < inputCount
                && inputSlotBuilders[linkIdx] != null
                && outputSlotBuilder != null) {
            builder.createFocusLink(inputSlotBuilders[linkIdx], outputSlotBuilder);
        }
    }
}
