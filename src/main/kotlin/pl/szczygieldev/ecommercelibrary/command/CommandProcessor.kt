package pl.szczygieldev.ecommercelibrary.command

import io.github.oshai.kotlinlogging.KotlinLogging
import pl.szczygieldev.ecommercelibrary.messaging.Message
import pl.szczygieldev.ecommercelibrary.messaging.MessageProcessingError
import pl.szczygieldev.ecommercelibrary.messaging.MessageQueue
import pl.szczygieldev.ecommercelibrary.messaging.MessageQueueListener

class CommandProcessor(val mediator: Mediator, val queue: MessageQueue<Command<*>>) : MessageQueueListener {
    private val log = KotlinLogging.logger(javaClass.name)
    override suspend fun onMessageAvailable() {
        if (!queue.isEmpty()) {
            queue.pull()?.let {
                processCommand(it)
            }
        }
    }

    suspend fun processCommand(message: Message<Command<*>>) {
        try {
            val result = mediator.send(message.payload)

            result.fold({
                log.error { "Error while processing command='${message.payload}', requeuing" }
                queue.requeue(message, MessageProcessingError(it.javaClass.name, it.message))
            }, {
                queue.ack(message)
            })
        } catch (e: Exception) {
            log.error { "Exception while processing command='${message.payload}', exception='$e' requeuing" }
            queue.requeue(message, MessageProcessingError(e.javaClass.name, e.message ?: ""))
        }
    }
}