package sonar.fluxnetworks.common.level;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;
import sonar.fluxnetworks.FluxConfig;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.common.device.TileFluxDevice;

public class FluxChunkLoading {
    public static final TicketController CONTROLLER = new TicketController(FluxNetworks.location("device"), FluxChunkLoading::forceloadChunks);

    public static void forceloadChunks(ServerLevel level, TicketHelper helper) {
        if (!FluxConfig.enableChunkLoading) {
            helper.getBlockTickets().keySet().forEach(helper::removeAllTickets);
            FluxNetworks.LOGGER.info("Removed all chunk loaders because chunk loading is disabled");
        } else {
            int chunks = 0;
            for (var entry : helper.getBlockTickets().entrySet()) {
                // this also loads the chunk
                if (level.getBlockEntity(entry.getKey()) instanceof TileFluxDevice e) {
                    e.setForcedLoading(true);
                    var pair = entry.getValue();
                    int count = 0;
                    count += pair.nonTicking().size() + pair.ticking().size();
                    if (count != 1) {
                        FluxNetworks.LOGGER.warn("{} in {} didn't load just one chunk {}",
                                entry.getValue(), level.dimension().location(), pair);
                    }
                    chunks += count;
                } else {
                    helper.removeAllTickets(entry.getKey());
                }
            }
            FluxNetworks.LOGGER.info("Loaded {} chunks by {} flux devices in {}",
                    chunks, helper.getBlockTickets().size(), level.dimension().location());
        }
    }
}
