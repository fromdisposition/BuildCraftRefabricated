package buildcraft.lib.mj;

import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjBattery;

public class MjRedstoneBatteryReceiver extends MjBatteryReceiver implements IMjRedstoneReceiver {
   public MjRedstoneBatteryReceiver(MjBattery battery) {
      super(battery);
   }
}
