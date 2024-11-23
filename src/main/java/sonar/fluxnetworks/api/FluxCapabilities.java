package sonar.fluxnetworks.api;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.*;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

public final class FluxCapabilities {
    /**
     * <p>
     *   Only use this capability if your mod can send/receive energy at a rate greater than {@code Integer.MAX_VALUE}.
     * </p>
     * <p>
     *   Functions the same as {@link net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage} but allows {@code Long.MAX_VALUE}.
     *   Flux Plug and Flux Point use this capability.
     * </p>
     */
    public static final BlockCapability<IFNEnergyStorage, Direction> BLOCK = BlockCapability.createSided(FluxNetworks.location("fn_energy"), IFNEnergyStorage.class);

    /**
     * <p>
     *   Only use this capability if your mod can send/receive energy at a rate greater than {@code Integer.MAX_VALUE}.
     * </p>
     * <p>
     *   Functions the same as {@link net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage} but allows {@code Long.MAX_VALUE}.
     * </p>
     */
    public static final ItemCapability<IFNEnergyStorage, Void> ITEM = ItemCapability.createVoid(FluxNetworks.location("fn_energy"), IFNEnergyStorage.class);

    /**
     * <p>
     *   Only use this capability if your mod can send/receive energy at a rate greater than {@code Integer.MAX_VALUE}.
     * </p>
     * <p>
     *   Functions the same as {@link net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage} but allows {@code Long.MAX_VALUE}.
     * </p>
     */
    public static final EntityCapability<IFNEnergyStorage, Direction> ENTITY = EntityCapability.createSided(FluxNetworks.location("fn_energy"), IFNEnergyStorage.class);

    private FluxCapabilities() {}
}
