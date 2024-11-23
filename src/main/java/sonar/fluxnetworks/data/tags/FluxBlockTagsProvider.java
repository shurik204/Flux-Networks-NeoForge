package sonar.fluxnetworks.data.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.register.RegistryBlocks;
import sonar.fluxnetworks.register.RegistryTags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class FluxBlockTagsProvider extends BlockTagsProvider {

    public FluxBlockTagsProvider(PackOutput output,
                                 CompletableFuture<HolderLookup.Provider> lookupProvider,
                                 @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, FluxNetworks.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@Nonnull HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(RegistryBlocks.FLUX_BLOCK.get())
                .add(RegistryBlocks.FLUX_PLUG.get())
                .add(RegistryBlocks.FLUX_POINT.get())
                .add(RegistryBlocks.FLUX_CONTROLLER.get())
                .add(RegistryBlocks.BASIC_FLUX_STORAGE.get())
                .add(RegistryBlocks.HERCULEAN_FLUX_STORAGE.get())
                .add(RegistryBlocks.GARGANTUAN_FLUX_STORAGE.get());

        tag(RegistryTags.FLUX_RECIPE_BASE_BLOCK)
                .add(Blocks.BEDROCK)
                .add(RegistryBlocks.FLUX_BLOCK.get());

        tag(RegistryTags.FLUX_RECIPE_CRUSHER_BLOCK)
                .add(Blocks.OBSIDIAN);
    }
}
