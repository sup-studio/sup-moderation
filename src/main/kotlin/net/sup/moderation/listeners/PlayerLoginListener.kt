package net.sup.moderation.listeners

import net.sup.moderation.utils.PunishmentManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent

class PlayerLoginListener : Listener {
    @EventHandler
    fun onLogin(e: PlayerLoginEvent) {
        val p = e.player
        val ban = PunishmentManager.getBan(p.uniqueId)
        if (ban != null) {
            e.disallow(PlayerLoginEvent.Result.KICK_BANNED, "§cYou are banned: ${ban.reason}")
        }
    }
}