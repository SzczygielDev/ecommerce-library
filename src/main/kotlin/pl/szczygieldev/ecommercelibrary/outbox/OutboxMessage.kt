package pl.szczygieldev.ecommercelibrary.outbox

import java.time.Instant

class OutboxMessage(
    val eventId: String,
    var status: OutboxMessageStatus,
    val eventData: String,
    val eventType: String,
    val timestamp: Instant
)