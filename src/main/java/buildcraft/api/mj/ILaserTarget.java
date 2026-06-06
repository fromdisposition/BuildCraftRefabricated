package buildcraft.api.mj;

public interface ILaserTarget {
   long getRequiredLaserPower();

   long receiveLaserPower(long var1);

   boolean isInvalidTarget();
}
