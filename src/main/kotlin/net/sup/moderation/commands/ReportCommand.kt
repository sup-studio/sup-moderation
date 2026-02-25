package net.sup.moderation.commands

import net.sup.moderation.SupModeration
import net.sup.moderation.utils.ReportManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*

class ReportCommand : CommandExecutor {
    private val webhookUrl: String
        get() = SupModeration.instance.config.getString("discord.webhook") ?: throw IllegalStateException("Webhook fehlt in config.yml")

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (label.equals("report", true)) {

            if (sender !is Player) return false
            if (args.size < 2) return false

            val target = Bukkit.getPlayerExact(args[0])
                ?: run {
                    sender.sendMessage("§cSpieler nicht gefunden.")
                    return true
                }

            val reason = args.copyOfRange(1, args.size).joinToString(" ")

            ReportManager.addReport(
                sender.name,
                target.name,
                reason
            )

            sender.sendMessage("§aReport gesendet.")

            sendDiscordWebhook(sender.name, target.name, reason)

            return true
        }

        if (!sender.hasPermission("sup.moderation.reports.view")) return false

        val reports = ReportManager.getConfig().getConfigurationSection("reports")?.getKeys(false) ?: emptySet()

        if (reports.isEmpty()) {
            sender.sendMessage("§eKeine Reports.")
            return true
        }

        reports.forEach { id ->
            val section = ReportManager.getConfig().getConfigurationSection("reports.$id")!!
            val reporter = section.getString("reporter")!!
            val target = section.getString("target")!!
            val reason = section.getString("reason")!!
            sender.sendMessage("§7[$id] §c$reporter -> $target: $reason")
        }

        return true
    }

    private fun sendDiscordWebhook(reporter: String, target: String, reason: String) {
        Bukkit.getScheduler().runTaskAsynchronously(SupModeration.instance, Runnable {

            try {
                val json = """
                {
                  "username": "SupSMP | Moderation",
                  "message": "<@&1476280606802444512>"
                  "embeds": [
                    {
                      "title": "Neuer Report",
                      "color": 15158332,
                      "fields": [
                        { "name": "Reporter", "value": "$reporter", "inline": true },
                        { "name": "Spieler", "value": "$target", "inline": true },
                        { "name": "Grund", "value": "$reason", "inline": false }
                      ],
                      "timestamp": "${java.time.Instant.now()}"
                    }
                  ]
                }
                """.trimIndent()

                val connection = URL(webhookUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.outputStream.use { it.write(json.toByteArray(StandardCharsets.UTF_8)) }
                connection.inputStream.close()
                connection.disconnect()
            } catch (_: Exception) {
            }

        })
    }
}