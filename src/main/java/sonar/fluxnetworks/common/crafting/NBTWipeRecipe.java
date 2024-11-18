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
import sonar.fluxnetworks.common.data.FluxDataComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

/**
 * save Flux Storage energy when wiping NBT
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NBTWipeRecipe extends ShapelessRecipe {
    public NBTWipeRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(group, category, result, ingredients);
    }

    public NBTWipeRecipe(ShapelessRecipe recipe) {
        // String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients
        super(recipe.getGroup(), recipe.category(), recipe.getResultItem(RegistryAccess.EMPTY), recipe.getIngredients());
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack originalStack = input.items().stream().filter(stack -> !stack.isEmpty() && stack.get(FluxDataComponents.FLUX_DATA) != null).findFirst().orElse(null);
        ItemStack output = super.assemble(input, registries);

        if (originalStack != null) {
            if (Block.byItem(output.getItem()) instanceof FluxStorageBlock) {
                FluxDataComponent data = originalStack.get(FluxDataComponents.FLUX_DATA);
                long energy = 0;
                if (data != null) {
                    energy = data.getEnergy();
                }
                if (energy != 0) {
                    output.set(FluxDataComponents.FLUX_DATA, FluxDataComponent.EMPTY.withEnergy(energy));
                }
            }
            return output;
        }
        return output;
    }

    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements RecipeSerializer<NBTWipeRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<NBTWipeRecipe> CODEC = RecipeSerializer.SHAPELESS_RECIPE.codec().xmap(NBTWipeRecipe::new, Function.identity());
        public static final StreamCodec<RegistryFriendlyByteBuf, NBTWipeRecipe> STREAM_CODEC = RecipeSerializer.SHAPELESS_RECIPE.streamCodec().map(NBTWipeRecipe::new, Function.identity());

        @Override
        public MapCodec<NBTWipeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, NBTWipeRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private Serializer() {}
    }
}
