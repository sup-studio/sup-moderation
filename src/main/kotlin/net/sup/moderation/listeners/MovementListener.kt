package net.sup.moderation.listeners

import net.sup.moderation.utils.FreezeManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class MovementListener : Listener {
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!event.hasChangedBlock()) return

        val player = event.player
        if (FreezeManager.isFrozen(player.uniqueId)) {
            event.isCancelled = true
            player.sendMessage("Du bist eingefroren und kannst dich nicht bewegen!")
        }
    }
}