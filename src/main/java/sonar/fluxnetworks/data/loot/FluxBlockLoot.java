package sonar.fluxnetworks.data.loot;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import sonar.fluxnetworks.api.FluxDataComponents;
import sonar.fluxnetworks.common.block.FluxDeviceBlock;
import sonar.fluxnetworks.common.device.TileFluxDevice;
import sonar.fluxnetworks.register.RegistryBlocks;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

public class FluxBlockLoot extends BlockLootSubProvider {

    // there are not many registry entries, so use an array
    private final Set<Block> knownBlocks = new ObjectArraySet<>();

    public FluxBlockLoot(HolderLookup.Provider registries) {
        super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Nonnull
    @Override
    public final Iterable<Block> getKnownBlocks() {
        return knownBlocks;
    }

    @Override
    protected final void add(@Nonnull Block blockIn, @Nonnull LootTable.Builder table) {
        super.add(blockIn, table);
        knownBlocks.add(blockIn);
    }

    @Override
    protected void generate() {
        dropSelf(RegistryBlocks.FLUX_BLOCK.get());
        add(RegistryBlocks.FLUX_PLUG.get(), this::createDevice);
        add(RegistryBlocks.FLUX_POINT.get(), this::createDevice);
        add(RegistryBlocks.FLUX_CONTROLLER.get(), this::createDevice);
        add(RegistryBlocks.BASIC_FLUX_STORAGE.get(), this::createDevice);
        add(RegistryBlocks.HERCULEAN_FLUX_STORAGE.get(), this::createDevice);
        add(RegistryBlocks.GARGANTUAN_FLUX_STORAGE.get(), this::createDevice);
    }

    /**
     * Sift out needed NBT from {@link TileFluxDevice#writeCustomTag(CompoundTag, byte)}, and
     * convert them to be readable by {@link FluxDeviceBlock#setPlacedBy(Level, BlockPos, BlockState, LivingEntity,
     * ItemStack)}
     *
     * @param block flux device block
     * @return loot table builder
     */
    @Nonnull
    protected LootTable.Builder createDevice(Block block) {
        if (!(block instanceof FluxDeviceBlock)) {
            throw new IllegalArgumentException();
        }
        CopyComponentsFunction.Builder copyComponent = CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY);
        copyComponent.include(FluxDataComponents.FLUX_CONFIG);
        copyComponent.include(FluxDataComponents.STORED_ENERGY);
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(block)
                        .apply(copyComponent));
        return LootTable.lootTable().withPool(applyExplosionCondition(block, pool));
    }
}
