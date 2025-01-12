package sonar.fluxnetworks.common.block;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import sonar.fluxnetworks.register.RegistryItems;
import sonar.fluxnetworks.common.device.TileFluxDevice;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Defines the block base class for any flux device.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class FluxDeviceBlock extends Block implements EntityBlock {

    public FluxDeviceBlock(Properties props) {
        super(props);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        if (player.getItemInHand(hand).is(RegistryItems.FLUX_CONFIGURATOR.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.getBlockEntity(pos) instanceof TileFluxDevice device) {
            device.onPlayerInteract(player);
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    /**
     * Called by BlockItem after this block has been placed.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof TileFluxDevice device) {
            // doing this client side to prevent network flickering when placing, we send a block update next
            // tick anyway.
            device.applyComponentsFromItemStack(stack);
            if (placer instanceof Player) {
                device.setOwnerUUID(placer.getUUID());
            }
        }
    }

    //TODO use BlockEvent
    /*@Override
    public boolean removedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest,
    FluidState fluid) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof FluxDeviceEntity  d && !d.canPlayerAccess(player)) {
                player.sendStatusMessage(StyleUtils.error(FluxTranslate.REMOVAL_DENIED), true);
                level.markAndNotifyBlock();
                return false;
            }
        }
        return super.removedByPlayer(state, level, pos, player, willHarvest, fluid);
    }*/
}
