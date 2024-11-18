package sonar.fluxnetworks.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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
import sonar.fluxnetworks.api.FluxConstants;
import sonar.fluxnetworks.api.FluxDataComponents;
import sonar.fluxnetworks.client.ClientCache;
import sonar.fluxnetworks.client.gui.basic.GuiFluxCore;
import sonar.fluxnetworks.common.block.FluxStorageBlock;
import sonar.fluxnetworks.common.data.FluxDataComponent;
import sonar.fluxnetworks.common.util.FluxUtils;

import javax.annotation.Nonnull;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class FluxStorageItemRenderer extends BlockEntityWithoutLevelRenderer {

    public FluxStorageItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(@Nonnull ItemStack stack, @Nonnull ItemDisplayContext transformType,
                             @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                             int packedLight, int packedOverlay) {
        int color; // 0xRRGGBB
        long energy;
        Optional<Integer> rootTag = stack.get(FluxDataComponents.FLUX_COLOR);
        if (rootTag != null) {
            if (rootTag.isEmpty()) {
                // GUI display
                Screen screen = Minecraft.getInstance().screen;
                if (screen instanceof GuiFluxCore gui) {
                    color = gui.getNetwork().getNetworkColor();
                } else {
                    color = FluxConstants.INVALID_NETWORK_COLOR;
                }
                FluxDataComponent data = stack.get(FluxDataComponents.FLUX_DATA);
                if (data != null) {
                    energy = data.getEnergy();
                } else {
                    energy = 0;
                }
            } else {
                Optional<Integer> colorData = stack.get(FluxDataComponents.FLUX_COLOR);
                FluxDataComponent data = stack.get(FluxDataComponents.FLUX_DATA);
                if (colorData != null) {
                    if (colorData.isPresent()) {
                        // TheOneProbe
                        color = colorData.get();
                    } else {
                        if (data != null) {
                            // ItemStack inventory
                            color = ClientCache.getNetwork(data.networkId()).getNetworkColor();
                        } else {
                            color = FluxConstants.INVALID_NETWORK_COLOR;
                        }
                    }
                    energy = data.getEnergy();
                } else {
                    color = FluxConstants.INVALID_NETWORK_COLOR;
                    energy = 0;
                }
            }
        } else {
            color = FluxConstants.INVALID_NETWORK_COLOR;
            energy = 0;
        }

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
