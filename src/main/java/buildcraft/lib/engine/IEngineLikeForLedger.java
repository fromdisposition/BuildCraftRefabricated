package buildcraft.lib.engine;

import buildcraft.api.enums.EnumPowerStage;

public interface IEngineLikeForLedger {
   EnumPowerStage getPowerStage();

   boolean isEngineOn();

   long getCurrentMjOutput();

   long getMjStored();

   double getHeat();
}
