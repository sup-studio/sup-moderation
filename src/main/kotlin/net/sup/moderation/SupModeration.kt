package net.sup.moderation

import net.sup.moderation.commands.BanCommand
import net.sup.moderation.utils.PunishmentManager
import org.bukkit.plugin.java.JavaPlugin

class SupModeration : JavaPlugin() {
    companion object {
        lateinit var instance: SupModeration
            private set
    }

    override fun onEnable() {
        instance = this
        saveDefaultConfig()
        PunishmentManager.init(this)
        getCommand("ban")?.setExecutor(BanCommand())
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
