/*
 * MCDocsPlus by RTRD (Originally by Tazzernator)
 * (RTRD ~ http://rtrd.no)
 * 
 * THIS PLUGIN IS LICENSED UNDER THE WTFPL - (Do What The Fuck You Want To Public License)
 * 
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details.
 * 
 * TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *   
 * 0. You just DO WHAT THE FUCK YOU WANT TO.
 *   
 * */

/*
 * 
 * MCDocsPlus by RTRD
 * (RTRD ~ http://rtrd.no)
 *    
 */

package no.rtrd.bukkit.mcdocsplus;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import no.rtrd.bukkit.mcdocsplus.Listener;

/**
 * MCDocsPlus Plugin for Bukkit
 * 
 * @author RTRD (RTRD ~ http://rtrd.no)
 * 
 */

public class Main extends JavaPlugin {

	private final Listener playerListener = new Listener(this);
	public static final Logger log = Logger.getLogger("Minecraft");

	Configuration config;

	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("" + pdfFile.getName() + " v" + pdfFile.getVersion()
				+ " by RTRD succesfully shutdown.");
	}

	public void onEnable() {

		config = getConfiguration();

		this.playerListener.setupConfig(config);

		// Register our events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener,
				Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS,
		this.playerListener, Priority.Normal, this);

		// Check all is well
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("" + pdfFile.getName() + " v" + pdfFile.getVersion()
				+ " by RTRD succesfully loaded.");
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmnd, String label,
			String[] args) {
		Player player = null;
		boolean commandLogEnabled = Listener.commandLogEnabled;
		PluginDescriptionFile pdfFile = this.getDescription();

		if (cmnd.getName().equalsIgnoreCase("mcdp")) {
			String arg = "";
			String name = "";

			if ((cs instanceof Player)) {
				player = (Player) cs;
			}

			try {
				arg = args[0];
			} catch (ArrayIndexOutOfBoundsException e) {
				// only return false here if you don't want /mdcp by itself to
				// do anything,
				// otherwise just put whatever code you want to execute and
				// return true
				if (player.hasPermission("mcdocsplus.admin")) {
					player.sendMessage(ChatColor.AQUA
							+ "[MCDocs+]" + ChatColor.RED
							+ " Try /mcdp help");
					// Log our user using the command.
					if (commandLogEnabled == true) {
						log.info("MCDocsPlus: " + player.getName() + ": /"
								+ cmnd.getName());
					}
				} else {
					cs.sendMessage(ChatColor.RED + "Insufficent Permissions!");
					return true;
				}
				return true;
			}

			if (arg.equalsIgnoreCase("help")) {
				// do help stuff here
				if (player.hasPermission("mcdocsplus.admin")) {
					player.sendMessage(ChatColor.AQUA
							+ "[MCDocs+]" + ChatColor.GRAY + " Available commands:");
					player.sendMessage(ChatColor.DARK_AQUA
							+ "/mcdp help" + ChatColor.GOLD + " - Shows available commands");
					player.sendMessage(ChatColor.DARK_AQUA
							+ "/mcdp reload" + ChatColor.GOLD + " - Reloads MCDocsPlus");
					player.sendMessage(ChatColor.DARK_AQUA
							+ "/mcdp version" + ChatColor.GOLD + " - Shows current version of MCDocsPlus");
					// Log our user using the command.
					if (commandLogEnabled == true) {
						log.info("MCDocsPlus: " + player.getName() + ": /"
								+ cmnd.getName() + " help");
					}
				}
				else {
					cs.sendMessage(ChatColor.RED + "Insufficent Permissions!");
					return true;
				}
				return true;
			}
			if (arg.equalsIgnoreCase("reload")) {
				// do version stuff here
				if (player.hasPermission("mcdocsplus.admin")) {
					playerListener.loadConfig();
					player.sendMessage(ChatColor.AQUA + "MCDocsPlus"+ ChatColor.DARK_AQUA +" has been reloaded.");
					// Log our user using the command.
					if (commandLogEnabled == true) {
						log.info("MCDocsPlus: Reloaded by " + player.getName());
					}
				} else {
					cs.sendMessage(ChatColor.RED + "Insufficent Permissions!");
					return true;
				}
				return true;
			}
			if (arg.equalsIgnoreCase("version")) {
				// do reload stuff here
				if (player.hasPermission("mcdocsplus.admin")) {
					player.sendMessage(ChatColor.AQUA + "MCDocsPlus" + ChatColor.GOLD + " v"
							+ pdfFile.getVersion() + ChatColor.DARK_AQUA + " by RTRD");
					// Log our user using the command.
					if (commandLogEnabled == true) {
						log.info("MCDocsPlus: " + player.getName() + ": /"
								+ cmnd.getName() + " version");
					}
				} else {
					cs.sendMessage(ChatColor.RED + "Insufficent Permissions!");
					return true;
				}
				return true;
			}
			// then finally return false if they didn't do it right
			return false;
		}
		return false;
	}
}
