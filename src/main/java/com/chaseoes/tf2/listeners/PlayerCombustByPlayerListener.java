package com.chaseoes.tf2.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;

import com.chaseoes.tf2.GamePlayer;
import com.chaseoes.tf2.GameUtilities;

public class PlayerCombustByPlayerListener implements Listener {

 @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCombust(EntityCombustByEntityEvent event) {
    	if (event.isCancelled()) {
            return;
        }
    	if (event.getEntity() instanceof Player) {
    		Player combustee = (Player) event.getEntity();
    		GamePlayer gCombustee = GameUtilities.getUtilities().getGamePlayer(combustee);
    		if (gCombustee.isIngame()) {
    			if (event.getCombuster() instanceof Player) {
    				GamePlayer gcombustee = GameUtilities.getUtilities().getGamePlayer(combustee);
    				Player combuster = (Player) event.getCombuster();
    				GamePlayer gcombuster = GameUtilities.getUtilities().getGamePlayer(combuster);
    				if (!gcombuster.isIngame()) {
    					event.setCancelled(true);
    					return;
    				}
    				if (gcombustee.getTeam() == gcombuster.getTeam()) {
    					event.setDuration(0);
    					event.setCancelled(true);
    					return;
    				}

    				if (!gcombuster.getGame().getMapName().equalsIgnoreCase(gcombustee.getGame().getMapName())) {
    					event.setCancelled(true);
    					return;
    				}
    			}      
    		}
    	}
    }
}