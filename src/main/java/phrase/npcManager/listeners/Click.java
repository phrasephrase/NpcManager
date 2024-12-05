package phrase.npcManager.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import phrase.npcManager.npc.Npc;

public class Click implements Listener {

    @EventHandler
    public void onClick(PlayerInteractEvent event) {

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Npc.Path.setPoint(event.getPlayer(), event.getClickedBlock());

    }
}
