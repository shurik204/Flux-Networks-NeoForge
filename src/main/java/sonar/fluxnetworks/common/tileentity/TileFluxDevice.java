package sonar.fluxnetworks.common.tileentity;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import sonar.fluxnetworks.FluxConfig;
import sonar.fluxnetworks.api.device.IFluxDevice;
import sonar.fluxnetworks.api.misc.FluxConstants;
import sonar.fluxnetworks.api.network.FluxLogicType;
import sonar.fluxnetworks.api.network.IFluxNetwork;
import sonar.fluxnetworks.client.FluxClientCache;
import sonar.fluxnetworks.common.connection.FluxNetworkInvalid;
import sonar.fluxnetworks.common.connection.FluxNetworkServer;
import sonar.fluxnetworks.common.handler.NetworkHandler;
import sonar.fluxnetworks.common.item.ItemFluxDevice;
import sonar.fluxnetworks.common.misc.ContainerConnector;
import sonar.fluxnetworks.common.misc.FluxUtils;
import sonar.fluxnetworks.common.network.TileMessage;
import sonar.fluxnetworks.common.storage.FluxChunkManager;
import sonar.fluxnetworks.common.storage.FluxNetworkData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
public abstract class TileFluxDevice extends TileEntity implements IFluxDevice, ITickableTileEntity, INamedContainerProvider {

    public final Set<PlayerEntity> playerUsing = new ObjectArraySet<>();

    //TODO keep empty when created and client can use translated name as default
    private String customName;
    private UUID playerUUID = FluxConstants.DEFAULT_UUID;

    private int networkID;

    // 0xRRGGBB, this value only available on client for rendering, updated from server
    public int clientColor;

    protected int priority;
    protected long limit;

    public int mFlags;

    private GlobalPos globalPos;

    private IFluxNetwork network = FluxNetworkInvalid.INSTANCE;

    private boolean sLoad = false;

    // packet flag
    private boolean sSettingsChanged;

    public TileFluxDevice(TileEntityType<?> tileEntityTypeIn, String customName, long limit) {
        super(tileEntityTypeIn);
        this.customName = customName;
        this.limit = limit;
    }

    @Override
    public void remove() {
        super.remove();
        if (!world.isRemote && sLoad) {
            network.enqueueConnectionRemoval(this, false);
            if (isForcedLoading()) {
                FluxChunkManager.removeChunkLoader((ServerWorld) world, this);
            }
            sLoad = false;
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (!world.isRemote && sLoad) {
            network.enqueueConnectionRemoval(this, true);
            sLoad = false;
        }
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            if (!playerUsing.isEmpty()) {
                NetworkHandler.INSTANCE.sendToPlayers(new TileMessage(this, TileMessage.S2C_GUI_SYNC), playerUsing);
                sSettingsChanged = false;
            }
            if (!sLoad) {
                if (networkID > 0) {
                    IFluxNetwork network = FluxNetworkData.getNetwork(networkID);
                    if (network.isValid() && !(getDeviceType().isController() &&
                            network.getConnections(FluxLogicType.CONTROLLER).size() > 0)) {
                        network.enqueueConnectionAddition(this);
                    }
                } else {
                    networkID = FluxConstants.INVALID_NETWORK_ID;
                }
                updateTransfers(Direction.values());
                sendFullUpdatePacket();
                sLoad = true;
            }
        }
    }

    @Override
    public void onConnect(@Nonnull IFluxNetwork network) {
        this.network = network;
        this.networkID = network.getNetworkID();
        sendFullUpdatePacket();
    }

    @Override
    public void onDisconnect() {
        if (network.isValid()) {
            network = FluxNetworkInvalid.INSTANCE;
            networkID = network.getNetworkID();
            sendFullUpdatePacket();
        }
    }

    @Override
    public int getNetworkID() {
        return networkID;
    }

    @Override
    public IFluxNetwork getNetwork() {
        return network;
    }

    @Nonnull
    @Override
    public final SUpdateTileEntityPacket getUpdatePacket() {
        // Server side, write block update data
        CompoundNBT nbt = new CompoundNBT();
        writeCustomNBT(nbt, FluxConstants.FLAG_TILE_UPDATE);
        return new SUpdateTileEntityPacket(pos, -1, nbt);
    }

