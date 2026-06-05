package buildcraft.api.mj;

public interface IMjPassiveProvider extends IMjConnector {

    long extractPower(long min, long max, boolean simulate);
}
