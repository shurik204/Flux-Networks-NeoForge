package sonar.fluxnetworks.common.device;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.energy.IEnergyStorage;
import sonar.fluxnetworks.api.device.FluxDeviceType;
import sonar.fluxnetworks.api.device.IFluxPlug;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;
import sonar.fluxnetworks.common.util.FluxGuiStack;
import sonar.fluxnetworks.register.RegistryBlockEntityTypes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class TileFluxPlug extends TileFluxConnector implements IFluxPlug {

    private final FluxPlugHandler mHandler = new FluxPlugHandler();

    private final Map<Direction, EnergyStorage> mEnergyStorage = new HashMap<>();

    public TileFluxPlug(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(RegistryBlockEntityTypes.FLUX_PLUG.get(), pos, state);
    }

    @Nonnull
    @Override
    public FluxDeviceType getDeviceType() {
        return FluxDeviceType.PLUG;
    }

    @Nonnull
    @Override
    public FluxPlugHandler getTransferHandler() {
        return mHandler;
    }

    @Nonnull
    @Override
    public ItemStack getDisplayStack() {
        return FluxGuiStack.FLUX_PLUG;
    }

    @Override
    @SuppressWarnings("NonExtendableApiUsage")
    public void invalidateCapabilities() {
        mEnergyStorage.clear();
        super.invalidateCapabilities();
    }

    @Nullable
    @Override
    public <T> T getEnergyCapability(BlockCapability<T, Direction> cap, @Nullable Direction side) {
        if (!isRemoved()) {
            EnergyStorage storage = mEnergyStorage.computeIfAbsent(side != null ? side : Direction.from3DDataValue(0), EnergyStorage::new);
            return (T) storage;
        }
        return null;
    }

    private class EnergyStorage implements IEnergyStorage, IFNEnergyStorage {

        @Nonnull
        private final Direction mSide;

        public EnergyStorage(@Nonnull Direction side) {
            mSide = side;
        }

        ///// FORGE \\\\\

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (getNetwork().isValid()) {
                return (int) mHandler.receive(maxReceive, mSide, simulate, getNetwork().getBufferLimiter());
            }
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(getEnergyStoredL(), Integer.MAX_VALUE);
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) Math.min(getMaxEnergyStoredL(), Integer.MAX_VALUE);
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return getNetwork().isValid();
        }

        ///// FLUX EXTENDED \\\\\

        @Override
        public long receiveEnergyL(long maxReceive, boolean simulate) {
            if (getNetwork().isValid()) {
                return mHandler.receive(maxReceive, mSide, simulate, getNetwork().getBufferLimiter());
            }
            return 0;
        }

        @Override
        public long extractEnergyL(long maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public long getEnergyStoredL() {
            return mHandler.getBuffer();
        }

        @Override
        public long getMaxEnergyStoredL() {
            return Math.max(mHandler.getBuffer(), mHandler.getLimit());
        }
    }
}