    @Override
    public final void onDataPacket(NetworkManager net, @Nonnull SUpdateTileEntityPacket pkt) {
        // Client side, read block update data
        readCustomNBT(pkt.getNbtCompound(), FluxConstants.FLAG_TILE_UPDATE);
        world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 0);
    }

    @Nonnull
    @Override
    public final CompoundNBT getUpdateTag() {
        // Server side, write NBT when updating chunk data
        return write(new CompoundNBT());
    }

    @Override
    public final void handleUpdateTag(BlockState state, CompoundNBT tag) {
        // Client side, read NBT when updating chunk data
        read(state, tag);
    }

    @Nonnull
    @Override
    public final World getFluxWorld() {
        // Access world with interface
        return world;
    }

    @Nonnull
    @Override
    public final CompoundNBT write(@Nonnull CompoundNBT compound) {
        writeCustomNBT(super.write(compound), FluxConstants.FLAG_SAVE_ALL);
        return compound;
    }

    @Override
    public final void read(@Nonnull BlockState state, @Nonnull CompoundNBT compound) {
        super.read(state, compound);
        readCustomNBT(compound, FluxConstants.FLAG_SAVE_ALL);
    }

    public void writeCustomNBT(CompoundNBT tag, int flag) {
        if (flag <= FluxConstants.FLAG_TILE_UPDATE) {
            tag.putInt("0", priority);
            tag.putLong("1", limit);
            tag.putInt("2", mFlags);
            tag.putLong("3", getTransferHandler().getBuffer());
            tag.putInt("4", networkID);
            tag.putUniqueId("5", playerUUID);
            tag.putString("6", customName);
        }
        if (flag == FluxConstants.FLAG_TILE_UPDATE) {
            tag.putInt("7", network.getNetworkColor());
            getTransferHandler().writeNetworkedNBT(tag);
        }
        if (flag == FluxConstants.FLAG_TILE_DROP) {
            tag.putLong("buffer", getTransferHandler().getBuffer());
            tag.putInt(ItemFluxDevice.PRIORITY, priority);
            tag.putLong(ItemFluxDevice.LIMIT, limit);
            tag.putBoolean(ItemFluxDevice.DISABLE_LIMIT, getDisableLimit());
            tag.putBoolean(ItemFluxDevice.SURGE_MODE, getSurgeMode());
            tag.putInt(FluxNetworkData.NETWORK_ID, getNetworkID());
            tag.putString(ItemFluxDevice.CUSTOM_NAME, customName);
        }
    }

    public void readCustomNBT(CompoundNBT tag, int flag) {
        if (flag <= FluxConstants.FLAG_TILE_UPDATE) {
            priority = tag.getInt("0");
            limit = tag.getLong("1");
            mFlags = tag.getInt("2");
            getTransferHandler().setBuffer(tag.getLong("3"));
            networkID = tag.getInt("4");
            playerUUID = tag.getUniqueId("5");
            customName = tag.getString("6");
        }
        if (flag == FluxConstants.FLAG_TILE_UPDATE) {
            clientColor = tag.getInt("7");
            getTransferHandler().readNetworkedNBT(tag);
        }
        if (flag == FluxConstants.FLAG_TILE_DROP) {
            long l = tag.getLong("buffer");
            if (l > 0)
                getTransferHandler().setBuffer(l);
            priority = tag.getInt(ItemFluxDevice.PRIORITY);
            l = tag.getLong(ItemFluxDevice.LIMIT);
            if (l > 0)
                limit = l;
            setDisableLimit(tag.getBoolean(ItemFluxDevice.DISABLE_LIMIT));
            setSurgeMode(tag.getBoolean(ItemFluxDevice.SURGE_MODE));
            int i = tag.getInt(FluxNetworkData.NETWORK_ID);
            if (i > 0)
                networkID = i;
            String name = tag.getString(ItemFluxDevice.CUSTOM_NAME);
            if (!name.isEmpty())
                customName = name;
            clientColor = FluxClientCache.getNetwork(networkID).getNetworkColor();
        }
    }

    public boolean canPlayerAccess(@Nonnull PlayerEntity player) {
        if (network.isValid()) {
            if (PlayerEntity.getUUID(player.getGameProfile()).equals(playerUUID)) {
                return true;
            }
            return network.getPlayerAccess(player).canUse();
        }
        return true;
    }

    //// PACKETS\\\\

    /**
     * Sends a block update
     */
    public void sendFullUpdatePacket() {
        if (!world.isRemote) {
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 1 | 2);
        }
    }

    /*@Deprecated
    private void sendTilePacketToUsing(byte packetID) {
        if (!world.isRemote) {
            for (PlayerEntity playerEntity : playerUsing) {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerEntity), new TilePacketBufferPacket(this, pos, packetID));
            }
        }
    }

    @Deprecated
    private void sendTilePacketToNearby(byte packetID) {
        if (!world.isRemote) {
            PacketHandler.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)), new TilePacketBufferPacket(this, pos, packetID));
        }
    }

    @Deprecated
    private void sendTilePacketToServer(byte packetID) {
        PacketHandler.CHANNEL.sendToServer(new TilePacketBufferPacket(this, pos, packetID));
    }*/

    public void writePacket(PacketBuffer buf, byte id) {
        switch (id) {
            case TileMessage.C2S_CUSTOM_NAME:
                buf.writeString(customName, 256);
                break;
            case TileMessage.C2S_PRIORITY:
                buf.writeInt(priority);
                break;
            case TileMessage.C2S_LIMIT:
                buf.writeLong(limit);
                break;
            case TileMessage.C2S_SURGE_MODE:
                buf.writeBoolean(getSurgeMode());
                break;
            case TileMessage.C2S_DISABLE_LIMIT:
                buf.writeBoolean(getDisableLimit());
                break;
            case TileMessage.C2S_CHUNK_LOADING:
                buf.writeBoolean(isForcedLoading());
                break;
            case TileMessage.S2C_GUI_SYNC:
                buf.writeBoolean(sSettingsChanged);
                if (sSettingsChanged) {
                    buf.writeString(customName, 256);
                    buf.writeInt(priority);
                    buf.writeLong(limit);
                    buf.writeByte(mFlags >> 6);
                }
                buf.writeCompoundTag(getTransferHandler().writeNetworkedNBT(new CompoundNBT()));
                break;
        }
    }

    public void readPacket(PacketBuffer buf, byte id) {
        switch (id) {
            case TileMessage.C2S_CUSTOM_NAME:
                customName = buf.readString(256);
                markLiteSettingChanged();
                break;
            case TileMessage.C2S_PRIORITY:
                priority = buf.readInt();
                sortNetworkConnections();
                break;
            case TileMessage.C2S_LIMIT:
                limit = buf.readLong();
                markLiteSettingChanged();
                break;
            case TileMessage.C2S_SURGE_MODE:
                setSurgeMode(buf.readBoolean());
                sortNetworkConnections();
                break;
            case TileMessage.C2S_DISABLE_LIMIT:
                setDisableLimit(buf.readBoolean());
                markLiteSettingChanged();
                break;
            case TileMessage.C2S_CHUNK_LOADING:
                boolean toLoad = buf.readBoolean();
                if (FluxConfig.enableChunkLoading) {
                    if (toLoad && !isForcedLoading()) {
                        FluxChunkManager.addChunkLoader((ServerWorld) world, this);
                        setForcedLoading(true);
                    } else if (!toLoad && isForcedLoading()) {
                        FluxChunkManager.removeChunkLoader((ServerWorld) world, this);
                        setForcedLoading(false);
                    }
                } else {
                    setForcedLoading(false);
                    //TODO
                    //PacketHandler.CHANNEL.reply(new FeedbackPacket(EnumFeedbackInfo.BANNED_LOADING), context);
                }
                sSettingsChanged = true;
                break;
            case TileMessage.S2C_GUI_SYNC:
                if (buf.readBoolean()) {
                    customName = buf.readString(256);
                    priority = buf.readInt();
                    limit = buf.readLong();
                    mFlags = (mFlags & 0x3f) | buf.readByte() << 6;
                }
                getTransferHandler().readNetworkedNBT(buf.readCompoundTag());
                break;
        }
    }

    protected void sortNetworkConnections() {
        if (network instanceof FluxNetworkServer) {
            FluxNetworkServer fluxNetworkServer = (FluxNetworkServer) network;
            fluxNetworkServer.sortConnections = true;
            markLiteSettingChanged();
        }
    }

    protected void markLiteSettingChanged() {
        if (network instanceof FluxNetworkServer) {
            FluxNetworkServer fluxNetworkServer = (FluxNetworkServer) network;
            fluxNetworkServer.markLiteSettingChanged(this);
            sSettingsChanged = true;
        }
    }

    /*@Override
    public CompoundNBT copyConfiguration(CompoundNBT config) {
        return FluxUtils.copyConfiguration(this, config);
    }

    @Override
    public void pasteConfiguration(CompoundNBT config) {
        FluxUtils.pasteConfiguration(this, config);
    }*/

    @Override
    public UUID getConnectionOwner() {
        return playerUUID;
    }

    @Override
    public void setConnectionOwner(UUID uuid) {
        playerUUID = uuid;
    }

    public void updateTransfers(Direction... dirs) {
        getTransferHandler().updateTransfers(dirs);
    }

    public void onContainerOpened(PlayerEntity player) {
        if (!world.isRemote) {
            playerUsing.add(player);
            sendFullUpdatePacket();
        }
    }

    public void onContainerClosed(PlayerEntity player) {
        if (!world.isRemote) {
            playerUsing.remove(player);
        }
    }

    @Override
    public String getCustomName() {
        return customName;
    }

    @Override
    public void setCustomName(String customName) {
        this.customName = customName;
    }

    @Override
    public boolean isChunkLoaded() {
        return !isRemoved();
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean isForcedLoading() {
        return (mFlags & 1 << 8) != 0;
    }

    @Override
    public void setForcedLoading(boolean forcedLoading) {
        if (isForcedLoading() != forcedLoading) {
            mFlags ^= 1 << 8;
        }
    }

    @Override
    public int getRawPriority() {
        return priority;
    }

    @Override
    public int getLogicPriority() {
        return getSurgeMode() ? Integer.MAX_VALUE : priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public long getRawLimit() {
        return limit;
    }

    @Override
    public long getLogicLimit() {
        return getDisableLimit() ? Long.MAX_VALUE : limit;
    }

    @Override
    public void setLimit(long limit) {
        this.limit = limit;
    }

    @Override
    public long getMaxTransferLimit() {
        return Long.MAX_VALUE;
    }

    /*@Override
    public int getFolderID() {
        return folderID;
    }*/

    /*public Coord4D coord4D;

    @Override
    public Coord4D getCoords() {
        if (coord4D == null)
            coord4D = new Coord4D(this);
        return coord4D;
    }*/

    @Nonnull
    @Override
    public final GlobalPos getGlobalPos() {
        if (globalPos == null)
            globalPos = FluxUtils.getGlobalPos(this);
        return globalPos;
    }

    @Override
    public boolean getDisableLimit() {
        return (mFlags & 1 << 7) != 0;
    }

    @Override
    public void setDisableLimit(boolean disableLimit) {
        if (getDisableLimit() != disableLimit) {
            mFlags ^= 1 << 7;
        }
    }

    @Override
    public boolean getSurgeMode() {
        return (mFlags & 1 << 6) != 0;
    }

    @Override
    public void setSurgeMode(boolean surgeMode) {
        if (getSurgeMode() != surgeMode) {
            mFlags ^= 1 << 6;
        }
    }

    /* TODO - FIX OPEN COMPUTERS INTEGRATION
    @Override
    public String[] getOCMethods() {
        return new String[]{"getNetworkInfo", "getCountInfo", "getEnergyInfo", "getFluxInfo"};
    }

    @Override
    public Object[] invokeMethods(String method, Arguments arguments) {
        switch (method) {
            case "getNetworkInfo": {
                Map<Object, Object> map = new HashMap<>();
                map.put("id", network.getNetworkID());
                map.put("name", network.getNetworkName());
                map.put("ownerUUID", network.getSetting(NetworkSettings.NETWORK_OWNER).toString());
                map.put("securityType", network.getSetting(NetworkSettings.NETWORK_SECURITY).name().toLowerCase());
                map.put("energyType", network.getSetting(NetworkSettings.NETWORK_ENERGY).getStorageSuffix());
                map.put("averageTick", network.getSetting(NetworkSettings.NETWORK_STATISTICS).average_tick_micro);
                return new Object[]{map};
            }
            case "getCountInfo": {
                Map<Object, Object> map = new HashMap<>();
                NetworkStatistics stats = network.getSetting(NetworkSettings.NETWORK_STATISTICS);
                map.put("plugCount", stats.fluxPlugCount);
                map.put("pointCount", stats.fluxPointCount);
                map.put("controllerCount", stats.fluxControllerCount);
                map.put("storageCount", stats.fluxStorageCount);
                return new Object[]{map};
            }
            case "getEnergyInfo": {
                Map<Object, Object> map = new HashMap<>();
                NetworkStatistics stats = network.getSetting(NetworkSettings.NETWORK_STATISTICS);
                map.put("energyInput", stats.energyInput);
                map.put("energyOutput", stats.energyOutput);
                map.put("totalBuffer", stats.totalBuffer);
                map.put("totalEnergy", stats.totalEnergy);
                return new Object[]{map};
            }
            case "getFluxInfo": {
                Map<Object, Object> map = new HashMap<>();
                map.put("customName", customName);
                map.put("priority", priority);
                map.put("transferLimit", limit);
                map.put("surgeMode", surgeMode);
                map.put("unlimited", disableLimit);
                map.put("buffer", getTransferHandler().getEnergyStored());
                return new Object[]{map};
            }
        }
        return new Object[0];
    }
    */

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("");
    }

    @Nullable
    public Container createMenu(int windowID, @Nonnull PlayerInventory playerInventory, @Nonnull PlayerEntity entity) {
        return new ContainerConnector<>(windowID, playerInventory, this);
    }
}