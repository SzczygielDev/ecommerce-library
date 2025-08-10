package pl.szczygieldev.ecommercelibrary.messaging

interface MessageQueue<T> {
    suspend  fun push(message: Message<T>)
    fun pull(): Message<T>?
    fun ack(message: Message<T>)
    suspend fun requeue(message: Message<T>, error: MessageProcessingError)
    fun getMessage(messageId: MessageId): Message<T>?
    fun isEmpty(): Boolean
    fun isDlqEmpty(): Boolean
    fun getDlqSize(): Int
    fun registerListener(listener: MessageQueueListener)
}