/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.gui;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.tiles.IControllable;
import buildcraft.builders.container.ContainerFiller;
import buildcraft.core.BCCoreSprites;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.ledger.LedgerOwnership;
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
import java.util.Locale;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

public class GuiFiller extends BcScreen<ContainerFiller> {
   private static final Identifier TEXTURE = Identifier.parse("buildcraftbuilders:textures/gui/filler.png");
   private static final Identifier LOCK_TEXTURE = Identifier.parse("buildcraftlib:textures/icons/lock.png");

   public GuiFiller(ContainerFiller container, Inventory playerInv, Component title) {
      super(container, playerInv, Component.translatable("block.buildcraftbuilders.filler"), 176, 241);
   }

   @Override
   protected void initGuiElements() {
      this.mainGui.shownElements.add(new GuiElementStatementDrag(this.mainGui));
      if (((ContainerFiller)this.menu).tile != null) {
         this.mainGui
            .shownElements
            .add(new LedgerOwnership(this.mainGui, () -> ((ContainerFiller)this.menu).tile != null ? ((ContainerFiller)this.menu).tile.getOwner() : null, true));
      }

      this.mainGui.shownElements.add(new LedgerFillerProgress(this.mainGui, (ContainerFiller)this.menu));
      this.mainGui.shownElements.add(new GuiElementStatementSource<>(this.mainGui, true, ((ContainerFiller)this.menu).possiblePatternsContext));
      IGuiArea patternArea = new GuiRectangle(12.0, 32.0, 32.0, 32.0).offset(this.mainGui.rootElement);
      this.mainGui
         .shownElements
         .add(
            new GuiElementStatement<IFillerPattern>(
               this.mainGui, patternArea, ((ContainerFiller)this.menu).getPatternStatementClient(), ((ContainerFiller)this.menu).possiblePatternsContext, true
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
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               patternArea,
               new ElementHelpInfo(
                  "buildcraft.help.filler.pattern.title", -7811960, "buildcraft.help.filler.pattern.desc1", "buildcraft.help.filler.pattern.desc2"
               )
            )
         );
      IGuiArea paramsArea = new GuiRectangle(53.0, 39.0, 72.0, 18.0).offset(this.mainGui.rootElement);
      this.mainGui
         .shownElements
         .add(new DummyHelpElement(paramsArea, new ElementHelpInfo("buildcraft.help.filler.params.title", -3364216, "buildcraft.help.filler.params.desc")));
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
            .add(new GuiElementStatementParam(this.mainGui, paramArea, fakeContainer, ((ContainerFiller)this.menu).getPatternStatementClient(), i, true));
      }

