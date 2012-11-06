package me.desht.voidsickness.commands;

import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;
import me.desht.voidsickness.VoidSickness;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class ReloadCommand extends AbstractCommand {

	public ReloadCommand() {
		super("vsick r", 0, 0);
		setPermissionNode("voidsickness.commands.reload");
		setUsage("/vsick reload");
	}

	@Override
	public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
		plugin.reloadConfig();

		((VoidSickness)plugin).processConfig();

		MiscUtil.statusMessage(sender, "Plugin config has been reloaded");
		
		return true;
	}
}
