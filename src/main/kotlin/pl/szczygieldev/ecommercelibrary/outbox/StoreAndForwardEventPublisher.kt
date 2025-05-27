package pl.szczygieldev.ecommercelibrary.outbox

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import pl.szczygieldev.ecommercelibrary.command.Mediator
import pl.szczygieldev.ecommercelibrary.ddd.core.DomainEvent
import pl.szczygieldev.ecommercelibrary.ddd.core.DomainEventPublisher

abstract class StoreAndForwardEventPublisher<T : DomainEvent<T>>(
    val mediator: Mediator,
    val outbox: Outbox,
    val eventMapper: IntegrationEventMapper<DomainEvent<*>>,
) : DomainEventPublisher<T> {
    companion object {
        private val log = KotlinLogging.logger { }
    }

    override fun publish(domainEvent: T): Unit = runBlocking{
        mediator.send(domainEvent)
        eventMapper.toIntegrationEvent(domainEvent)?.let { integrationEvent ->
            outbox.insertEvent(integrationEvent)
        }
    }

    override fun publishBatch(events: List<T>): Unit = runBlocking {
        events.forEach { domainEvent ->
            mediator.send(domainEvent)
            eventMapper.toIntegrationEvent(domainEvent)?.let { integrationEvent ->
                outbox.insertEvent(integrationEvent)
            }
        }
    }
}