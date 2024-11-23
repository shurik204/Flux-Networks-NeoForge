package sonar.fluxnetworks.register;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;
import sonar.fluxnetworks.FluxNetworks;

public class RegistrySounds {
    public static final ResourceLocation BUTTON_CLICK_KEY = FluxNetworks.location("button");

    public static final DeferredHolder<SoundEvent, SoundEvent> BUTTON_CLICK = DeferredHolder.create(BuiltInRegistries.SOUND_EVENT.key(), BUTTON_CLICK_KEY);

    static void register(RegisterEvent.RegisterHelper<SoundEvent> helper) {
        helper.register(BUTTON_CLICK_KEY, SoundEvent.createVariableRangeEvent(BUTTON_CLICK_KEY));
    }

    private RegistrySounds() {}
}
