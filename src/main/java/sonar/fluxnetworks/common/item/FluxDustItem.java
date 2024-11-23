package sonar.fluxnetworks.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import sonar.fluxnetworks.FluxConfig;
import sonar.fluxnetworks.api.FluxTranslate;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class FluxDustItem extends Item {

    public FluxDustItem(@Nonnull Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (FluxConfig.enableFluxRecipe) {
            tooltip.add(FluxTranslate.FLUX_DUST_TOOLTIP.getComponent());
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
