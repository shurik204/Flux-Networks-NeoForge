package sonar.fluxnetworks.common.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import sonar.fluxnetworks.api.FluxDataComponents;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FluxStorageRecipe extends ShapedRecipe {
    public FluxStorageRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean flag) {
        super(group, category, pattern, result, flag);
    }

    public FluxStorageRecipe(ShapedRecipe recipe) {
        super(recipe.getGroup(), recipe.category(), recipe.pattern, recipe.getResultItem(RegistryAccess.EMPTY));
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack result = super.assemble(input, registries);
        // Find all flux storage items in the input
        List<ItemStack> storageItems = input.items().stream().filter(stack -> !stack.isEmpty() && stack.get(FluxDataComponents.STORED_ENERGY) != null).toList();
        // If none found, bail
        if (storageItems.isEmpty()) {
            return result;
        }
        // Sum the energy of all storage items
        long totalEnergy = storageItems.stream().map(stack -> stack.get(FluxDataComponents.STORED_ENERGY)).reduce(0L, Long::sum);
        // Put the total energy value into the resulting item
        result.set(FluxDataComponents.STORED_ENERGY, totalEnergy);
        // Copy device configuration from the first storage item (if present)
        result.copyFrom(storageItems.getFirst(), FluxDataComponents.FLUX_CONFIG);

        return result;
    }

    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements RecipeSerializer<FluxStorageRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<FluxStorageRecipe> CODEC = RecipeSerializer.SHAPED_RECIPE.codec().xmap(FluxStorageRecipe::new, Function.identity());
        public static final StreamCodec<RegistryFriendlyByteBuf, FluxStorageRecipe> STREAM_CODEC = RecipeSerializer.SHAPED_RECIPE.streamCodec().map(FluxStorageRecipe::new, Function.identity());

        @Override
        public MapCodec<FluxStorageRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FluxStorageRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private Serializer() {}
    }
}
