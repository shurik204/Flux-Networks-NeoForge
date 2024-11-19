package sonar.fluxnetworks.common.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import sonar.fluxnetworks.api.FluxDataComponents;
import sonar.fluxnetworks.common.block.FluxStorageBlock;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

/**
 * save Flux Storage energy when wiping NBT
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ConfigWipeRecipe extends ShapelessRecipe {
    public ConfigWipeRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(group, category, result, ingredients);
    }

    public ConfigWipeRecipe(ShapelessRecipe recipe) {
        // String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients
        super(recipe.getGroup(), recipe.category(), recipe.getResultItem(RegistryAccess.EMPTY), recipe.getIngredients());
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack originalStack = input.items().stream().filter(stack -> !stack.isEmpty() && stack.get(FluxDataComponents.FLUX_CONFIG) != null).findFirst().orElse(null);
        ItemStack output = super.assemble(input, registries);

        if (originalStack != null) {
            if (Block.byItem(output.getItem()) instanceof FluxStorageBlock) {
                output.copyFrom(originalStack, FluxDataComponents.STORED_ENERGY);
            }
            return output;
        }
        return output;
    }

    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements RecipeSerializer<ConfigWipeRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<ConfigWipeRecipe> CODEC = RecipeSerializer.SHAPELESS_RECIPE.codec().xmap(ConfigWipeRecipe::new, Function.identity());
        public static final StreamCodec<RegistryFriendlyByteBuf, ConfigWipeRecipe> STREAM_CODEC = RecipeSerializer.SHAPELESS_RECIPE.streamCodec().map(ConfigWipeRecipe::new, Function.identity());

        @Override
        public MapCodec<ConfigWipeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ConfigWipeRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private Serializer() {}
    }
}
