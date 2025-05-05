package pl.szczygieldev.ecommercelibrary.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import pl.szczygieldev.ecommercelibrary.messaging.IntegrationEvent

class OutboxBackgroundWorker(
    val outbox: Outbox,
    val objectMapper: ObjectMapper,
    val onPublish : (event: IntegrationEvent) -> Unit
) {
    companion object {
        private val log = KotlinLogging.logger { }
    }

    @Scheduled(fixedRate = 1000)
    fun processOutbox() {
        outbox.getEventsForProcessing().map { outboxMessage ->
            objectMapper.readerFor(Class.forName(outboxMessage.eventType))
                .readValue<IntegrationEvent>(outboxMessage.eventData)
        }
            .forEach { integrationEvent ->
                log.info { "publishing event='$integrationEvent'" }
                onPublish(integrationEvent)
                outbox.markAsProcessed(integrationEvent)
            }
    }
}