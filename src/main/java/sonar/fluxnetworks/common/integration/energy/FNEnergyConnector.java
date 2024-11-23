package sonar.fluxnetworks.common.integration.energy;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import sonar.fluxnetworks.api.FluxCapabilities;
import sonar.fluxnetworks.api.energy.*;
import sonar.fluxnetworks.common.util.FluxUtils;

import javax.annotation.Nonnull;

public class FNEnergyConnector implements IBlockEnergyConnector, IItemEnergyConnector {

    public static final FNEnergyConnector INSTANCE = new FNEnergyConnector();

    private FNEnergyConnector() {
    }

    @Override
    public boolean hasCapability(@Nonnull BlockEntity target, @Nonnull Direction side) {
        return !target.isRemoved() && FluxCapabilities.BLOCK.getCapability(target.getLevel(), target.getBlockPos(), target.getBlockState(), target, side) != null;
    }

    @Override
    public boolean canSendTo(@Nonnull BlockEntity target, @Nonnull Direction side) {
        if (!target.isRemoved()) {
            IFNEnergyStorage storage = FluxUtils.get(target, FluxCapabilities.BLOCK, side);
            return storage != null && storage.canReceive();
        }
        return false;
    }

    @Override
    public boolean canReceiveFrom(@Nonnull BlockEntity target, @Nonnull Direction side) {
        if (!target.isRemoved()) {
            IFNEnergyStorage storage = FluxUtils.get(target, FluxCapabilities.BLOCK, side);
            return storage != null && storage.canExtract();
        }
        return false;
    }

    @Override
    public long sendTo(long amount, @Nonnull BlockEntity target, @Nonnull Direction side, boolean simulate) {
        IFNEnergyStorage storage = FluxUtils.get(target, FluxCapabilities.BLOCK, side);
        return storage == null ? 0 : storage.receiveEnergyL(amount, simulate);
    }

    @Override
    public long receiveFrom(long amount, @Nonnull BlockEntity target, @Nonnull Direction side, boolean simulate) {
        IFNEnergyStorage storage = FluxUtils.get(target, FluxCapabilities.BLOCK, side);
        return storage == null ? 0 : storage.extractEnergyL(amount, simulate);
    }

    @Override
    public boolean hasCapability(@Nonnull ItemStack stack) {
        return !stack.isEmpty() && stack.getCapability(FluxCapabilities.ITEM) != null;
    }

    @Override
    public boolean canSendTo(@Nonnull ItemStack stack) {
        if (!stack.isEmpty()) {
            IFNEnergyStorage storage = FluxUtils.get(stack, FluxCapabilities.ITEM);
            return storage != null && storage.canReceive();
        }
        return false;
    }

    @Override
    public boolean canReceiveFrom(@Nonnull ItemStack stack) {
        if (!stack.isEmpty()) {
            IFNEnergyStorage storage = FluxUtils.get(stack, FluxCapabilities.ITEM);
            return storage != null && storage.canExtract();
        }
        return false;
    }

    @Override
    public long sendTo(long amount, @Nonnull ItemStack stack, boolean simulate) {
        IFNEnergyStorage storage = FluxUtils.get(stack, FluxCapabilities.ITEM);
        return storage == null ? 0 : storage.receiveEnergyL(amount, simulate);
    }

    @Override
    public long receiveFrom(long amount, @Nonnull ItemStack stack, boolean simulate) {
        IFNEnergyStorage storage = FluxUtils.get(stack, FluxCapabilities.ITEM);
        return storage == null ? 0 : storage.extractEnergyL(amount, simulate);
    }
}
