package sonar.fluxnetworks.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import sonar.fluxnetworks.api.FluxConstants;
import sonar.fluxnetworks.common.util.CodecsExtras;

import javax.annotation.Nonnull;
import java.util.Optional;

public record FluxDeviceConfigComponent(int networkId, Optional<String> customName, Optional<Integer> priority, Optional<Boolean> surgeMode, Optional<Long> limit, Optional<Boolean> disableLimit) {
    public static final FluxDeviceConfigComponent EMPTY = new FluxDeviceConfigComponent(FluxConstants.INVALID_NETWORK_ID, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    public static final Codec<FluxDeviceConfigComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    CodecsExtras.NETWORK_ID.fieldOf(FluxConstants.NETWORK_ID).forGetter(FluxDeviceConfigComponent::networkId),
                    Codec.STRING.optionalFieldOf(FluxConstants.CUSTOM_NAME).forGetter(FluxDeviceConfigComponent::customName),
                    Codec.INT.optionalFieldOf(FluxConstants.PRIORITY).forGetter(FluxDeviceConfigComponent::priority),
                    Codec.BOOL.optionalFieldOf(FluxConstants.SURGE_MODE).forGetter(FluxDeviceConfigComponent::surgeMode),
                    CodecsExtras.NON_NEGATIVE_LONG.optionalFieldOf(FluxConstants.LIMIT).forGetter(FluxDeviceConfigComponent::limit),
                    Codec.BOOL.optionalFieldOf(FluxConstants.DISABLE_LIMIT).forGetter(FluxDeviceConfigComponent::disableLimit)
            )
            .apply(instance, FluxDeviceConfigComponent::new)
    );

    public static final StreamCodec<ByteBuf, FluxDeviceConfigComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            FluxDeviceConfigComponent::networkId,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            FluxDeviceConfigComponent::customName,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT),
            FluxDeviceConfigComponent::priority,
            ByteBufCodecs.optional(ByteBufCodecs.BOOL),
            FluxDeviceConfigComponent::surgeMode,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_LONG),
            FluxDeviceConfigComponent::limit,
            ByteBufCodecs.optional(ByteBufCodecs.BOOL),
            FluxDeviceConfigComponent::disableLimit,
            FluxDeviceConfigComponent::new
    );

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FluxDeviceConfigComponent(int oId, Optional<String> oName, Optional<Integer> oPriority, Optional<Boolean> oSurgeMode, Optional<Long> oLimit, Optional<Boolean> oDisableLimit) &&
                networkId() == oId &&
                customName().equals(oName) &&
                priority().equals(oPriority) &&
                surgeMode().equals(oSurgeMode) &&
                limit().equals(oLimit) &&
                disableLimit().equals(oDisableLimit);
    }

    public long getLimit() {
        return limit.orElse(0L);
    }

    public int getPriority() {
        return priority.orElse(0);
    }

    public FluxDeviceConfigComponent withNetwork(int networkId) {
        return new FluxDeviceConfigComponent(networkId, customName, priority, surgeMode, limit, disableLimit);
    }

    public FluxDeviceConfigComponent withNetworkAndName(int networkId, @Nonnull String name) {
        return new FluxDeviceConfigComponent(networkId, Optional.of(name), priority, surgeMode, limit, disableLimit);
    }
}
