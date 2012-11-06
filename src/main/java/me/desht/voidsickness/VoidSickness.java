package me.desht.voidsickness;

/*
    This file is part of PortableHole

    PortableHole is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PortableHole is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PortableHole.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.List;
import java.util.Map;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.LogUtils;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.CommandManager;
import me.desht.voidsickness.commands.ReloadCommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class VoidSickness extends JavaPlugin implements Listener {

	private static VoidSickness instance = null;
	private CommandManager cmds = new CommandManager(this);

	private int y_min;

	public void onEnable() { 

		LogUtils.init(this);

		PluginManager pm = this.getServer().getPluginManager();

		pm.registerEvents(this, this);

		registerCommands();

		this.getConfig().options().copyDefaults(true);
		this.getConfig().options().header("See http://dev.bukkit.org/server-mods/voidsickness/pages/configuration");
		this.saveConfig();

		processConfig();

		instance = this;
	}

	public void onDisable() {
		instance = null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		try {
			return cmds.dispatch(sender, command.getName(), args);
		} catch (DHUtilsException e) {
			MiscUtil.errorMessage(sender, e.getMessage());
			return true;
		}
	}

	public static VoidSickness getInstance() {
		return instance;
	}

	private void registerCommands() {
		cmds.registerCommand(new ReloadCommand());
	}

	public void processConfig() {
		y_min = getConfig().getInt("y_min");
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerMoved(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		if (p.getLocation().getBlockY() < y_min) {
			// port player to spawn and apply effects
			event.setTo(p.getWorld().getSpawnLocation());
			MiscUtil.playNamedSound(event.getFrom(), getConfig().getString("port_sound"), 1.0f, 1.0f);
			if (!p.hasPermission("voidsickness.exempt")) {
				applySicknessEffects(p);
				MiscUtil.alertMessage(p, getConfig().getString("sickness_message"));
			} else {
				MiscUtil.alertMessage(p, getConfig().getString("exempt_message"));
			}
		}
	}

	private void applySicknessEffects(Player p) {
		List<?> effects = getConfig().getList("effects");

		for (Object o : effects) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String,Object>) o;
			String typeName = (String)map.get("effect");
			PotionEffectType type = PotionEffectType.getByName(typeName);
			if (type == null) {
				LogUtils.warning("unknown effect type (ignoring): " + typeName);
				continue;
			}
			int duration = (Integer)map.get("duration") * 20;
			int amplifier = map.containsKey("amplifier") ? (Integer) map.get("amplifier") : 0;
			PotionEffect pe = new PotionEffect(type, duration, amplifier);
			p.addPotionEffect(pe);
		}
	}
}
