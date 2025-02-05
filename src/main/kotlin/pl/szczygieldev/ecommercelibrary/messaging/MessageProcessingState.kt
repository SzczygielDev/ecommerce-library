package pl.szczygieldev.ecommercelibrary.messaging

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class MessageProcessingState(
    var status: MessageProcessingStatus,
    var failCount: Int = 0,
    val errors: List<MessageProcessingError> = mutableListOf(),
    val beginTimestamp : Instant = Clock.System.now(),
    val finishTimestamp : Instant? = null,
)