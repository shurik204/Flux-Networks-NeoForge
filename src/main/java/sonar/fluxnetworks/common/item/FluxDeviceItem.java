package sonar.fluxnetworks.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import sonar.fluxnetworks.api.FluxDataComponents;
import sonar.fluxnetworks.api.FluxTranslate;
import sonar.fluxnetworks.api.energy.EnergyType;
import sonar.fluxnetworks.client.ClientCache;
import sonar.fluxnetworks.common.block.FluxStorageBlock;
import sonar.fluxnetworks.common.connection.FluxNetwork;
import sonar.fluxnetworks.common.data.FluxDataComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FluxDeviceItem extends BlockItem {

    public FluxDeviceItem(Block block, Properties props) {
        super(block, props);
    }

    @Override
    public Component getName(ItemStack stack) {
        FluxDataComponent component = stack.get(FluxDataComponents.FLUX_DATA);
        if (component != null) {
            Optional<String> value = component.customName();
            if (value.isPresent()) {
                return Component.literal(value.get());
            }
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        FluxDataComponent component = stack.get(FluxDataComponents.FLUX_DATA);
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

            if (component.buffer().isPresent()) {
                tooltip.add(Component.literal(ChatFormatting.BLUE + FluxTranslate.INTERNAL_BUFFER.get() + ": " +
                        ChatFormatting.RESET + EnergyType.FE.getStorage(component.buffer().get())));
            } else if (component.energy().isPresent()) {
                long energy = component.energy().get();
                Block block = getBlock();
                double percentage;
                if (block instanceof FluxStorageBlock)
                    percentage = Math.min((double) energy / ((FluxStorageBlock) block).getEnergyCapacity(), 1.0);
                else
                    percentage = 0;
                tooltip.add(Component.literal(ChatFormatting.BLUE + FluxTranslate.ENERGY_STORED.get() + ": " +
                        ChatFormatting.RESET + EnergyType.FE.getStorage(energy) + String.format(" (%.1f%%)",
                        percentage * 100)));
            }

        } else {
            super.appendHoverText(stack, context, tooltip, flag);
        }
    }
}
