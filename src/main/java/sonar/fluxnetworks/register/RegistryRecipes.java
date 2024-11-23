package sonar.fluxnetworks.register;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.common.crafting.FluxStorageRecipe;
import sonar.fluxnetworks.common.crafting.ConfigWipeRecipe;

public class RegistryRecipes {
    public static final ResourceLocation FLUX_STORAGE_RECIPE_KEY = FluxNetworks.location("flux_storage_recipe");
    public static final ResourceLocation CONFIG_WIPE_RECIPE_KEY = FluxNetworks.location("config_wipe_recipe");

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FluxStorageRecipe>> FLUX_STORAGE_RECIPE = DeferredHolder.create(BuiltInRegistries.RECIPE_SERIALIZER.key(), FLUX_STORAGE_RECIPE_KEY);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ConfigWipeRecipe>> CONFIG_WIPE_RECIPE = DeferredHolder.create(BuiltInRegistries.RECIPE_SERIALIZER.key(), CONFIG_WIPE_RECIPE_KEY);

    static void register(RegisterEvent.RegisterHelper<RecipeSerializer<?>> helper) {
        helper.register(FLUX_STORAGE_RECIPE_KEY, FluxStorageRecipe.Serializer.INSTANCE);
        helper.register(CONFIG_WIPE_RECIPE_KEY, ConfigWipeRecipe.Serializer.INSTANCE);
    }

    private RegistryRecipes() {}
}
