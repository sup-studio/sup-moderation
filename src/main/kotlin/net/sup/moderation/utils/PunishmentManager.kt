package net.sup.moderation.utils

import net.sup.moderation.SupModeration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

data class BanEntry(val uuid: UUID, val reason: String?, val issuer: String?, val time: Long)
data class MuteEntry(val uuid: UUID, val until: Long, val reason: String?, val issuer: String?)
data class WarnEntry(val uuid: UUID, val reason: String, val issuer: String, val time: Long)

object PunishmentManager {
    private lateinit var bansFile: File
    private lateinit var mutesFile: File
    private lateinit var warnsFile: File
    private lateinit var bansCfg: YamlConfiguration
    private lateinit var mutesCfg: YamlConfiguration
    private lateinit var warnsCfg: YamlConfiguration

    private val bans = HashMap<UUID, BanEntry>()
    private val mutes = HashMap<UUID, MuteEntry>()
    private val warns = HashMap<UUID, MutableList<WarnEntry>>()

    fun init(plugin: SupModeration) {
        bansFile = File(plugin.dataFolder, "bans.yml")
        mutesFile = File(plugin.dataFolder, "mutes.yml")
        warnsFile = File(plugin.dataFolder, "warns.yml")
        if (!plugin.dataFolder.exists()) plugin.dataFolder.mkdirs()
        if (!bansFile.exists()) bansFile.createNewFile()
        if (!mutesFile.exists()) mutesFile.createNewFile()
        if (!warnsFile.exists()) warnsFile.createNewFile()
        bansCfg = YamlConfiguration.loadConfiguration(bansFile)
        mutesCfg = YamlConfiguration.loadConfiguration(mutesFile)
        warnsCfg = YamlConfiguration.loadConfiguration(warnsFile)
        loadAll()
    }

    fun loadAll() {
        bans.clear()
        mutes.clear()
        warns.clear()
        bansCfg.getKeys(false).forEach { k ->
            val uuid = UUID.fromString(k)
            val reason = bansCfg.getString("$k.reason", "No reason")
            val issuer = bansCfg.getString("$k.issuer", "Console")
            val time = bansCfg.getLong("$k.time", System.currentTimeMillis())
            bans[uuid] = BanEntry(uuid, reason, issuer, time)
        }
        mutesCfg.getKeys(false).forEach { k ->
            val uuid = UUID.fromString(k)
            val until = mutesCfg.getLong("$k.until", Long.MAX_VALUE)
            val reason = mutesCfg.getString("$k.reason", "No reason")
            val issuer = mutesCfg.getString("$k.issuer", "Console")
            mutes[uuid] = MuteEntry(uuid, until, reason, issuer)
        }
        warnsCfg.getKeys(false).forEach { k ->
            val list = mutableListOf<WarnEntry>()
            warnsCfg.getMapList(k).forEach { m ->
                val uuid = UUID.fromString(m["uuid"].toString())
                val reason = m["reason"].toString()
                val issuer = m["issuer"].toString()
                val time = (m["time"] as? Long) ?: (m["time"].toString().toLongOrNull() ?: System.currentTimeMillis())
                list.add(WarnEntry(uuid, reason, issuer, time))
            }
            warns[UUID.fromString(k)] = list
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
        }
        mutes.forEach { (uuid, entry) ->
            val k = uuid.toString()
            mutesCfg.set("$k.until", entry.until)
            mutesCfg.set("$k.reason", entry.reason)
            mutesCfg.set("$k.issuer", entry.issuer)
        }
        warns.forEach { (uuid, list) ->
            val k = uuid.toString()
            val mapList = list.map { mapOf("uuid" to it.uuid.toString(), "reason" to it.reason, "issuer" to it.issuer, "time" to it.time) }
            warnsCfg.set(k, mapList)
        }
        bansCfg.save(bansFile)
        mutesCfg.save(mutesFile)
        warnsCfg.save(warnsFile)
    }

    fun ban(uuid: UUID, reason: String, issuer: String) {
        bans[uuid] = BanEntry(uuid, reason, issuer, System.currentTimeMillis())
        saveAll()
    }

    fun unbanByNameOrUUID(id: String): Boolean {
        val uuid = try { UUID.fromString(id) } catch (e: Exception) { null }
        if (uuid != null && bans.remove(uuid) != null) { saveAll(); return true }
        val found = bans.values.find { it.uuid.toString() == id || it.issuer.equals(id, true) }
        if (found != null) { bans.remove(found.uuid); saveAll(); return true }
        return false
    }

    fun getBan(uuid: UUID): BanEntry? = bans[uuid]
    fun isBanned(uuid: UUID): Boolean = bans.containsKey(uuid)

    fun warn(uuid: UUID, reason: String, issuer: String) {
        val entry = WarnEntry(uuid, reason, issuer, System.currentTimeMillis())
        warns.computeIfAbsent(uuid) { mutableListOf() }.add(entry)
        saveAll()
    }

    fun getWarns(uuid: UUID): List<WarnEntry> = warns[uuid] ?: emptyList()
}