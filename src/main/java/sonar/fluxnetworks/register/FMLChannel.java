package sonar.fluxnetworks.register;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
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
                (msg, ctx) ->
                        ClientMessages.msg(msg.data.readShort(), msg.data, () -> (LocalPlayer) ctx.player())
        );
    }

    @SubscribeEvent
    public static void registerServerPayloadHandler(final RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL).playToServer(
                C2SMessage.TYPE,
                C2SMessage.CODEC,
                (msg, ctx) ->
                        Messages.msg(msg.data.readShort(), msg.data, () -> (ServerPlayer) ctx.player())
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
        ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().forEach(
                p -> p.connection.send(new S2CMessage(payload))
        );
    }

    @Override
    public void sendToTrackingChunk(@Nonnull FriendlyByteBuf payload, @Nonnull LevelChunk chunk) {
        final S2CMessage packet = new S2CMessage(payload);
        ((ServerLevel) chunk.getLevel()).getChunkSource().chunkMap.getPlayers(
                chunk.getPos(), /* boundaryOnly */ false).forEach(p -> p.connection.send(packet));
    }



    // A 🩼 to use plain byte buffers for Client<=>Server communication
    // Define a custom payload with a type of byte buffer
    public record S2CMessage(FriendlyByteBuf data) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<S2CMessage> TYPE = new CustomPacketPayload.Type<>(FluxNetworks.location("network_s2c"));
        public static final StreamCodec<ByteBuf, S2CMessage> CODEC = StreamCodec.composite(
                // Max payload size is 1 MiB. Should be more than enough
                byteBufferCodec(1048576), S2CMessage::data, S2CMessage::new
        );

        public S2CMessage(ByteBuf data) { this(new FriendlyByteBuf(data)); }

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record C2SMessage(FriendlyByteBuf data) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<C2SMessage> TYPE = new CustomPacketPayload.Type<>(FluxNetworks.location("network_c2s"));
        public static final StreamCodec<ByteBuf, C2SMessage> CODEC = StreamCodec.composite(
                // Max payload size is 1 MiB. Should be more than enough
                byteBufferCodec(1048576), C2SMessage::data, C2SMessage::new
        );

        public C2SMessage(ByteBuf data) { this(new FriendlyByteBuf(data)); }

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // Adapted from ByteBufCodecs.byteArray codec
    private static StreamCodec<ByteBuf, ByteBuf> byteBufferCodec(final int maxSize) {
        return new StreamCodec<>() {
            public ByteBuf decode(ByteBuf payload) {
                // When we need to read data from payload, we slice the buffer
                int size = VarInt.read(payload);
                if (size > maxSize) {
                    throw new EncoderException("ByteArray with size " + size + " is bigger than allowed " + maxSize);
                } else {
                    var data = payload.retainedSlice(payload.readerIndex(), size);
                    // Skip the bytes we just "read", otherwise minecraft will get upset
                    payload.skipBytes(size);
                    return data;
                }
            }

            public void encode(ByteBuf payload, ByteBuf data) {
                // When writing data, we store the length and copy bytes to the payload
                data.resetReaderIndex(); // TODO: Do we need to reset the reader index?
                int length = data.readableBytes();
                if (length > maxSize) {
                    throw new EncoderException("ByteBuf with size " + length + " is bigger than allowed " + maxSize);
                } else {
                    VarInt.write(payload, length);
                    payload.writeBytes(data);
                }
            }
        };
    }
}
