package com.chaseoes.tf2.commands;

import com.chaseoes.tf2.localization.Localizers;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class HelpCreateMapCommand {

    static HelpCreateMapCommand instance = new HelpCreateMapCommand();

    public static HelpCreateMapCommand getCommand() {
        return instance;
    }

    public void execHelpCreateMapCommand(CommandSender cs, String[] strings, Command cmnd) {
        cs.sendMessage(ChatColor.AQUA + "[" + ChatColor.GOLD + "---------------" + ChatColor.AQUA + "]" + ChatColor.DARK_AQUA + " Team Fortress 2 Help " + ChatColor.AQUA + "[" + ChatColor.GOLD + "---------------" + ChatColor.AQUA + "]");
        if (cs.hasPermission("tf2.create")) {
            Localizers.getDefaultLoc().TF2_HELP_CREATE_MAP.send(cs);
        }
    }

}
