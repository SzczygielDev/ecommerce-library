package pl.szczygieldev.ecommercelibrary.messaging

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import pl.szczygieldev.ecommercelibrary.messaging.config.MessageQueueConfig
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList

class InMemoryMessageQueue<T>(val config: MessageQueueConfig) : MessageQueue<T> {
    private val messageDb = ConcurrentHashMap<MessageId, Message<T>>()
    private val queue = ConcurrentLinkedQueue<Message<T>>()
    private val messagesInProcessing =
        ConcurrentHashMap.newKeySet<Message<T>>()
    private val dlq = CopyOnWriteArrayList<Message<T>>()
    private val listeners = CopyOnWriteArrayList<MessageQueueListener>()

    private val log = KotlinLogging.logger(javaClass.name)
    private val coroutineScope =
        CoroutineScope(Job() + CoroutineExceptionHandler { context, throwable -> log.error { "Exception while processing command in background: $throwable" } })


    override suspend fun push(message: Message<T>) {
        if (messageDb.putIfAbsent(message.id, message) != null) return

        queue.add(message)

        notifyListeners()
    }

    override fun pull(): Message<T>? {
        val message = queue.poll() ?: return null
        val updatedMessage = message.copy(state = message.state.copy(status = MessageProcessingStatus.PROCESSING, beginTimestamp = Clock.System.now(), finishTimestamp = null))
        messagesInProcessing.add(updatedMessage)

        messageDb[updatedMessage.id] = updatedMessage
        return message
    }

    override fun ack(message: Message<T>) {
        messagesInProcessing.remove(message)

        val updatedMessage = message.copy(state = message.state.copy(status = MessageProcessingStatus.SUCCESS, finishTimestamp =Clock.System.now()))

        messageDb[updatedMessage.id] = updatedMessage
    }

    override suspend fun requeue(message: Message<T>, error: MessageProcessingError) {
        messagesInProcessing.remove(message)

        val updatedMessage = message.copy(
            state = message.state.copy(
                status = MessageProcessingStatus.WAITING, failCount = message.state.failCount + 1, errors =
                    message.state.errors + error
            )
        )

        if (updatedMessage.state.failCount >= config.maxMessageRetries) {
            val dlqMessage =
                updatedMessage.copy(state = updatedMessage.state.copy(status = MessageProcessingStatus.FAILURE, finishTimestamp =Clock.System.now()))
            dlq.add(dlqMessage)
            messageDb[dlqMessage.id] = dlqMessage
            return
        }

        queue.add(updatedMessage)
        messageDb[updatedMessage.id] = updatedMessage
        notifyListeners()
    }

    override fun getMessage(messageId: MessageId): Message<T>? = messageDb[messageId]

    override fun isEmpty(): Boolean = queue.isEmpty()

    override fun registerListener(listener: MessageQueueListener) {
        listeners.add(listener)
    }

    private fun notifyListeners() {
        coroutineScope.launch(Dispatchers.Default) {
            listeners.forEach { listener ->
                launch { listener.onMessageAvailable() }
            }
        }
    }
}