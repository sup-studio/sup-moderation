package net.sup.moderation.commands

import net.sup.moderation.utils.PunishmentManager
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.util.*

class BanCommand : CommandExecutor {
    companion object {
        private const val PERMISSION = "sup.moderation.ban"
        private val PERMANENT = setOf("perm", "permanent")
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {

        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage("§cDu hast keine Berechtigung für diesen Befehl.")
            return true
        }

        if (args.size < 2) {
            sendUsage(sender)
            return true
        }

        val targetPlayer = resolvePlayer(args[0])
        if (targetPlayer == null) {
            sender.sendMessage("§cSpieler nicht gefunden.")
            return true
        }

        val uuid = targetPlayer.uniqueId

        if (PunishmentManager.isBanned(uuid)) {
            sender.sendMessage("§c${targetPlayer.name ?: args[0]} ist bereits gebannt.")
            return true
        }

        val length = args[1]
        val reason = args.drop(2).ifEmpty { listOf("No reason") }.joinToString(" ")

        try {
            PunishmentManager.ban(
                uuid,
                reason,
                sender.name,
                length
            )
        } catch (_: IllegalArgumentException) {
            sender.sendMessage("§cUngültige Zeitangabe: '$length'")
            sender.sendMessage("§7Beispiele: §f30m §7| §f7d §7| §f1mo §7| §fperm")
            return true
        }

        val ban = PunishmentManager.getBan(uuid)

        val banMessage = buildString {
            appendLine("§cDu wurdest gebannt!")
            appendLine("§7Grund: §f$reason")
            appendLine("§7Von: §f${sender.name}")

            if (length.lowercase() in PERMANENT) {
                appendLine("§7Dauer: §fPermanent")
            } else {
                appendLine("§7Bis: §f${formatUntil(ban?.until)}")
            }
        }

        Bukkit.getPlayer(uuid)?.kickPlayer(banMessage)

        sender.sendMessage(
            "§a${targetPlayer.name ?: args[0]} wurde für §f$length §agebannt. §7(Grund: $reason)"
        )

        return true
    }

    private fun resolvePlayer(input: String): OfflinePlayer? {
        Bukkit.getPlayerExact(input)?.let {
            return it
        }

        return try {
            Bukkit.getOfflinePlayer(UUID.fromString(input))
        } catch (_: Exception) {
            Bukkit.getOfflinePlayerIfCached(input)
        }
    }

    private fun sendUsage(sender: CommandSender) {
        sender.sendMessage("§cVerwendung: /ban <Spieler> <Dauer> [Grund]")
        sender.sendMessage(
            "§7Dauer: §fs §7= Sekunden, §fm §7= Minuten, §fh §7= Stunden, §fd §7= Tage, §fw §7= Wochen, §fmo §7= Monate, §fperm §7= Permanent"
        )
    }

    private fun formatUntil(until: Long?): String {
        if (until == null || until == -1L) return "Permanent"

        val diff = until - System.currentTimeMillis()
        if (diff <= 0) return "Abgelaufen"

        val days = diff / 86_400_000
        val hours = (diff % 86_400_000) / 3_600_000
        val minutes = (diff % 3_600_000) / 60_000

        return when {
            days > 0 -> "${days}d ${hours}h ${minutes}m"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }
}