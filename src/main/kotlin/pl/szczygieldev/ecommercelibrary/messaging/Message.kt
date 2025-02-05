package pl.szczygieldev.ecommercelibrary.messaging

data class Message<T>(
    val id: MessageId = MessageId(),
    val payload: T,
    val state: MessageProcessingState = MessageProcessingState(status = MessageProcessingStatus.WAITING),
    val timestamp: kotlinx.datetime.Instant,
)