package buildcraft.lib.gui.pos;

import java.util.function.DoubleSupplier;

public interface IGuiArea extends IGuiPosition {
   double getWidth();

   double getHeight();

   default double getCenterX() {
      return this.getX() + this.getWidth() / 2.0;
   }

   default double getCenterY() {
      return this.getY() + this.getHeight() / 2.0;
   }

   default double getEndX() {
      return this.getX() + this.getWidth();
   }

   default double getEndY() {
      return this.getY() + this.getHeight();
   }

   default boolean contains(double x, double y) {
      double tx = this.getX();
      double ty = this.getY();
      double w = this.getWidth();
      double h = this.getHeight();
      return x < tx || x >= tx + w ? false : !(y < ty) && !(y >= ty + h);
   }

   default boolean contains(IGuiPosition position) {
      return this.contains(position.getX(), position.getY());
   }

   default boolean contains(IGuiArea element) {
      return element.getX() < this.getX() || element.getEndX() >= this.getEndX()
         ? false
         : !(element.getY() < this.getY()) && !(element.getEndY() >= this.getEndY());
   }

   default String rectangleToString() {
      return "[x = " + this.getX() + ", y = " + this.getY() + ", w = " + this.getWidth() + ", h = " + this.getHeight() + "]";
   }

   default GuiRectangle asImmutable() {
      return new GuiRectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
   }

   default IGuiPosition getCenter() {
      return this.getPosition(0, 0);
   }

   default IGuiPosition getEnd() {
      return this.getPosition(1, 1);
   }

   default IGuiPosition getCenterTop() {
      return this.getPosition(0, 0);
   }

   default IGuiPosition getPosition(int partX, int partY) {
      DoubleSupplier x = partX < 0 ? this::getX : (partX > 0 ? this::getEndX : this::getCenterX);
      DoubleSupplier y = partY < 0 ? this::getY : (partY > 0 ? this::getEndY : this::getCenterY);
      return new PositionCallable(x, y);
   }

   default IGuiArea offset(IGuiPosition by) {
      return by instanceof PositionAbsolute && by.getX() == 0.0 && by.getY() == 0.0 ? this : this.offset(by::getX, by::getY);
   }

   default IGuiArea offset(double x, DoubleSupplier y) {
      return create(() -> this.getX() + x, () -> this.getY() + y.getAsDouble(), this::getWidth, this::getHeight);
   }

   default IGuiArea offset(DoubleSupplier x, double y) {
      return create(() -> this.getX() + x.getAsDouble(), () -> this.getY() + y, this::getWidth, this::getHeight);
   }

   default IGuiArea offset(DoubleSupplier x, DoubleSupplier y) {
      return create(() -> this.getX() + x.getAsDouble(), () -> this.getY() + y.getAsDouble(), this::getWidth, this::getHeight);
   }

   default IGuiArea offset(double x, double y) {
      return x == 0.0 && y == 0.0 ? this : create(() -> this.getX() + x, () -> this.getY() + y, this::getWidth, this::getHeight);
   }

   default IGuiArea resize(double newWidth, double newHeight) {
      return create(this::getX, this::getY, () -> newWidth, () -> newHeight);
   }

   default IGuiArea resize(DoubleSupplier newWidth, DoubleSupplier newHeight) {
      return create(this::getX, this::getY, newWidth, newHeight);
   }

   default IGuiArea expand(double by) {
      return this.expand(by, by);
   }

   default IGuiArea expand(double dX, double dY) {
      return create(() -> this.getX() - dX, () -> this.getY() - dY, () -> this.getWidth() + dX * 2.0, () -> this.getHeight() + dY * 2.0);
   }

   default IGuiArea expand(DoubleSupplier by) {
      return this.expand(by, by);
   }

   default IGuiArea expand(DoubleSupplier dX, DoubleSupplier dY) {
      return create(
         () -> this.getX() - dX.getAsDouble(),
         () -> this.getY() - dY.getAsDouble(),
         () -> this.getWidth() + dX.getAsDouble() * 2.0,
         () -> this.getHeight() + dY.getAsDouble() * 2.0
      );
   }

   default IGuiArea offsetToOrigin() {
      return create(() -> 0.0, () -> 0.0, this::getWidth, this::getHeight);
   }

   static IGuiArea create(DoubleSupplier width, DoubleSupplier height) {
      return new AreaCallable(width, height);
   }

   static IGuiArea create(DoubleSupplier x, DoubleSupplier y, DoubleSupplier width, DoubleSupplier height) {
      return new AreaCallable(x, y, width, height);
   }

   static IGuiArea create(IGuiPosition pos, double width, double height) {
      return pos instanceof PositionAbsolute
         ? new GuiRectangle(pos.getX(), pos.getY(), width, height)
         : new AreaCallable(pos::getX, pos::getY, () -> width, () -> height);
   }
}
