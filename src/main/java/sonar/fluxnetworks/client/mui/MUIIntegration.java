package sonar.fluxnetworks.client.mui;

import icyllis.modernui.ModernUI;
import icyllis.modernui.core.Core;
import icyllis.modernui.mc.ExtendedGuiGraphics;
import icyllis.modernui.mc.neoforge.MenuScreenFactory;
import icyllis.modernui.mc.neoforge.MuiForgeApi;
import icyllis.modernui.text.SpannableString;
import icyllis.modernui.text.Spanned;
import icyllis.modernui.text.style.ForegroundColorSpan;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.widget.Toast;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.joml.Quaternionf;
import sonar.fluxnetworks.FluxConfig;
import sonar.fluxnetworks.api.FluxTranslate;
import sonar.fluxnetworks.common.connection.FluxMenu;
import sonar.fluxnetworks.common.device.TileFluxDevice;

import javax.annotation.Nonnull;

public class MUIIntegration {

    public static void showToastError(@Nonnull FluxTranslate translate) {
        SpannableString text = new SpannableString(translate.get());
        text.setSpan(new ForegroundColorSpan(0xFFCF1515), 0, text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (Core.isOnUiThread()) {
            Toast.makeText(ModernUI.getInstance(), text, Toast.LENGTH_SHORT).show();
        } else {
            MuiForgeApi.postToUiThread(() -> Toast.makeText(ModernUI.getInstance(), text, Toast.LENGTH_SHORT).show());
        }
    }

    public static void drawBackgroundAndFrame(@Nonnull GuiGraphics gr,
                                              float width, float height,
                                              int networkRGB, float alpha) {
        float cx = width / 2f;
        float cy = height / 2f + 5;
        var exGr = new ExtendedGuiGraphics(gr);
        exGr.setColor((int) (0xB3 * alpha) << 24);
        exGr.fillRoundRect(cx - 85, cy - 85, cx + 85, cy + 85,
                8);
        exGr.setColor((int) (0xFF * alpha) << 24 | networkRGB);
        exGr.setStrokeWidth(1.5f);
        exGr.strokeRoundRect(cx - 85, cy - 85, cx + 85, cy + 85,
                8);
    }

    public static void drawOuterFrame(@Nonnull GuiGraphics gr, float x, float y,
                                      float width, float height, int color) {
        var exGr = new ExtendedGuiGraphics(gr);
        exGr.setColor(color);
        exGr.setStrokeWidth(1.0f);
        exGr.strokeRoundRect(x - 0.5f, y - 0.5f, x + width + 0.5f, y + height + 0.5f,
                1.5f);
    }

    public static void drawEditBoxBorder(@Nonnull GuiGraphics gr, float x, float y,
                                         float width, float height, int outlineColor) {
        var exGr = new ExtendedGuiGraphics(gr);
        exGr.setColor(0x30000000);
        exGr.fillRoundRect(x, y, x + width, y + height,
                1.5f);
        exGr.setColor(outlineColor);
        exGr.setStrokeWidth(1.0f);
        exGr.strokeRoundRect(x - 0.5f, y - 0.5f, x + width + 0.5f, y + height + 0.5f,
                1.5f);
    }

    public static void drawChartLinesAndPoints(@Nonnull GuiGraphics gr, float x, @Nonnull FloatList currentHeight) {
        var exGr = new ExtendedGuiGraphics(gr);
        exGr.pose().translate(0, 0, 1);
        exGr.setColor(~0);
        for (int i = 0; i < currentHeight.size() - 1; i++) {
            float lx = x + 20 * i;
            float ly = currentHeight.getFloat(i);
            float rx = x + 20 * (i + 1);
            float ry = currentHeight.getFloat(i + 1);
            float cx = (lx + rx) * 0.5f;
            float cy = (ly + ry) * 0.5f;
            double ang = Math.atan2(ry - ly, rx - lx);
            exGr.pose().pushPose();
            exGr.pose().translate(cx, cy, 0);
            exGr.pose().mulPose(new Quaternionf()
                    .setAngleAxis(ang, 0, 0, 1));
            exGr.pose().translate(-cx, -cy, 0);
            float sin = (float) Math.sin(-ang);
            float cos = (float) Math.cos(-ang);
            float left = (lx - cx) * cos - (ly - cy) * sin + cx;
            float right = (rx - cx) * cos - (ry - cy) * sin + cx;
            exGr.fillRoundRect(left - 0.75f, cy - 0.75f, right + 0.75f, cy + 0.75f, 0.75f);
            exGr.pose().popPose();
        }
        for (int i = 0; i < currentHeight.size(); i++) {
            float cx = x + 20 * i;
            float cy = currentHeight.getFloat(i);
            exGr.fillCircle(cx, cy, 2);
        }
    }

    // screen the screen
    @Nonnull
    public static MenuScreens.ScreenConstructor<FluxMenu, AbstractContainerScreen<FluxMenu>> upgradeScreenFactory(
            MenuScreens.ScreenConstructor<FluxMenu, AbstractContainerScreen<FluxMenu>> predecessor) {
        MenuScreens.ScreenConstructor<FluxMenu, AbstractContainerScreen<FluxMenu>> successor = getScreenFactory();
        return (menu, inventory, title) -> FluxConfig.enableGuiDebug
                ? successor.create(menu, inventory, title)
                : predecessor.create(menu, inventory, title);
    }

    private static MenuScreens.ScreenConstructor<FluxMenu, AbstractContainerScreen<FluxMenu>> getScreenFactory() {
        return MenuScreenFactory.create(menu -> {
            FluxDeviceUI fragment = new FluxDeviceUI((TileFluxDevice) menu.mProvider);
            menu.mOnResultListener = fragment;
            DataSet args = new DataSet();
            args.putInt("token", menu.containerId);
            fragment.setArguments(args);
            return fragment;
        });
    }
}
