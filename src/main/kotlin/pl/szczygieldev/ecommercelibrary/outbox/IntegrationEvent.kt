package pl.szczygieldev.ecommercelibrary.outbox

import java.time.Instant
import java.util.UUID

abstract class IntegrationEvent(val id: UUID, val occurredOn: Instant)