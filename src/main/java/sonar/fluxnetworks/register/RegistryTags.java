package sonar.fluxnetworks.register;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import sonar.fluxnetworks.FluxNetworks;

public class RegistryTags {
    public static final TagKey<Item> FLUX_STORAGE = ItemTags.create(FluxNetworks.location("storage"));
}
