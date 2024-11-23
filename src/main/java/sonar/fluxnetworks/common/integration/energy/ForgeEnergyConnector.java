package sonar.fluxnetworks.common.integration.energy;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.energy.IEnergyStorage;
import sonar.fluxnetworks.api.energy.IBlockEnergyConnector;
import sonar.fluxnetworks.api.energy.IItemEnergyConnector;
import sonar.fluxnetworks.common.util.FluxUtils;

import javax.annotation.Nonnull;

public class ForgeEnergyConnector implements IBlockEnergyConnector, IItemEnergyConnector {

    public static final BlockCapability<IEnergyStorage, Direction> BLOCK_CAP = Capabilities.EnergyStorage.BLOCK;
    public static final ItemCapability<IEnergyStorage, Void> ITEM_CAP = Capabilities.EnergyStorage.ITEM;
    public static final ForgeEnergyConnector INSTANCE = new ForgeEnergyConnector();

    private ForgeEnergyConnector() {
    }

    @Override
    public boolean hasCapability(@Nonnull BlockEntity target, @Nonnull Direction side) {
        return !target.isRemoved() && FluxUtils.get(target, BLOCK_CAP, side) != null;
    }

    @Override
    public boolean canSendTo(@Nonnull BlockEntity target, @Nonnull Direction side) {
        if (!target.isRemoved()) {
            IEnergyStorage storage = FluxUtils.get(target, BLOCK_CAP, side);
            return storage != null && storage.canReceive();
        }
        return false;
    }

    @Override
    public boolean canReceiveFrom(@Nonnull BlockEntity target, @Nonnull Direction side) {
        if (!target.isRemoved()) {
            IEnergyStorage storage = FluxUtils.get(target, BLOCK_CAP, side);
            return storage != null && storage.canExtract();
        }
        return false;
    }

    @Override
    public long sendTo(long amount, @Nonnull BlockEntity target, @Nonnull Direction side, boolean simulate) {
        IEnergyStorage storage = FluxUtils.get(target, BLOCK_CAP, side);
        return storage == null ? 0 : storage.receiveEnergy((int) Math.min(amount, Integer.MAX_VALUE), simulate);
    }

    @Override
    public long receiveFrom(long amount, @Nonnull BlockEntity target, @Nonnull Direction side, boolean simulate) {
        IEnergyStorage storage = FluxUtils.get(target, BLOCK_CAP, side);
        return storage == null ? 0 : storage.extractEnergy((int) Math.min(amount, Integer.MAX_VALUE), simulate);
    }

    @Override
    public boolean hasCapability(@Nonnull ItemStack stack) {
        return !stack.isEmpty() && stack.getCapability(ITEM_CAP) != null;
    }

    @Override
    public boolean canSendTo(@Nonnull ItemStack stack) {
        if (!stack.isEmpty()) {
            IEnergyStorage storage = FluxUtils.get(stack, ITEM_CAP);
            return storage != null && storage.canReceive();
        }
        return false;
    }

    @Override
    public boolean canReceiveFrom(@Nonnull ItemStack stack) {
        if (!stack.isEmpty()) {
            IEnergyStorage storage = FluxUtils.get(stack, ITEM_CAP);
            return storage != null && storage.canExtract();
        }
        return false;
    }

    @Override
    public long sendTo(long amount, @Nonnull ItemStack stack, boolean simulate) {
        IEnergyStorage storage = FluxUtils.get(stack, ITEM_CAP);
        return storage == null ? 0 : storage.receiveEnergy((int) Math.min(amount, Integer.MAX_VALUE), simulate);
    }

    @Override
    public long receiveFrom(long amount, @Nonnull ItemStack stack, boolean simulate) {
        IEnergyStorage storage = FluxUtils.get(stack, ITEM_CAP);
        return storage == null ? 0 : storage.extractEnergy((int) Math.min(amount, Integer.MAX_VALUE), simulate);
    }
}
