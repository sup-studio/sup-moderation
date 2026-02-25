package net.sup.moderation

import net.sup.moderation.commands.BanCommand
import net.sup.moderation.commands.ReportCommand
import net.sup.moderation.commands.UnbanCommand
import net.sup.moderation.commands.WarnCommand
import net.sup.moderation.utils.PunishmentManager
import net.sup.moderation.utils.ReportManager
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
        ReportManager.init(this)
        getCommand("ban")?.setExecutor(BanCommand())
        getCommand("unban")?.setExecutor(UnbanCommand())
        getCommand("warn")?.setExecutor(WarnCommand())
        getCommand("report")?.setExecutor(ReportCommand())
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
