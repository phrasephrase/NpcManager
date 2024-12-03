package phrase.npcManager.listeners;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import phrase.npcManager.npc.Npc;

public class Join implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        ServerPlayer serverPlayer = ((CraftPlayer) event.getPlayer()).getHandle();
        Npc.loadNpc(serverPlayer.connection);

    }
}
