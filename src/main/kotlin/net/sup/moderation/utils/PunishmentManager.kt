package net.sup.moderation.utils

import net.sup.moderation.SupModeration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class BanEntry(
    val uuid: UUID,
    val reason: String?,
    val issuer: String?,
    val time: Long,
    val until: Long = -1L
)

data class MuteEntry(
    val uuid: UUID,
    val until: Long,
    val reason: String?,
    val issuer: String?
)

data class WarnEntry(
    val uuid: UUID,
    val reason: String,
    val issuer: String,
    val time: Long
)

object PunishmentManager {
    private lateinit var bansFile: File
    private lateinit var mutesFile: File
    private lateinit var warnsFile: File

    private lateinit var bansCfg: YamlConfiguration
    private lateinit var mutesCfg: YamlConfiguration
    private lateinit var warnsCfg: YamlConfiguration

    private val bans = ConcurrentHashMap<UUID, BanEntry>()
    private val mutes = ConcurrentHashMap<UUID, MuteEntry>()
    private val warns = ConcurrentHashMap<UUID, MutableList<WarnEntry>>()

    private val PERMANENT = setOf("perm", "permanent")

    fun init(plugin: SupModeration) {
        plugin.dataFolder.mkdirs()

        bansFile = createFile(plugin, "bans.yml")
        mutesFile = createFile(plugin, "mutes.yml")
        warnsFile = createFile(plugin, "warns.yml")

        reload()
    }

    fun reload() {
        bansCfg = YamlConfiguration.loadConfiguration(bansFile)
        mutesCfg = YamlConfiguration.loadConfiguration(mutesFile)
        warnsCfg = YamlConfiguration.loadConfiguration(warnsFile)

        loadAll()
    }

    private fun createFile(plugin: SupModeration, name: String): File {
        val file = File(plugin.dataFolder, name)
        if (!file.exists()) file.createNewFile()
        return file
    }

    private fun loadAll() {
        bans.clear()
        mutes.clear()
        warns.clear()

        bansCfg.getKeys(false).forEach { key ->
            runCatching {
                val uuid = UUID.fromString(key)

                bans[uuid] = BanEntry(
                    uuid,
                    bansCfg.getString("$key.reason", "No reason"),
                    bansCfg.getString("$key.issuer", "Console"),
                    bansCfg.getLong("$key.time"),
                    bansCfg.getLong("$key.until", -1L)
                )
            }
        }

        mutesCfg.getKeys(false).forEach { key ->
            runCatching {

                val uuid = UUID.fromString(key)

                mutes[uuid] = MuteEntry(
                    uuid,
                    mutesCfg.getLong("$key.until", Long.MAX_VALUE),
                    mutesCfg.getString("$key.reason", "No reason"),
                    mutesCfg.getString("$key.issuer", "Console")
                )
            }
        }

        warnsCfg.getKeys(false).forEach { key ->
            runCatching {
                val uuid = UUID.fromString(key)

                val list = warnsCfg.getMapList(key).map {

                    WarnEntry(
                        UUID.fromString(it["uuid"].toString()),
                        it["reason"].toString(),
                        it["issuer"].toString(),
                        (it["time"] as? Long)
                            ?: it["time"].toString().toLongOrNull()
                            ?: System.currentTimeMillis()
                    )

                }.toMutableList()

                warns[uuid] = list
            }
        }
    }

    fun saveAll() {
        bansCfg = YamlConfiguration()
        mutesCfg = YamlConfiguration()
        warnsCfg = YamlConfiguration()

        bans.forEach { (uuid, entry) ->
            val k = uuid.toString()

            bansCfg.set("$k.reason", entry.reason)
            bansCfg.set("$k.issuer", entry.issuer)
            bansCfg.set("$k.time", entry.time)
            bansCfg.set("$k.until", entry.until)
        }

        mutes.forEach { (uuid, entry) ->
            val k = uuid.toString()

            mutesCfg.set("$k.until", entry.until)
            mutesCfg.set("$k.reason", entry.reason)
            mutesCfg.set("$k.issuer", entry.issuer)
        }

        warns.forEach { (uuid, list) ->
            warnsCfg.set(
                uuid.toString(),
                list.map {

                    mapOf(
                        "uuid" to it.uuid.toString(),
                        "reason" to it.reason,
                        "issuer" to it.issuer,
                        "time" to it.time
                    )
                }
            )
        }

        bansCfg.save(bansFile)
        mutesCfg.save(mutesFile)
        warnsCfg.save(warnsFile)
    }

    fun ban(uuid: UUID, reason: String, issuer: String) {
        bans[uuid] = BanEntry(
            uuid,
            reason,
            issuer,
            System.currentTimeMillis(),
            -1L
        )

        saveAll()
    }

    fun ban(uuid: UUID, reason: String, issuer: String, length: String) {
        val until = parseDuration(length)
            ?: throw IllegalArgumentException("Invalid duration")

        bans[uuid] = BanEntry(
            uuid,
            reason,
            issuer,
            System.currentTimeMillis(),
            until
        )

        saveAll()
    }

    fun unban(uuid: UUID): Boolean {
        val removed = bans.remove(uuid) ?: return false

        saveAll()
        return removed != null
    }

    fun getBan(uuid: UUID): BanEntry? = bans[uuid]

    fun isBanned(uuid: UUID): Boolean {
        val entry = bans[uuid] ?: return false

        if (entry.until == -1L) return true

        if (entry.until > System.currentTimeMillis()) return true

        bans.remove(uuid)
        saveAll()

        return false
    }

    fun warn(uuid: UUID, reason: String, issuer: String) {
        warns.computeIfAbsent(uuid) { mutableListOf() }
            .add(
                WarnEntry(
                    uuid,
                    reason,
                    issuer,
                    System.currentTimeMillis()
                )
            )

        saveAll()
    }

    fun getWarns(uuid: UUID): List<WarnEntry> =
        warns[uuid]?.toList() ?: emptyList()

    private fun parseDuration(input: String): Long? {
        val trimmed = input.lowercase().trim()

        if (trimmed in PERMANENT) return -1L

        val regex = Regex("(\\d+)(mo|w|d|h|m|s)")
        val matches = regex.findAll(trimmed)

        if (!matches.any()) return null

        var totalMillis = 0L

        matches.forEach {
            val amount = it.groupValues[1].toLong()

            totalMillis += when (it.groupValues[2]) {
                "s" -> TimeUnit.SECONDS.toMillis(amount)
                "m" -> TimeUnit.MINUTES.toMillis(amount)
                "h" -> TimeUnit.HOURS.toMillis(amount)
                "d" -> TimeUnit.DAYS.toMillis(amount)
                "w" -> TimeUnit.DAYS.toMillis(amount * 7)
                "mo" -> TimeUnit.DAYS.toMillis(amount * 30)

                else -> 0
            }
        }

        return System.currentTimeMillis() + totalMillis
    }
}