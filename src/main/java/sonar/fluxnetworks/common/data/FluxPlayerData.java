package sonar.fluxnetworks.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.INBTSerializable;
import sonar.fluxnetworks.FluxConfig;
import sonar.fluxnetworks.api.FluxConstants;
import sonar.fluxnetworks.common.util.FluxUtils;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static sonar.fluxnetworks.api.FluxConstants.INVALID_NETWORK_ID;

@ParametersAreNonnullByDefault
public class FluxPlayerData implements INBTSerializable<CompoundTag> {

    private boolean superAdmin = false;
    private int wirelessMode = 0;
    private int wirelessNetwork = INVALID_NETWORK_ID;

    public boolean isSuperAdmin() {
        return superAdmin;
    }

    public boolean setSuperAdmin(boolean superAdmin) {
        if (this.superAdmin != superAdmin) {
            this.superAdmin = superAdmin;
            return true;
        }
        return false;
    }

    public int getWirelessMode() {
        return this.wirelessMode;
    }

    public void setWirelessMode(int wirelessMode) {
        this.wirelessMode = wirelessMode;
    }

    public int getWirelessNetwork() {
        return this.wirelessNetwork;
    }

    public void setWirelessNetwork(int wirelessNetwork) {
        this.wirelessNetwork = wirelessNetwork;
    }

    public void copy(FluxPlayerData other) {
        superAdmin = other.superAdmin;
        wirelessMode = other.wirelessMode;
        wirelessNetwork = other.wirelessNetwork;
    }

    // Utils (Server-side only)
    public static boolean canActivateSuperAdmin(ServerPlayer player) {
        return FluxConfig.enableSuperAdmin && player.hasPermissions(FluxConfig.superAdminRequiredPermission);
    }

    public static boolean isPlayerSuperAdmin(@Nonnull ServerPlayer player) {
        if (FluxConfig.enableSuperAdmin) {
            return FluxUtils.getPlayerData(player).isSuperAdmin();
        }
        return false;
    }

    @Nonnull
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(FluxConstants.SUPER_ADMIN, superAdmin);
        tag.putInt(FluxConstants.WIRELESS_MODE, wirelessMode);
        tag.putInt(FluxConstants.WIRELESS_NETWORK, wirelessNetwork);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        superAdmin = nbt.getBoolean(FluxConstants.SUPER_ADMIN);
        wirelessMode = nbt.getInt(FluxConstants.WIRELESS_MODE);
        wirelessNetwork = nbt.getInt(FluxConstants.WIRELESS_NETWORK);
    }
}
