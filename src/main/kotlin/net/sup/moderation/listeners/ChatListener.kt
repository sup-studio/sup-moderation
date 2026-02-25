package net.sup.moderation.listeners

import net.sup.moderation.utils.PunishmentManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class ChatListener : Listener {
    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        val player = event.player

        if (PunishmentManager.isBanned(player.uniqueId)) {
            event.isCancelled = true
        }
    }
}