package sonar.fluxnetworks.register;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.RegisterEvent;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.common.item.*;

public class RegistryItems {
    private static final ResourceLocation FLUX_DUST_KEY = FluxNetworks.location("flux_dust");
    private static final ResourceLocation FLUX_CORE_KEY = FluxNetworks.location("flux_core");
    private static final ResourceLocation FLUX_CONFIGURATOR_KEY = FluxNetworks.location("flux_configurator");
    private static final ResourceLocation ADMIN_CONFIGURATOR_KEY = FluxNetworks.location("admin_configurator");

    public static final DeferredItem<BlockItem> FLUX_BLOCK = holder(RegistryBlocks.FLUX_BLOCK_KEY);
    public static final DeferredItem<FluxDeviceItem> FLUX_PLUG = holder(RegistryBlocks.FLUX_PLUG_KEY);
    public static final DeferredItem<FluxDeviceItem> FLUX_POINT = holder(RegistryBlocks.FLUX_POINT_KEY);
    public static final DeferredItem<FluxDeviceItem> FLUX_CONTROLLER = holder(RegistryBlocks.FLUX_CONTROLLER_KEY);
    public static final DeferredItem<FluxStorageItem> BASIC_FLUX_STORAGE = holder(RegistryBlocks.BASIC_FLUX_STORAGE_KEY);
    public static final DeferredItem<FluxStorageItem> HERCULEAN_FLUX_STORAGE = holder(RegistryBlocks.HERCULEAN_FLUX_STORAGE_KEY);
    public static final DeferredItem<FluxStorageItem> GARGANTUAN_FLUX_STORAGE = holder(RegistryBlocks.GARGANTUAN_FLUX_STORAGE_KEY);
    public static final DeferredItem<FluxDustItem> FLUX_DUST = holder(FLUX_DUST_KEY);
    public static final DeferredItem<Item> FLUX_CORE = holder(FLUX_CORE_KEY);
    public static final DeferredItem<ItemFluxConfigurator> FLUX_CONFIGURATOR = holder(FLUX_CONFIGURATOR_KEY);
    public static final DeferredItem<ItemAdminConfigurator> ADMIN_CONFIGURATOR = holder(ADMIN_CONFIGURATOR_KEY);

    static <T extends Item> DeferredItem<T> holder(ResourceLocation location) {
        return DeferredItem.createItem(location);
    }

    static void register(RegisterEvent.RegisterHelper<Item> helper) {
        Item.Properties normalProps = new Item.Properties().fireResistant();
        Item.Properties toolProps = new Item.Properties().fireResistant().stacksTo(1);

        helper.register(RegistryBlocks.FLUX_BLOCK_KEY, new BlockItem(RegistryBlocks.FLUX_BLOCK.get(), normalProps));
        helper.register(RegistryBlocks.FLUX_PLUG_KEY, new FluxDeviceItem(RegistryBlocks.FLUX_PLUG.get(), normalProps));
        helper.register(RegistryBlocks.FLUX_POINT_KEY, new FluxDeviceItem(RegistryBlocks.FLUX_POINT.get(), normalProps));
        helper.register(RegistryBlocks.FLUX_CONTROLLER_KEY, new FluxDeviceItem(RegistryBlocks.FLUX_CONTROLLER.get(), normalProps));
        helper.register(RegistryBlocks.BASIC_FLUX_STORAGE_KEY, new FluxStorageItem(RegistryBlocks.BASIC_FLUX_STORAGE.get(), normalProps));
        helper.register(RegistryBlocks.HERCULEAN_FLUX_STORAGE_KEY, new FluxStorageItem(RegistryBlocks.HERCULEAN_FLUX_STORAGE.get(), normalProps));
        helper.register(RegistryBlocks.GARGANTUAN_FLUX_STORAGE_KEY, new FluxStorageItem(RegistryBlocks.GARGANTUAN_FLUX_STORAGE.get(), normalProps));

        helper.register(FLUX_DUST_KEY, new FluxDustItem(normalProps));
        helper.register(FLUX_CORE_KEY, new Item(normalProps));

        helper.register(FLUX_CONFIGURATOR_KEY, new ItemFluxConfigurator(toolProps));
        helper.register(ADMIN_CONFIGURATOR_KEY, new ItemAdminConfigurator(toolProps));
    }

    private RegistryItems() {}
}
