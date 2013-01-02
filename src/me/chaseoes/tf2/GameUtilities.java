package me.chaseoes.tf2;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class GameUtilities {

    public TF2 plugin;
    static GameUtilities instance = new GameUtilities();
    public HashMap<String, Game> games = new HashMap<String, Game>();
    public HashMap<String, GamePlayer> players = new HashMap<String, GamePlayer>();

    private GameUtilities() {

    }

    public static GameUtilities getUtilities() {
        return instance;
    }

    public void setup(TF2 p) {
        plugin = p;
    }

    public void addGame(Map map) {
        games.put(map.getName(), new Game(map, plugin));
    }
    
    public Game getGame(Map map) {
        return games.get(map.getName());
    }

    public boolean isIngame(Player player) {
        return getCurrentGame(player) != null;
    }

    public Game getCurrentGame(Player player) {
        for (Game g : games.values()) {
            for (String name : g.playersInGame.keySet()) {
                if (player.getName().equalsIgnoreCase(name)) {
                    return g;
                }
            }
        }
        return null;
    }
    
    public GamePlayer getGamePlayer(Player player) {
        for (Game g : games.values()) {
            for (GamePlayer gp : g.playersInGame.values()) {
                if (gp.getName().equalsIgnoreCase(player.getName())) {
                    return gp;
                }
            }
        }
        
        if (!players.containsKey(player.getName())) {
            players.put(player.getName(), new GamePlayer(player));
        }
        
        return players.get(player.getName());
    }

    public void playerJoinServer(Player player) {
        players.put(player.getName(), new GamePlayer(player));
    }

    public void playerLeaveServer(Player player) {
        players.remove(player.getName());
    }

    public Game removeGame(Map m) {
        return games.remove(m.getName());
    }
}