package buildcraft.lib.gui.elem;

import buildcraft.lib.client.fluid.BcFluidGuiDrawer;
import buildcraft.lib.fabric.transfer.FluidStorageSnapshot;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.widget.WidgetFluidTank;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.ChatFormatting;
import org.jspecify.annotations.Nullable;

public class GuiElementFluidTank implements IInteractionElement {
   private final BuildCraftGui gui;
   private final IGuiArea area;
   private final Supplier<FluidStorageSnapshot> snapshotSupplier;
   private final @Nullable Storage<FluidVariant> tank;
   private final WidgetFluidTank widget;
   private final GuiIcon overlay;

   public GuiElementFluidTank(BuildCraftGui gui, IGuiArea area, @Nullable Storage<FluidVariant> tank, WidgetFluidTank widget, GuiIcon overlay) {
      this(gui, area, tank == null ? () -> FluidStorageSnapshot.EMPTY : () -> FluidStorageSnapshot.of(tank), tank, widget, overlay);
   }

   public GuiElementFluidTank(BuildCraftGui gui, IGuiArea area, Supplier<FluidStorageSnapshot> snapshotSupplier, WidgetFluidTank widget, GuiIcon overlay) {
      this(gui, area, snapshotSupplier, widget != null ? widget.getTankStorage() : null, widget, overlay);
   }

   private GuiElementFluidTank(
      BuildCraftGui gui,
      IGuiArea area,
      Supplier<FluidStorageSnapshot> snapshotSupplier,
      @Nullable Storage<FluidVariant> tank,
      WidgetFluidTank widget,
      GuiIcon overlay
   ) {
      this.gui = gui;
      this.area = area;
      this.snapshotSupplier = snapshotSupplier;
      this.tank = tank;
      this.widget = widget;
      this.overlay = overlay;
   }

   public @Nullable Storage<FluidVariant> getTankStorage() {
      return this.tank;
   }

   @Override
   public double getX() {
      return this.area.getX();
   }

   @Override
   public double getY() {
      return this.area.getY();
   }

   @Override
   public double getWidth() {
      return this.area.getWidth();
   }

   @Override
   public double getHeight() {
      return this.area.getHeight();
   }

   @Override
   public void drawBackground(float partialTicks) {
      FluidStorageSnapshot snapshot = this.snapshotSupplier.get();
      if (!snapshot.isEmpty() && snapshot.capacityMb() > 0) {
         BCGraphics graphics = GuiIcon.getGuiGraphics();
         if (graphics != null) {
            this.drawFluid(graphics, snapshot.toFluidStack(), snapshot.amountMb(), snapshot.capacityMb());
         }
      }

      if (this.overlay != null) {
         this.overlay.drawAt(this.area);
      }
   }

   private void drawFluid(BCGraphics graphics, FluidStack fluid, int amount, int capacity) {
      int x = (int)this.area.getX();
      int y = (int)this.area.getY();
      int w = (int)this.area.getWidth();
      int h = (int)this.area.getHeight();
      int fillHeight = (int)((float)amount / capacity * h);
      if (fillHeight <= 0 && amount > 0) {
         fillHeight = 1;
      }

      int fillY = y + h - fillHeight;
      BcFluidGuiDrawer.drawFluidStack(graphics, x, fillY, w, fillHeight, fluid);
   }

   @Override
   public void addToolTips(List<ToolTip> tooltips) {
      if (this.contains(this.gui.mouse.getX(), this.gui.mouse.getY())) {
         FluidStorageSnapshot snapshot = this.snapshotSupplier.get();
         String name = snapshot.isEmpty() ? "Empty" : snapshot.toFluidStack().getHoverName().getString();
         tooltips.add(new ToolTip(name, "" + ChatFormatting.GRAY + snapshot.amountMb() + " / " + snapshot.capacityMb() + " mB"));
      }
   }

   @Override
   public void onMouseClicked(int button) {
      if (this.widget != null && this.contains(this.gui.mouse.getX(), this.gui.mouse.getY())) {
         this.widget.sendClick();
      }
   }
}