      IGuiArea excavateArea = new GuiRectangle(130.0, 40.0, 16.0, 16.0).offset(this.mainGui.rootElement);
      this.mainGui.shownElements.add(new GuiElementSimple(this.mainGui, excavateArea) {
         @Override
         public void addToolTips(List<ToolTip> tooltips) {
            if (this.contains(GuiFiller.this.mainGui.mouse)) {
               String key = ((ContainerFiller)GuiFiller.this.menu).getSyncedCanExcavate() ? "tip.filler.excavate.on" : "tip.filler.excavate.off";
               tooltips.add(new ToolTip(LocaleUtil.localize(key)));
            }
         }

         @Override
         public void addHelpElements(List<ElementHelpInfo.HelpPosition> elements) {
            elements.add(new ElementHelpInfo("buildcraft.help.filler.excavate.title", -3364216, "buildcraft.help.filler.excavate.desc").target(this));
         }
      });
      IGuiArea invertArea = new GuiRectangle(152.0, 40.0, 16.0, 16.0).offset(this.mainGui.rootElement);
      this.mainGui.shownElements.add(new GuiElementSimple(this.mainGui, invertArea) {
         @Override
         public void addToolTips(List<ToolTip> tooltips) {
            if (this.contains(GuiFiller.this.mainGui.mouse)) {
               String key = ((ContainerFiller)GuiFiller.this.menu).isInverted() ? "tip.filler.invert.on" : "tip.filler.invert.off";
               tooltips.add(new ToolTip(LocaleUtil.localize(key)));
            }
         }

         @Override
         public void addHelpElements(List<ElementHelpInfo.HelpPosition> elements) {
            elements.add(new ElementHelpInfo("buildcraft.help.filler.invert.title", -3364216, "buildcraft.help.filler.invert.desc").target(this));
         }
      });
      IGuiArea controlModeArea = new GuiRectangle(28.0, 16.0, 16.0, 16.0).offset(this.mainGui.rootElement);
      this.mainGui
         .shownElements
         .add(
            new GuiElementSimple(this.mainGui, controlModeArea) {
               @Override
               public void addToolTips(List<ToolTip> tooltips) {
                  if (this.contains(GuiFiller.this.mainGui.mouse)) {
                     IControllable.Mode mode = ((ContainerFiller)GuiFiller.this.menu).getSyncedMode();
                     if (mode != IControllable.Mode.ON) {
                        String key = "gate.action.machine." + mode.name().toLowerCase(Locale.ROOT);
                        tooltips.add(new ToolTip(LocaleUtil.localize(key)));
                     }
                  }
               }

               @Override
               public void addHelpElements(List<ElementHelpInfo.HelpPosition> elements) {
                  elements.add(
                     new ElementHelpInfo(
                           "buildcraft.help.filler.mode.title", -13386753, "buildcraft.help.filler.mode.desc1", "buildcraft.help.filler.mode.desc2"
                        )
                        .target(this)
                  );
               }
            }
         );
      IGuiArea lockArea = new GuiRectangle(12.0, 16.0, 16.0, 16.0).offset(this.mainGui.rootElement);
      this.mainGui.shownElements.add(new GuiElementSimple(this.mainGui, lockArea) {
         @Override
         public void addToolTips(List<ToolTip> tooltips) {
            if (this.contains(GuiFiller.this.mainGui.mouse) && ((ContainerFiller)GuiFiller.this.menu).getSyncedLocked()) {
               tooltips.add(new ToolTip("Locked"));
            }
         }

         @Override
         public void addHelpElements(List<ElementHelpInfo.HelpPosition> elements) {
            elements.add(new ElementHelpInfo("buildcraft.help.filler.locked.title", -17613, "buildcraft.help.filler.locked.desc").target(this));
         }
      });
   }

   @Override
   protected void containerTick() {
      super.containerTick();
      ((ContainerFiller)this.menu).getPatternStatementClient().canInteract = !((ContainerFiller)this.menu).getSyncedLocked();
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
      int mx = (int)this.mainGui.mouse.getX() - this.leftPos;
      int my = (int)this.mainGui.mouse.getY() - this.topPos;
      boolean excavateHover = mx >= 130 && mx < 146 && my >= 40 && my < 56;
      boolean invertHover = mx >= 152 && mx < 168 && my >= 40 && my < 56;
      int excavateU = ((ContainerFiller)this.menu).getSyncedCanExcavate() ? 208 : 192;
      int excavateV = excavateHover ? 16 : 0;
      new GuiIcon(TEXTURE, excavateU, excavateV, 16.0, 16.0).drawAt(this.leftPos + 130, this.topPos + 40);
      int invertU = ((ContainerFiller)this.menu).isInverted() ? 240 : 224;
      int invertV = invertHover ? 16 : 0;
      new GuiIcon(TEXTURE, invertU, invertV, 16.0, 16.0).drawAt(this.leftPos + 152, this.topPos + 40);
      this.drawControlModeIcon(this.leftPos + 28, this.topPos + 16);
      if (((ContainerFiller)this.menu).getSyncedLocked()) {
         new GuiIcon(LOCK_TEXTURE, 0.0, 0.0, 16.0, 16.0, 16).drawAt(this.leftPos + 12, this.topPos + 16);
      }
   }

   private void drawControlModeIcon(int x, int y) {
      IControllable.Mode mode = ((ContainerFiller)this.menu).getSyncedMode();
      SpriteHolderRegistry.SpriteHolder holder = BCCoreSprites.ACTION_MACHINE_CONTROL.get(mode);
      if (holder != null) {
         GuiIcon.drawAt(holder, x, y, 16.0);
      }
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      String titleStr = Component.translatable("block.buildcraftbuilders.filler").getString();
      graphics.text(this.font, titleStr, (this.imageWidth - this.font.width(titleStr)) / 2, 10, -12566464, false);
      graphics.text(this.font, Component.translatable("gui.filling.resources").getString(), 7, 74, -12566464, false);
      graphics.text(this.font, Component.translatable("container.inventory").getString(), 7, 141, -12566464, false);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (event.button() == 0) {
         int mx = (int)event.x() - this.leftPos;
         int my = (int)event.y() - this.topPos;
         if (mx >= 130 && mx < 146 && my >= 40 && my < 56) {
            ((ContainerFiller)this.menu).sendMessage(10, buf -> {});
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
         }

         if (mx >= 152 && mx < 168 && my >= 40 && my < 56) {
            ((ContainerFiller)this.menu).sendMessage(12, buf -> {});
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
         }
      }

      return super.mouseClicked(event, doubleClick);
   }
}
