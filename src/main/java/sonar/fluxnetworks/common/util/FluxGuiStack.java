package sonar.fluxnetworks.common.util;

import net.minecraft.world.item.ItemStack;
import sonar.fluxnetworks.api.FluxDataComponents;
import sonar.fluxnetworks.register.RegistryBlocks;

import java.util.Optional;

public class FluxGuiStack {

    public static final ItemStack FLUX_PLUG = new ItemStack(RegistryBlocks.FLUX_PLUG.get());
    public static final ItemStack FLUX_POINT = new ItemStack(RegistryBlocks.FLUX_POINT.get());
    public static final ItemStack FLUX_CONTROLLER = new ItemStack(RegistryBlocks.FLUX_CONTROLLER.get());

    public static final ItemStack BASIC_STORAGE = new ItemStack(RegistryBlocks.BASIC_FLUX_STORAGE.get());
    public static final ItemStack HERCULEAN_STORAGE = new ItemStack(RegistryBlocks.HERCULEAN_FLUX_STORAGE.get());
    public static final ItemStack GARGANTUAN_STORAGE = new ItemStack(RegistryBlocks.GARGANTUAN_FLUX_STORAGE.get());

    static {
        FLUX_PLUG.set(FluxDataComponents.FLUX_COLOR, Optional.empty());
        FLUX_POINT.set(FluxDataComponents.FLUX_COLOR, Optional.empty());
        FLUX_CONTROLLER.set(FluxDataComponents.FLUX_COLOR, Optional.empty());
    }
}
