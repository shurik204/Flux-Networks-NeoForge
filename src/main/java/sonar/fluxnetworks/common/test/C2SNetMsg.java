package sonar.fluxnetworks.common.test;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class C2SNetMsg {

    /*private static final NetworkHandler sNetwork = NetworkHandler.sInstance;

    // indices are defined in S2CNetMsg
    static final Functor[] sFunctors = new Functor[]{
            C2SNetMsg::tileEntity, // 0
            C2SNetMsg::feedback, // 1
            C2SNetMsg::updateSuperAdmin, // 2
            C2SNetMsg::lavaEffect, // 3
            C2SNetMsg::updateNetwork, // 4
            C2SNetMsg::updateAccess, // 5
            C2SNetMsg::updateConnection}; // 6

    @FunctionalInterface
    interface Functor {

        // handle S2C packets
        void f(@Nonnull PacketBuffer payload, @Nonnull ClientPlayerEntity player);
    }

    // request to change flux tile entity settings
    public static void tileEntity(@Nonnull FluxDeviceEntity tile, byte type) {
        PacketBuffer data = sNetwork.targetAt(0);
        data.writeBlockPos(tile.getPos());
        data.writeByte(type);
        tile.writePacket(data, type);
        sNetwork.sendToServer(data);
    }

    // request to enable/disable super admin permission
    public static void requestSuperAdmin() {
        PacketBuffer data = sNetwork.targetAt(1);
        sNetwork.sendToServer(data);
    }

    // edit member permission
    public static void editMember(int networkID, UUID playerChanged, int type) {
        PacketBuffer data = sNetwork.targetAt(2);
        data.writeVarInt(networkID);
        data.writeUniqueId(playerChanged);
        data.writeVarInt(type);
        sNetwork.sendToServer(data);
    }

    // edit network settings
    public static void editNetwork(int networkID, String name, int color, @Nonnull SecurityLevel security,
                                   String password) {
        PacketBuffer data = sNetwork.targetAt(3);
        data.writeVarInt(networkID);
        data.writeString(name, 256);
        data.writeInt(color);
        data.writeVarInt(security.ordinal());
        data.writeString(password, 256);
        sNetwork.sendToServer(data);
    }

    // edit wireless mode
    public static void editWireless(int networkID, int mode) {
        PacketBuffer data = sNetwork.targetAt(4);
        data.writeVarInt(networkID);
        data.writeVarInt(mode);
        sNetwork.sendToServer(data);
    }

    // request a network update
    public static void requestNetworkUpdate(@Nonnull IFluxNetwork network, int type) {
        PacketBuffer data = sNetwork.targetAt(5);
        data.writeVarInt(type);
        data.writeVarInt(1); // size = 1
        data.writeVarInt(network.getNetworkID());
        sNetwork.sendToServer(data);
    }

    // request a network update
    public static void requestNetworkUpdate(@Nonnull Collection<IFluxNetwork> networks, int type) {
        PacketBuffer data = sNetwork.targetAt(5);
        data.writeVarInt(type);
        data.writeVarInt(networks.size());
        networks.forEach(net -> data.writeVarInt(net.getNetworkID()));
        sNetwork.sendToServer(data);
    }

    // set (connect to) network for a flux tile entity
    public static void setNetwork(BlockPos pos, int networkID, String password) {
        PacketBuffer data = sNetwork.targetAt(6);
        data.writeBlockPos(pos);
        data.writeVarInt(networkID);
        data.writeString(password, 256);
        sNetwork.sendToServer(data);
    }

    // create a flux network
    public static void createNetwork(String name, int color, @Nonnull SecurityLevel security, String password) {
        PacketBuffer data = sNetwork.targetAt(7);
        data.writeString(name, 256);
        data.writeInt(color);
        data.writeVarInt(security.ordinal());
        data.writeString(password, 256);
        sNetwork.sendToServer(data);
    }

    // delete a flux network
    public static void deleteNetwork(int networkID) {
        PacketBuffer data = sNetwork.targetAt(8);
        data.writeVarInt(networkID);
        sNetwork.sendToServer(data);
    }

    // request a network access level update for GUI
    public static void requestAccessUpdate(int networkID) {
        PacketBuffer data = sNetwork.targetAt(9);
        data.writeVarInt(networkID);
        sNetwork.sendToServer(data);
    }

    public static void disconnect(int networkID, @Nonnull List<GlobalPos> list) {
        PacketBuffer data = sNetwork.targetAt(10);
        data.writeVarInt(networkID);
        data.writeVarInt(FluxConstants.FLAG_EDIT_DISCONNECT);
        data.writeVarInt(list.size());
        list.forEach(pos -> FluxUtils.writeGlobalPos(data, pos));
        sNetwork.sendToServer(data);
    }

    public static void editConnections(int networkID, @Nonnull List<GlobalPos> list, int flags, String name, int
    priority,
                                       long limit, boolean surgeMode, boolean disableLimit, boolean chunkLoading) {
        PacketBuffer data = sNetwork.targetAt(10);
        data.writeVarInt(networkID);
        data.writeVarInt(flags);
        data.writeVarInt(list.size());
        list.forEach(pos -> FluxUtils.writeGlobalPos(data, pos));
        if ((flags & FluxConstants.FLAG_EDIT_NAME) != 0) {
            data.writeString(name, 0x100);
        }
        if ((flags & FluxConstants.FLAG_EDIT_PRIORITY) != 0) {
            data.writeInt(priority);
        }
        if ((flags & FluxConstants.FLAG_EDIT_LIMIT) != 0) {
            data.writeLong(limit);
        }
        if ((flags & FluxConstants.FLAG_EDIT_SURGE_MODE) != 0) {
            data.writeBoolean(surgeMode);
        }
        if ((flags & FluxConstants.FLAG_EDIT_DISABLE_LIMIT) != 0) {
            data.writeBoolean(disableLimit);
        }
        if ((flags & FluxConstants.FLAG_EDIT_CHUNK_LOADING) != 0) {
            data.writeBoolean(chunkLoading);
        }
        sNetwork.sendToServer(data);
    }

    public static void requestConnectionUpdate(int networkID, @Nonnull List<GlobalPos> list) {
        PacketBuffer data = sNetwork.targetAt(11);
        data.writeVarInt(networkID);
        data.writeVarInt(list.size());
        list.forEach(pos -> FluxUtils.writeGlobalPos(data, pos));
        sNetwork.sendToServer(data);
    }

    public static void configuratorNet(int id, String password) {
        PacketBuffer data = sNetwork.targetAt(12);
        data.writeVarInt(id);
        data.writeString(password, 256);
        sNetwork.sendToServer(data);
    }

    public static void configuratorEdit(String customName, CompoundNBT tag) {
        PacketBuffer data = sNetwork.targetAt(13);
        data.writeString(customName, 256);
        data.writeCompoundTag(tag);
        sNetwork.sendToServer(data);
    }

    ///  HANDLING  \\\

    private static void tileEntity(@Nonnull PacketBuffer data, @Nonnull ClientPlayerEntity player) {
        final TileEntity tile = player.world.getTileEntity(data.readBlockPos());
        if (tile instanceof FluxDeviceEntity) {
            ((FluxDeviceEntity) tile).readPacket(data, data.readByte());
        }
    }

    private static void feedback(@Nonnull PacketBuffer data, @Nonnull ClientPlayerEntity player) {
        final FeedbackInfo info = FeedbackInfo.values()[data.readVarInt()];
        final boolean action = info.action();
        final Screen screen = Minecraft.getInstance().currentScreen;
        if (!action) {
            FluxClientCache.setFeedbackText(info);
        } else if (screen instanceof GuiFluxCore) {
            ((GuiFluxCore) screen).onFeedbackAction(info);
        }
    }

    private static void updateSuperAdmin(@Nonnull PacketBuffer data, @Nonnull ClientPlayerEntity player) {
        FluxClientCache.superAdmin = data.readBoolean();
        final Screen screen = Minecraft.getInstance().currentScreen;
        if (screen instanceof GuiFluxAdminHome) {
            ((GuiFluxAdminHome) screen).superAdmin.toggled = FluxClientCache.superAdmin;
        }
    }

    private static void lavaEffect(@Nonnull PacketBuffer data, @Nonnull ClientPlayerEntity player) {
        final BlockPos pos = data.readBlockPos();
        final int count = data.readVarInt();
        final ClientWorld world = player.worldClient;
        if (world != null) {
            for (int i = 0; i < count; i++) {
                world.addParticle(ParticleTypes.LAVA,
                        pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0, 0);
            }
        }
    }

    private static void updateNetwork(@Nonnull PacketBuffer data, @Nonnull ClientPlayerEntity player) {
        final int type = data.readVarInt();
        Int2ObjectMap<CompoundNBT> updatedNetworks = new Int2ObjectArrayMap<>();
        final int size = data.readVarInt();
        if (size == 0) {
            return;
        }
        for (int i = 0; i < size; i++) {
            updatedNetworks.put(data.readVarInt(), data.readCompoundTag());
        }
        FluxClientCache.updateNetworks(updatedNetworks, type);
    }

    private static void updateAccess(@Nonnull PacketBuffer data, @Nonnull ClientPlayerEntity player) {
        AccessLevel access = AccessLevel.values()[data.readVarInt()];
        Screen screen = Minecraft.getInstance().currentScreen;
        if (screen instanceof GuiFluxCore) {
            GuiFluxCore gui = (GuiFluxCore) screen;
            gui.accessLevel = access;
        }
    }

    private static void updateConnection(@Nonnull PacketBuffer data, @Nonnull ClientPlayerEntity player) {
        final int networkID = data.readVarInt();
        final int size = data.readVarInt();
        final List<CompoundNBT> tags = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            tags.add(data.readCompoundTag());
        }
        FluxClientCache.updateConnections(networkID, tags);
    }*/
}
