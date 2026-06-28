/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.gui;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.builders.container.ContainerFillerPlanner;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.statement.GuiElementStatement;
import buildcraft.lib.gui.statement.GuiElementStatementDrag;
import buildcraft.lib.gui.statement.GuiElementStatementParam;
import buildcraft.lib.gui.statement.GuiElementStatementSource;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.statement.FullStatement;
import buildcraft.lib.statement.StatementContext;
import java.util.List;
//? if >= 1.21.10 {
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

public class GuiFillerPlanner extends BcScreen<ContainerFillerPlanner> {
   private static final Identifier TEXTURE = Identifier.parse("buildcraftbuilders:textures/gui/filler_planner.png");

   public GuiFillerPlanner(ContainerFillerPlanner container, Inventory playerInv, Component title) {
      super(container, playerInv, Component.translatable("item.buildcraftbuilders.filler_planner"), 176, 81);
   }

   @Override
   protected boolean shouldAddHelpLedger() {
      return false;
   }

   @Override
   protected void initGuiElements() {
      this.mainGui.shownElements.add(new GuiElementStatementDrag(this.mainGui));
      this.mainGui.shownElements.add(new GuiElementStatementSource<>(this.mainGui, true, ((ContainerFillerPlanner)this.menu).possiblePatternsContext));
      IGuiArea patternArea = new GuiRectangle(12.0, 32.0, 32.0, 32.0).offset(this.mainGui.rootElement);
      this.mainGui
         .shownElements
         .add(
            new GuiElementStatement<IFillerPattern>(
               this.mainGui,
               patternArea,
               ((ContainerFillerPlanner)this.menu).getPatternStatementClient(),
               ((ContainerFillerPlanner)this.menu).possiblePatternsContext,
               true
            ) {
               @Override
               public void drawBackground(float partialTicks) {
                  IFillerPattern statement = this.get();
                  double x = this.getX();
                  double y = this.getY();
                  if (statement != null) {
                     ISprite sprite = statement.getSprite();
                     if (sprite != null) {
                        GuiIcon.drawAt(sprite, x, y, 32.0);
                     }
                  } else {
                     GuiElementStatement.ICON_SLOT_NOT_SET.drawAt(x, y);
                  }
               }
            }
         );
      IStatementContainer fakeContainer = new IStatementContainer() {
         @Override
         public BlockEntity getTile() {
            return null;
         }

         @Override
         public BlockEntity getNeighbourTile(Direction side) {
            return null;
         }
      };

      for (int i = 0; i < 4; i++) {
         IGuiArea paramArea = new GuiRectangle(53 + 18 * i, 39.0, 18.0, 18.0).offset(this.mainGui.rootElement);
         this.mainGui
            .shownElements
            .add(new GuiElementStatementParam(this.mainGui, paramArea, fakeContainer, ((ContainerFillerPlanner)this.menu).getPatternStatementClient(), i, true));
      }

      IGuiArea invertArea = new GuiRectangle(152.0, 40.0, 16.0, 16.0).offset(this.mainGui.rootElement);
      this.mainGui.shownElements.add(new GuiElementSimple(this.mainGui, invertArea) {
         @Override
         public void addToolTips(List<ToolTip> tooltips) {
            if (this.contains(GuiFillerPlanner.this.mainGui.mouse)) {
               String key = ((ContainerFillerPlanner)GuiFillerPlanner.this.menu).isInverted() ? "tip.filler.invert.on" : "tip.filler.invert.off";
               tooltips.add(new ToolTip(LocaleUtil.localize(key)));
            }
         }

         @Override
         public void addHelpElements(List<ElementHelpInfo.HelpPosition> elements) {
            elements.add(new ElementHelpInfo("buildcraft.help.filler.invert.title", -3364216, "buildcraft.help.filler.invert.desc").target(this));
         }
      });
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      graphics.blit(TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
      int mx = (int)this.mainGui.mouse.getX() - this.leftPos;
      int my = (int)this.mainGui.mouse.getY() - this.topPos;
      boolean invertHover = mx >= 152 && mx < 168 && my >= 40 && my < 56;
      int invertU = ((ContainerFillerPlanner)this.menu).isInverted() ? 240 : 224;
      int invertV = invertHover ? 16 : 0;
      new GuiIcon(TEXTURE, invertU, invertV, 16.0, 16.0).drawAt(this.leftPos + 152, this.topPos + 40);
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      String titleStr = Component.translatable("item.buildcraftbuilders.filler_planner").getString();
      graphics.text(this.font, titleStr, (this.imageWidth - this.font.width(titleStr)) / 2, 10, -12566464, false);
   }

   @Override
   //? if >= 1.21.10 {
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      return this.bcMouseClicked((int)event.x(), (int)event.y(), event.button()) || super.mouseClicked(event, doubleClick);
   }
   //?} else {
   /*public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return this.bcMouseClicked((int)mouseX, (int)mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
   }
   *///?}

   private boolean bcMouseClicked(int clickX, int clickY, int button) {
      if (button == 0) {
         int mx = clickX - this.leftPos;
         int my = clickY - this.topPos;
         if (mx >= 152 && mx < 168 && my >= 40 && my < 56) {
            ((ContainerFillerPlanner)this.menu).sendMessage(12, buf -> {});
            if (this.minecraft.player != null) {
               this.minecraft.player.playSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
            }

            return true;
         }
      }

      return false;
   }
}
