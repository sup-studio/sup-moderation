package net.sup.moderation.commands

import net.sup.moderation.utils.PunishmentManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class UnbanCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("sup.moderation.unban")) return false
        if (args.isEmpty()) return false
        val id = args[0]
        val success = PunishmentManager.unbanByNameOrUUID(id)
        sender.sendMessage(if (success) "§aUnban erfolgreich." else "§cUnban fehlgeschlagen.")
        return true
    }
}