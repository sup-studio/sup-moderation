package net.sup.moderation.utils

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

object ReportManager {

    private lateinit var file: File
    private lateinit var config: FileConfiguration

    fun init(plugin: JavaPlugin) {

        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        file = File(plugin.dataFolder, "reports.yml")

        if (!file.exists()) {
            file.createNewFile()
        }

        config = YamlConfiguration.loadConfiguration(file)
    }

    fun saveAll() {
        config.save(file)
    }

    fun addReport(
        reporter: String,
        target: String,
        reason: String
    ) {

        val id = System.currentTimeMillis().toString()

        config.set("reports.$id.reporter", reporter)
        config.set("reports.$id.target", target)
        config.set("reports.$id.reason", reason)
        config.set("reports.$id.timestamp", System.currentTimeMillis())

        saveAll()
    }

    fun getConfig(): FileConfiguration {
        return config
    }
}