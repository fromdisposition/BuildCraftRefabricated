package buildcraft.api.tiles;

public interface IHeatable {
   double getMinHeatValue();

   double getIdealHeatValue();

   double getMaxHeatValue();

   double getCurrentHeatValue();

   double setHeatValue(double var1);
}
