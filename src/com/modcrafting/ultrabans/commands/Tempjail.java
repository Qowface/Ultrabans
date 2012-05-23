package com.modcrafting.ultrabans.commands;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Tempjail implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.tempjail";
	public Tempjail(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean auth = false;
		boolean anon = false;
		Player player = null;
		String admin = config.getString("defAdminName", "server");
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission) || player.isOp()) auth = true;
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		if (args.length < 3) return false;

		String p = args[0]; // Get the victim's potential name
		
		if(plugin.autoComplete)
			p = plugin.util.expandName(p);
		Player victim = plugin.getServer().getPlayer(p);
		
		//Figured this out after the fact...... Ugh
		//Neglect to study bukkit. 
		//Screw it if it works... go with it.
		//victim = Bukkit.getOfflinePlayer(p).getPlayer();

		
		String reason = config.getString("defReason", "not sure");
		boolean broadcast = true;
		if(args.length > 3){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = plugin.util.combineSplit(4, args, " ");
			}else{
				if(args[1].equalsIgnoreCase("-a")){
					anon = true;
					reason = plugin.util.combineSplit(4, args, " ");
				}else{
				reason = plugin.util.combineSplit(3, args, " ");
				}
			}
		}

		if (anon){
			admin = config.getString("defAdminName", "server");
		}

		long tempTime = plugin.util.parseTimeSpec(args[1],args[2]);
		if(tempTime == 0)
			return false;
		long temp = System.currentTimeMillis()/1000+tempTime; //epoch time
		//Separate for Online-Offline
		if(victim != null){
			if(victim.getName() == admin){
				sender.sendMessage(ChatColor.RED + "You cannot emotempjail yourself!");
				return true;
			}
			if(victim.hasPermission( "ultraban.override.tempjail")){
				sender.sendMessage(ChatColor.RED + "Your tempjail has been denied! Player Notified!");
				victim.sendMessage(ChatColor.RED + "Player:" + admin + " Attempted to tempjail you!");
				return true;
			}	
			if(plugin.jailed.contains(victim.getName().toLowerCase())){
				sender.sendMessage(ChatColor.BLUE + victim.getName() +  ChatColor.GRAY + " is already jailed for " + reason);
				return true;
			}
			plugin.tempJail.put(victim.getName().toLowerCase(), temp);
			plugin.db.addPlayer(victim.getName(), reason, admin, temp, 6);
			plugin.jailed.add(p.toLowerCase());
			Location stlp = getJail();
			victim.teleport(stlp);
			log.log(Level.INFO, "[UltraBan] " + admin + " tempjailed player " + victim.getName() + ".");
			String tempjailMsgVictim = config.getString("messages.tempjailMsgVictim", "You have been temp. jailed by %admin%. Reason: %reason%!");
			tempjailMsgVictim = tempjailMsgVictim.replaceAll(plugin.regexAdmin, admin);
			tempjailMsgVictim = tempjailMsgVictim.replaceAll(plugin.regexReason, reason);
			if(broadcast){
				String tempjailMsgBroadcast = config.getString("messages.tempjailMsgBroadcast", "%victim% was temp. jailed by %admin%. Reason: %reason%!");
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll(plugin.regexReason, reason);
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll(plugin.regexVictim, victim.getName());
				plugin.getServer().broadcastMessage(plugin.util.formatMessage(tempjailMsgBroadcast));
			}else{
				String tempjailMsgBroadcast = config.getString("messages.tempjailMsgBroadcast", "%victim% was temp. jailed by %admin%. Reason: %reason%!");
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll(plugin.regexReason, reason);
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll(plugin.regexVictim, victim.getName());
				sender.sendMessage(plugin.util.formatMessage(":S:" + tempjailMsgBroadcast));
			}
		}else{
			victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
			if(victim != null){
				if(victim.hasPermission( "ultraban.override.tempjail")){
					sender.sendMessage(ChatColor.RED + "Your tempjail has been denied!");
					return true;
				}
			}
			if(plugin.jailed.contains(p.toLowerCase())){
				sender.sendMessage(ChatColor.BLUE + p +  ChatColor.GRAY + " is already jailed for " + reason);
				return true;
			}
			plugin.tempJail.put(p.toLowerCase(), temp);
			plugin.jailed.add(p.toLowerCase());
			plugin.db.addPlayer(p, reason, admin, temp, 6);
			log.log(Level.INFO, "[UltraBan] " + admin + " temp jail player " + p + ".");
			if(broadcast){
				String tempjailMsgBroadcast = config.getString("messages.tempjailMsgBroadcast", "%victim% was temp. jailed by %admin%. Reason: %reason%!");
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll(plugin.regexReason, reason);
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll(plugin.regexVictim, p);
				plugin.getServer().broadcastMessage(plugin.util.formatMessage(tempjailMsgBroadcast));
			}else{
				String tempjailMsgBroadcast = config.getString("messages.tempjailMsgBroadcast", "%victim% was temp. jailed by %admin%. Reason: %reason%!");
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll(plugin.regexReason, reason);
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll(plugin.regexVictim, p);
				sender.sendMessage(plugin.util.formatMessage(":S:" + tempjailMsgBroadcast));
			}
		}
		return true;
	}

    public Location getJail(){
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
        Location setlp = new Location(
                plugin.getServer().getWorld(config.getString("jail.world", plugin.getServer().getWorlds().get(0).getName())),
                config.getInt("jail.x", 0),
                config.getInt("jail.y", 0),
                config.getInt("jail.z", 0));
        	return setlp;
    }
}
