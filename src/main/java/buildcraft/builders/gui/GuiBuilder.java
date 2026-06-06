package buildcraft.builders.gui;

import buildcraft.builders.container.ContainerBuilder;
import buildcraft.builders.snapshot.EnumContainerContentsMode;
import buildcraft.builders.snapshot.EnumFluidHandlingMode;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.button.BCButton;
import buildcraft.lib.gui.elem.GuiElementFluidTank;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.widget.WidgetFluidTank;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GuiBuilder extends BcScreen<ContainerBuilder> {
   private static final Identifier TEXTURE_BASE = Identifier.parse("buildcraftbuilders:textures/gui/builder.png");
   private static final Identifier TEXTURE_BLUEPRINT = Identifier.parse("buildcraftbuilders:textures/gui/builder_blueprint.png");
   private static final int SIZE_X = 176;
   private static final int SIZE_BLUEPRINT_X = 256;
   private static final int SIZE_Y = 222;
   private static final int BLUEPRINT_WIDTH = 87;
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0.0, 0.0, 176.0, 222.0);
   private static final GuiIcon ICON_BLUEPRINT_GUI = new GuiIcon(TEXTURE_BLUEPRINT, 169.0, 0.0, 87.0, 222.0);
   private static final GuiIcon ICON_TANK_OVERLAY = new GuiIcon(TEXTURE_BLUEPRINT, 0.0, 54.0, 16.0, 47.0);
   private static final int TANK_Y = 145;
   private static final int TANK_W = 16;
   private static final int TANK_H = 47;
   private static final int FLUID_BUTTON_X = 150;
   private static final int FLUID_BUTTON_Y = 20;
   private static final int FLUID_BUTTON_SIZE = 20;
   private static final int CONTENTS_BUTTON_X = 130;
   private static final int CONTENTS_BUTTON_Y = 20;
   private static final int CONTENTS_BUTTON_SIZE = 20;
   private static final int SNAPSHOT_X = 80;
   private static final int SNAPSHOT_Y = 27;
   private static final int RESOURCE_X = 8;
   private static final int RESOURCE_Y = 72;
   private static final int RESOURCE_W = 160;
   private static final int RESOURCE_H = 52;
   private static final int DISPLAY_X = 179;
   private static final int DISPLAY_Y = 18;
   private static final int DISPLAY_W = 70;
   private static final int DISPLAY_H = 106;
   private static final int TANK_ROW_X = 179;
   private static final int TANK_ROW_W = 70;
   private GuiBuilder.FluidModeButton fluidModeButton;
   private GuiBuilder.ContentsModeButton contentsModeButton;

   private static int tankX(int i) {
      return 179 + i * 18;
   }

   public GuiBuilder(ContainerBuilder container, Inventory playerInv, Component title) {
      super(container, playerInv, title, 256, 222);
      this.inventoryLabelY = this.imageHeight - 94;
   }

   @Override
   protected void initGuiElements() {
      if (((ContainerBuilder)this.menu).tile != null) {
         for (int i = 0; i < 4; i++) {
            int idx = i;
            WidgetFluidTank widget = idx < ((ContainerBuilder)this.menu).widgetTanks.size() ? ((ContainerBuilder)this.menu).widgetTanks.get(idx) : null;
            this.mainGui
               .shownElements
               .add(
                  new GuiElementFluidTank(
                     this.mainGui,
                     new GuiRectangle(tankX(idx), 145.0, 16.0, 47.0).offset(this.mainGui.rootElement),
                     widget != null ? widget.getTankStorage() : null,
                     widget,
                     ICON_TANK_OVERLAY
                  )
               );
         }

         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(80.0, 27.0, 16.0, 16.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo(
                     "buildcraft.help.builder.snapshot.title", -7811960, "buildcraft.help.builder.snapshot.desc1", "buildcraft.help.builder.snapshot.desc2"
                  )
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(8.0, 72.0, 160.0, 52.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo(
                     "buildcraft.help.builder.resources.title", -13176, "buildcraft.help.builder.resources.desc1", "buildcraft.help.builder.resources.desc2"
                  )
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(150.0, 20.0, 20.0, 20.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo(
                     "buildcraft.help.builder.fluid_mode.title",
                     -7811841,
                     "buildcraft.help.builder.fluid_mode.desc1",
                     "buildcraft.help.builder.fluid_mode.desc2"
                  )
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(130.0, 20.0, 20.0, 20.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo(
                     "buildcraft.help.builder.contents_mode.title",
                     -3372852,
                     "buildcraft.help.builder.contents_mode.desc1",
                     "buildcraft.help.builder.contents_mode.desc2"
                  )
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(179.0, 18.0, 70.0, 106.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo(
                     "buildcraft.help.builder.display.title", -1980113, "buildcraft.help.builder.display.desc1", "buildcraft.help.builder.display.desc2"
                  )
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(179.0, 145.0, 70.0, 47.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo(
                     "buildcraft.help.builder.tanks.title", -7820545, "buildcraft.help.builder.tanks.desc1", "buildcraft.help.builder.tanks.desc2"
                  )
               )
            );
      }
   }

   @Override
   protected void init() {
      super.init();
      this.fluidModeButton = new GuiBuilder.FluidModeButton(this.leftPos + 150, this.topPos + 20);
      this.addRenderableWidget(this.fluidModeButton);
      this.contentsModeButton = new GuiBuilder.ContentsModeButton(this.leftPos + 130, this.topPos + 20);
      this.addRenderableWidget(this.contentsModeButton);
   }

   @Override
   protected void containerTick() {
      super.containerTick();
      if (this.fluidModeButton != null) {
         this.fluidModeButton.refreshTooltip();
      }

      if (this.contentsModeButton != null) {
         this.contentsModeButton.refreshTooltip();
      }
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
      ICON_BLUEPRINT_GUI.drawAt(this.mainGui.rootElement.offset(169.0, 0.0));
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      String titleStr = this.title.getString();
      int titleWidth = this.font.width(titleStr);
      graphics.text(this.font, titleStr, (176 - titleWidth) / 2, 6, -12566464, false);
      int leftToBreak = ((ContainerBuilder)this.menu).getSyncedLeftToBreak();
      int leftToPlace = ((ContainerBuilder)this.menu).getSyncedLeftToPlace();
      int y = 50;
      if (leftToBreak > 0) {
         graphics.text(this.font, Component.literal("Break: " + leftToBreak).withStyle(ChatFormatting.DARK_GRAY), 10, y, -12566464, false);
         y += 10;
      }

      if (leftToPlace > 0) {
         graphics.text(this.font, Component.literal("Place: " + leftToPlace).withStyle(ChatFormatting.DARK_GRAY), 10, y, -12566464, false);
      }
   }

   private class ContentsModeButton extends BCButton {
      private static final ItemStack CHEST_ICON = new ItemStack(Items.CHEST);
      private static final ItemStack BARRIER_OVERLAY = new ItemStack(Items.BARRIER);
      private EnumContainerContentsMode lastKnown;

      ContentsModeButton(int x, int y) {
         super(x, y, 20, 20, Component.empty());
         this.refreshTooltip();
      }

      public void onPress(InputWithModifiers modifiers) {
         ((ContainerBuilder)GuiBuilder.this.menu).sendMessage(13, buf -> {});
      }

      @Override
      protected void drawButtonContent(BCGraphics graphics, int mouseX, int mouseY, float partialTick) {
         this.drawDefaultButtonSprite(graphics);
         graphics.item(CHEST_ICON, this.getX() + 2, this.getY() + 2);
         if (((ContainerBuilder)GuiBuilder.this.menu).getSyncedContentsMode() == EnumContainerContentsMode.IGNORE) {
            graphics.item(BARRIER_OVERLAY, this.getX() + 2, this.getY() + 2);
         }
      }

      @Override
      protected void updateWidgetNarration(NarrationElementOutput output) {
         this.defaultButtonNarrationText(output);
      }

      void refreshTooltip() {
         EnumContainerContentsMode mode = ((ContainerBuilder)GuiBuilder.this.menu).getSyncedContentsMode();
         if (mode != this.lastKnown) {
            this.lastKnown = mode;
            this.setTooltip(Tooltip.create(Component.translatable(mode.tooltipKey())));
         }
      }
   }

   private class FluidModeButton extends BCButton {
      private EnumFluidHandlingMode lastKnown;

      FluidModeButton(int x, int y) {
         super(x, y, 20, 20, Component.empty());
         this.refreshTooltip();
      }

      public void onPress(InputWithModifiers modifiers) {
         ((ContainerBuilder)GuiBuilder.this.menu).sendMessage(12, buf -> {});
      }

      @Override
      protected void drawButtonContent(BCGraphics graphics, int mouseX, int mouseY, float partialTick) {
         this.drawDefaultButtonSprite(graphics);
         EnumFluidHandlingMode mode = ((ContainerBuilder)GuiBuilder.this.menu).getSyncedFluidMode();
         graphics.item(mode.icon(), this.getX() + 2, this.getY() + 2);
      }

      @Override
      protected void updateWidgetNarration(NarrationElementOutput output) {
         this.defaultButtonNarrationText(output);
      }

      void refreshTooltip() {
         EnumFluidHandlingMode mode = ((ContainerBuilder)GuiBuilder.this.menu).getSyncedFluidMode();
         if (mode != this.lastKnown) {
            this.lastKnown = mode;
            this.setTooltip(Tooltip.create(Component.translatable(mode.tooltipKey())));
         }
      }
   }
}
