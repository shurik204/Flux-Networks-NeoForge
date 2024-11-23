package sonar.fluxnetworks.register;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.api.FluxConstants;
import sonar.fluxnetworks.common.connection.FluxMenu;
import sonar.fluxnetworks.common.device.TileFluxDevice;
import sonar.fluxnetworks.common.item.ItemAdminConfigurator;
import sonar.fluxnetworks.common.item.ItemFluxConfigurator;

/**
 * ContainerType has the function to create container on client side<br>
 * Register the create container function that will be opened on client side from the packet that from the server
 */
public class RegistryMenuTypes {
    public static final ResourceLocation FLUX_MENU_KEY = FluxNetworks.location("flux_menu");
    public static final DeferredHolder<MenuType<?>, MenuType<FluxMenu>> FLUX_MENU = DeferredHolder.create(BuiltInRegistries.MENU.key(), FLUX_MENU_KEY);

    static void register(RegisterEvent.RegisterHelper<MenuType<?>> helper) {
        helper.register(FLUX_MENU_KEY, IMenuTypeExtension.create((containerId, inventory, buffer) -> {
            // check if it's tile entity
            if (buffer.readBoolean()) {
                BlockPos pos = buffer.readBlockPos();
                if (inventory.player.level().getBlockEntity(pos) instanceof TileFluxDevice device) {
                    CompoundTag tag = buffer.readNbt();
                    if (tag != null) {
                        device.readCustomTag(tag, FluxConstants.NBT_TILE_UPDATE);
                    }
                    return new FluxMenu(containerId, inventory, device);
                }
            } else {
                ItemStack stack = inventory.player.getMainHandItem();
                if (stack.getItem() == RegistryItems.FLUX_CONFIGURATOR.get()) {
                    return new FluxMenu(containerId, inventory, new ItemFluxConfigurator.Provider(stack));
                }
            }
            return new FluxMenu(containerId, inventory, new ItemAdminConfigurator.Provider());
        }));
    }

    private RegistryMenuTypes() {}
}
