/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.function.DoubleSupplier;

import net.minecraft.client.Minecraft;
import buildcraft.lib.gui.BCGraphics;

import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;

public class GuiUtil {

    public static final IGuiArea AREA_WHOLE_SCREEN;

    static {
        AREA_WHOLE_SCREEN = IGuiArea.create(() -> 0, () -> 0, GuiUtil::getScreenWidth, GuiUtil::getScreenHeight);
    }

    public static int getScreenWidth() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    public static int getScreenHeight() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }

    public static IGuiArea moveRectangleToCentre(GuiRectangle area) {
        final double w = area.width;
        final double h = area.height;

        DoubleSupplier posX = () -> (AREA_WHOLE_SCREEN.getWidth() - w) / 2;
        DoubleSupplier posY = () -> (AREA_WHOLE_SCREEN.getHeight() - h) / 2;

        IGuiPosition position = IGuiPosition.create(posX, posY);
        return IGuiArea.create(position, area.width, area.height);
    }

    public static <D> void drawVerticallyAppending(IGuiPosition element, Iterable<? extends D> iterable,
        IVerticalAppendingDrawer<D> drawer) {
        double x = element.getX();
        double y = element.getY();
        for (D drawable : iterable) {
            y += drawer.draw(drawable, x, y);
        }
    }

    @FunctionalInterface
    public interface IVerticalAppendingDrawer<D> {
        double draw(D drawable, double x, double y);
    }

    public static AutoGlScissor scissor(BCGraphics graphics, double x, double y, double w, double h) {
        graphics.enableScissor((int) x, (int) y, (int) (x + w), (int) (y + h));
        return new AutoGlScissor() {
            @Override
            public void close() {
                graphics.disableScissor();
            }
        };
    }

    public interface AutoGlScissor extends AutoCloseable {
        @Override
        void close();
    }
}
