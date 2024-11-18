package sonar.fluxnetworks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import sonar.fluxnetworks.api.FluxConstants;
import sonar.fluxnetworks.api.FluxDataComponents;
import sonar.fluxnetworks.client.gui.basic.GuiFluxCore;
import sonar.fluxnetworks.common.data.FluxDataComponent;
import sonar.fluxnetworks.common.device.TileFluxDevice;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Render network color on blocks and items.
 */
//FIXME
@OnlyIn(Dist.CLIENT)
public class FluxColorHandler implements BlockColor, ItemColor {

    public static final FluxColorHandler INSTANCE = new FluxColorHandler();

    /*private final Int2IntMap colorMap = new Int2IntArrayMap();

    private final Int2ObjectMap<String> nameMap = new Int2ObjectArrayMap<>();

    private final List<Integer> requests = new ArrayList<>();
    private final List<Integer> sentRequests = new ArrayList<>();

    {
        colorMap.defaultReturnValue(-1);
    }

    public void reset() {
        colorMap.clear();
        nameMap.clear();
        requests.clear();
    }

    public void loadColorCache(int id, int color) {
        if (id != -1) {
            colorMap.put(id, color);
        }
    }

    public void loadNameCache(int id, String name) {
        if (id != -1) {
            nameMap.put(id, name);
        }
    }

    public void placeRequest(int id) {
        if (id != -1 && !requests.contains(id) && !sentRequests.contains(id)) {
            requests.add(id);
        }
    }

    public int getOrRequestNetworkColor(int id) {
        if (id == -1) {
            return NO_NETWORK_COLOR;
        }
        int cached = colorMap.get(id);
        if (cached != -1) {
            return cached;
        }
        placeRequest(id);
        return NO_NETWORK_COLOR;
    }

    public String getOrRequestNetworkName(int id) {
        if (id == -1) {
            return "NONE";
        }
        String cached = nameMap.get(id);
        if (cached != null) {
            return cached;
        }
        placeRequest(id);
        return "WAITING FOR SERVER";
    }

    public int tickCount;

    public void tick() {
        if (!requests.isEmpty()) {
            tickCount++;
            if (tickCount > 10) {
                tickCount = 0;
                PacketHandler.CHANNEL.sendToServer(new NetworkColourRequestPacket(Lists.newArrayList(requests)));
                sentRequests.addAll(requests);
                requests.clear();
            }
        }
    }

    public void receiveCache(@Nonnull Map<Integer, Tuple<Integer, String>> cache) {
        cache.forEach((id, colorToName) -> {
            loadColorCache(id, colorToName.getA());
            loadNameCache(id, colorToName.getB());
            sentRequests.remove(id);
            requests.remove(id);
        });
    }*/

    @Override
    public int getColor(@Nonnull BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos,
                        int tintIndex) {
        // called when renderer updated
        if (tintIndex == 1 && pos != null && world != null) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileFluxDevice) {
                /*TileFluxDevice t = (TileFluxDevice) tile;
                if (t.getNetworkID() == -1) {
                    return NO_NETWORK_COLOR;
                }*/
                return ((TileFluxDevice) tile).mClientColor;
            }
        }
        return ~0;
    }

    @Override
    public int getColor(@Nonnull ItemStack stack, int tintIndex) {
        // called every frame
        if (tintIndex == 1) {
            Optional<Integer> color = stack.get(FluxDataComponents.FLUX_COLOR);
            // TODO: what??????
            if (color != null && color.isPresent()) {
                /*if (FluxConfig.enableGuiDebug && FluxNetworks.modernUILoaded) {
                    return NavigationHome.network.isInvalid() ? NO_NETWORK_COLOR : NavigationHome.network.getSetting
                    (NetworkSettings.NETWORK_COLOR) | 0xff000000;
                }*/
                Screen screen = Minecraft.getInstance().screen;
                if (screen instanceof GuiFluxCore gui) {
                    return gui.getNetwork().getNetworkColor();
                }
            }
            FluxDataComponent data = stack.get(FluxDataComponents.FLUX_DATA);
            if (data != null) {
                return ClientCache.getNetwork(data.networkId()).getNetworkColor();
            }
            return FluxConstants.INVALID_NETWORK_COLOR;
        }
        return ~0;
    }

    public static int colorMultiplierForConfigurator(ItemStack stack, int tintIndex) {
        if (tintIndex == 1) {
            /*Screen screen = Minecraft.getInstance().currentScreen;
            if (screen instanceof GuiFluxCore) {
                GuiFluxCore gui = (GuiFluxCore) screen;
                if (gui.getContainer().bridge instanceof ItemFluxConfigurator.MenuBridge) {
                    return gui.network.getNetworkColor();
                }
            }*/
            FluxDataComponent component = stack.get(FluxDataComponents.FLUX_CONFIG);
            if (component != null) {
                return ClientCache.getNetwork(component.networkId()).getNetworkColor();
            }
            return FluxConstants.INVALID_NETWORK_COLOR;
        }
        return ~0;
    }
}
