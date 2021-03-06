package com.chaseoes.tf2.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.chaseoes.tf2.Game;
import com.chaseoes.tf2.GamePlayer;
import com.chaseoes.tf2.GameUtilities;
import com.chaseoes.tf2.commands.SpectateCommand;

public class PlayerQuitListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        GamePlayer gPlayer = GameUtilities.getUtilities().getGamePlayer(event.getPlayer());
        if (gPlayer.isIngame()) {
            Game game = gPlayer.getGame();
            if (game != null) {
                game.leaveGame(gPlayer.getPlayer());
            }
        }
        SpectateCommand.getCommand().stopSpectating(event.getPlayer());
        SpectateCommand.getCommand().playerLogout(event.getPlayer());
        GameUtilities.getUtilities().playerLeaveServer(event.getPlayer());
    }

}
