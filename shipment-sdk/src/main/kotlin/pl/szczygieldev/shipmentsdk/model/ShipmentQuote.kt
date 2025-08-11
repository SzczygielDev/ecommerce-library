package pl.szczygieldev.shipmentsdk.model

import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class ShipmentQuote(
    val id: UUID,
    val price: BigDecimal,
    val timestamp: Instant,
    val expireTimestamp: Instant
) {
    val isStale: Boolean
        get() = Instant.now().isBefore(expireTimestamp)
}