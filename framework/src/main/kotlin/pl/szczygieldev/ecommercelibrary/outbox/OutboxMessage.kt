package pl.szczygieldev.ecommercelibrary.outbox

import java.time.Instant
import java.util.UUID

class OutboxMessage(
    val eventId: UUID,
    var status: OutboxMessageStatus,
    val eventData: String,
    val eventType: String,
    val timestamp: Instant
)