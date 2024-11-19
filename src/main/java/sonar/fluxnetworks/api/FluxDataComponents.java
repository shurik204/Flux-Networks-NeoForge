package sonar.fluxnetworks.api;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.common.data.FluxDataComponent;
import sonar.fluxnetworks.common.util.CodecsExtras;

import java.util.Optional;

public class FluxDataComponents {
    public static final DeferredRegister<DataComponentType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE.key(), FluxNetworks.MODID);

    public static final DataComponentType<FluxDataComponent> FLUX_DATA = DataComponentType.<FluxDataComponent>builder().networkSynchronized(FluxDataComponent.STREAM_CODEC).persistent(FluxDataComponent.CODEC).build();
    public static final DataComponentType<Optional<Integer>> FLUX_COLOR = DataComponentType.<Optional<Integer>>builder().persistent(Codec.optionalField("value", Codec.INT, false).codec()).networkSynchronized(ByteBufCodecs.optional(ByteBufCodecs.VAR_INT)).build();
    public static final DataComponentType<FluxDataComponent> FLUX_CONFIG = DataComponentType.<FluxDataComponent>builder().networkSynchronized(FluxDataComponent.STREAM_CODEC).persistent(FluxDataComponent.CODEC).build();
    public static final DataComponentType<Long> STORED_ENERGY = DataComponentType.<Long>builder().persistent(CodecsExtras.NON_NEGATIVE_LONG).networkSynchronized(ByteBufCodecs.VAR_LONG).build();

    static {
        REGISTRY.register(FluxConstants.FLUX_DATA_COMPONENT, () -> FLUX_DATA);
        REGISTRY.register(FluxConstants.FLUX_COLOR_COMPONENT, () -> FLUX_COLOR);
        REGISTRY.register(FluxConstants.FLUX_CONFIG_COMPONENT, () -> FLUX_CONFIG);
        REGISTRY.register(FluxConstants.STORED_ENERGY_COMPONENT, () -> STORED_ENERGY);
    }
}
