package net.sup.moderation.utils

import java.util.*
import java.util.concurrent.ConcurrentHashMap

object FreezeManager {
    private val frozen = ConcurrentHashMap.newKeySet<UUID>()

    fun freeze(uuid: UUID) {
        frozen.add(uuid)
    }

    fun unfreeze(uuid: UUID) {
        frozen.remove(uuid)
    }

    fun isFrozen(uuid: UUID): Boolean {
        return frozen.contains(uuid)
    }
}