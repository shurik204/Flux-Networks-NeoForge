package sonar.fluxnetworks.register;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.RegisterEvent;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.common.block.FluxControllerBlock;
import sonar.fluxnetworks.common.block.FluxPlugBlock;
import sonar.fluxnetworks.common.block.FluxPointBlock;
import sonar.fluxnetworks.common.block.FluxStorageBlock;

public class RegistryBlocks {
    public static final ResourceLocation FLUX_BLOCK_KEY = FluxNetworks.location("flux_block");
    public static final ResourceLocation FLUX_PLUG_KEY = FluxNetworks.location("flux_plug");
    public static final ResourceLocation FLUX_POINT_KEY = FluxNetworks.location("flux_point");
    public static final ResourceLocation FLUX_CONTROLLER_KEY = FluxNetworks.location("flux_controller");
    public static final ResourceLocation BASIC_FLUX_STORAGE_KEY = FluxNetworks.location("basic_flux_storage");
    public static final ResourceLocation HERCULEAN_FLUX_STORAGE_KEY = FluxNetworks.location("herculean_flux_storage");
    public static final ResourceLocation GARGANTUAN_FLUX_STORAGE_KEY = FluxNetworks.location("gargantuan_flux_storage");

    public static final DeferredBlock<Block> FLUX_BLOCK = holder(FLUX_BLOCK_KEY);
    public static final DeferredBlock<FluxPlugBlock> FLUX_PLUG = holder(FLUX_PLUG_KEY);
    public static final DeferredBlock<FluxPointBlock> FLUX_POINT = holder(FLUX_POINT_KEY);
    public static final DeferredBlock<FluxControllerBlock> FLUX_CONTROLLER = holder(FLUX_CONTROLLER_KEY);
    public static final DeferredBlock<FluxStorageBlock.Basic> BASIC_FLUX_STORAGE = holder(BASIC_FLUX_STORAGE_KEY);
    public static final DeferredBlock<FluxStorageBlock.Herculean> HERCULEAN_FLUX_STORAGE = holder(HERCULEAN_FLUX_STORAGE_KEY);
    public static final DeferredBlock<FluxStorageBlock.Gargantuan> GARGANTUAN_FLUX_STORAGE = holder(GARGANTUAN_FLUX_STORAGE_KEY);

    static <T extends Block> DeferredBlock<T> holder(ResourceLocation location) {
        return DeferredBlock.createBlock(location);
    }

    static void register(RegisterEvent.RegisterHelper<Block> helper) {
        BlockBehaviour.Properties normalProps = BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL)
                .strength(1.0F, 1000F);
        BlockBehaviour.Properties deviceProps = BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL)
                .strength(1.0F, 1000F).noOcclusion();

        helper.register(FLUX_BLOCK_KEY, new Block(normalProps));
        helper.register(FLUX_PLUG_KEY, new FluxPlugBlock(deviceProps));
        helper.register(FLUX_POINT_KEY, new FluxPointBlock(deviceProps));
        helper.register(FLUX_CONTROLLER_KEY, new FluxControllerBlock(deviceProps));
        helper.register(BASIC_FLUX_STORAGE_KEY, new FluxStorageBlock.Basic(deviceProps));
        helper.register(HERCULEAN_FLUX_STORAGE_KEY, new FluxStorageBlock.Herculean(deviceProps));
        helper.register(GARGANTUAN_FLUX_STORAGE_KEY, new FluxStorageBlock.Gargantuan(deviceProps));
    }

    private RegistryBlocks() {}
}
