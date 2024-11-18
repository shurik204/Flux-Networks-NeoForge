package sonar.fluxnetworks.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import sonar.fluxnetworks.api.FluxConstants;
import sonar.fluxnetworks.api.FluxDataComponents;
import sonar.fluxnetworks.api.FluxTranslate;
import sonar.fluxnetworks.api.device.IFluxProvider;
import sonar.fluxnetworks.api.energy.EnergyType;
import sonar.fluxnetworks.api.misc.FluxConfigurationType;
import sonar.fluxnetworks.client.ClientCache;
import sonar.fluxnetworks.common.connection.FluxMenu;
import sonar.fluxnetworks.common.connection.FluxNetwork;
import sonar.fluxnetworks.common.data.FluxDataComponent;
import sonar.fluxnetworks.common.device.TileFluxDevice;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemFluxConfigurator extends Item {

    public ItemFluxConfigurator(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof TileFluxDevice device) {
            if (!device.canPlayerAccess(player)) {
                player.displayClientMessage(FluxTranslate.ACCESS_DENIED, true);
                return InteractionResult.FAIL;
            }
            if (player.isShiftKeyDown()) {
                FluxDataComponent component = stack.get(FluxDataComponents.FLUX_CONFIG);
                if (component != null) {
                    for (FluxConfigurationType type : FluxConfigurationType.VALUES) {
                        type.copy(player, component.asNbt(), device);
                    }
                    player.displayClientMessage(FluxTranslate.CONFIG_COPIED, false);
                }
            } else {
                FluxDataComponent component = stack.get(FluxDataComponents.FLUX_CONFIG);
                if (component != null) {
                    for (FluxConfigurationType type : FluxConfigurationType.VALUES) {
                        type.paste(player, component.asNbt(), device);
                    }
                    player.displayClientMessage(FluxTranslate.CONFIG_PASTED, false);
                }
            }
            return InteractionResult.SUCCESS;
        }
        player.openMenu(new Provider(stack), buf -> buf.writeBoolean(true));
        return InteractionResult.SUCCESS;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player,
                                                  @Nonnull InteractionHand hand) {
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        FluxDataComponent component = stack.get(FluxDataComponents.FLUX_CONFIG);
        if (component != null) {
            final FluxNetwork network = ClientCache.getNetwork(component.networkId());
            if (network.isValid()) {
                tooltip.add(Component.literal(ChatFormatting.BLUE + FluxTranslate.NETWORK_FULL_NAME.get() + ": " +
                        ChatFormatting.RESET + network.getNetworkName()));
            }

            if (component.limit().isPresent()) {
                tooltip.add(Component.literal(ChatFormatting.BLUE + FluxTranslate.TRANSFER_LIMIT.get() + ": " +
                        ChatFormatting.RESET + EnergyType.FE.getStorage(component.getLimit())));
            }

            if (component.priority().isPresent()) {
                tooltip.add(Component.literal(ChatFormatting.BLUE + FluxTranslate.PRIORITY.get() + ": " +
                        ChatFormatting.RESET + component.getPriority()));
            }
        }
    }

    public static class Provider implements IFluxProvider {

        public final ItemStack mStack;

        public Provider(@Nonnull ItemStack stack) {
            mStack = stack;
        }

        @Override
        public int getNetworkID() {
            FluxDataComponent config = mStack.get(FluxDataComponents.FLUX_CONFIG);
            return config != null ? config.networkId() : FluxConstants.INVALID_NETWORK_ID;
        }

        @Override
        public void onPlayerOpened(@Nonnull Player player) {
        }

        @Override
        public void onPlayerClosed(@Nonnull Player player) {
        }

        @Nullable
        @Override
        public FluxMenu createMenu(int containerId, @Nonnull Inventory inventory, @Nonnull Player player) {
            return new FluxMenu(containerId, inventory, this);
        }
    }
}
