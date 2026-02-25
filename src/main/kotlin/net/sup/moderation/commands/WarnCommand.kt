package net.sup.moderation.commands

import net.sup.moderation.utils.PunishmentManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class WarnCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("sup.moderation.warn")) return false
        if (args.size < 2) return false
        val target = Bukkit.getPlayerExact(args[0])
        if (target == null) {
            sender.sendMessage("§cSpieler nicht gefunden.")
            return true
        }
        val reason = args.copyOfRange(1, args.size).joinToString(" ")
        PunishmentManager.warn(target.uniqueId, reason, sender.name)
        sender.sendMessage("§a${target.name} verwarnt.")
        target.sendMessage("§aDu wurdest wegen ${reason} verwarnt.")
        return true
    }
}