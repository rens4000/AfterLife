package me.rens4000.afterlife;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin implements Listener {
	
	public HashMap<UUID, Integer> afterlife = new HashMap<>();
	public HashMap<UUID, BukkitRunnable> cooldownTask;
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(command.getName().equalsIgnoreCase("afterlife")) {
			if(sender.hasPermission("AfterLife.Admin")) {
			if(args.length == 0) {
				sender.sendMessage(ChatColor.RED + "Te weinig argumenten!");
				return false;
			}
			if(Bukkit.getPlayer(args[0]) == null) {
				sender.sendMessage(ChatColor.RED + "Speler bestaat niet!");
				return false;
			}
			Player p = Bukkit.getPlayer(args[0]);
			sender.sendMessage(ChatColor.AQUA + "Speler is in afterlife gezet!");
			afterlife.put(Bukkit.getPlayer(args[0]).getUniqueId(), 300);//300 is het aantal secondes.
			p.setGameMode(GameMode.ADVENTURE);
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, Integer.MAX_VALUE, false, false));
            Bukkit.getOnlinePlayers().forEach((otherPlayer) -> otherPlayer.hidePlayer(p));
            p.setGameMode(GameMode.SURVIVAL);
			cooldownTask.put(p.getUniqueId(), new BukkitRunnable() {
                public void run() {
                        afterlife.put(p.getUniqueId(), afterlife.get(p.getUniqueId()) - 1);
                        if (afterlife.get(p) == 0) {
                        	afterlife.remove(p);
                        	
                                cooldownTask.remove(p);
                                p.removePotionEffect(PotionEffectType.INVISIBILITY);
                                Bukkit.getOnlinePlayers().forEach((otherPlayer) -> otherPlayer.showPlayer(p));
                                p.sendMessage(ChatColor.AQUA + "Je bent weer levend! Veel succes.");
                                cancel();
                        }
                }
        });
       
			
			} else {
				sender.sendMessage(ChatColor.RED + "Niet genoeg permissie!");
			}
		}
		return false;	
		
	}
	@EventHandler
	public void chatEvent(AsyncPlayerChatEvent e) {
		if(afterlife.containsKey(e.getPlayer())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "Je zit in de afterlive, dus kan je niet praten! Je bent nog voor " + afterlife.get(e.getPlayer().getUniqueId()) + " secondes in afterlive!");
		}
	}
	
	@EventHandler
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

}
