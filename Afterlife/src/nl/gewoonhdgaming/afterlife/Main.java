package nl.gewoonhdgaming.afterlife;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.connorlinfoot.actionbarapi.ActionBarAPI;

import net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;

@SuppressWarnings("deprecation")
public class Main extends JavaPlugin implements Listener {
	
	public HashMap<UUID, Integer> afterlife = new HashMap<>();
	public HashMap<UUID, BukkitRunnable> cooldownTask;
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
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
			
			Player ghg = (Player) sender;
			if (args.length == 0) {
				if (ghg.hasPermission("AfterLife.user")) {
					ghg.sendMessage(ChatColor.RED + "Development by: Boykev en Rens");
					ghg.sendMessage(ChatColor.RED + "Product Owner: GewoonHDEnterprise");
					ghg.sendMessage(ChatColor.RED + "Copyright, contribution not allowed!");
			}
			}			
			else if (args[0].equalsIgnoreCase("info")) {
				if (ghg.hasPermission("AfterLife.user")) {
					ghg.sendMessage(ChatColor.RED + "AfterLife is een plugin waarin mensen tijdelijk dood zijn zonder deze uit de server te hoeven bannen");
			}
			}
			else {
				ghg.sendMessage(ChatColor.RED + "Er is wat fout gegaan!");
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
        Bukkit.getOnlinePlayers().forEach((otherPlayer) -> otherPlayer.hidePlayer(p));
        p.setGameMode(GameMode.SURVIVAL);
        p.setCanPickupItems(true);
		 new BukkitRunnable() {
				
				@Override
				public void run() {
					if(afterlife.containsKey(p.getUniqueId()) && !afterlife.get(p.getUniqueId()).equals(1)) {
						afterlife.put(p.getUniqueId(), afterlife.get(p.getUniqueId()) - 1);
						return;
					}
						afterlife.remove(p.getUniqueId());
						 p.removePotionEffect(PotionEffectType.INVISIBILITY);
                         Bukkit.getOnlinePlayers().forEach((otherPlayer) -> otherPlayer.showPlayer(p));
                         p.sendMessage(ChatColor.AQUA + "Je bent weer levend! Veel succes.");
                         p.kickPlayer(ChatColor.RED + "Je bent weer levend! Rejoin de server zodat mensen je weer kunnen zien!");
                         getConfig().set("sessions." + p.getUniqueId(), null);
                         saveConfig();
						this.cancel();
					if(!afterlife.containsKey(p.getUniqueId()))
						return;
					int i = afterlife.get(p.getUniqueId());
					ActionBarAPI.sendActionBar(p, "Je bent nog voor" + ChatColor.AQUA + i + ChatColor.WHITE + " secondes een geest");
				}
			}.runTaskTimerAsynchronously(this, 0, 20);
	}
	
	public void sendAction(Player p, String message) {
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"));
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}
	
	@EventHandler
	public void chatEvent(AsyncPlayerChatEvent e) {
		if(afterlife.containsKey(e.getPlayer())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "Je zit in de afterlife, dus kan je niet praten! Je bent nog voor " + afterlife.get(e.getPlayer().getUniqueId()) + " secondes in afterlive!");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void godMode(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if(afterlife.containsKey(p.getUniqueId())) {
				p.sendMessage(ChatColor.RED + "Je zit in de afterlife, dus kan je niet dood gaan! Je bent nog voor "
			+ afterlife.get(p.getUniqueId()) + " secondes in afterlife!");
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void itemPickup(PlayerPickupItemEvent e) {
		if(afterlife.containsKey(e.getPlayer().getUniqueId())) {
			e.getPlayer().sendMessage(ChatColor.RED + "Je zit in de afterlive, dus kan je geen items oppakken! Je bent nog voor "
					+ afterlife.get(e.getPlayer().getUniqueId()) + " secondes in afterlife!");
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
		if(getConfig().contains("sessions." + e.getPlayer().getUniqueId())) {
			Player p = (Player) e.getPlayer();
			afterlife.put(p.getUniqueId(), getConfig().getInt("sessions." + e.getPlayer().getUniqueId()));//Default = 300
			p.setGameMode(GameMode.ADVENTURE);
	        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, Integer.MAX_VALUE, false, false));
	        p.setCanPickupItems(false);
	        p.sendTitle(ChatColor.AQUA + "U bent dood gegaan", ChatColor.RED + "U bent tijdelijk een geest");
	        p.sendMessage(ChatColor.RED + "Je bent nu " + getConfig().get("sessions." + e.getPlayer().getUniqueId()) + " secondes niet zichbaar voor andere spelers, ook kan je niet chatten, items opakken of commands uitvoeren. Na 5 minuten zal je weer levend zijn en kan je alles weer!");
	        Bukkit.getOnlinePlayers().forEach((otherPlayer) -> otherPlayer.hidePlayer(p));
	        p.setGameMode(GameMode.SURVIVAL);
	        p.setCanPickupItems(true);
			 new BukkitRunnable() {
					
					@Override
					public void run() {
						if(getConfig().contains("sessions." + e.getPlayer().getUniqueId())) {
							if(afterlife.containsKey(p.getUniqueId()) && !afterlife.get(p.getUniqueId()).equals(1)) {
							afterlife.put(p.getUniqueId(), afterlife.get(p.getUniqueId()) - 1);
						} if(afterlife.get(p.getUniqueId()).equals(0)){
							afterlife.remove(p.getUniqueId());
							 p.removePotionEffect(PotionEffectType.INVISIBILITY);
	                         Bukkit.getOnlinePlayers().forEach((otherPlayer) -> otherPlayer.showPlayer(p));
	                         p.sendMessage(ChatColor.AQUA + "Je bent weer levend! Veel succes.");
	                         getConfig().set("sessions." + p.getUniqueId(), null);
	                         saveConfig();
	                         p.kickPlayer(ChatColor.RED + "Je bent weer levend! Rejoin de server om dit te bevestigen!");
							this.cancel();
						}
						int i = afterlife.get(p.getUniqueId());
						ActionBarAPI.sendActionBar(p,  ChatColor.WHITE + "Je bent nog voor " + ChatColor.AQUA + i + ChatColor.WHITE + " secondes een geest!");
					}
					}
				}.runTaskTimerAsynchronously(this, 0, 20);
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_AIR) {
			if(afterlife.containsKey(p.getUniqueId())) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(ChatColor.RED + "Je zit in de afterlife, dus kan je niet interacten! Je bent nog voor "
						+ afterlife.get(e.getPlayer().getUniqueId()) + " secondes in afterlife!");
			}
         }
	}
	
	@EventHandler
	public void onChat(PlayerChatEvent e) {
		Player p = e.getPlayer();
			if(afterlife.containsKey(p.getUniqueId())) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(ChatColor.RED + "Je zit in de afterlife, dus kan je niet Chatten! Je bent nog voor "
						+ afterlife.get(e.getPlayer().getUniqueId()) + " secondes in afterlife!");
			}
         }
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent  e) {
		Player p = e.getPlayer();
			if(afterlife.containsKey(p.getUniqueId())) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(ChatColor.RED + "Je zit in de afterlife, dus kan je Geen commando's uitvoeren! Je bent nog voor "
						+ afterlife.get(e.getPlayer().getUniqueId()) + " secondes in afterlife!");
			}
         }

}
