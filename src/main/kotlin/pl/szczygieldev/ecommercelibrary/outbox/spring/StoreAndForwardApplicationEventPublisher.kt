package pl.szczygieldev.ecommercelibrary.outbox.spring

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import pl.szczygieldev.ecommercelibrary.ddd.core.DomainEvent
import pl.szczygieldev.ecommercelibrary.ddd.core.DomainEventPublisher
import pl.szczygieldev.ecommercelibrary.outbox.IntegrationEventMapper
import pl.szczygieldev.ecommercelibrary.outbox.Outbox

abstract class StoreAndForwardApplicationEventPublisher<T : DomainEvent<T>>(
    val eventPublisher: ApplicationEventPublisher,
    val outbox: Outbox,
    val eventMapper: IntegrationEventMapper<DomainEvent<*>>,
) : DomainEventPublisher<T> {
    companion object {
        private val log = KotlinLogging.logger { }
    }

    override fun publish(domainEvent: T) {
        eventPublisher.publishEvent(domainEvent)
        eventMapper.toIntegrationEvent(domainEvent)?.let { integrationEvent ->
            outbox.insertEvent(integrationEvent)
        }
    }

    override fun publishBatch(events: List<T>) {
        events.forEach { domainEvent ->
            eventPublisher.publishEvent(domainEvent)
            eventMapper.toIntegrationEvent(domainEvent)?.let { integrationEvent ->
                outbox.insertEvent(integrationEvent)
            }
        }
    }
}