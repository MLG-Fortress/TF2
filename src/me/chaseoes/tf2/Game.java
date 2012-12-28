package me.chaseoes.tf2;

import me.chaseoes.tf2.capturepoints.CapturePointUtilities;
import me.chaseoes.tf2.lobbywall.LobbyWall;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.kitteh.tag.TagAPI;

import java.util.*;

public class Game {

    TF2 plugin;
    Map map;
    GameStatus status = GameStatus.WAITING;
    public boolean redHasBeenTeleported = false;
    public int time = 0;

    public HashSet<String> playersInGame = new HashSet<String>();
    public HashMap<String, Integer> kills = new HashMap<String, Integer>();
    public HashMap<String, Integer> deaths = new HashMap<String, Integer>();
    public HashMap<String, ItemStack[]> inventories = new HashMap<String, ItemStack[]>();
    public HashMap<String, ItemStack[]> armorinventories = new HashMap<String, ItemStack[]>();
    public HashMap<String, String> teams = new HashMap<String, String>();
    public HashMap<String, String> capturepoints = new HashMap<String, String>();

    //TODO: Convert to player metadata
    public HashSet<String> makingchangeclassbutton = new HashSet<String>();
    public HashSet<String> usingchangeclassbutton = new HashSet<String>();

    public Game(Map m, TF2 plugin) {
        map = m;
        this.plugin = plugin;
    }

