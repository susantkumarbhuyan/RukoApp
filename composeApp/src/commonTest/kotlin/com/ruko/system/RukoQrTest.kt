package com.ruko.system

import com.ruko.system.utils.RukoQr
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RukoQrTest {
    @Test fun acceptsGeneratedPayloadAndScannerPayload() {
        val payload = """{"queueId":"queue-123"}"""
        assertEquals("queue-123", RukoQr.decode("RUKO_" + payload)?.queueId)
        assertEquals("queue-123", RukoQr.decode(payload)?.queueId)
    }

    @Test fun rejectsBrokenOrUnsafePayloads() {
        assertNull(RukoQr.decode("RUKO_invalid"))
        assertNull(RukoQr.decode("""{"queueId":""}"""))
        assertNull(RukoQr.decode("""{"queueId":"other/path"}"""))
        assertNull(RukoQr.decode("x".repeat(4097)))
    }
}
