package sonar.fluxnetworks.register;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.common.data.FluxPlayerData;

import java.util.function.Supplier;

public class DataAttachments {
    public static final DeferredRegister<AttachmentType<?>> REGISTER = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, FluxNetworks.MODID);
    public static final Supplier<AttachmentType<FluxPlayerData>> PLAYER_DATA = REGISTER.register(
            "player_data", () -> AttachmentType.serializable(FluxPlayerData::new).build()
    );
}
