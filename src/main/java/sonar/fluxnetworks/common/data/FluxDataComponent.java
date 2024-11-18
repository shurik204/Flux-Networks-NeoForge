package sonar.fluxnetworks.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import sonar.fluxnetworks.api.FluxConstants;
import sonar.fluxnetworks.common.util.CodecsExtras;

import java.util.Optional;

public record FluxDataComponent(int networkId, Optional<String> customName, Optional<Long> limit, Optional<Long> buffer, Optional<Integer> priority, Optional<Long> energy) {
    public static final FluxDataComponent EMPTY = new FluxDataComponent(-1, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    public static final Codec<FluxDataComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    CodecsExtras.NETWORK_ID.fieldOf(FluxConstants.NETWORK_ID).forGetter(FluxDataComponent::networkId),
                    Codec.STRING.optionalFieldOf(FluxConstants.CUSTOM_NAME).forGetter(FluxDataComponent::customName),
                    CodecsExtras.NON_NEGATIVE_LONG.optionalFieldOf(FluxConstants.LIMIT).forGetter(FluxDataComponent::limit),
                    CodecsExtras.NON_NEGATIVE_LONG.optionalFieldOf(FluxConstants.BUFFER).forGetter(FluxDataComponent::buffer),
                    CodecsExtras.INTEGER.optionalFieldOf(FluxConstants.PRIORITY).forGetter(FluxDataComponent::priority),
                    CodecsExtras.NON_NEGATIVE_LONG.optionalFieldOf(FluxConstants.ENERGY).forGetter(FluxDataComponent::energy)
            )
            .apply(instance, FluxDataComponent::new)
    );

    public static final StreamCodec<ByteBuf, FluxDataComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            FluxDataComponent::networkId,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            FluxDataComponent::customName,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_LONG),
            FluxDataComponent::limit,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_LONG),
            FluxDataComponent::buffer,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT),
            FluxDataComponent::priority,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_LONG),
            FluxDataComponent::energy,
            FluxDataComponent::new
    );

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FluxDataComponent(int oId, Optional<String> oName, Optional<Long> oLimit, Optional<Long> oBuffer, Optional<Integer> oPriority, Optional<Long> oEnergy) &&
                networkId() == oId &&
                customName().equals(oName) &&
                limit().equals(oLimit) &&
                buffer().equals(oBuffer) &&
                priority().equals(oPriority) &&
                energy().equals(oEnergy);
    }

    public long getEnergy() {
        return energy.orElse(0L);
    }

    public long getLimit() {
        return limit.orElse(0L);
    }

    public int getPriority() {
        return priority.orElse(0);
    }

    public FluxDataComponent withEnergy(long energy) {
        return new FluxDataComponent(networkId, customName, limit, buffer, priority, Optional.of(energy));
    }

    public FluxDataComponent withNetwork(int networkId) {
        return new FluxDataComponent(networkId, customName, limit, buffer, priority, energy);
    }

    public FluxDataComponent withNetworkAndName(int networkId, String name) {
        return new FluxDataComponent(networkId, Optional.of(name), limit, buffer, priority, energy);
    }

    public CompoundTag asNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(FluxConstants.NETWORK_ID, networkId);
        customName.ifPresent(name -> tag.putString(FluxConstants.CUSTOM_NAME, name));
        limit.ifPresent(l -> tag.putLong(FluxConstants.LIMIT, l));
        buffer.ifPresent(b -> tag.putLong(FluxConstants.BUFFER, b));
        priority.ifPresent(p -> tag.putInt(FluxConstants.PRIORITY, p));
        energy.ifPresent(e -> tag.putLong(FluxConstants.ENERGY, e));
        return tag;
    }
}
