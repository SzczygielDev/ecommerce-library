package pl.szczygieldev.ecommercelibrary.event

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import pl.szczygieldev.ecommercelibrary.ddd.core.DomainEvent
import pl.szczygieldev.ecommercelibrary.eventstore.EventStore
import pl.szczygieldev.ecommercelibrary.messaging.Message
import pl.szczygieldev.ecommercelibrary.messaging.MessageId
import pl.szczygieldev.ecommercelibrary.messaging.MessageQueue
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

abstract class PollingAsyncEventHandler<T : DomainEvent<T>>(
    private val eventClass: KClass<T>,
    private val objectMapper: ObjectMapper,
    open val eventStore: EventStore,
    override val eventQueue: MessageQueue<T>
) : AsyncEventHandler<T>(eventQueue) {
    private val log = KotlinLogging.logger(javaClass.name)
    var offset = 0;
    val limit = 100;

    suspend fun processInternal() {
        log.debug { "Begin event pulling TX" }
        newSuspendedTransaction {
            val eventsForHandler = eventStore.getEventsForType<T>(eventClass.jvmName, offset, limit)
            log.debug { "Pulled events=[${eventsForHandler}], offset='$offset', limit='$limit'" }

            eventsForHandler.forEach { event ->
                val message = Message(id = MessageId(event.id), payload = event, timestamp = Clock.System.now())
                eventQueue.push(message)
                offset++;
                log.debug { "Pushing message on internal event queue='$message'" }
            }
            log.debug { "New offset='$offset'" }
            commit()
        }
        log.debug { "Finish event pulling TX" }
        while (!eventQueue.isEmpty()) {
            newSuspendedTransaction {
                log.debug { "Event processing TX begin" }
                val event = eventQueue.pull() ?: return@newSuspendedTransaction
                log.debug { "Pulled event='$event'" }

                try {
                    handle(event.payload)
                    log.debug { "Event handling success" }
                    commit()
                    log.debug { "Event processing TX commited" }
                } catch (ex: Exception) {
                    log.error { "Exception while handling event='$event', exception='$ex', doing rollback" }
                    rollback()
                }
            }
        }
        log.info { "Processing completed" }
    }
}