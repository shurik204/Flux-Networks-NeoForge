package sonar.fluxnetworks.register;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import sonar.fluxnetworks.FluxNetworks;

public class RegistryTags {
    public static final TagKey<Item> FLUX_STORAGE = ItemTags.create(FluxNetworks.location("storage"));

    public static final TagKey<Block> FLUX_RECIPE_BASE_BLOCK = BlockTags.create(FluxNetworks.location("flux_recipe_base"));
    public static final TagKey<Block> FLUX_RECIPE_CRUSHER_BLOCK = BlockTags.create(FluxNetworks.location("flux_recipe_crusher"));
}
