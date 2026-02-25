package net.sup.moderation.commands

import net.sup.moderation.utils.PunishmentManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.util.UUID

class BanCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("sup.moderation.ban")) return false
        if (args.isEmpty()) return false
        val target = Bukkit.getPlayerExact(args[0])
        val reason = if (args.size >= 2) args.copyOfRange(1, args.size).joinToString(" ") else "No reason"
        val uuid = target?.uniqueId ?: try { UUID.fromString(args[0]); UUID.fromString(args[0]) } catch (e: Exception) { null }
        if (target == null && uuid == null) {
            sender.sendMessage("§cSpieler nicht gefunden.")
            return true
        }
        val id = uuid ?: target!!.uniqueId
        PunishmentManager.ban(id, reason, sender.name)
        if (target != null) target.kickPlayer("§cYou are banned: $reason")
        sender.sendMessage("§a${args[0]} gebannt.")
        return true
    }
}