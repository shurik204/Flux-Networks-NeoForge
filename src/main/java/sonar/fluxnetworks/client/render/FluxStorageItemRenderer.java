package sonar.fluxnetworks.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import sonar.fluxnetworks.api.FluxDataComponents;
import sonar.fluxnetworks.client.FluxColorHandler;
import sonar.fluxnetworks.common.block.FluxStorageBlock;
import sonar.fluxnetworks.common.util.FluxUtils;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class FluxStorageItemRenderer extends BlockEntityWithoutLevelRenderer {

    public FluxStorageItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(@Nonnull ItemStack stack, @Nonnull ItemDisplayContext transformType,
                             @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                             int packedLight, int packedOverlay) {
        int color = FluxColorHandler.getColorForItem(stack); // 0xRRGGBB
        Long storedEnergy = stack.get(FluxDataComponents.STORED_ENERGY);
        long energy = storedEnergy != null ? storedEnergy : 0;

        FluxStorageBlock block = (FluxStorageBlock) Block.byItem(stack.getItem());
        BlockState renderState = block.defaultBlockState();

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(renderState);

        float r = FluxUtils.getRed(color), g = FluxUtils.getGreen(color), b = FluxUtils.getBlue(color);
        dispatcher.getModelRenderer()
                .renderModel(poseStack.last(), bufferSource.getBuffer(Sheets.cutoutBlockSheet()),
                        renderState, model, r, g, b, packedLight, packedOverlay, ModelData.EMPTY, null);
        FluxStorageEntityRenderer.render(poseStack, bufferSource.getBuffer(FluxStorageRenderType.getType()),
                color, packedOverlay, energy, block.getEnergyCapacity());
    }
}
