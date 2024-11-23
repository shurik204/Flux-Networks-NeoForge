package sonar.fluxnetworks.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import sonar.fluxnetworks.common.device.TileFluxStorage;

import javax.annotation.Nonnull;

public class FluxStorageEntityRenderer implements BlockEntityRenderer<TileFluxStorage> {

    private static final FluxStorageEntityRenderer INSTANCE = new FluxStorageEntityRenderer();

    public static final BlockEntityRendererProvider<TileFluxStorage> PROVIDER = INSTANCE::onContextChanged;

    private static final float START = 2.0f / 16.0f;
    private static final float END = 14.0f / 16.0f;
    private static final float OFFSET = 1.0f / 16.0f;
    private static final float WIDTH = 12.0f / 16.0f;
    private static final float HEIGHT = 13.0f / 16.0f;

    private static final int ALPHA = 150;

    private FluxStorageEntityRenderer() {
    }

    @Nonnull
    private FluxStorageEntityRenderer onContextChanged(@Nonnull BlockEntityRendererProvider.Context context) {
        return this;
    }

    @Override
    public void render(@Nonnull TileFluxStorage entity, float partialTick, @Nonnull PoseStack poseStack,
                       @Nonnull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        render(poseStack, bufferSource.getBuffer(FluxStorageRenderType.getType()), entity.mClientColor,
                packedOverlay, entity.getTransferBuffer(), entity.getMaxTransferLimit());
    }

    static void render(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer consumer, int color,
                       int overlay, long energy, long capacity) {
        if (energy <= 0 || capacity <= 0) {
            return;
        }
        float renderHeight = Math.min(HEIGHT * energy / capacity, HEIGHT);

        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;

        renderSide(poseStack, consumer, Direction.NORTH, START, OFFSET, END, WIDTH, renderHeight, r, g, b,
                overlay, -1);
        renderSide(poseStack, consumer, Direction.SOUTH, START, OFFSET, END, WIDTH, renderHeight, r, g, b,
                overlay, -1);
        renderSide(poseStack, consumer, Direction.EAST, START, OFFSET, END, WIDTH, renderHeight, r, g, b,
                overlay, -1);
        renderSide(poseStack, consumer, Direction.WEST, START, OFFSET, END, WIDTH, renderHeight, r, g, b,
                overlay, -1);
        if (renderHeight < HEIGHT) {
            renderSide(poseStack, consumer, Direction.UP, OFFSET, START + renderHeight, OFFSET,
                    END, -END, r, g, b, overlay, 1);
        }
    }

    private static void renderSide(@Nonnull PoseStack stack, @Nonnull VertexConsumer consumer, @Nonnull Direction dir,
                                   float x, float y, float z, float width, float height,
                                   int r, int g, int b, int overlay, int normalY) {
        stack.pushPose();
        stack.translate(0.5, 0.5, 0.5);
        stack.mulPose(dir.getRotation());
        stack.translate(-0.5, -0.5, -0.5);
        Matrix4f pose = stack.last().pose();
        PoseStack.Pose normal = stack.last();
        consumer.addVertex(pose, x, y, z - height).setColor(r, g, b, ALPHA)
                .setUv(x, z - height).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(normal, 0, normalY, 0);
        consumer.addVertex(pose, x + width, y, z - height).setColor(r, g, b, ALPHA)
                .setUv(x + width, z - height).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(normal, 0, normalY, 0);
        consumer.addVertex(pose, x + width, y, z).setColor(r, g, b, ALPHA)
                .setUv(x + width, z).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(normal, 0, normalY, 0);
        consumer.addVertex(pose, x, y, z).setColor(r, g, b, ALPHA)
                .setUv(x, z).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(normal, 0, normalY, 0);
        stack.popPose();
    }
}
