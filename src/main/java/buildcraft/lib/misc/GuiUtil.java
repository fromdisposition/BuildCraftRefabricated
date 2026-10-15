/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import java.util.function.DoubleSupplier;
import net.minecraft.client.Minecraft;

public class GuiUtil {
   public static final IGuiArea AREA_WHOLE_SCREEN = IGuiArea.create(() -> 0.0, () -> 0.0, GuiUtil::getScreenWidth, GuiUtil::getScreenHeight);

   /** Maps a vanilla {@link com.mojang.blaze3d.platform.InputConstants} mouse-button id onto the classic
    * 0=left/1=right/2=middle numbering BC click handlers use (vanilla ids became 1=left/3=right/2=middle in 26.3). */
   public static int classicButton(int button) {
      if (button == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT) {
         return 0;
      }
      if (button == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_RIGHT) {
         return 1;
      }
      return button == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_MIDDLE ? 2 : button;
   }

   public static int getScreenWidth() {
      return Minecraft.getInstance().getWindow().getGuiScaledWidth();
   }

   public static int getScreenHeight() {
      return Minecraft.getInstance().getWindow().getGuiScaledHeight();
   }

   public static IGuiArea moveRectangleToCentre(GuiRectangle area) {
      double w = area.width;
      double h = area.height;
      DoubleSupplier posX = () -> (AREA_WHOLE_SCREEN.getWidth() - w) / 2.0;
      DoubleSupplier posY = () -> (AREA_WHOLE_SCREEN.getHeight() - h) / 2.0;
      IGuiPosition position = IGuiPosition.create(posX, posY);
      return IGuiArea.create(position, area.width, area.height);
   }

   public static GuiUtil.AutoGlScissor scissor(final BCGraphics graphics, double x, double y, double w, double h) {
      graphics.enableScissor((int)x, (int)y, (int)(x + w), (int)(y + h));
      return new GuiUtil.AutoGlScissor() {
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

   @FunctionalInterface
   public interface IVerticalAppendingDrawer<D> {
      double draw(D var1, double var2, double var4);
   }
}
