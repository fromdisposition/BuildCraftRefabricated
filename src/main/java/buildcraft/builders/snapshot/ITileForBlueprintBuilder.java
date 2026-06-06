package buildcraft.builders.snapshot;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.lib.fabric.transfer.MultiFluidTankStorage;

public interface ITileForBlueprintBuilder extends ITileForSnapshotBuilder {
   Blueprint.BuildingInfo getBlueprintBuildingInfo();

   IItemTransactor getInvResources();

   MultiFluidTankStorage getFluidTanks();
}
