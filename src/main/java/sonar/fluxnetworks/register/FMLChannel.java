package sonar.fluxnetworks.register;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import sonar.fluxnetworks.FluxNetworks;

import javax.annotation.Nonnull;

@EventBusSubscriber(modid = FluxNetworks.MODID, bus = EventBusSubscriber.Bus.MOD)
public class FMLChannel extends Channel {
    FMLChannel() {}

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerClientPayloadHandler(final RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL).playToClient(
                S2CMessage.TYPE,
                S2CMessage.CODEC,
                (msg, ctx) -> ClientMessages.msg(msg.data.readShort(), msg.data,
                        () -> (LocalPlayer) ctx.player(),
                        ctx.listener().getMainThreadEventLoop())
        );
    }

    @SubscribeEvent
    public static void registerServerPayloadHandler(final RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL).playToServer(
                C2SMessage.TYPE,
                C2SMessage.CODEC,
                (msg, ctx) -> Messages.msg(msg.data.readShort(), msg.data,
                        () -> (ServerPlayer) ctx.player(),
                        ctx.listener().getMainThreadEventLoop())
        );
    }

    @Override
    public void sendToServer(@Nonnull FriendlyByteBuf payload) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.send(new C2SMessage(payload));
        } else {
            payload.release();
        }
    }

    @Override
    public void sendToPlayer(@Nonnull FriendlyByteBuf payload, @Nonnull ServerPlayer player) {
        player.connection.send(new S2CMessage(payload));
    }

    @Override
    public void sendToAll(@Nonnull FriendlyByteBuf payload) {
        final var packet = new ClientboundCustomPayloadPacket(new S2CMessage(payload));
        ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastAll(packet);
    }

    @Override
    public void sendToTrackingChunk(@Nonnull FriendlyByteBuf payload, @Nonnull LevelChunk chunk) {
        final var packet = new ClientboundCustomPayloadPacket(new S2CMessage(payload));
        ((ServerLevel) chunk.getLevel()).getChunkSource().chunkMap.getPlayers(
                chunk.getPos(), /* boundaryOnly */ false).forEach(p -> p.connection.send(packet));
    }

    public static final StreamCodec<FriendlyByteBuf, FriendlyByteBuf> BYTE_BUFFER_CODEC = new StreamCodec<>() {
        @Nonnull
        @Override
        public FriendlyByteBuf decode(@Nonnull FriendlyByteBuf source) {
            return new FriendlyByteBuf(source.readRetainedSlice(source.readableBytes()));
        }

        @Override
        public void encode(@Nonnull FriendlyByteBuf target, @Nonnull FriendlyByteBuf source) {
            target.writeBytes(source.slice());
        }
    };

    // A 🩼 to use plain byte buffers for Client<=>Server communication
    // Define a custom payload with a type of byte buffer
    public record S2CMessage(FriendlyByteBuf data) implements CustomPacketPayload {
        public static final Type<S2CMessage> TYPE = new Type<>(FluxNetworks.location("s2c_packet"));
        public static final StreamCodec<FriendlyByteBuf, S2CMessage> CODEC = StreamCodec.composite(
                BYTE_BUFFER_CODEC, S2CMessage::data, S2CMessage::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record C2SMessage(FriendlyByteBuf data) implements CustomPacketPayload {
        public static final Type<C2SMessage> TYPE = new Type<>(FluxNetworks.location("c2s_packet"));
        public static final StreamCodec<FriendlyByteBuf, C2SMessage> CODEC = StreamCodec.composite(
                BYTE_BUFFER_CODEC, C2SMessage::data, C2SMessage::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