    public void startMatch() {
        setStatus(GameStatus.INGAME);
        CapturePointUtilities.getUtilities().uncaptureAll(map);
        Schedulers.getSchedulers().startTimeLimitCounter(map.getName());
        Schedulers.getSchedulers().startRedTeamCountdown(map.getName());
        for (String player : playersInGame) {
            Player p = Bukkit.getPlayerExact(player);
            if (getTeam(p).equalsIgnoreCase("blue")) {
                p.teleport(MapUtilities.getUtilities().loadTeamSpawn(map.getName(), "blue"));
            }
        }
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (String player : getIngameList()) {
                    if (getTeam(Bukkit.getPlayerExact(player)).equalsIgnoreCase("red")) {
                        plugin.getServer().getPlayerExact(player).teleport(MapUtilities.getUtilities().loadTeamSpawn(map.getName(), "red"));
                    }
                }
                redHasBeenTeleported = true;
                Schedulers.getSchedulers().stopRedTeamCountdown(map.getName());
            }
        }, MapConfiguration.getMaps().getMap(map.getName()).getInt("teleport-red-team") * 20L);
    }

    public void stopMatch() {
        setStatus(GameStatus.WAITING);
        Schedulers.getSchedulers().stopRedTeamCountdown(map.getName());
        Schedulers.getSchedulers().stopTimeLimitCounter(map.getName());
        for (String player : getIngameList()) {
            Player p = Bukkit.getPlayerExact(player);
            leaveGame(p);
            p.sendMessage("§e[TF2] The game has ended.");
        }
        redHasBeenTeleported = false;
    }

    public void winGame(String team) {
        String[] winlines = new String[4];
        winlines[0] = " ";
        winlines[1] = "§4§lRed Team";
        if (team.equalsIgnoreCase("blue")) {
            winlines[1] = "§9§lBlue Team";
        }
        winlines[2] = "§a§lWins!";
        winlines[3] = " ";
        String te = " §4§lred §r§e";
        if (team.equalsIgnoreCase("blue")) {
            te = " §9§lblue §r§e";
        }
        LobbyWall.getWall().setAllLines(map.getName(), null, winlines, false, true);
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                String[] creditlines = new String[4];
                creditlines[0] = " ";
                creditlines[1] = "§lTF2 Plugin By:";
                creditlines[2] = "§9chaseoes";
                creditlines[3] = " ";
                LobbyWall.getWall().unsetNoUpdate(map.getName());
                LobbyWall.getWall().setAllLines(map.getName(), 4, creditlines, false, true);
            }
        }, 120L);
        plugin.getServer().broadcastMessage("§eThe" + te + "team has won on the map §l" + map.getName() + "§r§e!");
        stopMatch();
    }

    public void joinGame(Player player, String team) {
        inventories.put(player.getName(), player.getInventory().getContents());
        armorinventories.put(player.getName(), player.getInventory().getArmorContents());
        player.getInventory().clear();
        playersInGame.add(player.getName());
        teams.put(player.getName(), team);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setGameMode(GameMode.SURVIVAL);
        player.teleport(MapUtilities.getUtilities().loadTeamLobby(map.getName(), team));
        LobbyWall.getWall().update();
        TagAPI.refreshPlayer(player);
        kills.put(player.getName(), 0);
        deaths.put(player.getName(), 0);
        if (getStatus().equals(GameStatus.WAITING)) {
            if ((double) getIngameList().size() / MapConfiguration.getMaps().getMap(map.getName()).getInt("playerlimit") * 100 >= plugin.getConfig().getInt("autostart-percent")) {
                Schedulers.getSchedulers().startCountdown(map.getName());
                setStatus(GameStatus.STARTING);
            }
        }
        player.updateInventory();
    }

    public void leaveGame(Player player) {
        kills.remove(player.getName());
        deaths.remove(player.getName());
        player.teleport(MapUtilities.getUtilities().loadLobby());
        player.setGameMode(plugin.getServer().getDefaultGameMode());
        TagAPI.refreshPlayer(player);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        if (getStatus().equals(GameStatus.STARTING) && getIngameList().size() == 1) {
            stopMatch();
        }
        playersInGame.remove(player.getName());
        teams.remove(player.getName());
        checkQueue();
        if (getIngameList().size() == 0) {
            stopMatch();
        }
        LobbyWall.getWall().update();
        player.getInventory().setArmorContents(null);
        player.getInventory().setContents(null);
        if (inventories.containsKey(player.getName())) {
            player.getInventory().setContents(inventories.get(player.getName()));
            inventories.remove(player.getName());
        }
        if (armorinventories.containsKey(player.getName())) {
            player.getInventory().setArmorContents(armorinventories.get(player.getName()));
            armorinventories.remove(player.getName());
        }
        player.updateInventory();
    }

    public Integer getAmountOnTeam(String team) {
        Integer red = 0;
        Integer blue = 0;
        for (String player : playersInGame) {
            if (teams.get(player).equalsIgnoreCase("red")) {
                red++;
            }
            if (teams.get(player).equalsIgnoreCase("blue")) {
                blue++;
            }
        }
        if (team.equalsIgnoreCase("blue")) {
            return blue;
        }
        return red;
    }

    public String decideTeam() {
        Integer red = 0;
        Integer blue = 0;
        for (String player : playersInGame) {
            if (teams.get(player).equalsIgnoreCase("red")) {
                red++;
            }
            if (teams.get(player).equalsIgnoreCase("blue")) {
                blue++;
            }

        }
        if (red > blue) {
            return "blue";
        }
        return "red";
    }

    public void setStatus(GameStatus s) {
        status = s;
    }

    public GameStatus getStatus() {
        return status;
    }

    public String getName(){
        return map.getName();
    }

    public String getTeam(Player p) {
        if (p != null)
            return teams.get(p.getName());
        return null;
    }

    public String getTeamColor(Player player) {
        String color = "§9§l";
        if (GameUtilities.getUtilities().getTeam(player).equalsIgnoreCase("red")) {
            color = "§4§l";
        }
        return color;
    }

    public boolean isIngame(Player p) {
        return playersInGame.contains(p.getName());
    }

    public List<String> getIngameList() {
        return new ArrayList<String>(playersInGame);
    }

    public void updateTime(int time) {
        this.time = time;
    }

    public Integer getTimeLeftSeconds() {
        return MapConfiguration.getMaps().getMap(map.getName()).getInt("timelimit") - time;
    }

    public String getTimeLeft() {
        if (getStatus().equals(GameStatus.WAITING) || getStatus().equals(GameStatus.STARTING)) {
            return "Not Started";
        }
        int time = getTimeLeftSeconds();
        int hours = time / (60 * 60);
        time = time % (60 * 60);
        int minutes = time / 60;
        time = time % 60;

        if (hours == 0) {
            return minutes + "m " + time + "s";
        }
        return Math.abs(hours) + "h " + Math.abs(minutes) + "m " + Math.abs(time) + "s";
    }

    public String getTimeLeftPretty() {
        if (getStatus().equals(GameStatus.WAITING) || getStatus().equals(GameStatus.STARTING)) {
            return "Not Started";
        }
        Integer time = getTimeLeftSeconds();
        int hours = time / (60 * 60);
        time = time % (60 * 60);
        int minutes = time / 60;
        time = time % 60;

        if (hours == 0) {
            if (time == 0) {
                return minutes + " §9minutes";
            }
            if (minutes == 0) {
                return time + " §9seconds";
            }
            return minutes + " §9minutes §b" + time + " §9seconds";
        }
        return Math.abs(hours) + "h " + Math.abs(minutes) + "m " + Math.abs(time) + "s";
    }

    public void broadcast(String message){
        for(String player : playersInGame){
            Bukkit.getServer().getPlayerExact(player).sendMessage(message);
        }
    }

    public void checkQueue() {
        try {
            Queue q = plugin.getQueue(map.getName());
            if (q != null) {
                String team = GameUtilities.getUtilities().decideTeam(map.getName());
                Iterator<String> it = q.getQueue().iterator();
                while (it.hasNext()) {
                    String pl = it.next();
                    Player p = plugin.getServer().getPlayerExact(pl);
                    if (p != null) {
                        Integer position = q.getPosition(p.getName());
                        if (position != null) {
                            if (q.contains(p)) {
                                q.remove(p.getName());
                            }
                            if (!(position <= MapConfiguration.getMaps().getMap(map.getName()).getInt("playerlimit"))) {
                                p.sendMessage("§e[TF2] You are #" + position + " in line for the map §l" + map + "§r§e.");
                            } else {
                                joinGame(p, team);
                                q.remove(p.getName());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
