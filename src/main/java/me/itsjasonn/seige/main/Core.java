package me.itsjasonn.seige.main;

import java.io.File;

import me.itsjasonn.seige.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import me.itsjasonn.seige.commands.Hub;
import me.itsjasonn.seige.commands.Siege;
import me.itsjasonn.seige.listener.EntityStats;
import me.itsjasonn.seige.listener.PlayerInteractAtEntity;
import me.itsjasonn.seige.listener.PlayerJoin;
import me.itsjasonn.seige.listener.PlayerQuit;
import me.itsjasonn.seige.listener.ProjectileHit;
import me.itsjasonn.seige.map.Map;
import me.itsjasonn.seige.utils.MySQLManager;
import me.itsjasonn.seige.utils.Server;

public class Core extends JavaPlugin implements PluginMessageListener {
	private MySQLManager sqlManager;
	
	@SuppressWarnings("deprecation")
	public void onEnable() {
		File config = new File(getDataFolder(), "config.yml");
		if(!config.exists()) {
			saveDefaultConfig();
		}
		MetricsLite metrics = new MetricsLite(this);
		Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "-----------========      Siege     ========-----------");
		Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "- All the files in this plugin are 'read-only'. This means that you are not allowed to open, remove, change, replace the file in any case!");
		Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "- If an error occurs you are supposed to contact the developer and DO NOT try out things yourself.");
		Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "- Report bugs to the developer if there are any.");
		Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "-----------=========================-----------");

		getCommand("Hub").setExecutor(new Hub());
		getCommand("Siege").setExecutor(new Siege());

		new Plugin(this);
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerInteractAtEntity(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerQuit(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new EntityStats(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new ProjectileHit(this), this);
		for (Player players : Bukkit.getOnlinePlayers()) {
			players.spigot().setCollidesWithEntities(false);
		}
		Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
		for (Player players : Bukkit.getOnlinePlayers()) {
			players.sendMessage(ChatColor.RED + "You have been disconnected from the server to avoid any problems during the reload process. After this process you will be able to join again.");
			Server server = new me.itsjasonn.seige.utils.Server("lobby");
			server.sendPlayerToServer(players);
		}
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				for(World worlds : Bukkit.getServer().getWorlds()) {
					worlds.setStorm(false);
					worlds.setThundering(false);
				}
			}
		}, 0L, 8);
		
		if(getConfig().getBoolean("mysql.enabled")) {
			sqlManager = new MySQLManager("siegedb", getConfig().getString("mysql.ip"), getConfig().getString("mysql.port"), getConfig().getString("mysql.username"), getConfig().getString("mysql.password"));
			try {
				sqlManager.createTable("stats", new String[] {"kills", "deaths", "gamesWon"});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void onDisable() {
		for (Player players : Bukkit.getOnlinePlayers()) {
			players.setScoreboard(Bukkit.getServer().getScoreboardManager().getNewScoreboard());
			if (Map.getMapManager().isInGame(players)) {
				Map.getMapManager().TeleportToHub(players);
				Map.getMapManager().removePlayer(players, false);
			}
		}
	}

	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals("BungeeCord")) {
			return;
		}
	}
	
	public MySQLManager getSQLManager() {
		return this.sqlManager;
	}
}
