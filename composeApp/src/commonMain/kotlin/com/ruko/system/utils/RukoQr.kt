package com.ruko.system.utils

import com.ruko.system.domain.model.QRQueue
import kotlinx.serialization.json.Json

object RukoQr {
    private val json = Json { ignoreUnknownKeys = true }

    fun decode(raw: String): QRQueue? = runCatching {
        require(raw.length <= 4096)
        json.decodeFromString(QRQueue.serializer(), raw.removePrefix("RUKO_")).also {
            require(it.queueId.isNotBlank())
            require(it.queueId.none { char -> char in ".#$[]/" || char.code < 32 })
        }
    }.getOrNull()
}
