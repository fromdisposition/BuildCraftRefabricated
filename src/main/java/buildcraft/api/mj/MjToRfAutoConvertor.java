package buildcraft.api.mj;

import buildcraft.lib.transfer.energy.EnergyHandler;
import buildcraft.lib.transfer.transaction.Transaction;

public class MjToRfAutoConvertor implements IMjReadable {

    final EnergyHandler fe;

    public static MjToRfAutoConvertor create(EnergyHandler fe) {
        if (fe == null) {
            return null;
        }
        if (!MjAPI.isRfAutoConversionEnabled()) {
            return null;
        }

        return new OfBoth(fe);
    }

    public static IMjReceiver createReceiver(EnergyHandler fe) {
        MjToRfAutoConvertor convertor = create(fe);
        if (convertor instanceof IMjReceiver) {
            return (IMjReceiver) convertor;
        } else {
            return null;
        }
    }

    public static IMjPassiveProvider createProvider(EnergyHandler fe) {
        MjToRfAutoConvertor convertor = create(fe);
        if (convertor instanceof IMjPassiveProvider) {
            return (IMjPassiveProvider) convertor;
        } else {
            return null;
        }
    }

    MjToRfAutoConvertor(EnergyHandler handler) {
        this.fe = handler;
    }

    @Override
    public boolean canConnect(IMjConnector other) {
        return true;
    }

    @Override
    public long getStored() {
        return fe.getAmountAsLong() * MjAPI.getRfConversion().mjPerRf;
    }

    @Override
    public long getCapacity() {
        return fe.getCapacityAsLong() * MjAPI.getRfConversion().mjPerRf;
    }

    long implGetPowerRequested() {
        return (fe.getCapacityAsLong() - fe.getAmountAsLong()) * MjAPI.getRfConversion().mjPerRf;
    }

    long implReceivePower(long microJoules, boolean simulate) {
        long mjPerRf = MjAPI.getRfConversion().mjPerRf;
        int maxRf = (int) (microJoules / mjPerRf);
        if (maxRf <= 0) {
            return microJoules;
        }

        if (simulate) {

            try (Transaction tx = Transaction.openRoot()) {
                int received = fe.insert(maxRf, tx);

                return microJoules - received * mjPerRf;
            }
        } else {
            try (Transaction tx = Transaction.openRoot()) {
                int received = fe.insert(maxRf, tx);
                tx.commit();
                return microJoules - received * mjPerRf;
            }
        }
    }

    long implExtractPower(long min, long max, boolean simulate) {
        long mjPerRf = MjAPI.getRfConversion().mjPerRf;
        int maxRf = (int) (max / mjPerRf);
        if (maxRf <= 0) {
            return 0;
        }

        long extractedMJ;
        try (Transaction simTx = Transaction.openRoot()) {
            int extractedRF = fe.extract(maxRf, simTx);
            extractedMJ = extractedRF * mjPerRf;

        }

        if (extractedMJ < min) {
            return 0;
        }

        if (!simulate) {
            try (Transaction tx = Transaction.openRoot()) {
                int extractedRF = fe.extract(maxRf, tx);
                tx.commit();
                return extractedRF * mjPerRf;
            }
        }

        return extractedMJ;
    }
}

final class OfBoth extends MjToRfAutoConvertor implements IMjReceiver, IMjPassiveProvider {

    OfBoth(EnergyHandler handler) {
        super(handler);
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    @Override
    public long getPowerRequested() {
        return implGetPowerRequested();
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        return implReceivePower(microJoules, simulate);
    }

    @Override
    public long extractPower(long min, long max, boolean simulate) {
        return implExtractPower(min, max, simulate);
    }
}
