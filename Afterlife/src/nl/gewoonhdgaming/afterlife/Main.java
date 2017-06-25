package nl.gewoonhdgaming.afterlife;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.server.v1_10_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_10_R1.PacketPlayOutChat;

public class Main extends JavaPlugin implements Listener {
	
	public HashMap<UUID, Integer> afterlife = new HashMap<>();
	public HashMap<UUID, BukkitRunnable> cooldownTask;
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
//		if(command.getName().equalsIgnoreCase("afterlife")) {
//			if(sender.hasPermission("AfterLife.Admin")) {
//			if(args.length == 0) {
//				sender.sendMessage(ChatColor.RED + "Te weinig argumenten!");
//				return false;
//			}
//			if(Bukkit.getPlayer(args[0]) == null) {
//				sender.sendMessage(ChatColor.RED + "Speler bestaat niet!");
//				return false;
//			}
//			
//       
//			
//			} else {
//				sender.sendMessage(ChatColor.RED + "Niet genoeg permissie!");
//			}
//		}
    	 if (command.getName().equalsIgnoreCase("al") && sender instanceof Player){
			
			Player p = (Player) sender;
			if (args.length == 0) {
				if (p.hasPermission("Teamspeak.admin")) {
					p.sendMessage(ChatColor.RED + "Development by: Boykev en Rens");
					p.sendMessage(ChatColor.RED + "Product Owner: GewoonHDEnterprise");
					p.sendMessage(ChatColor.RED + "Copyright, contribution not allowed!");
			}
			}			
			else if (args[0].equalsIgnoreCase("info")) {
				if (p.hasPermission("Teamspeak.admin")) {
					p.sendMessage(ChatColor.RED + "AfterLife is een plugin waarin mensen tijdelijk dood zijn zonder deze uit de server te hoeven bannen");
			}
			}
			else {
				p.sendMessage(ChatColor.RED + "Er is wat fout gegaan!");
			}
			return true;
		}
		return false;	
		
	}
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		if(!(e.getEntity() instanceof Player)) {
			return;
		}
		Player p = e.getEntity();
		afterlife.put(p.getUniqueId(), 300);//Default = 300
		p.setGameMode(GameMode.ADVENTURE);
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, Integer.MAX_VALUE, false, false));
        p.setCanPickupItems(false);
        p.sendTitle(ChatColor.AQUA + "U bent dood gegaan", ChatColor.RED + "U bent tijdelijk een geest");
        p.sendMessage(ChatColor.RED + "Je bent nu 5 minuten niet zichbaar voor andere spelers, ook kan je niet chatten, items opakken of commands uitvoeren. Na 5 minuten zal je weer levend zijn en kan je alles weer!");
        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "mute " + p.getName() + " 5m"); //default = 5m
        Bukkit.getOnlinePlayers().forEach((otherPlayer) -> otherPlayer.hidePlayer(p));
        p.setGameMode(GameMode.SURVIVAL);
        p.setCanPickupItems(true);
		 new BukkitRunnable() {
				
				@Override
				public void run() {
					if(!afterlife.get(p.getUniqueId()).equals(0)) {
						afterlife.put(p.getUniqueId(), afterlife.get(p.getUniqueId()) - 1);
					} else {
						afterlife.remove(p.getUniqueId());
						 p.removePotionEffect(PotionEffectType.INVISIBILITY);
                         Bukkit.getOnlinePlayers().forEach((otherPlayer) -> otherPlayer.showPlayer(p));
                         p.sendMessage(ChatColor.AQUA + "Je bent weer levend! Veel succes.");
						this.cancel();
					}
					int i = afterlife.get(p.getUniqueId());
					sendAction(p, ChatColor.WHITE + "Je bent nog voor " + ChatColor.AQUA + i + ChatColor.WHITE + " secondes een geest!");
				}
			}.runTaskTimerAsynchronously(this, 20, 20);
	}
	
	public void sendAction(Player p, String message) {
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte) 2);
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}
	
	@EventHandler
	public void chatEvent(AsyncPlayerChatEvent e) {
		if(afterlife.containsKey(e.getPlayer())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "Je zit in de afterlive, dus kan je niet praten! Je bent nog voor " + afterlife.get(e.getPlayer().getUniqueId()) + " secondes in afterlive!");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void godMode(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if(afterlife.containsKey(p.getUniqueId())) {
				p.sendMessage(ChatColor.RED + "Je zit in de afterlive, dus kan je niet dood gaan! Je bent nog voor "
			+ afterlife.get(p.getUniqueId()) + " secondes in afterlive!");
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void itemPickup(PlayerPickupItemEvent e) {
		if(afterlife.containsKey(e.getPlayer().getUniqueId())) {
			e.getPlayer().sendMessage(ChatColor.RED + "Je zit in de afterlive, dus kan je geen items oppakken! Je bent nog voor "
					+ afterlife.get(e.getPlayer().getUniqueId()) + " secondes in afterlive!");
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onDisconnect(PlayerQuitEvent e) {
		if(afterlife.containsKey(e.getPlayer().getUniqueId())) {
			getConfig().set("sessions." + e.getPlayer().getUniqueId(), afterlife.get(e.getPlayer().getUniqueId()));
			saveConfig();
			afterlife.remove(e.getPlayer().getUniqueId());
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if(getConfig().get("sessions." + e.getPlayer().getUniqueId()) != null || getConfig().getInt("sessions." + e.getPlayer().getUniqueId()) != 0 || getConfig().getInt("sessions." + e.getPlayer().getUniqueId()) > 0) {
			Player p = (Player) e.getPlayer();
			afterlife.put(p.getUniqueId(), getConfig().getInt("sessions." + e.getPlayer().getUniqueId()));//Default = 300
			p.setGameMode(GameMode.ADVENTURE);
	        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, Integer.MAX_VALUE, false, false));
	        p.setCanPickupItems(false);
	        p.sendTitle(ChatColor.AQUA + "U bent dood gegaan", ChatColor.RED + "U bent tijdelijk een geest");
	        p.sendMessage(ChatColor.RED + "Je bent nu " + getConfig().get("sessions." + e.getPlayer().getUniqueId()) + " secondes niet zichbaar voor andere spelers, ook kan je niet chatten, items opakken of commands uitvoeren. Na 5 minuten zal je weer levend zijn en kan je alles weer!");
	        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "mute " + p.getName() + getConfig().getInt("sessions." + e.getPlayer().getUniqueId()) + "s"); //default = 5m
	        Bukkit.getOnlinePlayers().forEach((otherPlayer) -> otherPlayer.hidePlayer(p));
	        p.setGameMode(GameMode.SURVIVAL);
	        p.setCanPickupItems(true);
			 new BukkitRunnable() {
					
					@Override
					public void run() {
						if(!afterlife.get(p.getUniqueId()).equals(0)) {
							afterlife.put(p.getUniqueId(), afterlife.get(p.getUniqueId()) - 1);
						} else {
							afterlife.remove(p.getUniqueId());
							 p.removePotionEffect(PotionEffectType.INVISIBILITY);
	                         Bukkit.getOnlinePlayers().forEach((otherPlayer) -> otherPlayer.showPlayer(p));
	                         p.sendMessage(ChatColor.AQUA + "Je bent weer levend! Veel succes.");
							this.cancel();
						}
						int i = afterlife.get(p.getUniqueId());
						sendAction(p, ChatColor.WHITE + "Je bent nog voor " + ChatColor.AQUA + i + ChatColor.WHITE + " secondes een geest!");
					}
				}.runTaskTimerAsynchronously(this, 20, 20);
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_AIR) {
			if(afterlife.containsKey(p.getUniqueId())) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(ChatColor.RED + "Je zit in de afterlive, dus kan je niet interacten! Je bent nog voor "
						+ afterlife.get(e.getPlayer().getUniqueId()) + " secondes in afterlive!");
			}
         }
	}

}
