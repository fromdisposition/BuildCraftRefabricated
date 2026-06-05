package buildcraft.lib.engine;

import net.minecraft.resources.Identifier;

import buildcraft.api.enums.EnumPowerStage;

public interface IEngineLikeForLedger {

    EnumPowerStage getPowerStage();

    boolean isEngineOn();

    long getCurrentMjOutput();

    long getMjStored();

    double getHeat();

}
