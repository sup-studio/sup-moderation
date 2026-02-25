package net.sup.moderation.commands

import net.sup.moderation.utils.PunishmentManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.util.UUID

class BanCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("sup.moderation.ban")) {
            sender.sendMessage("§cDu hast keine Berechtigung für diesen Befehl.")
            return true
        }
        if (args.size < 2) {
            sender.sendMessage("§cVerwendung: /ban <Spieler> <Dauer> [Grund]")
            sender.sendMessage("§7Dauer: §fs §7= Sekunden, §fm §7= Minuten, §fh §7= Stunden, §fd §7= Tage, §fw §7= Wochen, §fmo §7= Monate, §fperm §7= Permanent")
            return true
        }

        val target = Bukkit.getPlayerExact(args[0])
        val uuid = target?.uniqueId ?: try { UUID.fromString(args[0]) } catch (e: Exception) { null }

        if (uuid == null) {
            sender.sendMessage("§cSpieler nicht gefunden.")
            return true
        }

        if (PunishmentManager.isBanned(uuid)) {
            sender.sendMessage("§c${args[0]} ist bereits gebannt.")
            return true
        }

        val length = args[1]
        val reason = if (args.size >= 3) args.copyOfRange(2, args.size).joinToString(" ") else "No reason"

        try {
            PunishmentManager.ban(uuid, reason, sender.name, length)
        } catch (e: IllegalArgumentException) {
            sender.sendMessage("§cUngültige Zeitangabe: '${length}'")
            sender.sendMessage("§7Beispiele: §f30m §7| §f7d §7| §f1mo §7| §fperm")
            return true
        }

        val banMessage = buildString {
            appendLine("§cDu wurdest gebannt!")
            appendLine("§7Grund: §f$reason")
            appendLine("§7Von: §f${sender.name}")
            if (length.lowercase() in listOf("perm", "permanent")) {
                appendLine("§7Dauer: §fPermanent")
            } else {
                appendLine("§7Bis: §f${formatUntil(PunishmentManager.getBan(uuid)?.until)}")
            }
        }

        target?.kickPlayer(banMessage)
        sender.sendMessage("§a${args[0]} wurde für §f$length §agebannt. §7(Grund: $reason)")
        return true
    }

    private fun formatUntil(until: Long?): String {
        if (until == null || until == -1L) return "Permanent"
        val diff = until - System.currentTimeMillis()
        if (diff <= 0) return "Abgelaufen"
        val days = diff / 86400000
        val hours = (diff % 86400000) / 3600000
        val minutes = (diff % 3600000) / 60000
        return when {
            days > 0  -> "${days}d ${hours}h ${minutes}m"
            hours > 0 -> "${hours}h ${minutes}m"
            else      -> "${minutes}m"
        }
    }
}