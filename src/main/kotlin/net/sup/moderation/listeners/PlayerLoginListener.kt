package net.sup.moderation.listeners

import net.sup.moderation.utils.PunishmentManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent

class PlayerLoginListener : Listener {
    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        val player = event.player
        val uuid = player.uniqueId

        if (!PunishmentManager.isBanned(uuid)) return

        val ban = PunishmentManager.getBan(uuid) ?: return

        val untilText = when {
            ban.until == -1L -> "§cPermanent"
            else -> {
                val diff = ban.until - System.currentTimeMillis()
                if (diff <= 0) return // ban expired
                val days = diff / 86400000
                val hours = (diff % 86400000) / 3600000
                val minutes = (diff % 3600000) / 60000
                when {
                    days > 0  -> "§f${days}d ${hours}h ${minutes}m"
                    hours > 0 -> "§f${hours}h ${minutes}m"
                    else      -> "§f${minutes}m"
                }
            }
        }

        event.disallow(
            PlayerLoginEvent.Result.KICK_BANNED,
            """
            §c§lDu bist gebannt!
            §7Grund: §f${ban.reason ?: "Kein Grund angegeben"}
            §7Von: §f${ban.issuer ?: "Unbekannt"}
            §7Bis: $untilText
            """.trimIndent()
        )
    }
}