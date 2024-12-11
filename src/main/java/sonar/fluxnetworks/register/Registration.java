package sonar.fluxnetworks.register;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.common.integration.TOPIntegration;
import sonar.fluxnetworks.common.level.FluxChunkLoading;
import sonar.fluxnetworks.common.util.EnergyUtils;
import sonar.fluxnetworks.data.loot.FluxLootTableProvider;
import sonar.fluxnetworks.data.tags.FluxBlockTagsProvider;
import sonar.fluxnetworks.data.tags.FluxItemTagsProvider;

import javax.annotation.Nonnull;

@EventBusSubscriber(modid = FluxNetworks.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Registration {

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        Channel.get().setC2SMessageHandler((index, payload, context) -> Messages.msg(
                index, payload, () -> (ServerPlayer) context.player(),
                context.listener().getMainThreadEventLoop()
        ));
        EnergyUtils.register();
    }

    @SubscribeEvent
    public static void registerPayloadHandler(RegisterPayloadHandlersEvent event) {
        event.registrar(Channel.PROTOCOL).playBidirectional(
                Channel.Message.TYPE,
                Channel.Message.CODEC,
                Channel.Message::handle
        );
    }

    @SubscribeEvent
    public static void enqueueIMC(InterModEnqueueEvent event) {
        if (ModList.get().isLoaded("carryon")) {
            InterModComms.sendTo("carryon", "blacklistBlock", () -> FluxNetworks.MODID + ":*");
        }
        if (ModList.get().isLoaded("theoneprobe")) {
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", TOPIntegration::new);
        }
    }

    @SubscribeEvent
    public static void registerTicketControllers(RegisterTicketControllersEvent event) {
        event.register(FluxChunkLoading.CONTROLLER);
    }

    @SubscribeEvent
    public static void gatherData(@Nonnull GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        if (event.includeServer()) {
            FluxBlockTagsProvider blockTags = new FluxBlockTagsProvider(packOutput, event.getLookupProvider(), event.getExistingFileHelper());
            generator.addProvider(true, new FluxLootTableProvider(packOutput, event.getLookupProvider()));
            generator.addProvider(true, blockTags);
            generator.addProvider(true, new FluxItemTagsProvider(packOutput, event.getLookupProvider(), blockTags.contentsGetter(), event.getExistingFileHelper()));
        }
    }

    @SubscribeEvent
    public static void register(@Nonnull RegisterEvent event) {
        event.register(BuiltInRegistries.BLOCK.key(), RegistryBlocks::register);
        event.register(BuiltInRegistries.ITEM.key(), RegistryItems::register);
        event.register(BuiltInRegistries.BLOCK_ENTITY_TYPE.key(), RegistryBlockEntityTypes::register);
        event.register(BuiltInRegistries.MENU.key(), RegistryMenuTypes::register);
        event.register(BuiltInRegistries.RECIPE_SERIALIZER.key(), RegistryRecipes::register);
        event.register(BuiltInRegistries.SOUND_EVENT.key(), RegistrySounds::register);
        event.register(BuiltInRegistries.CREATIVE_MODE_TAB.key(), RegistryCreativeModeTabs::register);
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        RegistryBlockEntityTypes.registerBlockCapabilities(event);
    }
}