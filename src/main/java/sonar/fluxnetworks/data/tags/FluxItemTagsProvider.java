package sonar.fluxnetworks.data.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.register.RegistryBlocks;
import sonar.fluxnetworks.register.RegistryTags;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class FluxItemTagsProvider extends ItemTagsProvider {
    public FluxItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> blockTagProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTagProvider, FluxNetworks.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@Nonnull HolderLookup.Provider provider) {
        tag(RegistryTags.FLUX_STORAGE)
                .add(RegistryBlocks.BASIC_FLUX_STORAGE.get().asItem())
                .add(RegistryBlocks.HERCULEAN_FLUX_STORAGE.get().asItem())
                .add(RegistryBlocks.GARGANTUAN_FLUX_STORAGE.get().asItem());
    }
}
